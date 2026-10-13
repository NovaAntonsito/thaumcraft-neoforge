package thaumcraft.common.anomaly;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** The anomaly clock of the player's dimension: time left, whether it is paused, and the active anomalies. */
public record PacketAnomalySync(boolean enabled, int remaining, int total, boolean paused, int cycle, List<Entry> active)
        implements CustomPacketPayload {
    public static final Type<PacketAnomalySync> TYPE = new Type<>(Thaumcraft.id("anomaly_sync"));
    public static final PacketAnomalySync DISABLED = new PacketAnomalySync(false, 0, 1, false, 0, List.of());

    public record Entry(String id, int remaining, int duration) {
        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Entry::id,
                ByteBufCodecs.VAR_INT, Entry::remaining,
                ByteBufCodecs.VAR_INT, Entry::duration,
                Entry::new);
    }

    public static final StreamCodec<ByteBuf, PacketAnomalySync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketAnomalySync::enabled,
            ByteBufCodecs.VAR_INT, PacketAnomalySync::remaining,
            ByteBufCodecs.VAR_INT, PacketAnomalySync::total,
            ByteBufCodecs.BOOL, PacketAnomalySync::paused,
            ByteBufCodecs.VAR_INT, PacketAnomalySync::cycle,
            Entry.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), PacketAnomalySync::active,
            PacketAnomalySync::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAnomalySync msg, IPayloadContext context) {
        AnomalyClientState.update(msg, context.player().level().getGameTime());
    }
}
