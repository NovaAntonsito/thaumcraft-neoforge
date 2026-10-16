package thaumcraft.common.golems.seals;


import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.seals.ISealConfigToggles;

/** Port of SealGuardAdvanced: Guard with the mob, animal and player options, for smart fighters. */
public class SealGuardAdvanced extends SealGuard implements ISealConfigToggles {
    ResourceLocation icon;

    public SealGuardAdvanced() {
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_guard_advanced");
    }

    @Override
    public String getKey() {
        return "thaumcraft:guard_advanced";
    }

    @Override
    public ResourceLocation getSealIcon() {
        return icon;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {2, 3, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.FIGHTER, EnumGolemTrait.SMART};
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
