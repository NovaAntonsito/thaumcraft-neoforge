package thaumcraft.client.gui;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.common.anomaly.AnomalyClientState;
import thaumcraft.common.items.tools.ItemThaumometer;

/** Opens the Foresight cards when they arrive, and again on sneak + use with a thaumometer. Not in Thaumcraft. */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class AnomalyClientEvents {
    private AnomalyClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!AnomalyClientState.choiceIds.isEmpty() && mc.level.getGameTime() > AnomalyClientState.choiceUntil + 40L) {
            // The server closes the choice itself; this only tidies up if that packet was missed.
            AnomalyClientState.choiceIds = java.util.List.of();
        }
        if (AnomalyClientState.choiceToOpen && mc.screen == null) {
            AnomalyClientState.choiceToOpen = false;
            if (!AnomalyClientState.choiceIds.isEmpty()) mc.setScreen(new GuiAnomalyChoice());
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide || AnomalyClientState.choiceIds.isEmpty()) return;
        if (event.getEntity().isShiftKeyDown() && event.getItemStack().getItem() instanceof ItemThaumometer) {
            Minecraft.getInstance().setScreen(new GuiAnomalyChoice());
            event.setCanceled(true);
        }
    }
}
