package thaumcraft.common.items.baubles;

import net.minecraft.world.item.Item;

/**
 * The headband of curiosity: halves the experience picked up and turns the rest into a chance at knowledge
 * (PlayerEvents.pickupXP). Worn on the head, drawn by RenderBaubleHead.
 */
public class ItemCuriosityBand extends Item {
    public ItemCuriosityBand(Item.Properties properties) {
        super(properties.stacksTo(1));
    }
}
