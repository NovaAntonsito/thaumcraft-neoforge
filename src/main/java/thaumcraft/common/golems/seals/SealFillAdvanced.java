package thaumcraft.common.golems.seals;


import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.seals.ISealConfigToggles;

/** Port of SealFillAdvanced: Store with 9 filter slots and the filter options, for smart golems. */
public class SealFillAdvanced extends SealFill implements ISealConfigToggles {
    ResourceLocation icon;

    public SealFillAdvanced() {
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_fill_advanced");
    }

    @Override
    public String getKey() {
        return "thaumcraft:fill_advanced";
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
}
