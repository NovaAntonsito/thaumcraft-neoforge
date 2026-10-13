package thaumcraft.common.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.common.lib.crafting.InfusionEnchantmentRecipe;
import thaumcraft.common.lib.crafting.RecipeMagicDust;
import thaumcraft.common.lib.crafting.ShapedArcaneVoidJar;

/** Recipe types and serializers. In 1.12 these lived in ThaumcraftApi's recipe maps. */
public final class TCRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Thaumcraft.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Thaumcraft.MODID);

    public static final Supplier<RecipeType<CrucibleRecipe>> CRUCIBLE_TYPE = RECIPE_TYPES.register("crucible",
            () -> RecipeType.simple(Thaumcraft.id("crucible")));
    public static final Supplier<RecipeSerializer<CrucibleRecipe>> CRUCIBLE_SERIALIZER = RECIPE_SERIALIZERS.register("crucible",
            CrucibleRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<RecipeMagicDust>> SALIS_MUNDUS = RECIPE_SERIALIZERS.register("salis_mundus",
            () -> new SimpleCraftingRecipeSerializer<>(RecipeMagicDust::new));

    /** Arcane workbench recipes. In 1.12 they were IArcaneRecipe entries of the normal crafting registry. */
    public static final Supplier<RecipeType<IArcaneRecipe>> ARCANE_TYPE = RECIPE_TYPES.register("arcane_crafting",
            () -> RecipeType.simple(Thaumcraft.id("arcane_crafting")));
    public static final Supplier<RecipeSerializer<ShapedArcaneRecipe>> ARCANE_SHAPED = RECIPE_SERIALIZERS.register("arcane_shaped",
            () -> new ShapedArcaneRecipe.Serializer<>(ShapedArcaneRecipe::new));
    public static final Supplier<RecipeSerializer<ShapelessArcaneRecipe>> ARCANE_SHAPELESS = RECIPE_SERIALIZERS.register("arcane_shapeless",
            ShapelessArcaneRecipe.Serializer::new);
    /** ShapedArcaneVoidJar: the void jar keeps the essentia of the warded jar it is made from. */
    public static final Supplier<RecipeSerializer<ShapedArcaneVoidJar>> ARCANE_VOID_JAR = RECIPE_SERIALIZERS.register("arcane_void_jar",
            () -> new ShapedArcaneRecipe.Serializer<>(ShapedArcaneVoidJar::new));

    /** Infusion altar recipes. InfusionEnchantmentRecipe shares the type with its own serializer. */
    public static final Supplier<RecipeType<InfusionRecipe>> INFUSION_TYPE = RECIPE_TYPES.register("infusion",
            () -> RecipeType.simple(Thaumcraft.id("infusion")));
    public static final Supplier<RecipeSerializer<InfusionRecipe>> INFUSION_SERIALIZER = RECIPE_SERIALIZERS.register("infusion",
            InfusionRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<InfusionEnchantmentRecipe>> INFUSION_ENCHANTMENT_SERIALIZER =
            RECIPE_SERIALIZERS.register("infusion_enchantment", InfusionEnchantmentRecipe.Serializer::new);

    private TCRecipes() {
    }
}
