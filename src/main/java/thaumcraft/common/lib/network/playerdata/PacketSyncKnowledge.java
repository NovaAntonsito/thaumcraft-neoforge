package thaumcraft.common.lib.network.playerdata;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.client.ClientPacketHandlers;
import thaumcraft.common.registry.TCAttachments;

/**
 * Port of PacketSyncKnowledge: the whole knowledge to its owner. The server clears the POPUP flags once sent;
 * the client shows a research toast for each of them.
 */
public record PacketSyncKnowledge(PlayerKnowledge knowledge) implements CustomPacketPayload {
    public static final Type<PacketSyncKnowledge> TYPE = new Type<>(Thaumcraft.id("sync_knowledge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncKnowledge> STREAM_CODEC =
            PlayerKnowledge.STREAM_CODEC.map(PacketSyncKnowledge::new, PacketSyncKnowledge::knowledge);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** IPlayerKnowledge.sync */
    public static void sync(ServerPlayer player) {
        PlayerKnowledge pk = player.getData(TCAttachments.KNOWLEDGE);
        // Encoded now, before the popups are cleared (the original serialized in the packet constructor).
        PacketDistributor.sendToPlayer(player, new PacketSyncKnowledge(pk.copy()));
        for (String key : pk.getResearchList()) pk.clearResearchFlag(key, PlayerKnowledge.EnumResearchFlag.POPUP);
    }

    public static void handle(PacketSyncKnowledge msg, IPayloadContext context) {
        ClientPacketHandlers.syncKnowledge(context.player(), msg.knowledge);
    }
}
