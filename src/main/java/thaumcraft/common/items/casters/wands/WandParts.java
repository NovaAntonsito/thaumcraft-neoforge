package thaumcraft.common.items.casters.wands;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.api.aspects.Aspect;

/**
 * The rod and cap of a wand (Thaumcraft 4's WandRod and WandCap). The rod sets how much vis the wand holds and
 * most of how complex a focus it can cast; the cap sets the vis cost and the rest of the complexity. Primal rods
 * slowly refill themselves up to a tenth of their capacity. The gauntlet stays above every wand: it draws straight
 * from the aura and casts any focus. Not in Thaumcraft 6.
 */
public record WandParts(String rod, String cap) {
    public static final WandParts DEFAULT = new WandParts("wood", "iron");
    public static final Codec<WandParts> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("rod").forGetter(WandParts::rod),
            Codec.STRING.fieldOf("cap").forGetter(WandParts::cap)).apply(i, WandParts::new));
    public static final StreamCodec<ByteBuf, WandParts> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WandParts::rod, ByteBufCodecs.STRING_UTF8, WandParts::cap, WandParts::new);

    /**
     * A rod: its vis capacity (Thaumcraft 4's), the focus complexity it allows, its Thaumcraft 4 craft cost, and
     * the primal it refills from (null if it does not refill).
     */
    public record Rod(String id, int capacity, int complexity, int craftCost, @Nullable Aspect primal, boolean glowing) {
    }

    /** A cap: the vis cost multiplier (Thaumcraft 4's), the focus complexity it adds and its craft cost. */
    public record Cap(String id, float costModifier, int complexity, int craftCost) {
    }

    public static final Map<String, Rod> RODS = new LinkedHashMap<>();
    public static final Map<String, Cap> CAPS = new LinkedHashMap<>();

    static {
        rod(new Rod("wood", 25, 6, 1, null, false));
        rod(new Rod("greatwood", 50, 10, 3, null, false));
        rod(new Rod("obsidian", 75, 16, 6, Aspect.EARTH, false));
        rod(new Rod("blaze", 75, 16, 6, Aspect.FIRE, true));
        rod(new Rod("ice", 75, 16, 6, Aspect.WATER, false));
        rod(new Rod("quartz", 75, 16, 6, Aspect.ORDER, false));
        rod(new Rod("bone", 75, 16, 6, Aspect.ENTROPY, false));
        rod(new Rod("reed", 75, 16, 6, Aspect.AIR, false));
        rod(new Rod("silverwood", 100, 26, 9, null, false));
        cap(new Cap("iron", 1.1f, 2, 1));
        cap(new Cap("copper", 1.05f, 3, 2));
        cap(new Cap("gold", 1.0f, 5, 3));
        cap(new Cap("silver", 0.95f, 8, 4));
        cap(new Cap("thaumium", 0.9f, 12, 6));
        cap(new Cap("void", 0.8f, 16, 9));
    }

    private static void rod(Rod r) {
        RODS.put(r.id(), r);
    }

    private static void cap(Cap c) {
        CAPS.put(c.id(), c);
    }

    public Rod getRod() {
        return RODS.getOrDefault(rod, RODS.get("wood"));
    }

    public Cap getCap() {
        return CAPS.getOrDefault(cap, CAPS.get("iron"));
    }

    /** The most complex focus package this wand casts. */
    public int maxComplexity() {
        return getRod().complexity() + getCap().complexity();
    }
}
