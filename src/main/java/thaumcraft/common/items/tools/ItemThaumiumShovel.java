package thaumcraft.common.items.tools;

import net.minecraft.world.item.ShovelItem;
import thaumcraft.api.ThaumcraftMaterials;

/** The thaumium shovel. Repaired with thaumium by its material. */
public class ItemThaumiumShovel extends ShovelItem {
    public ItemThaumiumShovel(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_THAUMIUM, properties.attributes(ShovelItem.createAttributes(ThaumcraftMaterials.TOOLMAT_THAUMIUM, 1.5f, -3.0f)));
    }
}
