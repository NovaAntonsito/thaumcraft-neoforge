package thaumcraft.common.container;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/** Port of InventoryFake: a plain inventory holding a copy of the given stacks. */
public class InventoryFake extends SimpleContainer {
    public InventoryFake(int size) {
        super(size);
    }

    public InventoryFake(NonNullList<ItemStack> inv) {
        super(inv.size());
        for (int a = 0; a < inv.size(); ++a) {
            setItem(a, inv.get(a));
        }
    }

    public InventoryFake(ItemStack... stacks) {
        super(stacks.length);
        for (int a = 0; a < stacks.length; ++a) {
            setItem(a, stacks[a]);
        }
    }
}
