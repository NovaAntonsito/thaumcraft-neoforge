package thaumcraft.common.items.tools;

import net.minecraft.world.item.PickaxeItem;
import thaumcraft.api.ThaumcraftMaterials;

/** The thaumium pickaxe. Repaired with thaumium by its material. */
public class ItemThaumiumPickaxe extends PickaxeItem {
    public ItemThaumiumPickaxe(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_THAUMIUM, properties.attributes(PickaxeItem.createAttributes(ThaumcraftMaterials.TOOLMAT_THAUMIUM, 1.0f, -2.8f)));
    }
}
