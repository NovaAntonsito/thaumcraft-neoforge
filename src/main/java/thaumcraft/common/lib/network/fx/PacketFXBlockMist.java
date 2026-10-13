package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFX;

/** Port of PacketFXBlockMist: a colored mist rising from a block (the lamp of growth on the plant it boosts). */
public record PacketFXBlockMist(BlockPos pos, int color) implements CustomPacketPayload {
    public static final Type<PacketFXBlockMist> TYPE = new Type<>(Thaumcraft.id("fx_block_mist"));

    public static final StreamCodec<ByteBuf, PacketFXBlockMist> STREAM_CODEC = StreamCodec.of((buf, m) -> {
        BlockPos.STREAM_CODEC.encode(buf, m.pos);
        buf.writeInt(m.color);
    }, buf -> new PacketFXBlockMist(BlockPos.STREAM_CODEC.decode(buf), buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBlockMist msg, IPayloadContext context) {
        ClientFX.blockMist(msg.pos, msg.color);
    }
}
