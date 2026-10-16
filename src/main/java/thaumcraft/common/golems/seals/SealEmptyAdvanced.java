package thaumcraft.common.golems.seals;

import java.util.ArrayList;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.seals.ISealConfigToggles;

/** Port of SealEmptyAdvanced: Empty with 9 filter slots, the filter options and the whitelist cycle, for smart golems. */
public class SealEmptyAdvanced extends SealEmpty implements ISealConfigToggles {
    ResourceLocation icon;

    public SealEmptyAdvanced() {
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_empty_advanced");
    }

    @Override
    public String getKey() {
        return "thaumcraft:empty_advanced";
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public ResourceLocation getSealIcon() {
        return icon;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {1, 3, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.SMART};
    }

    @Override
    public SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int indx, boolean value) {
        props[indx].setValue(value);
    }

    /** With "cycle whitelist", each task takes only the next stack of the whitelist. */
    @Override
    public NonNullList<ItemStack> getInv(int c) {
        if (getToggles()[4].value && !isBlacklist()) {
            ArrayList<ItemStack> w = new ArrayList<>();
            for (ItemStack s : super.getInv()) {
                if (s != null && !s.isEmpty()) {
                    w.add(s);
                }
            }
            if (w.size() > 0) {
                int i = Math.abs(c % w.size());
                return NonNullList.withSize(1, w.get(i));
            }
        }
        return super.getInv();
    }
}
