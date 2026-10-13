package thaumcraft.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.casters.foci.FocusEffectRift;
import thaumcraft.common.registry.TCBlockEntities;

/**
 * The dimensional tear of the rift focus. On its first tick it opens the eight blocks around it (a 3x3 ring) and the
 * next block deeper, then puts its old block back when the countdown runs out.
 * 1.21: the sparkles along the walls are blue dust until the FX stage.
 */
public class TileHole extends TileMemory {
    public short countdown = 0;
    public short countdownmax = 120;
    public byte count = 0;
    public Direction direction = null;

    public TileHole(BlockPos pos, BlockState state) {
        super(TCBlockEntities.HOLE.get(), pos, state);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, TileHole tile) {
        tile.update();
    }

    public void update() {
        if (level.isClientSide) {
            for (int a = 0; a < 2; ++a) {
                surroundwithsparkles();
            }
            return;
        }
        if (countdown == 0 && count > 1 && direction != null) {
            switch (direction.getAxis()) {
                case Y -> {
                    for (int a = 0; a < 9; ++a) {
                        if (a / 3 != 1 || a % 3 != 1) {
                            FocusEffectRift.createHole(level, getBlockPos().offset(-1 + a / 3, 0, -1 + a % 3), null, (byte) 1, countdownmax);
                        }
                    }
                }
                case Z -> {
                    for (int a = 0; a < 9; ++a) {
                        if (a / 3 != 1 || a % 3 != 1) {
                            FocusEffectRift.createHole(level, getBlockPos().offset(-1 + a / 3, -1 + a % 3, 0), null, (byte) 1, countdownmax);
                        }
                    }
                }
                case X -> {
                    for (int a = 0; a < 9; ++a) {
                        if (a / 3 != 1 || a % 3 != 1) {
                            FocusEffectRift.createHole(level, getBlockPos().offset(0, -1 + a / 3, -1 + a % 3), null, (byte) 1, countdownmax);
                        }
                    }
                }
            }
            if (!FocusEffectRift.createHole(level, getBlockPos().relative(direction.getOpposite()), direction, (byte) (count - 1), countdownmax)) {
                count = 0;
            }
        }
        ++countdown;
        if (countdown % 20 == 0) {
            setChanged();
        }
        if (countdown >= countdownmax) {
            level.setBlock(getBlockPos(), oldblock, 3);
        }
    }

    private void surroundwithsparkles() {
        BlockPos pos = getBlockPos();
        for (Direction d1 : Direction.values()) {
            BlockState b1 = level.getBlockState(pos.relative(d1));
            if (b1.getBlock() != ConfigBlocks.hole.get() && !b1.isSolidRender(level, pos.relative(d1))) {
                for (Direction d2 : Direction.values()) {
                    if (d1.getAxis() != d2.getAxis() && (level.getBlockState(pos.relative(d2)).isSolidRender(level, pos.relative(d2))
                            || level.getBlockState(pos.relative(d1).relative(d2)).isSolidRender(level, pos.relative(d1).relative(d2)))) {
                        float sx = 0.5f * d1.getStepX();
                        float sy = 0.5f * d1.getStepY();
                        float sz = 0.5f * d1.getStepZ();
                        if (sx == 0.0f) {
                            sx = 0.5f * d2.getStepX();
                        }
                        if (sy == 0.0f) {
                            sy = 0.5f * d2.getStepY();
                        }
                        if (sz == 0.0f) {
                            sz = 0.5f * d2.getStepZ();
                        }
                        sx = sx == 0.0f ? level.random.nextFloat() : sx + 0.5f;
                        sy = sy == 0.0f ? level.random.nextFloat() : sy + 0.5f;
                        sz = sz == 0.0f ? level.random.nextFloat() : sz + 0.5f;
                        level.addParticle(new DustParticleOptions(new Vector3f(0.25f, 0.25f, 1.0f), 0.5f), pos.getX() + sx, pos.getY() + sy, pos.getZ() + sz, 0, 0, 0);
                    }
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbttagcompound, HolderLookup.Provider registries) {
        super.loadAdditional(nbttagcompound, registries);
        countdown = nbttagcompound.getShort("countdown");
        countdownmax = nbttagcompound.getShort("countdownmax");
        count = nbttagcompound.getByte("count");
        byte db = nbttagcompound.getByte("direction");
        direction = db >= 0 ? Direction.values()[db] : null;
    }

    @Override
    protected void saveAdditional(CompoundTag nbttagcompound, HolderLookup.Provider registries) {
        super.saveAdditional(nbttagcompound, registries);
        nbttagcompound.putShort("countdown", countdown);
        nbttagcompound.putShort("countdownmax", countdownmax);
        nbttagcompound.putByte("count", count);
        nbttagcompound.putByte("direction", direction == null ? -1 : (byte) direction.ordinal());
    }
}
