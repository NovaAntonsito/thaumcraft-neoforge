package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFX;

/** Port of PacketFXEssentiaSource: essentia flowing from a source (pos - d) to a tile at pos. */
public record PacketFXEssentiaSource(BlockPos pos, byte dx, byte dy, byte dz, int color, int ext) implements CustomPacketPayload {
    public static final Type<PacketFXEssentiaSource> TYPE = new Type<>(Thaumcraft.id("fx_essentia_source"));

    public static final StreamCodec<ByteBuf, PacketFXEssentiaSource> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketFXEssentiaSource::pos,
            ByteBufCodecs.BYTE, PacketFXEssentiaSource::dx,
            ByteBufCodecs.BYTE, PacketFXEssentiaSource::dy,
            ByteBufCodecs.BYTE, PacketFXEssentiaSource::dz,
            ByteBufCodecs.INT, PacketFXEssentiaSource::color,
            ByteBufCodecs.VAR_INT, PacketFXEssentiaSource::ext,
            PacketFXEssentiaSource::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXEssentiaSource msg, IPayloadContext context) {
        BlockPos source = msg.pos.offset(-msg.dx, -msg.dy, -msg.dz);
        ClientFX.essentiaTrail(source, msg.pos, msg.color);
    }
}
