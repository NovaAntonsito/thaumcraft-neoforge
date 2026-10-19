package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.FXDispatcher;

/** Port of PacketFXPollute: pink flux wisps where flux was released into the aura, one per point (at most 40). */
public record PacketFXPollute(BlockPos pos, byte amount) implements CustomPacketPayload {
    public static final Type<PacketFXPollute> TYPE = new Type<>(Thaumcraft.id("fx_pollute"));

    public static final StreamCodec<ByteBuf, PacketFXPollute> STREAM_CODEC = StreamCodec.of((buf, m) -> {
        BlockPos.STREAM_CODEC.encode(buf, m.pos);
        buf.writeByte(m.amount);
    }, buf -> new PacketFXPollute(BlockPos.STREAM_CODEC.decode(buf), buf.readByte()));

    public PacketFXPollute(BlockPos pos, float amt) {
        this(pos, (byte) (amt < 1.0f && amt > 0.0f ? 1.0f : amt));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXPollute message, IPayloadContext context) {
        for (int a = 0; a < Math.min(40, message.amount); ++a) {
            FXDispatcher.drawPollutionParticles(message.pos);
        }
    }
}
