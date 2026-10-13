package thaumcraft.common.tiles.essentia;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.registry.TCBlockEntities;

/** Port of TileTubeFilter: once marked with an aspect, only that essentia's suction passes through. */
public class TileTubeFilter extends TileTube implements IAspectContainer {
    @Nullable public Aspect aspectFilter;

    public TileTubeFilter(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_FILTER.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        aspectFilter = Aspect.getAspect(tag.getString("AspectFilter"));
        super.readSyncNBT(tag, registries);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeSyncNBT(tag, registries);
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.getTag());
        }
    }

    @Override
    void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean dir) {
        super.calculateSuction(aspectFilter, restrict, dir);
    }

    @Override
    public AspectList getAspects() {
        if (aspectFilter != null) {
            return new AspectList().add(aspectFilter, -1);
        }
        return null;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return false;
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }
}
