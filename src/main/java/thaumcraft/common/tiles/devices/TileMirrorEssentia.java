package thaumcraft.common.tiles.devices;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.blocks.devices.BlockMirror;
import thaumcraft.common.lib.events.EssentiaHandler;
import thaumcraft.common.registry.TCBlockEntities;

/**
 * Port of TileMirrorEssentia. A pair of essentia mirrors acts as an essentia source: whatever draws essentia from
 * one (like the infusion altar) is served from the jars within 8 blocks in front of the other one, one point at
 * a time. The link logic is the item mirror's, so it extends TileMirror (the original duplicated it).
 */
public class TileMirrorEssentia extends TileMirror implements IAspectSource {
    /** The facing of the linked mirror; null until it is looked up (DOWN meant "unknown" in the original). */
    @Nullable
    private Direction linkedFacing;

    public TileMirrorEssentia(BlockPos pos, BlockState state) {
        super(TCBlockEntities.MIRROR_ESSENTIA.get(), pos, state);
    }

    @Override
    protected int getInstabilityLimit() {
        return 64;
    }

    @Override
    protected boolean ejectsItems() {
        return false;
    }

    @Override
    public void invalidateLink() {
        super.invalidateLink();
        linkedFacing = null;
    }

    /** The linked mirror, with the facing of its block looked up the first time. */
    @Nullable
    private TileMirrorEssentia getLinked() {
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) {
            return null;
        }
        if (linkedFacing == null) {
            BlockState bs = targetWorld.getBlockState(getLinkPos());
            if (bs.hasProperty(BlockMirror.FACING)) {
                linkedFacing = bs.getValue(BlockMirror.FACING);
            }
        }
        return targetWorld.getBlockEntity(getLinkPos()) instanceof TileMirrorEssentia te ? te : null;
    }

    @Override
    public AspectList getAspects() {
        return null;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        if (getTargetWorld() == null) {
            return false;
        }
        TileMirrorEssentia te = getLinked();
        return te == null || EssentiaHandler.canAcceptEssentia(te, tag, linkedFacing, 8, true);
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        if (!isLinkValid() || amount > 1) {
            return amount;
        }
        TileMirrorEssentia te = getLinked();
        if (te != null) {
            boolean b = EssentiaHandler.addEssentia(te, tag, linkedFacing, 8, true, 5);
            if (b) {
                addInstability(null, amount);
            }
            return b ? 0 : 1;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        if (!isLinkValid() || amount > 1) {
            return false;
        }
        TileMirrorEssentia te = getLinked();
        if (te != null) {
            boolean b = EssentiaHandler.drainEssentia(te, tag, linkedFacing, 8, true, 5);
            if (b) {
                addInstability(null, amount);
            }
            return b;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        if (!isLinkValid() || amount > 1) {
            return false;
        }
        TileMirrorEssentia te = getLinked();
        return te != null && EssentiaHandler.findEssentia(te, tag, linkedFacing, 8, true);
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    // Essentia mirrors take no items.

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }
}
