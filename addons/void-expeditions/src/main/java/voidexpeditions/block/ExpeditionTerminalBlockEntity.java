package voidexpeditions.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import voidexpeditions.registry.VEBlockEntities;
import voidexpeditions.run.Difficulty;
import voidexpeditions.run.RunLength;

/**
 * The dialing device of a void gate: the settings of the next expedition through it, and the middle of the gate it is
 * linked to once formed (see {@link GateStructure}).
 */
public class ExpeditionTerminalBlockEntity extends BlockEntity {
    private Difficulty difficulty = Difficulty.RESTLESS;
    private int length = RunLength.OPTIONS[0];
    @Nullable
    private BlockPos gateCenter;

    public ExpeditionTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(VEBlockEntities.TERMINAL.get(), pos, state);
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getLength() {
        return length;
    }

    public boolean isLinked() {
        return gateCenter != null;
    }

    @Nullable
    public BlockPos getGateCenter() {
        return gateCenter;
    }

    public void link(BlockPos center) {
        gateCenter = center.immutable();
        changed();
    }

    public void unlink() {
        gateCenter = null;
        changed();
    }

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void configure(Difficulty difficulty, int length) {
        this.difficulty = difficulty;
        this.length = RunLength.clamp(length);
        changed();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Difficulty", difficulty.ordinal());
        tag.putInt("Length", length);
        if (gateCenter != null) tag.putLong("Gate", gateCenter.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        difficulty = Difficulty.byIndex(tag.getInt("Difficulty"));
        length = RunLength.clamp(tag.getInt("Length"));
        gateCenter = tag.contains("Gate") ? BlockPos.of(tag.getLong("Gate")) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
