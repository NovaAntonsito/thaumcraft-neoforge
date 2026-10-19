package thaumcraft.common.blocks.eldritch;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.eldritch.TileEldritchAltar;
import thaumcraft.common.tiles.eldritch.TileEldritchCap;
import thaumcraft.common.tiles.eldritch.TileEldritchObelisk;

/**
 * The structural metas 0-3 of TC4's BlockEldritch, one block each: the altar keystone (0), the obelisk base (1), the
 * invisible obelisk body (2) and the capstones around the ring (3). They are drawn by their block entity renderers,
 * drop nothing, and breaking any of them tears down the rest of the structure nearby with a small explosion.
 */
public class BlockEldritch extends BaseEntityBlock {
    public enum Type {
        ALTAR, OBELISK, OBELISK_BODY, CAP
    }

    public final Type type;
    private final MapCodec<BlockEldritch> codec;

    public BlockEldritch(Type type) {
        // setHardness(50), setResistance(20000): 1.7 divided the stored resistance (x3) by 5.
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(50.0f, 12000.0f).sound(SoundType.STONE)
                .lightLevel(s -> 8).noOcclusion().isValidSpawn(BlockEldritch::never).noLootTable());
        this.type = type;
        this.codec = simpleCodec(p -> new BlockEldritch(type));
    }

    static Boolean never(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> entity) {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (type) {
            case ALTAR -> new TileEldritchAltar(pos, state);
            case OBELISK -> new TileEldritchObelisk(pos, state);
            case CAP -> new TileEldritchCap(pos, state);
            case OBELISK_BODY -> null;
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (this.type == Type.ALTAR && !level.isClientSide) {
            return createTickerHelper(type, TCBlockEntities.ELDRITCH_ALTAR.get(), TileEldritchAltar::serverTick);
        }
        if (this.type == Type.OBELISK) {
            return createTickerHelper(type, TCBlockEntities.ELDRITCH_OBELISK.get(), TileEldritchObelisk::tick);
        }
        return null;
    }

    /** An Eldritch Eye on the altar keystone; from the third eye on it calls eldritch guardians. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (type != Type.ALTAR || player.isShiftKeyDown() || !stack.is(ConfigItems.eldritchEye.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileEldritchAltar tile && tile.getEyes() < 4) {
            if (tile.getEyes() >= 2) {
                tile.setSpawner(true);
                tile.setSpawnType((byte) 1);
            }
            tile.setEyes((byte) (tile.getEyes() + 1));
            tile.checkForMaze();
            stack.shrink(1);
            tile.setChanged();
            tile.syncTile(true);
            world.playSound(null, pos, SoundsTC.CRYSTAL.get(), SoundSource.BLOCKS, 0.2f, 1.0f);
        }
        // The original always fell through to the default activation, which did nothing for this block.
        return ItemInteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!world.isClientSide && !state.is(newState.getBlock())) {
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3))) {
                if (!p.equals(pos) && isStructure(world.getBlockState(p))) {
                    world.removeBlock(p, false);
                }
            }
            world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0f, false, Level.ExplosionInteraction.BLOCK);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    /** blockEldritch with metadata below 4. */
    public static boolean isStructure(BlockState state) {
        return state.is(ConfigBlocks.eldritchAltar.get()) || state.is(ConfigBlocks.eldritchObelisk.get())
                || state.is(ConfigBlocks.eldritchObeliskBody.get()) || state.is(ConfigBlocks.eldritchCap.get());
    }
}
