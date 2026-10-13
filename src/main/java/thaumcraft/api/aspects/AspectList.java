package thaumcraft.api.aspects;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Port of thaumcraft.api.aspects.AspectList.
 * <p>
 * The original readFromNBT/writeToNBT are replaced by {@link #CODEC} (disk and JSON)
 * and {@link #STREAM_CODEC} (network). JSON form: {"ignis": 5, "terra": 2}.
 */
public class AspectList {
    public static final Codec<AspectList> CODEC = Codec.unboundedMap(Aspect.CODEC, Codec.INT)
            .xmap(AspectList::fromMap, list -> list.aspects);

    public static final StreamCodec<ByteBuf, AspectList> STREAM_CODEC = ByteBufCodecs
            .map(LinkedHashMap<Aspect, Integer>::new, Aspect.STREAM_CODEC, ByteBufCodecs.VAR_INT)
            .map(AspectList::fromMap, list -> list.aspects);

    public final LinkedHashMap<Aspect, Integer> aspects = new LinkedHashMap<>();

    public AspectList() {
    }

    /** The aspects associated with an item (a copy). Empty if it has none. */
    public AspectList(ItemStack stack) {
        add(AspectHelper.getObjectAspects(stack));
    }

    private static AspectList fromMap(Map<Aspect, Integer> map) {
        AspectList out = new AspectList();
        map.forEach(out::add);
        return out;
    }

    public AspectList copy() {
        return new AspectList().add(this);
    }

    public boolean isEmpty() {
        return aspects.isEmpty();
    }

    public int size() {
        return aspects.size();
    }

    /** Total amount of all aspects. */
    public int visSize() {
        int q = 0;
        for (int amount : aspects.values()) q += amount;
        return q;
    }

    public Aspect[] getAspects() {
        return aspects.keySet().toArray(new Aspect[0]);
    }

    public Aspect[] getAspectsSortedByName() {
        Aspect[] out = getAspects();
        Arrays.sort(out, Comparator.comparing(Aspect::getTag));
        return out;
    }

    public Aspect[] getAspectsSortedByAmount() {
        Aspect[] out = getAspects();
        Arrays.sort(out, Comparator.comparingInt(this::getAmount).reversed());
        return out;
    }

    public int getAmount(Aspect key) {
        return aspects.getOrDefault(key, 0);
    }

    /** Reduces the amount only if there is enough. Returns true if it was reduced. */
    public boolean reduce(Aspect key, int amount) {
        if (getAmount(key) >= amount) {
            aspects.put(key, getAmount(key) - amount);
            return true;
        }
        return false;
    }

    /** Reduces the amount and removes the aspect if it drops to 0 or less. */
    public AspectList remove(Aspect key, int amount) {
        int am = getAmount(key) - amount;
        if (am <= 0) aspects.remove(key);
        else aspects.put(key, am);
        return this;
    }

    public AspectList remove(Aspect key) {
        aspects.remove(key);
        return this;
    }

    /** Adds the amount to the existing one. */
    public AspectList add(Aspect aspect, int amount) {
        aspects.merge(aspect, amount, Integer::sum);
        return this;
    }

    /** Keeps the higher of the existing and the new amount. */
    public AspectList merge(Aspect aspect, int amount) {
        aspects.merge(aspect, amount, Math::max);
        return this;
    }

    public AspectList add(AspectList in) {
        in.aspects.forEach(this::add);
        return this;
    }

    public AspectList remove(AspectList in) {
        in.aspects.forEach(this::remove);
        return this;
    }

    public AspectList merge(AspectList in) {
        in.aspects.forEach(this::merge);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AspectList other && aspects.equals(other.aspects);
    }

    @Override
    public int hashCode() {
        return aspects.hashCode();
    }

    @Override
    public String toString() {
        return aspects.toString();
    }
}
