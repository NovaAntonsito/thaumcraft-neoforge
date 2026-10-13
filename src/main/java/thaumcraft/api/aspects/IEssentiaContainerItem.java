package thaumcraft.api.aspects;

import net.minecraft.world.item.ItemStack;

/**
 * Port of thaumcraft.api.aspects.IEssentiaContainerItem.
 * Items that hold essentia report the aspects they contain instead of their registered object aspects.
 */
public interface IEssentiaContainerItem {
    AspectList getAspects(ItemStack stack);

    void setAspects(ItemStack stack, AspectList aspects);

    /** Return true to use the item's registered aspects instead of the contained ones. */
    default boolean ignoreContainedAspects() {
        return false;
    }
}
