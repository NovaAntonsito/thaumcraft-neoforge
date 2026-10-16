package thaumcraft.common.anomaly;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** A seer to the server: the anomaly card they picked. */
public record PacketAnomalyChoose(String id) implements CustomPacketPayload {
    public static final Type<PacketAnomalyChoose> TYPE = new Type<>(Thaumcraft.id("anomaly_choose"));
    public static final StreamCodec<ByteBuf, PacketAnomalyChoose> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketAnomalyChoose::id,
            PacketAnomalyChoose::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAnomalyChoose msg, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sp && AnomalyManager.isEnabledIn(sp.serverLevel())) {
            AnomalyManager.get(sp.serverLevel()).choose(sp, msg.id());
        }
    }
}
