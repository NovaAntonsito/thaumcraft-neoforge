package voidexpeditions.client;

import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import voidexpeditions.VoidExpeditions;

@EventBusSubscriber(modid = VoidExpeditions.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class VEClient {
    private VEClient() {
    }

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        LayeredDraw.Layer hud = RunHud::render;
        event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, RunHud.ID, hud);
    }
}
