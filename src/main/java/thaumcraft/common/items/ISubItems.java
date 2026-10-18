package thaumcraft.common.items;

import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;

/** getSubItems of 1.12: the stacks an item shows in the creative tab, when it is not just itself. */
public interface ISubItems {
    void fillSubItems(Consumer<ItemStack> output);
}
