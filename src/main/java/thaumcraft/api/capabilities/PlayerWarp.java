package thaumcraft.api.capabilities;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * Port of IPlayerWarp + PlayerWarp.
 * <p>
 * Was a Capability in 1.12.2; now a data attachment ({@code TCAttachments.WARP}) saved with {@link #CODEC}
 * and synced with {@link #STREAM_CODEC}. Call {@code player.syncData(TCAttachments.WARP)} after changing it.
 */
public class PlayerWarp {
    public enum EnumWarpType implements StringRepresentable {
        PERMANENT, NORMAL, TEMPORARY;

        public static final Codec<EnumWarpType> CODEC = StringRepresentable.fromEnum(EnumWarpType::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public static final Codec<PlayerWarp> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(EnumWarpType.CODEC, Codec.INT).optionalFieldOf("warp", Map.of()).forGetter(w -> w.warp),
            Codec.INT.optionalFieldOf("counter", 0).forGetter(PlayerWarp::getCounter)
    ).apply(i, PlayerWarp::new));

    public static final StreamCodec<ByteBuf, PlayerWarp> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, w -> w.get(EnumWarpType.PERMANENT),
            ByteBufCodecs.VAR_INT, w -> w.get(EnumWarpType.NORMAL),
            ByteBufCodecs.VAR_INT, w -> w.get(EnumWarpType.TEMPORARY),
            ByteBufCodecs.VAR_INT, PlayerWarp::getCounter,
            (p, n, t, c) -> {
                PlayerWarp w = new PlayerWarp();
                w.set(EnumWarpType.PERMANENT, p);
                w.set(EnumWarpType.NORMAL, n);
                w.set(EnumWarpType.TEMPORARY, t);
                w.setCounter(c);
                return w;
            });

    private final EnumMap<EnumWarpType, Integer> warp = new EnumMap<>(EnumWarpType.class);
    private int counter;

    public PlayerWarp() {
    }

    private PlayerWarp(Map<EnumWarpType, Integer> warp, int counter) {
        this.warp.putAll(warp);
        this.counter = counter;
    }

    public void clear() {
        warp.clear();
        counter = 0;
    }

    public int get(EnumWarpType type) {
        return warp.getOrDefault(type, 0);
    }

    public void set(EnumWarpType type, int amount) {
        warp.put(type, Math.max(0, amount));
    }

    /** Adds and returns the new value. */
    public int add(EnumWarpType type, int amount) {
        set(type, get(type) + amount);
        return get(type);
    }

    /** Reduces and returns the new value (never below 0). */
    public int reduce(EnumWarpType type, int amount) {
        return add(type, -amount);
    }

    /** Total warp, as used by warp events. */
    public int getTotal() {
        int total = 0;
        for (int v : warp.values()) total += v;
        return total;
    }

    /** Internal counter used to space out warp events. */
    public int getCounter() {
        return counter;
    }

    public void setCounter(int amount) {
        counter = amount;
    }
}
