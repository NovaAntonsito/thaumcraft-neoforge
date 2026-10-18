package thaumcraft.common.items.tools;

import net.minecraft.world.item.AxeItem;
import thaumcraft.api.ThaumcraftMaterials;

/**
 * The thaumium axe. Repaired with thaumium by its material.
 * 1.12 set the axe damage to 8 without the material bonus; 1.21 adds the bonus, so the base is 8 minus it.
 */
public class ItemThaumiumAxe extends AxeItem {
    public ItemThaumiumAxe(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_THAUMIUM, properties.attributes(AxeItem.createAttributes(ThaumcraftMaterials.TOOLMAT_THAUMIUM, 5.5f, -3.0f)));
    }
}
