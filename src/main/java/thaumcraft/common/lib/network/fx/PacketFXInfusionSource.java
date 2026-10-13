package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFX;

/** Port of PacketFXInfusionSource: an infusion matrix (p1) draws items from a pedestal (p2) or entity (color = entity id). */
public record PacketFXInfusionSource(BlockPos p1, BlockPos p2, int color) implements CustomPacketPayload {
    public static final Type<PacketFXInfusionSource> TYPE = new Type<>(Thaumcraft.id("fx_infusion_source"));

    public static final StreamCodec<ByteBuf, PacketFXInfusionSource> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketFXInfusionSource::p1,
            BlockPos.STREAM_CODEC, PacketFXInfusionSource::p2,
            ByteBufCodecs.VAR_INT, PacketFXInfusionSource::color,
            PacketFXInfusionSource::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXInfusionSource msg, IPayloadContext context) {
        ClientFX.infusionSource(msg.p1, msg.p2, msg.color);
    }
}
