package thaumcraft.common.container;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/** Port of InventoryLogistics: the 81 stacks shown by the logistics screen, with no size limit. */
public class InventoryLogistics extends SimpleContainer {
    public InventoryLogistics() {
        super(81);
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    /** 1.21 also caps each stack at the item's own max size when it is set; the summed stacks must keep their count. */
    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
