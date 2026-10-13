package thaumcraft.common.tiles.devices;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileStabilizer. Gains one charge a second up to 15, giving off a little flux each time. Charge is spent
 * to make flux rifts within 8 blocks more stable and, through the redstone inlay, to absorb infusion instability.
 */
public class TileStabilizer extends TileThaumcraft {
    private int ticks = 0;
    private int delay = 0;
    protected int energy = 0;
    protected int capacity = 15;

    public TileStabilizer(BlockPos pos, BlockState state) {
        super(TCBlockEntities.STABILIZER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileStabilizer te) {
        te.update();
    }

    private void update() {
        ++ticks;
        if (energy < capacity && ticks % 20 == 0) {
            ++energy;
            AuraHelper.polluteAura(level, worldPosition, 0.25f, true);
            setChanged();
            syncTile(false);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        if (energy > 0 && delay <= 0 && ticks % 5 == 0) {
            int q = energy;
            tryAddStability();
            if (q != energy) {
                setChanged();
                syncTile(false);
            }
        }
        if (delay > 0) {
            --delay;
        }
    }

    private void tryAddStability() {
        List<EntityFluxRift> targets = level.getEntitiesOfClass(EntityFluxRift.class, new AABB(worldPosition).inflate(8.0));
        for (EntityFluxRift e : targets) {
            if (!e.isAlive()) {
                continue;
            }
            if (e.getStability() == EntityFluxRift.EnumStability.VERY_STABLE || !mitigate(1)) {
                continue;
            }
            e.addStability();
            delay += 5;
            if (energy <= 0) {
                return;
            }
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        int old = energy;
        energy = Math.min(tag.getInt("energy"), 15);
        // markBlockRangeForRenderUpdate: redraw the crystal with its new color.
        if (level != null && level.isClientSide && old != energy) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("energy", energy);
    }

    public int getEnergy() {
        return energy;
    }

    public boolean mitigate(int e) {
        if (energy >= e) {
            energy -= e;
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            return true;
        }
        return false;
    }
}
