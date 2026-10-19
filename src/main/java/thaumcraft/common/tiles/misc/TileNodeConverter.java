package thaumcraft.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of the Thaumcraft 4 TileNodeConverter, the node transducer. Placed right above a stabilized node and given a
 * redstone signal, it drains the node for 1000 ticks and then energizes it (status 1). Above an energized node it
 * keeps it so (status 2); without the signal it counts back down and turns it into a plain, empty node. Losing the
 * stabilizer while converting blows the node up.
 */
public class TileNodeConverter extends TileThaumcraft {
    public int count = -1;
    public int status = 0;

    public TileNodeConverter(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE_TRANSDUCER.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        status = tag.getInt("status");
        count = tag.getInt("count");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("status", status);
        tag.putInt("count", count);
    }

    private TileAuraNode nodeBelow() {
        return level.getBlockEntity(worldPosition.below()) instanceof TileAuraNode n ? n : null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileNodeConverter tile) {
        tile.update();
    }

    private void update() {
        if (count == -1) checkStatus();
        TileAuraNode node = nodeBelow();
        if (status == 1 && !level.isClientSide && count >= 1000) {
            if (node != null && !node.isEnergized()) {
                node.setEnergized(true);
                checkStatus();
                level.blockEvent(worldPosition, getBlockState().getBlock(), 10, 10);
                setChanged();
                syncTile(false);
            }
        }
        if (status == 2 && !level.isClientSide && count <= 50) {
            if (node != null && node.isEnergized()) {
                node.setEnergized(false);
                level.blockEvent(worldPosition, getBlockState().getBlock(), 10, 10);
                status = 0;
                setChanged();
                syncTile(false);
            }
        }
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (status != 0 && powered) {
            if (count < 1000) {
                ++count;
                if (!level.isClientSide && node != null && !node.isEnergized()) {
                    AspectList al = node.getAspects();
                    if (al.size() > 0) {
                        node.takeFromContainer(al.getAspects()[level.random.nextInt(al.size())], 1);
                        if (count % 5 == 0 || node.getAspects().visSize() == 0) node.syncTile(false);
                    }
                }
                bolts();
            }
        } else if (count > 0) {
            --count;
            bolts();
        }
        if (count > 1000) count = 1000;
        if (!level.isClientSide && level.getGameTime() % 20 == 0) checkStatus();
    }

    private void bolts() {
        if (count > 50 && level.isClientSide) {
            float x = worldPosition.getX();
            float y = worldPosition.getY();
            float z = worldPosition.getZ();
            if (level.random.nextBoolean()) {
                thaumcraft.client.fx.FXDispatcher.nodeBolt(x + 0.25f + level.random.nextFloat() * 0.5f, y + 0.5f,
                        z + 0.25f + level.random.nextFloat() * 0.5f, x + 0.5f, y - 0.5f, z + 0.5f);
            }
            if (level.random.nextBoolean() && hasStabilizer()) {
                thaumcraft.client.fx.FXDispatcher.nodeBolt(x + 0.25f + level.random.nextFloat() * 0.5f, y - 1.5f,
                        z + 0.25f + level.random.nextFloat() * 0.5f, x + 0.5f, y - 0.5f, z + 0.5f);
            }
        }
    }

    private boolean hasStabilizer() {
        BlockPos p = worldPosition.below(2);
        return !level.hasNeighborSignal(p) && (level.getBlockState(p).is(ConfigBlocks.nodeStabilizer.get())
                || level.getBlockState(p).is(ConfigBlocks.nodeStabilizerAdvanced.get()));
    }

    /** checkStatus: what the transducer is doing, from the node below, the stabilizer and the signal. */
    public void checkStatus() {
        if (level == null || level.isClientSide) return;
        if (count == -1) count = 0;
        TileAuraNode node = nodeBelow();
        boolean energizedBelow = node != null && node.isEnergized();
        int old = status;
        if (status == 2 && count > 50 && !(hasStabilizer() && energizedBelow)) {
            // The original blew the node up here; the node's own check (TileAuraNode.energizedTick) does it as well.
            if (node != null) TileAuraNode.explodify(level, worldPosition.below());
            status = 0;
            count = 50;
        } else if (level.hasNeighborSignal(worldPosition) && node != null && !energizedBelow && hasStabilizer()) {
            status = 1;
        } else if (energizedBelow) {
            if (status != 2) count = 1000;
            status = 2;
        } else {
            status = 0;
        }
        if (old != status) {
            setChanged();
            syncTile(false);
        }
    }

    /** receiveClientEvent 10: the burst when a node changes. */
    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 10 && type == 10) {
            if (level.isClientSide) {
                thaumcraft.client.fx.FXDispatcher.burst(worldPosition.getX() + 0.5, worldPosition.getY() - 0.5, worldPosition.getZ() + 0.5, 1.0f);
                level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() - 0.5, worldPosition.getZ() + 0.5,
                        thaumcraft.common.lib.SoundsTC.CRAFTFAIL.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f, false);
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }
}
