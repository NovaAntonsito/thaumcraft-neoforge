package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteManager;

/** Reborn's PacketGivePlayerNoteToServer: a note clicked in the thaumonomicon. */
public record PacketGivePlayerNoteToServer(String key) implements CustomPacketPayload {
    public static final Type<PacketGivePlayerNoteToServer> TYPE = new Type<>(Thaumcraft.id("give_note"));
    public static final StreamCodec<ByteBuf, PacketGivePlayerNoteToServer> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(PacketGivePlayerNoteToServer::new, PacketGivePlayerNoteToServer::key);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketGivePlayerNoteToServer msg, IPayloadContext context) {
        Player player = context.player();
        if (!ResearchNoteManager.isNote(msg.key) || ThaumcraftCapabilities.knowsResearchStrict(player, msg.key)) return;
        String research = ResearchNoteManager.getStrippedKey(msg.key);
        if (ResearchManager.doesPlayerHaveRequisites(player, research) && ThaumcraftCapabilities.knowsResearch(player, research)) {
            ResearchNoteManager.givePlayerResearchNote(player.level(), player, msg.key);
        } else {
            player.sendSystemMessage(Component.translatable("tc.researcherror"));
        }
    }
}
