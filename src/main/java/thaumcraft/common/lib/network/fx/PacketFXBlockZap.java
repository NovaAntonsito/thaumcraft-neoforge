package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** Port of TC4's PacketFXBlockZap: a node bolt from a block to a point, with the zap sound. */
public record PacketFXBlockZap(float x, float y, float z, float dx, float dy, float dz) implements CustomPacketPayload {
    public static final Type<PacketFXBlockZap> TYPE = new Type<>(Thaumcraft.id("fx_block_zap"));
    public static final StreamCodec<ByteBuf, PacketFXBlockZap> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, PacketFXBlockZap::x,
            ByteBufCodecs.FLOAT, PacketFXBlockZap::y,
            ByteBufCodecs.FLOAT, PacketFXBlockZap::z,
            ByteBufCodecs.FLOAT, PacketFXBlockZap::dx,
            ByteBufCodecs.FLOAT, PacketFXBlockZap::dy,
            ByteBufCodecs.FLOAT, PacketFXBlockZap::dz,
            PacketFXBlockZap::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBlockZap message, IPayloadContext context) {
        thaumcraft.client.fx.FXDispatcher.nodeBolt(message.x, message.y, message.z, message.dx, message.dy, message.dz);
    }
}
