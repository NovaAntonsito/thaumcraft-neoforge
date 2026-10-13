package thaumcraft.common.lib.events;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.research.ResearchManager;

/**
 * Partial port of CraftingEvents.onCrafting: crafting an item that a research stage asks for (by item or by
 * item tag, which replaced the ore dictionary) records it as research "[#]hash".
 * TODO: the warp of crafted items and the label/phial return, with their features.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class CraftingEvents {
    private CraftingEvents() {
    }

    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() == null || event.getEntity().level().isClientSide) return;
        ItemStack crafting = event.getCrafting();
        int stackHash = ResearchManager.createItemStackHash(crafting.copy());
        if (ResearchManager.craftingReferences.contains(stackHash)) {
            ResearchManager.completeResearch(event.getEntity(), "[#]" + stackHash);
        }
        crafting.getTags().forEach(tag -> {
            int cd = ("tag:" + tag.location()).hashCode();
            if (ResearchManager.craftingReferences.contains(cd)) {
                ResearchManager.completeResearch(event.getEntity(), "[#]" + cd);
            }
        });
    }
}
