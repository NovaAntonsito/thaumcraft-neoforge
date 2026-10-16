package thaumcraft.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealConfigFilter;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;
import thaumcraft.common.aspects.ObjectAspects;

/** Port of SealFiltered: the item filter (black- or whitelist with optional counts) shared by most seals. */
public abstract class SealFiltered implements ISeal, ISealGui, ISealConfigFilter {
    NonNullList<ItemStack> filter;
    NonNullList<Integer> filterSize;
    boolean blacklist;

    public SealFiltered() {
        filter = NonNullList.withSize(getFilterSize(), ItemStack.EMPTY);
        filterSize = NonNullList.withSize(getFilterSize(), 0);
        blacklist = true;
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
        ContainerHelper.loadAllItems(nbt, filter = NonNullList.withSize(getFilterSize(), ItemStack.EMPTY), ObjectAspects.registries());
        for (ItemStack s : filter) {
            if (s.getCount() > 1) {
                s.setCount(1);
            }
        }
        blacklist = nbt.getBoolean("bl");
        filterSize = NonNullList.withSize(getFilterSize(), 0);
        ListTag nbttaglist = nbt.getList("Sizes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nbttaglist.size(); ++i) {
            CompoundTag nbttagcompound = nbttaglist.getCompound(i);
            int j = nbttagcompound.getByte("Slot") & 0xFF;
            if (j < filterSize.size()) {
                filterSize.set(j, nbttagcompound.getInt("Size"));
            }
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
        ContainerHelper.saveAllItems(nbt, filter, ObjectAspects.registries());
        nbt.putBoolean("bl", blacklist);
        ListTag nbttaglist = new ListTag();
        for (int i = 0; i < filterSize.size(); ++i) {
            int size = filterSize.get(i);
            if (size != 0) {
                CompoundTag nbttagcompound = new CompoundTag();
                nbttagcompound.putByte("Slot", (byte) i);
                nbttagcompound.putInt("Size", size);
                nbttaglist.add(nbttagcompound);
            }
        }
        nbt.put("Sizes", nbttaglist);
    }

    /** The menu provider of SealBaseContainer (1.12 returned the container itself). */
    @Override
    public Object returnContainer(Level world, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return thaumcraft.common.golems.client.gui.SealBaseContainer.provider(seal);
    }

    /** Not used: the screen (SealBaseGUI) comes from the menu type registered in ThaumcraftClient. */
    @Override
    public Object returnGui(Level world, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {0};
    }

    @Override
    public int getFilterSize() {
        return 1;
    }

    @Override
    public NonNullList<ItemStack> getInv() {
        return filter;
    }

    @Override
    public NonNullList<Integer> getSizes() {
        return filterSize;
    }

    @Override
    public ItemStack getFilterSlot(int i) {
        return filter.get(i);
    }

    @Override
    public int getFilterSlotSize(int i) {
        return filterSize.get(i);
    }

    @Override
    public void setFilterSlot(int i, ItemStack stack) {
        filter.set(i, stack.copy());
    }

    @Override
    public void setFilterSlotSize(int i, int size) {
        filterSize.set(i, size);
    }

    @Override
    public boolean isBlacklist() {
        return blacklist;
    }

    @Override
    public void setBlacklist(boolean black) {
        blacklist = black;
    }

    @Override
    public boolean hasStacksizeLimiters() {
        return false;
    }
}
