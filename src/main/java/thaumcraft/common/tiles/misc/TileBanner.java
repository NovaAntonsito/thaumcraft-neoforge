package thaumcraft.common.tiles.misc;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.blocks.basic.BlockBannerTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.TileThaumcraft;

/** Port of TileBanner: which way the banner faces (16 steps), whether it hangs on a wall, and its aspect. */
public class TileBanner extends TileThaumcraft {
    private byte facing = 0;
    @Nullable
    private Aspect aspect = null;
    private boolean onWall = false;

    public TileBanner(BlockPos pos, BlockState state) {
        super(TCBlockEntities.BANNER.get(), pos, state);
    }

    public byte getBannerFacing() {
        return facing;
    }

    public void setBannerFacing(byte face) {
        facing = face;
        setChanged();
    }

    public boolean getWall() {
        return onWall;
    }

    public void setWall(boolean b) {
        onWall = b;
        setChanged();
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        facing = tag.getByte("facing");
        String as = tag.getString("aspect");
        aspect = as.length() > 0 ? Aspect.getAspect(as) : null;
        onWall = tag.getBoolean("wall");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("facing", facing);
        tag.putString("aspect", aspect == null ? "" : aspect.getTag());
        tag.putBoolean("wall", onWall);
    }

    @Nullable
    public Aspect getAspect() {
        return aspect;
    }

    public void setAspect(@Nullable Aspect aspect) {
        this.aspect = aspect;
    }

    /** The dye's color, or -1 for the crimson cult banner. */
    public int getColor() {
        return getBlockState().getBlock() instanceof BlockBannerTC b && b.dye != null ? b.dye.getTextureDiffuseColor() : -1;
    }

    /** The drop keeps the aspect (the "aspect" tag of the original item). */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (aspect != null) {
            components.set(TCDataComponents.ASPECT_FILTER.get(), aspect);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("aspect");
    }
}
