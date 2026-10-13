package thaumcraft.common.tiles.devices;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import thaumcraft.common.blocks.devices.BlockBellows;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileBellows. Pumps on the client; on the server it speeds up a vanilla furnace in front of it.
 * Essentia smelteries and buffers count the bellows pointed at them with {@link #getBellows}.
 */
public class TileBellows extends TileThaumcraft {
    @Nullable private static Field cookingProgress;

    public float inflation = 1.0f;
    boolean direction;
    boolean firstrun = true;
    public int delay;

    public TileBellows(BlockPos pos, BlockState state) {
        super(TCBlockEntities.BELLOWS.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileBellows te) {
        te.update();
    }

    private void update() {
        BlockState state = getBlockState();
        if (!state.getValue(BlockBellows.ENABLED)) {
            return;
        }
        if (level.isClientSide) {
            if (firstrun) {
                inflation = 0.35f + level.random.nextFloat() * 0.55f;
            }
            firstrun = false;
            if (inflation > 0.35f && !direction) {
                inflation -= 0.075f;
            }
            if (inflation <= 0.35f && !direction) {
                direction = true;
            }
            if (inflation < 1.0f && direction) {
                inflation += 0.025f;
            }
            if (inflation >= 1.0f && direction) {
                direction = false;
                level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 0.01f,
                        0.5f + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f, false);
            }
        } else {
            ++delay;
            if (delay >= 2) {
                delay = 0;
                BlockEntity tile = level.getBlockEntity(worldPosition.relative(state.getValue(BlockBellows.FACING)));
                if (tile instanceof AbstractFurnaceBlockEntity tf) {
                    int ct = getCooktime(tf);
                    if (ct > 0 && ct < 199) {
                        setCooktime(tf, ct + 1);
                    }
                }
            }
        }
    }

    /** The furnace's cook progress is private in 1.21 (cookTime was public in 1.12). */
    @Nullable
    private static Field cookField() {
        if (cookingProgress == null) {
            try {
                cookingProgress = ObfuscationReflectionHelper.findField(AbstractFurnaceBlockEntity.class, "cookingProgress");
            } catch (Exception e) {
                return null;
            }
        }
        return cookingProgress;
    }

    public void setCooktime(AbstractFurnaceBlockEntity ent, int hit) {
        Field f = cookField();
        if (f != null) {
            try {
                f.setInt(ent, hit);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public int getCooktime(AbstractFurnaceBlockEntity ent) {
        Field f = cookField();
        if (f != null) {
            try {
                return f.getInt(ent);
            } catch (IllegalAccessException ignored) {
            }
        }
        return 0;
    }

    /** How many working bellows next to pos, on the given sides, point at it. */
    public static int getBellows(Level world, BlockPos pos, Direction[] directions) {
        int bellows = 0;
        for (Direction dir : directions) {
            BlockEntity tile = world.getBlockEntity(pos.relative(dir));
            if (tile instanceof TileBellows) {
                BlockState bs = tile.getBlockState();
                if (bs.getValue(BlockBellows.FACING) == dir.getOpposite() && bs.getValue(BlockBellows.ENABLED)) {
                    ++bellows;
                }
            }
        }
        return bellows;
    }
}
