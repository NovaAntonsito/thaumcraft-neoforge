package thaumcraft.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.blocks.devices.BlockVisGenerator;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileVisGenerator. When empty it drains 1 vis from the aura into 1000 energy, then pushes up to 20 a tick
 * into the block it faces. It only exposes energy on that face and nothing can be pulled out or pushed in.
 */
public class TileVisGenerator extends TileThaumcraft implements IEnergyStorage {
    protected int energy;
    protected int capacity = 1000;
    protected int maxExtract = 20;

    public TileVisGenerator(BlockPos pos, BlockState state) {
        super(TCBlockEntities.VIS_GENERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileVisGenerator te) {
        te.update();
    }

    private void update() {
        if (getBlockState().getValue(BlockVisGenerator.ENABLED)) {
            recharge();
            Direction face = getBlockState().getValue(BlockVisGenerator.FACING);
            IEnergyStorage capability = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(face), face.getOpposite());
            if (capability != null && capability.canReceive()) {
                int energyExtracted = Math.min(energy, maxExtract);
                energyExtracted = capability.receiveEnergy(energyExtracted, false);
                if (energyExtracted > 0) {
                    energy -= energyExtracted;
                    setChanged();
                    if (energy == 0) {
                        syncTile(false);
                    }
                }
            }
        }
    }

    private void recharge() {
        if (energy == 0) {
            float vis = AuraHelper.drainVis(level, worldPosition, 1.0f, false);
            energy = (int) (vis * 1000.0f);
            setChanged();
            syncTile(false);
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        energy = tag.getInt("energy");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("energy", energy);
    }

    /** The energy capability exists only on the face the generator points at. */
    public IEnergyStorage getEnergy(Direction side) {
        return side == getBlockState().getValue(BlockVisGenerator.FACING) ? this : null;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
