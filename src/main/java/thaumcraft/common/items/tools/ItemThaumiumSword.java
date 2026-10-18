package thaumcraft.common.items.tools;

import net.minecraft.world.item.SwordItem;
import thaumcraft.api.ThaumcraftMaterials;

/** The thaumium sword. Repaired with thaumium by its material. */
public class ItemThaumiumSword extends SwordItem {
    public ItemThaumiumSword(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_THAUMIUM, properties.attributes(SwordItem.createAttributes(ThaumcraftMaterials.TOOLMAT_THAUMIUM, 3, -2.4f)));
    }
}
