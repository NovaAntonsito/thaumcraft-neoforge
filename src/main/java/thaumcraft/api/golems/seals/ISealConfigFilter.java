package thaumcraft.api.golems.seals;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface ISealConfigFilter {
    NonNullList<ItemStack> getInv();

    NonNullList<Integer> getSizes();

    int getFilterSize();

    ItemStack getFilterSlot(int i);

    int getFilterSlotSize(int i);

    void setFilterSlot(int i, ItemStack stack);

    void setFilterSlotSize(int i, int size);

    boolean isBlacklist();

    void setBlacklist(boolean black);

    boolean hasStacksizeLimiters();
}
