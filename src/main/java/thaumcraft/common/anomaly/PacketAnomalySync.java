package thaumcraft.common.anomaly;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/**
 * The anomaly clock of the player's dimension: time left, whether it is paused, the active anomalies, the kind of
 * the next one (a {@link AnomalyType.Polarity} ordinal, -1 if unknown) and, while a seer chooses, the ticks left
 * to choose (0 otherwise).
 */
public record PacketAnomalySync(boolean enabled, int remaining, int total, boolean paused, int cycle, List<Entry> active,
                                int nextPolarity, int choosing) implements CustomPacketPayload {
    public static final Type<PacketAnomalySync> TYPE = new Type<>(Thaumcraft.id("anomaly_sync"));
    public static final PacketAnomalySync DISABLED = new PacketAnomalySync(false, 0, 1, false, 0, List.of(), -1, 0);

    /** An active anomaly. Local ones carry their x and z so the HUD can point at them. */
    public record Entry(String id, int remaining, int duration, boolean local, int x, int z) {
        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Entry::id,
                ByteBufCodecs.VAR_INT, Entry::remaining,
                ByteBufCodecs.VAR_INT, Entry::duration,
                ByteBufCodecs.BOOL, Entry::local,
                ByteBufCodecs.VAR_INT, Entry::x,
                ByteBufCodecs.VAR_INT, Entry::z,
                Entry::new);
    }

    private static final StreamCodec<ByteBuf, List<Entry>> ENTRIES = Entry.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new));

    /** Written by hand: composite takes at most six fields. */
    public static final StreamCodec<ByteBuf, PacketAnomalySync> STREAM_CODEC = StreamCodec.of((buf, m) -> {
        ByteBufCodecs.BOOL.encode(buf, m.enabled());
        ByteBufCodecs.VAR_INT.encode(buf, m.remaining());
        ByteBufCodecs.VAR_INT.encode(buf, m.total());
        ByteBufCodecs.BOOL.encode(buf, m.paused());
        ByteBufCodecs.VAR_INT.encode(buf, m.cycle());
        ENTRIES.encode(buf, m.active());
        ByteBufCodecs.VAR_INT.encode(buf, m.nextPolarity());
        ByteBufCodecs.VAR_INT.encode(buf, m.choosing());
    }, buf -> new PacketAnomalySync(ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
            ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.BOOL.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
            ENTRIES.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAnomalySync msg, IPayloadContext context) {
        AnomalyClientState.update(msg, context.player().level().getGameTime());
    }
}
