package thaumcraft.common.registry;

import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.research.ResearchNoteData;

/**
 * Item data components. Replace the NBT tags items used in 1.12.2.
 * Component values must be treated as immutable: copy an AspectList before changing it and set it again.
 */
public final class TCDataComponents {
    private TCDataComponents() {
    }

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Thaumcraft.MODID);

    /** Aspects held by an essentia container item. Replaces the "Aspects" NBT list of ItemGenericEssentiaContainer. */
    public static final Supplier<DataComponentType<AspectList>> ASPECTS = DATA_COMPONENTS.registerComponentType("aspects",
            builder -> builder.persistent(AspectList.CODEC).networkSynchronized(AspectList.STREAM_CODEC.cast()));

    /** The aspect a jar is locked to by its label. Replaces the "AspectFilter" NBT string of BlockJarItem. */
    public static final Supplier<DataComponentType<Aspect>> ASPECT_FILTER = DATA_COMPONENTS.registerComponentType("aspect_filter",
            builder -> builder.persistent(Aspect.CODEC).networkSynchronized(Aspect.STREAM_CODEC.cast()));

    /** Infusion enchantments and their level. Replaces the "infench" NBT list. */
    public static final Supplier<DataComponentType<java.util.Map<EnumInfusionEnchantment, Integer>>> INFUSION_ENCHANTMENTS =
            DATA_COMPONENTS.registerComponentType("infusion_enchantments",
                    builder -> builder.persistent(EnumInfusionEnchantment.MAP_CODEC).networkSynchronized(EnumInfusionEnchantment.MAP_STREAM_CODEC.cast()));

    /** The 3x3 orientation of the elemental shovel (0 flat, 1 upright, 2 sideways). Replaces the "or" NBT byte. */
    public static final Supplier<DataComponentType<Byte>> SHOVEL_ORIENTATION = DATA_COMPONENTS.registerComponentType("shovel_orientation",
            builder -> builder.persistent(com.mojang.serialization.Codec.BYTE).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BYTE.cast()));

    /** Warp the item gives its holder. Replaces the "TC.WARP" NBT byte. */
    public static final Supplier<DataComponentType<Integer>> WARP = DATA_COMPONENTS.registerComponentType("warp",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));

    /** The grid of a research note. Replaces the note's NBT (key, color, hexgrid, aspects, ...). */
    public static final Supplier<DataComponentType<ResearchNoteData>> RESEARCH_NOTE =
            DATA_COMPONENTS.registerComponentType("research_note", builder -> builder
                    .persistent(ResearchNoteData.CODEC)
                    .networkSynchronized(ResearchNoteData.STREAM_CODEC.cast()));

    /**
     * Where a magic mirror (or hand mirror) is linked to. Replaces the "linkX/Y/Z/Dim" NBT ints; the linked mirror
     * item model (metadata 1 in 1.12) is the "linked" property.
     */
    public static final Supplier<DataComponentType<net.minecraft.core.GlobalPos>> MIRROR_LINK =
            DATA_COMPONENTS.registerComponentType("mirror_link", builder -> builder
                    .persistent(net.minecraft.core.GlobalPos.CODEC)
                    .networkSynchronized(net.minecraft.core.GlobalPos.STREAM_CODEC));

    /** Vis charge of an IRechargable item. Replaces the "tc.charge" NBT int. */
    public static final Supplier<DataComponentType<Integer>> CHARGE = DATA_COMPONENTS.registerComponentType("charge",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));

    /** The packed parts of a golem placer. Replaces the "props" NBT long of ItemGolemPlacer. */
    public static final Supplier<DataComponentType<Long>> GOLEM_PROPS = DATA_COMPONENTS.registerComponentType("golem_props",
            builder -> builder.persistent(com.mojang.serialization.Codec.LONG).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG.cast()));

    /** The rank experience of a golem placer. Replaces the "xp" NBT int of ItemGolemPlacer. */
    public static final Supplier<DataComponentType<Integer>> GOLEM_XP = DATA_COMPONENTS.registerComponentType("golem_xp",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));

    /** Experience stored in a brain in a jar. Replaces the "xp" NBT int of BlockJarBrainItem. */
    public static final Supplier<DataComponentType<Integer>> XP = DATA_COMPONENTS.registerComponentType("xp",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));

    /** The focus package (node tree) of a focus. Replaces the "package" NBT compound; same layout inside. */
    public static final Supplier<DataComponentType<net.minecraft.nbt.CompoundTag>> FOCUS_PACKAGE =
            DATA_COMPONENTS.registerComponentType("focus_package", builder -> builder
                    .persistent(net.minecraft.nbt.CompoundTag.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.COMPOUND_TAG));

    /** Cached focus color. Replaces the "color" NBT int; written together with the package. */
    public static final Supplier<DataComponentType<Integer>> FOCUS_COLOR = DATA_COMPONENTS.registerComponentType("focus_color",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT.cast()));

    /** Cached focus sorting helper. Replaces the "srt" NBT int; written together with the package. */
    public static final Supplier<DataComponentType<Integer>> FOCUS_SORT = DATA_COMPONENTS.registerComponentType("focus_sort",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT.cast()));

    /** The focus mounted on a caster. Replaces the "focus" NBT compound. */
    public static final Supplier<DataComponentType<net.minecraft.world.item.component.ItemContainerContents>> CASTER_FOCUS =
            DATA_COMPONENTS.registerComponentType("caster_focus", builder -> builder
                    .persistent(net.minecraft.world.item.component.ItemContainerContents.CODEC)
                    .networkSynchronized(net.minecraft.world.item.component.ItemContainerContents.STREAM_CODEC));

    /** The block picked with an exchange focus (sneak + right click). Replaces the caster's "picked" NBT compound. */
    public static final Supplier<DataComponentType<net.minecraft.world.item.component.ItemContainerContents>> CASTER_PICKED =
            DATA_COMPONENTS.registerComponentType("caster_picked", builder -> builder
                    .persistent(net.minecraft.world.item.component.ItemContainerContents.CODEC)
                    .networkSynchronized(net.minecraft.world.item.component.ItemContainerContents.STREAM_CODEC));

    /** Plan focus area on a caster. Replace the "areax", "areay", "areaz" and "aread" NBT ints. */
    public static final Supplier<DataComponentType<Integer>> AREA_X = DATA_COMPONENTS.registerComponentType("area_x",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
    public static final Supplier<DataComponentType<Integer>> AREA_Y = DATA_COMPONENTS.registerComponentType("area_y",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
    public static final Supplier<DataComponentType<Integer>> AREA_Z = DATA_COMPONENTS.registerComponentType("area_z",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
    public static final Supplier<DataComponentType<Integer>> AREA_DIM = DATA_COMPONENTS.registerComponentType("area_dim",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));

    // Stage 9e-2.
    /** ItemGrappleGun "loaded": a hook is out (the model shows the gun without it). */
    public static final Supplier<DataComponentType<Boolean>> GRAPPLE_LOADED = DATA_COMPONENTS.registerComponentType("grapple_loaded",
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL));
    /** ItemBootsTraveller "energy": seconds left before the boots use up another point of charge. */
    public static final Supplier<DataComponentType<Integer>> BOOTS_ENERGY = DATA_COMPONENTS.registerComponentType("boots_energy",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
    /** ItemVerdantCharm "type": 0 plain, 1 lifegiver, 2 sustainer. */
    public static final Supplier<DataComponentType<Integer>> VERDANT_TYPE = DATA_COMPONENTS.registerComponentType("verdant_type",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
    /** ItemFortressArmor "mask": 0 grinning devil, 1 angry ghost, 2 sipping fiend. */
    public static final Supplier<DataComponentType<Integer>> FORTRESS_MASK = DATA_COMPONENTS.registerComponentType("fortress_mask",
            builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
    /** ItemFortressArmor "goggles": the helm has goggles of revealing. */
    public static final Supplier<DataComponentType<Boolean>> FORTRESS_GOGGLES = DATA_COMPONENTS.registerComponentType("fortress_goggles",
            builder -> builder.persistent(com.mojang.serialization.Codec.BOOL).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL));
}
