package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.ClientPacketHandlers;

/** Reborn's PacketAspectDiscovery: the player learned a new aspect. */
public record PacketAspectDiscovery(String key) implements CustomPacketPayload {
    public static final Type<PacketAspectDiscovery> TYPE = new Type<>(Thaumcraft.id("aspect_discovery"));
    public static final StreamCodec<ByteBuf, PacketAspectDiscovery> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(PacketAspectDiscovery::new, PacketAspectDiscovery::key);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAspectDiscovery msg, IPayloadContext context) {
        ClientPacketHandlers.aspectDiscovery(context.player(), msg.key);
    }
}
