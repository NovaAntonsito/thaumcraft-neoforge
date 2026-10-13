package thaumcraft.common.tiles.devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.blocks.devices.BlockArcaneEar;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileArcaneEar. The note blocks played this tick in each dimension are collected in {@link #noteBlockEvents}
 * (x, y, z, instrument, note) by WorldEvents and cleared at the end of the tick.
 * <p>
 * 1.21: the tone is the note block instrument of the block behind the ear (BlockState.instrument()) instead of the
 * 1.12 material checks. The first ten instruments match the 1.12 ones in order.
 */
public class TileArcaneEar extends TileThaumcraft {
    public static final Map<ResourceKey<Level>, List<int[]>> noteBlockEvents = new HashMap<>();

    public byte note = 0;
    public byte tone = 0;
    public int redstoneSignal = 0;

    public TileArcaneEar(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_EAR.get(), pos, state);
    }

    public static void addNoteEvent(ResourceKey<Level> dim, BlockPos pos, int instrument, int note) {
        noteBlockEvents.computeIfAbsent(dim, k -> new ArrayList<>()).add(new int[] {pos.getX(), pos.getY(), pos.getZ(), instrument, note});
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("note", note);
        tag.putByte("tone", tone);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        note = tag.getByte("note");
        tone = tag.getByte("tone");
        if (note < 0) {
            note = 0;
        }
        if (note > 24) {
            note = 24;
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileArcaneEar tile) {
        tile.update(state);
    }

    private void update(BlockState state) {
        Direction behind = state.getValue(BlockArcaneEar.FACING).getOpposite();
        if (redstoneSignal > 0) {
            --redstoneSignal;
            if (redstoneSignal == 0) {
                level.setBlock(worldPosition, state.setValue(BlockArcaneEar.ENABLED, false), 3);
                level.updateNeighborsAt(worldPosition.relative(behind), state.getBlock());
            }
        }
        List<int[]> nbe = noteBlockEvents.get(level.dimension());
        if (nbe == null) {
            return;
        }
        for (int[] dat : nbe) {
            if (dat[3] == tone && dat[4] == note
                    && worldPosition.distToCenterSqr(dat[0] + 0.5, dat[1] + 0.5, dat[2] + 0.5) <= 4096.0) {
                triggerNote(level, worldPosition, true);
                BlockState now = level.getBlockState(worldPosition);
                if (((BlockArcaneEar) now.getBlock()).toggle) {
                    level.setBlock(worldPosition, now.cycle(BlockArcaneEar.ENABLED), 3);
                } else {
                    redstoneSignal = 10;
                    level.setBlock(worldPosition, now.setValue(BlockArcaneEar.ENABLED, true), 3);
                }
                level.updateNeighborsAt(worldPosition.relative(behind), now.getBlock());
                break;
            }
        }
    }

    private int instrumentBehind() {
        Direction behind = getBlockState().getValue(BlockArcaneEar.FACING).getOpposite();
        return level.getBlockState(worldPosition.relative(behind)).instrument().ordinal();
    }

    public void updateTone() {
        if (level == null) {
            return;
        }
        tone = (byte) instrumentBehind();
        setChanged();
    }

    public void changePitch() {
        note = (byte) ((note + 1) % 25);
        setChanged();
        syncTile(false);
    }

    public void triggerNote(Level world, BlockPos pos, boolean sound) {
        int i = sound ? instrumentBehind() : -1;
        world.blockEvent(pos, getBlockState().getBlock(), i, note);
    }
}
