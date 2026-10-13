package thaumcraft.api.crafting;

import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.registry.TCRecipes;

/**
 * Port of thaumcraft.api.crafting.InfusionRecipe, now a vanilla {@link Recipe} loaded from JSON:
 * <pre>
 * {"type": "thaumcraft:infusion", "research": "MIRROR", "instability": 1,
 *  "aspects": {"motus": 25, "tenebrae": 25, "permutatio": 25},
 *  "central": {"item": "thaumcraft:mirrored_glass"},
 *  "components": [{"tag": "c:ingots/gold"}, {"item": "minecraft:ender_pearl"}],
 *  "result": {"id": "thaumcraft:mirror"}}
 * </pre>
 * Without "central" any item fits the middle pedestal (the original Ingredient.EMPTY).
 * The 1.12 recipes could also output an NBT tag to set on the central item; those use their own recipe class.
 */
public class InfusionRecipe implements Recipe<InfusionRecipe.Input> {
    /** The central item and the items on the surrounding pedestals. */
    public record Input(ItemStack central, List<ItemStack> components) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? central : components.get(index - 1);
        }

        @Override
        public int size() {
            return 1 + components.size();
        }
    }

    protected final String group;
    public final String research;
    public final int instability;
    public final AspectList aspects;
    public final Ingredient sourceInput;
    protected final NonNullList<Ingredient> components;
    public final ItemStack recipeOutput;

    public InfusionRecipe(String group, String research, int instability, AspectList aspects, Ingredient sourceInput,
                          List<Ingredient> components, ItemStack recipeOutput) {
        this.group = group;
        this.research = research;
        this.instability = instability;
        this.aspects = aspects;
        this.sourceInput = sourceInput;
        this.components = NonNullList.copyOf(components);
        this.recipeOutput = recipeOutput;
    }

    /**
     * Used to check if a recipe matches the items on the pedestals. The player must know the research
     * (the original assumed a player; without one, as for automation, nothing matches).
     */
    public boolean matches(List<ItemStack> input, ItemStack central, Level world, @Nullable Player player) {
        if (player == null || !ThaumcraftCapabilities.getKnowledge(player).isResearchKnown(research)) {
            return false;
        }
        return (getRecipeInput().isEmpty() || getRecipeInput().test(central))
                && RecipeMatcher.findMatches(new ArrayList<>(input), getComponents()) != null;
    }

    public String getResearch() {
        return research;
    }

    /** Ingredient.EMPTY when the central item can be anything. */
    public Ingredient getRecipeInput() {
        return sourceInput;
    }

    public NonNullList<Ingredient> getComponents() {
        return components;
    }

    public ItemStack getRecipeOutput() {
        return recipeOutput;
    }

    public AspectList getAspects() {
        return aspects;
    }

    /** The item that replaces the central one. */
    public ItemStack getRecipeOutput(@Nullable Player player, ItemStack input, List<ItemStack> comps) {
        return recipeOutput.copy();
    }

    public AspectList getAspects(@Nullable Player player, ItemStack input, List<ItemStack> comps) {
        return aspects;
    }

    public int getInstability(@Nullable Player player, ItemStack input, List<ItemStack> comps) {
        return instability;
    }

    // Vanilla Recipe: only used for lookup and recipe sync. Infusion matches through the matrix, see matches above.

    @Override
    public boolean matches(Input input, Level level) {
        return matches(input.components(), input.central(), level, null);
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return getRecipeOutput(null, input.central(), input.components());
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
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> out = NonNullList.create();
        out.add(sourceInput);
        out.addAll(components);
        return out;
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
        return TCRecipes.INFUSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TCRecipes.INFUSION_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<InfusionRecipe> {
        public static final MapCodec<InfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
                Codec.INT.optionalFieldOf("instability", 0).forGetter(r -> r.instability),
                AspectList.CODEC.fieldOf("aspects").forGetter(r -> r.aspects),
                Ingredient.CODEC.optionalFieldOf("central", Ingredient.EMPTY).forGetter(r -> r.sourceInput),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("components").forGetter(r -> r.components),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.recipeOutput)
        ).apply(i, InfusionRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> COMPONENTS_STREAM_CODEC =
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());

        public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC = StreamCodec.of((buf, r) -> {
            ByteBufCodecs.STRING_UTF8.encode(buf, r.group);
            ByteBufCodecs.STRING_UTF8.encode(buf, r.research);
            ByteBufCodecs.VAR_INT.encode(buf, r.instability);
            AspectList.STREAM_CODEC.encode(buf, r.aspects);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.sourceInput);
            COMPONENTS_STREAM_CODEC.encode(buf, r.components);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, r.recipeOutput);
        }, buf -> new InfusionRecipe(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf), AspectList.STREAM_CODEC.decode(buf), Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                COMPONENTS_STREAM_CODEC.decode(buf), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)));

        @Override
        public MapCodec<InfusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
