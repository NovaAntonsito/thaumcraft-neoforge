package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.container.ContainerLogistics;

/** A string for the server's open menu. Id 0 is the search text of the logistics screen. */
public record PacketMiscStringToServer(int id, String text) implements CustomPacketPayload {
    public static final Type<PacketMiscStringToServer> TYPE = new Type<>(Thaumcraft.id("misc_string"));
    public static final StreamCodec<ByteBuf, PacketMiscStringToServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketMiscStringToServer::id,
            ByteBufCodecs.stringUtf8(256), PacketMiscStringToServer::text,
            PacketMiscStringToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMiscStringToServer message, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && message.id == 0 && player.containerMenu instanceof ContainerLogistics container) {
            container.searchText = message.text;
            container.refreshItemList(true);
        }
    }
}
