package thaumcraft.common.tiles.devices;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.devices.BlockLevitator;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileLevitator. Scans one block of its range per tick (stopping at the first opaque block), keeps 10 vis
 * drawn from the aura and spends range * 2 of it per entity per tick it lifts.
 */
public class TileLevitator extends TileThaumcraft {
    private static final int[] RANGES = {4, 8, 16, 32};
    private static final double MAX_SPEED = 0.3499999940395355;

    private int range = 1;
    private int rangeActual = 0;
    private int counter = 0;
    private int vis = 0;

    public TileLevitator(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LEVITATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileLevitator tile) {
        tile.update(state);
    }

    private void update(BlockState state) {
        Direction facing = state.getValue(BlockLevitator.FACING);
        if (rangeActual > RANGES[range]) {
            rangeActual = 0;
        }
        int p = counter % RANGES[range];
        BlockPos check = worldPosition.relative(facing, 1 + p);
        if (level.getBlockState(check).isSolidRender(level, check)) {
            if (1 + p < rangeActual) {
                rangeActual = 1 + p;
            }
            counter = -1;
        } else if (1 + p > rangeActual) {
            rangeActual = 1 + p;
        }
        ++counter;
        if (!level.isClientSide && vis < 10) {
            vis += (int) (AuraHelper.drainVis(level, worldPosition, 1.0f, false) * 1200.0f);
            setChanged();
            syncTile(false);
        }
        if (rangeActual > 0 && vis > 0 && state.getValue(BlockLevitator.ENABLED)) {
            int x = worldPosition.getX(), y = worldPosition.getY(), z = worldPosition.getZ();
            AABB area = new AABB(x - (facing.getStepX() < 0 ? rangeActual : 0), y - (facing.getStepY() < 0 ? rangeActual : 0),
                    z - (facing.getStepZ() < 0 ? rangeActual : 0), x + 1 + (facing.getStepX() > 0 ? rangeActual : 0),
                    y + 1 + (facing.getStepY() > 0 ? rangeActual : 0), z + 1 + (facing.getStepZ() > 0 ? rangeActual : 0));
            List<Entity> targets = level.getEntitiesOfClass(Entity.class, area);
            boolean lifted = false;
            for (Entity e : targets) {
                if (!(e instanceof ItemEntity) && !e.isPushable() && !(e instanceof AbstractHorse)) {
                    continue;
                }
                lifted = true;
                drawFXAt(e);
                drawFX(facing, 0.6);
                Vec3 m = e.getDeltaMovement();
                double mx = m.x, my = m.y, mz = m.z;
                if (e.isShiftKeyDown() && facing == Direction.UP) {
                    if (my < 0.0) {
                        my *= 0.8999999761581421;
                    }
                } else {
                    mx += 0.1f * facing.getStepX();
                    my += 0.1f * facing.getStepY();
                    mz += 0.1f * facing.getStepZ();
                    if (facing.getAxis() != Direction.Axis.Y && !e.onGround()) {
                        if (my < 0.0) {
                            my *= 0.8999999761581421;
                        }
                        my += 0.07999999821186066;
                    }
                    mx = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, mx));
                    my = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, my));
                    mz = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, mz));
                }
                e.setDeltaMovement(mx, my, mz);
                e.resetFallDistance();
                vis -= getCost();
                if (vis <= 0) {
                    break;
                }
            }
            drawFX(facing, 0.1);
            if (lifted && !level.isClientSide && counter % 20 == 0) {
                setChanged();
            }
        }
    }

    private void drawFX(Direction facing, double c) {
        if (level.isClientSide && level.random.nextFloat() < c) {
            float x = worldPosition.getX() + 0.25f + level.random.nextFloat() * 0.5f;
            float y = worldPosition.getY() + 0.25f + level.random.nextFloat() * 0.5f;
            float z = worldPosition.getZ() + 0.25f + level.random.nextFloat() * 0.5f;
            ClientFX.drawLevitatorParticles(level, x, y, z, facing.getStepX() / 50.0, facing.getStepY() / 50.0, facing.getStepZ() / 50.0);
        }
    }

    private void drawFXAt(Entity e) {
        if (level.isClientSide && level.random.nextFloat() < 0.1f) {
            float x = (float) (e.getX() + (level.random.nextFloat() - level.random.nextFloat()) * e.getBbWidth());
            float y = (float) (e.getY() + level.random.nextFloat() * e.getBbHeight());
            float z = (float) (e.getZ() + (level.random.nextFloat() - level.random.nextFloat()) * e.getBbWidth());
            ClientFX.drawLevitatorParticles(level, x, y, z, (level.random.nextFloat() - level.random.nextFloat()) * 0.01,
                    (level.random.nextFloat() - level.random.nextFloat()) * 0.01, (level.random.nextFloat() - level.random.nextFloat()) * 0.01);
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        range = Math.floorMod(tag.getByte("range"), RANGES.length);
        vis = tag.getInt("vis");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("range", (byte) range);
        tag.putInt("vis", vis);
    }

    public int getCost() {
        return RANGES[range] * 2;
    }

    public void increaseRange(Player player) {
        rangeActual = 0;
        if (!level.isClientSide) {
            ++range;
            if (range >= RANGES.length) {
                range = 0;
            }
            setChanged();
            syncTile(false);
            player.sendSystemMessage(Component.translatable("tc.levitator", RANGES[range], getCost()));
        }
    }
}
