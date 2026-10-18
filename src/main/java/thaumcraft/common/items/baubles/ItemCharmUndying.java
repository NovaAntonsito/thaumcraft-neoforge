package thaumcraft.common.items.baubles;

import net.minecraft.world.item.Item;

/** The charm of undying: a totem of undying worn in the charm slot. The work is done in PlayerEvents.onDeath. */
public class ItemCharmUndying extends Item {
    public ItemCharmUndying(Item.Properties properties) {
        super(properties.stacksTo(1));
    }
}
