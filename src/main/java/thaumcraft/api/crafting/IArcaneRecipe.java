package thaumcraft.api.crafting;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigItems;

/**
 * An arcane workbench recipe: a normal crafting recipe plus a vis cost (drained from the aura) and crystals.
 * In 1.12 these were IRecipes in the crafting registry; here they have their own recipe type.
 */
public interface IArcaneRecipe extends Recipe<ArcaneCraftingInput> {
    int getVis();

    String getResearch();

    @Nullable
    AspectList getCrystals();

    /** The plain crafting recipe behind it (pattern, result, remaining items). */
    CraftingRecipe getCraftingRecipe();

    @Override
    default boolean matches(ArcaneCraftingInput input, Level level) {
        return hasCrystals(input, getCrystals()) && getCraftingRecipe().matches(input.grid().input(), level);
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    default NonNullList<net.minecraft.world.item.crafting.Ingredient> getIngredients() {
        return getCraftingRecipe().getIngredients();
    }

    @Override
    default boolean isSpecial() {
        return true; // no recipe book
    }

    /** Remaining items (buckets, ...) laid out on the 9 grid slots. */
    default NonNullList<ItemStack> getRemainingGridItems(ArcaneCraftingInput input) {
        CraftingInput.Positioned positioned = input.grid();
        CraftingInput grid = positioned.input();
        NonNullList<ItemStack> remaining = getCraftingRecipe().getRemainingItems(grid);
        NonNullList<ItemStack> out = NonNullList.withSize(ArcaneCraftingInput.GRID, ItemStack.EMPTY);
        for (int y = 0; y < grid.height(); y++) {
            for (int x = 0; x < grid.width(); x++) {
                out.set(x + positioned.left() + (y + positioned.top()) * 3, remaining.get(x + y * grid.width()));
            }
        }
        return out;
    }

    /** Each crystal of the recipe must be in one of the crystal slots, in the right amount. */
    static boolean hasCrystals(ArcaneCraftingInput input, @Nullable AspectList crystals) {
        if (crystals == null) return true;
        for (Aspect aspect : crystals.getAspects()) {
            ItemStack cs = ConfigItems.crystalEssence.get().makeCrystal(aspect, crystals.getAmount(aspect));
            boolean b = false;
            for (int i = 0; i < 6; ++i) {
                ItemStack itemstack1 = input.getItem(ArcaneCraftingInput.GRID + i);
                if (itemstack1.getCount() >= cs.getCount() && ItemStack.isSameItemSameComponents(cs, itemstack1)) {
                    b = true;
                }
            }
            if (!b) return false;
        }
        return true;
    }
}
