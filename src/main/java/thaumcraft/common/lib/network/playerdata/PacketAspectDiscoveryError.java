package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.ClientPacketHandlers;

/** Reborn's PacketAspectDiscoveryError: a scan failed because the player does not know this aspect yet. */
public record PacketAspectDiscoveryError(String key) implements CustomPacketPayload {
    public static final Type<PacketAspectDiscoveryError> TYPE = new Type<>(Thaumcraft.id("aspect_discovery_error"));
    public static final StreamCodec<ByteBuf, PacketAspectDiscoveryError> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(PacketAspectDiscoveryError::new, PacketAspectDiscoveryError::key);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAspectDiscoveryError msg, IPayloadContext context) {
        ClientPacketHandlers.aspectDiscoveryError(msg.key);
    }
}
