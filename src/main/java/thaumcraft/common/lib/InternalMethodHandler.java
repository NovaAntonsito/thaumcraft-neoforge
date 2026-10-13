package thaumcraft.common.lib;

import net.minecraft.network.chat.Component;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.common.registry.TCAttachments;

/** Partial port of InternalMethodHandler (ThaumcraftApi.internalMethods). */
public final class InternalMethodHandler {
    private InternalMethodHandler() {
    }

    /**
     * Adds warp of the given type to a player and syncs it.
     * TODO: the original also sends PacketWarpMessage (the chat line and sound).
     */
    public static void addWarpToPlayer(Player player, int amount, PlayerWarp.EnumWarpType type) {
        if (amount == 0 || player.level().isClientSide) {
            return;
        }
        PlayerWarp pw = player.getData(TCAttachments.WARP);
        int cur = pw.get(type);
        if (amount < 0 && cur + amount < 0) {
            amount = cur;
        }
        pw.add(type, amount);
        if (amount > 0) {
            pw.setCounter(pw.get(PlayerWarp.EnumWarpType.TEMPORARY) + pw.get(PlayerWarp.EnumWarpType.PERMANENT)
                    + pw.get(PlayerWarp.EnumWarpType.NORMAL));
        }
        if (type != PlayerWarp.EnumWarpType.TEMPORARY && ThaumcraftCapabilities.knowsResearchStrict(player, "FIRSTSTEPS")
                && !ThaumcraftCapabilities.knowsResearchStrict(player, "WARP")) {
            ResearchManager.completeResearch(player, "WARP");
            player.displayClientMessage(Component.translatable("research.WARP.warn"), true);
        }
        player.syncData(TCAttachments.WARP);
    }
}
