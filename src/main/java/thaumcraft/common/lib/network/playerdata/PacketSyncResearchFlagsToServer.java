package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.PlayerKnowledge.EnumResearchFlag;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;

/** Port of PacketSyncResearchFlagsToServer: the client's PAGE, POPUP and RESEARCH flags of one research. */
public record PacketSyncResearchFlagsToServer(String key, byte flags) implements CustomPacketPayload {
    public static final Type<PacketSyncResearchFlagsToServer> TYPE = new Type<>(Thaumcraft.id("sync_research_flags"));
    public static final StreamCodec<ByteBuf, PacketSyncResearchFlagsToServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketSyncResearchFlagsToServer::key,
            ByteBufCodecs.BYTE, PacketSyncResearchFlagsToServer::flags,
            PacketSyncResearchFlagsToServer::new);

    private static final EnumResearchFlag[] ORDER = {EnumResearchFlag.PAGE, EnumResearchFlag.POPUP, EnumResearchFlag.RESEARCH};

    public PacketSyncResearchFlagsToServer(Player player, String key) {
        this(key, pack(ThaumcraftCapabilities.getKnowledge(player), key));
    }

    private static byte pack(PlayerKnowledge knowledge, String key) {
        byte b = 0;
        for (int i = 0; i < ORDER.length; i++) {
            if (knowledge.hasResearchFlag(key, ORDER[i])) b |= (byte) (1 << i);
        }
        return b;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncResearchFlagsToServer msg, IPayloadContext context) {
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(context.player());
        for (int i = 0; i < ORDER.length; i++) {
            if ((msg.flags & (1 << i)) != 0) {
                knowledge.setResearchFlag(msg.key, ORDER[i]);
            } else {
                knowledge.clearResearchFlag(msg.key, ORDER[i]);
            }
        }
    }
}
