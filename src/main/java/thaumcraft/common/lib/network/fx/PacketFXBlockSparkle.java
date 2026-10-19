package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** Port of TC4's PacketFXBlockSparkle: seven sparkles of a colour around a block. */
public record PacketFXBlockSparkle(BlockPos pos, int color) implements CustomPacketPayload {
    public static final Type<PacketFXBlockSparkle> TYPE = new Type<>(Thaumcraft.id("fx_block_sparkle"));
    public static final StreamCodec<ByteBuf, PacketFXBlockSparkle> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketFXBlockSparkle::pos,
            ByteBufCodecs.INT, PacketFXBlockSparkle::color,
            PacketFXBlockSparkle::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBlockSparkle message, IPayloadContext context) {
        thaumcraft.client.fx.FXDispatcher.blockSparkle(message.pos.getX(), message.pos.getY(), message.pos.getZ(), message.color, 7);
    }
}
