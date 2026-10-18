package thaumcraft.common.lib.crafting;

import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.registry.TCRecipes;

/**
 * Port of InfusionEnchantmentRecipe: adds a rank of an infusion enchantment to the central tool or weapon.
 * <pre>
 * {"type": "thaumcraft:infusion_enchantment", "enchantment": "collector",
 *  "aspects": {"desiderium": 80, "aqua": 100},
 *  "components": [{"item": "minecraft:enchanted_book"}, {"item": "minecraft:lead"}]}
 * </pre>
 * Each rank and each other infusion enchantment on the item raise the essentia cost.
 */
public class InfusionEnchantmentRecipe extends InfusionRecipe {
    final EnumInfusionEnchantment enchantment;

    public InfusionEnchantmentRecipe(String group, EnumInfusionEnchantment ench, AspectList as, List<Ingredient> components) {
        super(group, ench.research, 4, as, Ingredient.EMPTY, components, ItemStack.EMPTY);
        enchantment = ench;
    }

    /** A copy with a fixed central item, for the thaumonomicon's display recipes. */
    public InfusionEnchantmentRecipe(InfusionEnchantmentRecipe recipe, ItemStack in) {
        super(recipe.group, recipe.enchantment.research, recipe.instability, recipe.aspects, Ingredient.of(in), recipe.components, ItemStack.EMPTY);
        enchantment = recipe.enchantment;
    }

    @Override
    public boolean matches(List<ItemStack> input, ItemStack central, Level world, @Nullable Player player) {
        if (central == null || central.isEmpty() || player == null || !ThaumcraftCapabilities.knowsResearch(player, research)) {
            return false;
        }
        if (EnumInfusionEnchantment.getInfusionEnchantmentLevel(central, enchantment) >= enchantment.maxLevel) {
            return false;
        }
        if (!enchantment.toolClasses.contains("all") && !fitsToolClass(central)) {
            return false;
        }
        return (getRecipeInput().isEmpty() || getRecipeInput().test(central))
                && RecipeMatcher.findMatches(new java.util.ArrayList<>(input), getComponents()) != null;
    }

    /**
     * The 1.12 checks: "weapon" means the item adds attack damage in the main hand, "axe"/"pickaxe"/"shovel"
     * are tool classes (item abilities now), armor matches its slot, baubles their Curios slot ("amulet", "belt",
     * "ring", or any for "bauble") and "chargable" takes IRechargable items.
     */
    private boolean fitsToolClass(ItemStack central) {
        if (enchantment.toolClasses.contains("weapon")) {
            boolean[] damage = {false};
            central.forEachModifier(EquipmentSlotGroup.MAINHAND, (attr, mod) -> {
                if (attr.is(Attributes.ATTACK_DAMAGE)) damage[0] = true;
            });
            if (damage[0]) return true;
        }
        if (enchantment.toolClasses.contains("axe") && central.canPerformAction(ItemAbilities.AXE_DIG)) return true;
        if (enchantment.toolClasses.contains("pickaxe") && central.canPerformAction(ItemAbilities.PICKAXE_DIG)) return true;
        if (enchantment.toolClasses.contains("shovel") && central.canPerformAction(ItemAbilities.SHOVEL_DIG)) return true;
        if (central.getItem() instanceof ArmorItem armor) {
            String at = switch (armor.getType()) {
                case HELMET -> "helm";
                case CHESTPLATE -> "chest";
                case LEGGINGS -> "legs";
                case BOOTS -> "boots";
                default -> "none";
            };
            if (enchantment.toolClasses.contains("armor") || enchantment.toolClasses.contains(at)) return true;
        }
        if (thaumcraft.common.lib.utils.BaublesHelper.isBauble(central)) {
            String at = thaumcraft.common.lib.utils.BaublesHelper.getBaubleType(central);
            if (enchantment.toolClasses.contains("bauble") || enchantment.toolClasses.contains(at)) return true;
        }
        return central.getItem() instanceof thaumcraft.api.items.IRechargable && enchantment.toolClasses.contains("chargable");
    }

    /** The central item with one more rank. The more infusion enchantments it already has, the likelier it warps. */
    @Override
    public ItemStack getRecipeOutput(@Nullable Player player, ItemStack input, List<ItemStack> comps) {
        if (input == null || input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack out = input.copy();
        int cl = EnumInfusionEnchantment.getInfusionEnchantmentLevel(out, enchantment);
        if (cl >= enchantment.maxLevel) {
            return ItemStack.EMPTY;
        }
        List<EnumInfusionEnchantment> el = EnumInfusionEnchantment.getInfusionEnchantments(input);
        RandomSource rand = RandomSource.create();
        if (rand.nextInt(10) < el.size()) {
            out.set(TCDataComponents.WARP.get(), 1 + input.getOrDefault(TCDataComponents.WARP.get(), 0));
        }
        EnumInfusionEnchantment.addInfusionEnchantment(out, enchantment, cl + 1);
        return out;
    }

    @Override
    public AspectList getAspects(@Nullable Player player, ItemStack input, List<ItemStack> comps) {
        AspectList out = new AspectList();
        if (input == null || input.isEmpty()) {
            return out;
        }
        int cl = EnumInfusionEnchantment.getInfusionEnchantmentLevel(input, enchantment) + 1;
        if (cl > enchantment.maxLevel) {
            return out;
        }
        List<EnumInfusionEnchantment> el = EnumInfusionEnchantment.getInfusionEnchantments(input);
        int otherEnchantments = el.size();
        if (el.contains(enchantment)) {
            --otherEnchantments;
        }
        float modifier = cl + otherEnchantments * 0.33f;
        for (Aspect a : getAspects().getAspects()) {
            out.add(a, (int) (getAspects().getAmount(a) * modifier));
        }
        return out;
    }

    public EnumInfusionEnchantment getEnchantment() {
        return enchantment;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TCRecipes.INFUSION_ENCHANTMENT_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<InfusionEnchantmentRecipe> {
        public static final MapCodec<InfusionEnchantmentRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                EnumInfusionEnchantment.CODEC.fieldOf("enchantment").forGetter(r -> r.enchantment),
                AspectList.CODEC.fieldOf("aspects").forGetter(r -> r.aspects),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("components").forGetter(r -> r.components)
        ).apply(i, InfusionEnchantmentRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, InfusionEnchantmentRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                EnumInfusionEnchantment.STREAM_CODEC.cast(), r -> r.enchantment,
                AspectList.STREAM_CODEC, r -> r.aspects,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.components,
                InfusionEnchantmentRecipe::new);

        @Override
        public MapCodec<InfusionEnchantmentRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, InfusionEnchantmentRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
