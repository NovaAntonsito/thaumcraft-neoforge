package thaumcraft.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/** Port of TileRedstoneRelay: the input threshold and the output strength, 1 to 15. */
public class TileRedstoneRelay extends TileThaumcraft {
    private int in = 1;
    private int out = 15;

    public TileRedstoneRelay(BlockPos pos, BlockState state) {
        super(TCBlockEntities.REDSTONE_RELAY.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        setIn(tag.getByte("in"));
        setOut(tag.getByte("out"));
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("in", (byte) getIn());
        tag.putByte("out", (byte) getOut());
    }

    public void increaseIn() {
        if (!level.isClientSide) {
            setIn(getIn() + 1);
            if (getIn() > 15) {
                setIn(1);
            }
            setChanged();
            syncTile(false);
        }
    }

    public void increaseOut() {
        if (!level.isClientSide) {
            setOut(getOut() + 1);
            if (getOut() > 15) {
                setOut(1);
            }
            setChanged();
            syncTile(false);
        }
    }

    public int getOut() {
        return out;
    }

    public void setOut(int out) {
        this.out = out;
    }

    public int getIn() {
        return in;
    }

    public void setIn(int in) {
        this.in = in;
    }
}
