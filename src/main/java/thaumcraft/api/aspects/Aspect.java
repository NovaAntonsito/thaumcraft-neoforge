package thaumcraft.api.aspects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;

/**
 * Port of thaumcraft.api.aspects.Aspect (TC 6.1.BETA26).
 * <p>
 * As in the original, aspects are static constants that register themselves in {@link #aspects}
 * when constructed, and are identified by their latin {@link #getTag() tag}.
 */
public class Aspect {
    /** All registered aspects, in registration order. Must be declared before the constants below. */
    public static final LinkedHashMap<String, Aspect> aspects = new LinkedHashMap<>();

    /** For easy reference of what compounds are made up of (component order does not matter). */
    private static final Map<String, Aspect> mixList = new HashMap<>();

    /** Serializes an aspect as its tag ("ignis"). */
    public static final Codec<Aspect> CODEC = Codec.STRING.comapFlatMap(
            tag -> {
                Aspect aspect = getAspect(tag);
                return aspect != null ? DataResult.success(aspect) : DataResult.error(() -> "Unknown aspect: " + tag);
            },
            Aspect::getTag);

    public static final StreamCodec<ByteBuf, Aspect> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Aspect::getAspect, Aspect::getTag);

    private final String tag;
    @Nullable
    private final Aspect[] components;
    private final int color;
    @Nullable
    private final ChatFormatting chatcolor;
    private final ResourceLocation image;
    private final int blend;

    /**
     * Use this constructor to register your own aspects.
     *
     * @param tag        the key that will be used to reference this aspect, as well as its latin display name
     * @param color      color to display the tag in
     * @param components the aspects this one is formed from
     * @param image      ResourceLocation pointing to a 32x32 icon of the aspect
     * @param blend      GL11 blendmode (1 or 771). Used for rendering nodes. Default is 1
     */
    public Aspect(String tag, int color, @Nullable Aspect[] components, ResourceLocation image, int blend) {
        this(tag, color, components, null, image, blend);
    }

    private Aspect(String tag, int color, @Nullable Aspect[] components, @Nullable ChatFormatting chatcolor, ResourceLocation image, int blend) {
        if (aspects.containsKey(tag)) throw new IllegalArgumentException(tag + " already registered!");
        if (components != null && components.length != 2) throw new IllegalArgumentException(tag + ": a compound aspect needs exactly 2 components");
        this.tag = tag;
        this.components = components;
        this.color = color;
        this.chatcolor = chatcolor;
        this.image = image;
        this.blend = blend;
        aspects.put(tag, this);
        if (components != null) mixList.put(mixKey(components[0], components[1]), this);
    }

    /** Shortcut constructor used for the default aspects - you shouldn't be using this. */
    public Aspect(String tag, int color, Aspect[] components) {
        this(tag, color, components, defaultImage(tag), 1);
    }

    /** Shortcut constructor used for the default aspects - you shouldn't be using this. */
    public Aspect(String tag, int color, Aspect[] components, int blend) {
        this(tag, color, components, defaultImage(tag), blend);
    }

    /** Shortcut constructor used for the primal aspects - making your own primal aspects will break all the things. */
    private Aspect(String tag, int color, ChatFormatting chatcolor, int blend) {
        this(tag, color, null, chatcolor, defaultImage(tag), blend);
    }

    private static ResourceLocation defaultImage(String tag) {
        return ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/aspects/" + tag + ".png");
    }

    private static String mixKey(Aspect a, Aspect b) {
        // Alphabetical order so (aer, ignis) and (ignis, aer) map to the same key.
        return a.tag.compareTo(b.tag) <= 0 ? a.tag + "+" + b.tag : b.tag + "+" + a.tag;
    }

    public String getTag() {
        return tag;
    }

    public int getColor() {
        return color;
    }

    @Nullable
    public Aspect[] getComponents() {
        return components;
    }

    public ResourceLocation getImage() {
        return image;
    }

    public int getBlend() {
        return blend;
    }

    @Nullable
    public ChatFormatting getChatcolor() {
        return chatcolor;
    }

    public boolean isPrimal() {
        return components == null;
    }

    /** Capitalized latin name ("Ignis"). */
    public String getName() {
        return Character.toUpperCase(tag.charAt(0)) + tag.substring(1);
    }

    /** Latin name colored with the aspect's color. */
    public MutableComponent getDisplayName() {
        return Component.literal(getName()).withStyle(s -> s.withColor(TextColor.fromRgb(color)));
    }

    /** Translated description, key tc.aspect.&lt;tag&gt;. */
    public MutableComponent getLocalizedDescription() {
        return Component.translatable("tc.aspect." + tag);
    }

    @Nullable
    public static Aspect getAspect(String tag) {
        return aspects.get(tag);
    }

    /** The compound formed by a and b, or null if there is none. */
    @Nullable
    public static Aspect getCombinationResult(Aspect a, Aspect b) {
        return mixList.get(mixKey(a, b));
    }

    public static List<Aspect> getPrimalAspects() {
        List<Aspect> out = new ArrayList<>();
        for (Aspect a : aspects.values()) if (a.isPrimal()) out.add(a);
        return Collections.unmodifiableList(out);
    }

    public static List<Aspect> getCompoundAspects() {
        List<Aspect> out = new ArrayList<>();
        for (Aspect a : aspects.values()) if (!a.isPrimal()) out.add(a);
        return Collections.unmodifiableList(out);
    }

    @Override
    public String toString() {
        return tag;
    }

    // PRIMAL
    public static final Aspect AIR = new Aspect("aer", 0xffff7e, ChatFormatting.YELLOW, 1);
    public static final Aspect EARTH = new Aspect("terra", 0x56c000, ChatFormatting.DARK_GREEN, 1);
    public static final Aspect FIRE = new Aspect("ignis", 0xff5a01, ChatFormatting.RED, 1);
    public static final Aspect WATER = new Aspect("aqua", 0x3cd4fc, ChatFormatting.DARK_AQUA, 1);
    public static final Aspect ORDER = new Aspect("ordo", 0xd5d4ec, ChatFormatting.GRAY, 1);
    public static final Aspect ENTROPY = new Aspect("perditio", 0x404040, ChatFormatting.DARK_GRAY, 771);

    // SECONDARY (PRIMAL + PRIMAL)
    public static final Aspect VOID = new Aspect("vacuos", 0x888888, new Aspect[]{AIR, ENTROPY}, 771);
    public static final Aspect LIGHT = new Aspect("lux", 0xffffc0, new Aspect[]{AIR, FIRE});
    public static final Aspect MOTION = new Aspect("motus", 0xcdccf4, new Aspect[]{AIR, ORDER});
    public static final Aspect COLD = new Aspect("gelum", 0xe1ffff, new Aspect[]{FIRE, ENTROPY});
    public static final Aspect CRYSTAL = new Aspect("vitreus", 0x80ffff, new Aspect[]{EARTH, AIR});
    public static final Aspect METAL = new Aspect("metallum", 0xb5b5cd, new Aspect[]{EARTH, ORDER});
    public static final Aspect LIFE = new Aspect("victus", 0xde0005, new Aspect[]{EARTH, WATER});
    public static final Aspect DEATH = new Aspect("mortuus", 0x6a0005, new Aspect[]{WATER, ENTROPY});
    public static final Aspect ENERGY = new Aspect("potentia", 0xc0ffff, new Aspect[]{ORDER, FIRE});
    public static final Aspect EXCHANGE = new Aspect("permutatio", 0x578357, new Aspect[]{ENTROPY, ORDER});

    // TERTIARY
    public static final Aspect MAGIC = new Aspect("praecantatio", 0xcf00ff, new Aspect[]{ENERGY, AIR});
    public static final Aspect AURA = new Aspect("auram", 0xffc0ff, new Aspect[]{MAGIC, AIR});
    public static final Aspect ALCHEMY = new Aspect("alkimia", 0x23ac9d, new Aspect[]{MAGIC, WATER});
    public static final Aspect FLUX = new Aspect("vitium", 0x800080, new Aspect[]{ENTROPY, MAGIC});
    public static final Aspect DARKNESS = new Aspect("tenebrae", 0x222222, new Aspect[]{VOID, LIGHT});
    public static final Aspect ELDRITCH = new Aspect("alienis", 0x805080, new Aspect[]{VOID, DARKNESS});
    public static final Aspect FLIGHT = new Aspect("volatus", 0xe7e7d7, new Aspect[]{AIR, MOTION});
    public static final Aspect PLANT = new Aspect("herba", 0x01ac00, new Aspect[]{LIFE, EARTH});
    public static final Aspect TOOL = new Aspect("instrumentum", 0x4040ee, new Aspect[]{METAL, ENERGY});
    public static final Aspect CRAFT = new Aspect("fabrico", 0x809d80, new Aspect[]{EXCHANGE, TOOL});
    public static final Aspect MECHANISM = new Aspect("machina", 0x8080a0, new Aspect[]{MOTION, TOOL});
    public static final Aspect TRAP = new Aspect("vinculum", 0x9a8080, new Aspect[]{MOTION, ENTROPY});
    public static final Aspect SOUL = new Aspect("spiritus", 0xebebfb, new Aspect[]{LIFE, DEATH});
    public static final Aspect MIND = new Aspect("cognitio", 0xf9967f, new Aspect[]{FIRE, SOUL});
    public static final Aspect SENSES = new Aspect("sensus", 0xc0ffc0, new Aspect[]{AIR, SOUL});
    public static final Aspect AVERSION = new Aspect("aversio", 0xc05050, new Aspect[]{SOUL, ENTROPY});
    public static final Aspect PROTECT = new Aspect("praemunio", 0x00c0c0, new Aspect[]{SOUL, EARTH});
    public static final Aspect DESIRE = new Aspect("desiderium", 0xe6be44, new Aspect[]{SOUL, VOID});
    public static final Aspect UNDEAD = new Aspect("exanimis", 0x3a4000, new Aspect[]{MOTION, DEATH});
    public static final Aspect BEAST = new Aspect("bestia", 0x9f6409, new Aspect[]{MOTION, LIFE});
    public static final Aspect MAN = new Aspect("humanus", 0xffd7c0, new Aspect[]{SOUL, LIFE});
}
