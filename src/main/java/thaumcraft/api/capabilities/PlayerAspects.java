package thaumcraft.api.capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/**
 * The player's research points per aspect (the TC4 "aspect pool"), from TC4 Research Port: Reborn (OldResStorage).
 * An aspect is known once it has an entry, even with 0 points; the six primals are known from the start.
 * Kept in insertion order, which is the order the research table lists them.
 * <p>
 * A data attachment ({@code TCAttachments.ASPECTS}): call {@code player.syncData(TCAttachments.ASPECTS)} after a bulk change.
 */
public class PlayerAspects {
    public static final List<Aspect> PRIMAL_ASPECTS = List.of(Aspect.AIR, Aspect.EARTH, Aspect.FIRE, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY);

    private record Entry(Aspect aspect, int amount) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Aspect.CODEC.fieldOf("aspect").forGetter(Entry::aspect),
                Codec.INT.fieldOf("amount").forGetter(Entry::amount)
        ).apply(i, Entry::new));
        static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                Aspect.STREAM_CODEC, Entry::aspect, ByteBufCodecs.VAR_INT, Entry::amount, Entry::new);
    }

    public static final Codec<PlayerAspects> CODEC = Entry.CODEC.listOf()
            .xmap(PlayerAspects::new, PlayerAspects::entries);

    public static final StreamCodec<ByteBuf, PlayerAspects> STREAM_CODEC = Entry.STREAM_CODEC.apply(ByteBufCodecs.list())
            .map(PlayerAspects::new, PlayerAspects::entries);

    private final Map<Aspect, Integer> aspects = new LinkedHashMap<>();

    public PlayerAspects() {
        PRIMAL_ASPECTS.forEach(this::researchAspect);
    }

    private PlayerAspects(List<Entry> entries) {
        this();
        for (Entry e : entries) {
            if (e.aspect() != null) aspects.put(e.aspect(), e.amount());
        }
    }

    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        aspects.forEach((a, n) -> out.add(new Entry(a, n)));
        return out;
    }

    public boolean isKnowAspect(@Nullable Aspect aspect) {
        return aspect != null && aspects.containsKey(aspect);
    }

    /** True if both components are known (a primal always is). */
    public boolean isKnowParentAspect(@Nullable Aspect aspect) {
        if (aspect == null) return false;
        Aspect[] components = aspect.getComponents();
        if (components == null) return true;
        return aspects.keySet().containsAll(Arrays.asList(components));
    }

    /** Marks the aspect as known. Returns false if it already was. */
    public boolean researchAspect(@Nullable Aspect aspect) {
        if (aspect == null || aspects.containsKey(aspect)) return false;
        aspects.put(aspect, 0);
        return true;
    }

    public boolean setAspectCount(@Nullable Aspect aspect, int count) {
        if (aspect == null) return false;
        aspects.put(aspect, count);
        return true;
    }

    /** Adds points (learning the aspect if needed). Removing fails if the aspect is unknown or would go below 0. */
    public boolean addToAspectPool(@Nullable Aspect aspect, int count) {
        if (aspect == null) return false;
        if (count == 0) return true;
        if (count > 0) {
            aspects.put(aspect, aspectCount(aspect) + count);
        } else {
            if (!aspects.containsKey(aspect)) return false;
            int curr = aspectCount(aspect);
            if (curr + count < 0) return false;
            aspects.put(aspect, curr + count);
        }
        return true;
    }

    public int aspectCount(@Nullable Aspect aspect) {
        return aspect == null ? 0 : aspects.getOrDefault(aspect, 0);
    }

    /** A copy of the pool: every known aspect with its points. */
    public AspectList aspectsPool() {
        AspectList list = new AspectList();
        list.aspects.putAll(aspects);
        return list;
    }

    /** Forgets everything but the primals, as a new player. */
    public void clear() {
        aspects.clear();
        PRIMAL_ASPECTS.forEach(this::researchAspect);
    }
}
