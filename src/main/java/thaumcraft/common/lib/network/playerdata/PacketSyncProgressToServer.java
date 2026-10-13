package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.utils.InventoryUtils;

/**
 * Port of PacketSyncProgressToServer: the client asks to progress a research ("first": whether the client thought
 * it was already known). With "checks", the stage requirements are verified and consumed first.
 */
public record PacketSyncProgressToServer(String key, boolean first, boolean checks, boolean noFlags) implements CustomPacketPayload {
    public static final Type<PacketSyncProgressToServer> TYPE = new Type<>(Thaumcraft.id("sync_progress"));
    public static final StreamCodec<ByteBuf, PacketSyncProgressToServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketSyncProgressToServer::key,
            ByteBufCodecs.BOOL, PacketSyncProgressToServer::first,
            ByteBufCodecs.BOOL, PacketSyncProgressToServer::checks,
            ByteBufCodecs.BOOL, PacketSyncProgressToServer::noFlags,
            PacketSyncProgressToServer::new);

    public PacketSyncProgressToServer(String key, boolean first) {
        this(key, first, false, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncProgressToServer msg, IPayloadContext context) {
        Player player = context.player();
        if (msg.first != ThaumcraftCapabilities.knowsResearch(player, msg.key)) {
            if (msg.checks && !checkRequisites(player, msg.key)) return;
            if (msg.noFlags) ResearchManager.noFlags = true;
            ResearchManager.progressResearch(player, msg.key);
        }
    }

    private static boolean checkRequisites(Player player, String key) {
        ResearchEntry research = ResearchCategories.getResearch(key);
        if (research == null || research.getStages() == null) return true;
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        int currentStage = knowledge.getResearchStage(key) - 1;
        if (currentStage < 0) return false;
        if (currentStage >= research.getStages().length) return true;
        ResearchStage stage = research.getStages()[currentStage];
        Object[] o = stage.getObtain();
        if (o != null) {
            for (Object req : o) {
                if (!InventoryUtils.isPlayerCarryingAmount(player, req)) return false;
            }
            for (Object req : o) InventoryUtils.consumePlayerItem(player, req, true);
        }
        Object[] c = stage.getCraft();
        if (c != null) {
            for (int a = 0; a < c.length; ++a) {
                if (!knowledge.isResearchKnown("[#]" + stage.getCraftReference()[a])) return false;
            }
        }
        String[] r = stage.getResearch();
        if (r != null) {
            for (String res : r) {
                if (!ThaumcraftCapabilities.knowsResearchStrict(player, res)) return false;
            }
        }
        ResearchStage.Knowledge[] k = stage.getKnow();
        if (k != null) {
            for (ResearchStage.Knowledge kn : k) {
                if (knowledge.getKnowledge(kn.type, kn.category) < kn.amount) return false;
            }
            for (ResearchStage.Knowledge kn : k) {
                ResearchManager.addKnowledge(player, kn.type, kn.category, -kn.amount * kn.type.getProgression());
            }
        }
        return true;
    }
}
