package thaumcraft.api.crafting;

import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.registry.TCRecipes;

/**
 * Port of ShapedArcaneRecipe (a ShapedOreRecipe with research, vis and crystals). JSON:
 * <pre>
 * { "type": "thaumcraft:arcane_shaped", "research": "BASEARTIFICE", "vis": 10, "crystals": { "ignis": 1 },
 *   "pattern": [...], "key": {...}, "result": {...} }
 * </pre>
 */
public class ShapedArcaneRecipe implements IArcaneRecipe {
    final String group;
    final String research;
    final int vis;
    @Nullable final AspectList crystals;
    final ShapedRecipePattern pattern;
    final ItemStack result;
    private final ShapedRecipe crafting;

    public ShapedArcaneRecipe(String group, String research, int vis, @Nullable AspectList crystals, ShapedRecipePattern pattern, ItemStack result) {
        this.group = group;
        this.research = research;
        this.vis = vis;
        this.crystals = crystals;
        this.pattern = pattern;
        this.result = result;
        this.crafting = new ShapedRecipe(group, CraftingBookCategory.MISC, pattern, result);
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
        return TCRecipes.ARCANE_SHAPED.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TCRecipes.ARCANE_TYPE.get();
    }

    /** Builds a recipe from its JSON fields; lets subclasses such as ShapedArcaneVoidJar reuse the serializer. */
    @FunctionalInterface
    public interface Factory<T extends ShapedArcaneRecipe> {
        T create(String group, String research, int vis, @Nullable AspectList crystals, ShapedRecipePattern pattern, ItemStack result);
    }

    public static class Serializer<T extends ShapedArcaneRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(Factory<T> factory) {
            codec = RecordCodecBuilder.mapCodec(i -> i.group(
                    com.mojang.serialization.Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                    com.mojang.serialization.Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
                    com.mojang.serialization.Codec.INT.optionalFieldOf("vis", 0).forGetter(r -> r.vis),
                    AspectList.CODEC.optionalFieldOf("crystals").forGetter(r -> Optional.ofNullable(r.crystals)),
                    ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result)
            ).apply(i, (group, research, vis, crystals, pattern, result) ->
                    factory.create(group, research, vis, crystals.orElse(null), pattern, result)));
            streamCodec = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, r -> r.group,
                    ByteBufCodecs.STRING_UTF8, r -> r.research,
                    ByteBufCodecs.VAR_INT, r -> r.vis,
                    ByteBufCodecs.optional(AspectList.STREAM_CODEC), r -> Optional.ofNullable(r.crystals),
                    ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
                    ItemStack.STREAM_CODEC, r -> r.result,
                    (group, research, vis, crystals, pattern, result) ->
                            factory.create(group, research, vis, crystals.orElse(null), pattern, result));
        }

        @Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
    }
}
