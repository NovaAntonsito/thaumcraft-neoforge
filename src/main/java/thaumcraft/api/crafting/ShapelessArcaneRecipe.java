package thaumcraft.api.crafting;

import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.registry.TCRecipes;

/**
 * Port of ShapelessArcaneRecipe (a ShapelessOreRecipe with research, vis and crystals). JSON:
 * <pre>
 * { "type": "thaumcraft:arcane_shapeless", "research": "...", "vis": 50, "crystals": {...},
 *   "ingredients": [...], "result": {...} }
 * </pre>
 */
public class ShapelessArcaneRecipe implements IArcaneRecipe {
    final String group;
    final String research;
    final int vis;
    @Nullable final AspectList crystals;
    final NonNullList<Ingredient> ingredients;
    final ItemStack result;
    private final ShapelessRecipe crafting;

    public ShapelessArcaneRecipe(String group, String research, int vis, @Nullable AspectList crystals, NonNullList<Ingredient> ingredients, ItemStack result) {
        this.group = group;
        this.research = research;
        this.vis = vis;
        this.crystals = crystals;
        this.ingredients = ingredients;
        this.result = result;
        this.crafting = new ShapelessRecipe(group, CraftingBookCategory.MISC, result, ingredients);
    }

    @Override
    public ItemStack assemble(ArcaneCraftingInput input, HolderLookup.Provider registries) {
        return crafting.assemble(input.grid().input(), registries);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public int getVis() {
        return vis;
    }

    @Override
    public String getResearch() {
        return research;
    }

    @Nullable
    @Override
    public AspectList getCrystals() {
        return crystals;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return crafting;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TCRecipes.ARCANE_SHAPELESS.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TCRecipes.ARCANE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ShapelessArcaneRecipe> {
        private static final Codec<NonNullList<Ingredient>> INGREDIENTS = Ingredient.CODEC_NONEMPTY.listOf().flatXmap(list -> {
            if (list.isEmpty()) return DataResult.error(() -> "No ingredients for shapeless arcane recipe");
            if (list.size() > 9) return DataResult.error(() -> "Too many ingredients for shapeless arcane recipe");
            return DataResult.success(NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new)));
        }, DataResult::success);

        public static final MapCodec<ShapelessArcaneRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
                Codec.INT.optionalFieldOf("vis", 0).forGetter(r -> r.vis),
                AspectList.CODEC.optionalFieldOf("crystals").forGetter(r -> Optional.ofNullable(r.crystals)),
                INGREDIENTS.fieldOf("ingredients").forGetter(r -> r.ingredients),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result)
        ).apply(i, (group, research, vis, crystals, ingredients, result) ->
                new ShapelessArcaneRecipe(group, research, vis, crystals.orElse(null), ingredients, result)));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessArcaneRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                ByteBufCodecs.STRING_UTF8, r -> r.research,
                ByteBufCodecs.VAR_INT, r -> r.vis,
                ByteBufCodecs.optional(AspectList.STREAM_CODEC), r -> Optional.ofNullable(r.crystals),
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list())
                        .map(list -> NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new)), list -> list),
                r -> r.ingredients,
                ItemStack.STREAM_CODEC, r -> r.result,
                (group, research, vis, crystals, ingredients, result) ->
                        new ShapelessArcaneRecipe(group, research, vis, crystals.orElse(null), ingredients, result));

        @Override
        public MapCodec<ShapelessArcaneRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapelessArcaneRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
