package thaumcraft.common.items.tools;

import net.minecraft.world.item.Item;
import thaumcraft.api.items.IScribeTools;

/** Port of ItemScribingTools: 100 uses of ink, refilled with a black dye. */
public class ItemScribingTools extends Item implements IScribeTools {
    public ItemScribingTools(Item.Properties properties) {
        super(properties.stacksTo(1).durability(100));
    }
}
