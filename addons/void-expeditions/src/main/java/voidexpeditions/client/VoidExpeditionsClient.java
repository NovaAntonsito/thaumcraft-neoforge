package voidexpeditions.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.client.ponder.VEPonderPlugin;

/** The client side of the mod. */
@Mod(value = VoidExpeditions.MODID, dist = Dist.CLIENT)
public class VoidExpeditionsClient {
    public VoidExpeditionsClient() {
        // Optional Ponder tutorials: the plugin class touches Ponder, so only load it when Ponder is there.
        if (ModList.get().isLoaded("ponder")) {
            VEPonderPlugin.register();
        }
    }
}
