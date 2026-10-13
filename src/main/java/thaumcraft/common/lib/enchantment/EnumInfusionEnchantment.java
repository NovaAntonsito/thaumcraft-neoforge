package thaumcraft.common.lib.enchantment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Port of EnumInfusionEnchantment. The 1.12 "infench" NBT list of {id, lvl} is now the
 * thaumcraft:infusion_enchantments data component, a map from the lower case name to the level.
 */
public enum EnumInfusionEnchantment implements StringRepresentable {
    COLLECTOR(Set.of("axe", "pickaxe", "shovel", "weapon"), 1, "INFUSIONENCHANTMENT"),
    DESTRUCTIVE(Set.of("axe", "pickaxe", "shovel"), 1, "INFUSIONENCHANTMENT"),
    BURROWING(Set.of("axe", "pickaxe"), 1, "INFUSIONENCHANTMENT"),
    SOUNDING(Set.of("pickaxe"), 4, "INFUSIONENCHANTMENT"),
    REFINING(Set.of("pickaxe"), 4, "INFUSIONENCHANTMENT"),
    ARCING(Set.of("weapon"), 4, "INFUSIONENCHANTMENT"),
    ESSENCE(Set.of("weapon"), 5, "INFUSIONENCHANTMENT"),
    VISBATTERY(Set.of("chargable"), 3, "?"),
    VISCHARGE(Set.of("chargable"), 1, "?"),
    SWIFT(Set.of("boots"), 4, "IEARMOR"),
    AGILE(Set.of("legs"), 1, "IEARMOR"),
    INFESTED(Set.of("chest"), 1, "IETAINT"),
    LAMPLIGHT(Set.of("axe", "pickaxe", "shovel"), 1, "INFUSIONENCHANTMENT");

    public static final Codec<EnumInfusionEnchantment> CODEC = StringRepresentable.fromEnum(EnumInfusionEnchantment::values);
    public static final StreamCodec<ByteBuf, EnumInfusionEnchantment> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(i -> values()[i], Enum::ordinal);
    /** The component value: enchantment to level, in the order they were added. */
    public static final Codec<Map<EnumInfusionEnchantment, Integer>> MAP_CODEC = Codec.unboundedMap(CODEC, Codec.INT);
    public static final StreamCodec<ByteBuf, Map<EnumInfusionEnchantment, Integer>> MAP_STREAM_CODEC =
            ByteBufCodecs.map(LinkedHashMap::new, STREAM_CODEC, ByteBufCodecs.VAR_INT);

    public final Set<String> toolClasses;
    public final int maxLevel;
    public final String research;

    EnumInfusionEnchantment(Set<String> toolClasses, int ml, String research) {
        this.toolClasses = toolClasses;
        this.maxLevel = ml;
        this.research = research;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    public static Map<EnumInfusionEnchantment, Integer> getInfusionEnchantmentMap(ItemStack stack) {
        return stack.isEmpty() ? Map.of() : stack.getOrDefault(TCDataComponents.INFUSION_ENCHANTMENTS.get(), Map.of());
    }

    public static List<EnumInfusionEnchantment> getInfusionEnchantments(ItemStack stack) {
        return new ArrayList<>(getInfusionEnchantmentMap(stack).keySet());
    }

    public static int getInfusionEnchantmentLevel(ItemStack stack, EnumInfusionEnchantment enchantment) {
        return getInfusionEnchantmentMap(stack).getOrDefault(enchantment, 0);
    }

    /** Sets the level, never lowering it nor going over maxLevel. */
    public static void addInfusionEnchantment(ItemStack stack, EnumInfusionEnchantment ie, int level) {
        if (stack.isEmpty() || level > ie.maxLevel) {
            return;
        }
        Map<EnumInfusionEnchantment, Integer> map = new LinkedHashMap<>(getInfusionEnchantmentMap(stack));
        if (map.getOrDefault(ie, 0) >= level) {
            return;
        }
        map.put(ie, level);
        stack.set(TCDataComponents.INFUSION_ENCHANTMENTS.get(), map);
    }
}
