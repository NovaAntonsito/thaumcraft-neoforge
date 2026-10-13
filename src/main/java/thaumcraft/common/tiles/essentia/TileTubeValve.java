package thaumcraft.common.tiles.essentia;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;

/**
 * Port of TileTubeValve. Closed by hand or by a redstone signal; a closed valve passes no suction.
 * The handle sits on {@link #facing}, which never connects.
 */
public class TileTubeValve extends TileTube {
    public boolean allowFlow = true;
    boolean wasPoweredLastTick;
    public float rotation;

    public TileTubeValve(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_VALVE.get(), pos, state);
    }

    @Override
    public void update() {
        if (!level.isClientSide && count % 5 == 0) {
            boolean gettingPower = gettingPower();
            if (wasPoweredLastTick && !gettingPower && !allowFlow) {
                allowFlow = true;
                level.playSound(null, worldPosition, SoundsTC.SQUEEK.get(), SoundSource.BLOCKS, 0.7f, 0.9f + level.random.nextFloat() * 0.2f);
                syncTile(true);
                setChanged();
            }
            if (!wasPoweredLastTick && gettingPower && allowFlow) {
                allowFlow = false;
                level.playSound(null, worldPosition, SoundsTC.SQUEEK.get(), SoundSource.BLOCKS, 0.7f, 0.9f + level.random.nextFloat() * 0.2f);
                syncTile(true);
                setChanged();
            }
            wasPoweredLastTick = gettingPower;
        }
        if (level.isClientSide) {
            if (!allowFlow && rotation < 360.0f) {
                rotation += 20.0f;
            } else if (allowFlow && rotation > 0.0f) {
                rotation -= 20.0f;
            }
        }
        super.update();
    }

    /** The handle can turn to any side with nothing to connect to. */
    @Override
    protected boolean canFaceCenterTo(Direction d) {
        return !canConnectSide(d);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        allowFlow = tag.getBoolean("flow");
        wasPoweredLastTick = tag.getBoolean("hadpower");
        super.readSyncNBT(tag, registries);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeSyncNBT(tag, registries);
        tag.putBoolean("flow", allowFlow);
        tag.putBoolean("hadpower", wasPoweredLastTick);
    }

    @Override
    public boolean isConnectable(@Nullable Direction face) {
        return face != facing && super.isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        if (allowFlow) {
            super.setSuction(aspect, amount);
        }
    }
}
