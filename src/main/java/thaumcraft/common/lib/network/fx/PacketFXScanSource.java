package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFX;

/** Port of PacketFXScanSource: the Sounding infusion enchantment reveals nearby ores. */
public record PacketFXScanSource(BlockPos loc, int size) implements CustomPacketPayload {
    public static final Type<PacketFXScanSource> TYPE = new Type<>(Thaumcraft.id("fx_scan_source"));

    public static final StreamCodec<ByteBuf, PacketFXScanSource> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketFXScanSource::loc,
            ByteBufCodecs.VAR_INT, PacketFXScanSource::size,
            PacketFXScanSource::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXScanSource msg, IPayloadContext context) {
        ClientFX.startScan(msg.loc, msg.size);
    }
}
