package thaumcraft.common.lib.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.consumables.ItemChunksEdible;
import thaumcraft.common.registry.TCRecipes;

/** Port of RecipeTripleMeatTreat: sugar and three different meat nuggets make a triple meat treat. */
public class RecipeTripleMeatTreat extends CustomRecipe {
    public RecipeTripleMeatTreat(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        boolean sugar = false;
        List<Item> meats = new ArrayList<>();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.SUGAR)) {
                if (sugar) return false;
                sugar = true;
            } else {
                if (!(stack.getItem() instanceof ItemChunksEdible)) return false;
                if (meats.contains(stack.getItem()) || meats.size() >= 3) return false;
                meats.add(stack.getItem());
            }
        }
        return sugar && meats.size() == 3;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        return new ItemStack(ConfigItems.tripleMeatTreat.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    /** For the thaumonomicon page (the original's fake shapeless recipe): three meat nuggets and sugar. */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ConfigItems.tripleMeatTreat.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        Ingredient meat = Ingredient.of(ConfigItems.chunks.values().stream().map(d -> new ItemStack(d.get())));
        return NonNullList.of(Ingredient.EMPTY, meat, meat, meat, Ingredient.of(Items.SUGAR));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TCRecipes.TRIPLE_MEAT_TREAT.get();
    }
}
