package thaumcraft.common.items.tools;

import net.minecraft.world.item.HoeItem;
import thaumcraft.api.ThaumcraftMaterials;

/**
 * The thaumium hoe. Repaired with thaumium by its material.
 * As a 1.12 hoe it adds no damage and its attack speed is the material damage minus 3.
 */
public class ItemThaumiumHoe extends HoeItem {
    public ItemThaumiumHoe(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_THAUMIUM, properties.attributes(HoeItem.createAttributes(ThaumcraftMaterials.TOOLMAT_THAUMIUM, -2.5f, -0.5f)));
    }
}
