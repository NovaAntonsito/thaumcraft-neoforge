package thaumcraft.common.items.baubles;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * The cloudstepper ring: one more jump in the air (client side, ClientBaubles.cloudRingTick), and falls hurt less
 * (PlayerEvents.onFallDamage).
 */
public class ItemCloudRing extends Item implements ICurioItem {
    public ItemCloudRing(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity().level().isClientSide) {
            thaumcraft.client.lib.events.ClientBaubles.cloudRingTick(slotContext.entity());
        }
    }
}
