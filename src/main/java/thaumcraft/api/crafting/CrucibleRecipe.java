package thaumcraft.api.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.registry.TCRecipes;

/**
 * Port of thaumcraft.api.crafting.CrucibleRecipe, now a vanilla {@link Recipe} loaded from JSON:
 * <pre>
 * {"type": "thaumcraft:crucible", "research": "METALLURGY@1",
 *  "catalyst": {"tag": "c:ingots/iron"}, "aspects": {"instrumentum": 5},
 *  "result": {"id": "thaumcraft:ingot_brass"}}
 * </pre>
 */
public class CrucibleRecipe implements Recipe<SingleRecipeInput> {
    private final String group;
    private final String research;
    private final Ingredient catalyst;
    private final AspectList aspects;
    private final ItemStack recipeOutput;

    public CrucibleRecipe(String group, String research, Ingredient catalyst, AspectList aspects, ItemStack result) {
        this.group = group;
        this.research = research;
        this.catalyst = catalyst;
        this.aspects = aspects;
        this.recipeOutput = result;
    }

    /** The catalyst matches and the crucible holds at least the required amount of every aspect. */
    public boolean matches(AspectList itags, ItemStack cat) {
        if (!catalyst.test(cat)) return false;
        if (itags == null) return false;
        for (Aspect tag : aspects.getAspects()) {
            if (itags.getAmount(tag) < aspects.getAmount(tag)) return false;
        }
        return true;
    }

    public boolean catalystMatches(ItemStack cat) {
        return catalyst.test(cat);
    }

    /** A copy of {@code itags} minus this recipe's aspects. */
    public AspectList removeMatching(AspectList itags) {
        AspectList temptags = itags.copy();
        for (Aspect tag : aspects.getAspects()) {
            temptags.remove(tag, aspects.getAmount(tag));
        }
        return temptags;
    }

    public ItemStack getRecipeOutput() {
        return recipeOutput;
    }

    public String getResearch() {
        return research;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    public AspectList getAspects() {
        return aspects;
    }

    // Vanilla Recipe: only used for lookup. The aspect check needs the crucible, see matches(AspectList, ItemStack).

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return catalyst.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return recipeOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return recipeOutput;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TCRecipes.CRUCIBLE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TCRecipes.CRUCIBLE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<CrucibleRecipe> {
        public static final MapCodec<CrucibleRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
                Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(r -> r.catalyst),
                AspectList.CODEC.fieldOf("aspects").forGetter(r -> r.aspects),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.recipeOutput)
        ).apply(i, CrucibleRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                ByteBufCodecs.STRING_UTF8, r -> r.research,
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.catalyst,
                AspectList.STREAM_CODEC, r -> r.aspects,
                ItemStack.STREAM_CODEC, r -> r.recipeOutput,
                CrucibleRecipe::new);

        @Override
        public MapCodec<CrucibleRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
