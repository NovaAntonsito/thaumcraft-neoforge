package thaumcraft.common.lib.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.registry.TCRecipes;

/** Port of RecipeMagicDust: bowl + flint + redstone + 3 vis crystals of different aspects = salis mundus. */
public class RecipeMagicDust extends CustomRecipe {
    public RecipeMagicDust(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        boolean bowl = false;
        boolean flint = false;
        boolean redstone = false;
        List<String> crystals = new ArrayList<>();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.BOWL)) {
                if (bowl) return false;
                bowl = true;
            } else if (stack.is(Items.FLINT)) {
                if (flint) return false;
                flint = true;
            } else if (stack.is(Items.REDSTONE)) {
                if (redstone) return false;
                redstone = true;
            } else {
                if (!stack.is(ConfigItems.crystalEssence.get())) return false;
                AspectList aspects = ConfigItems.crystalEssence.get().getAspects(stack);
                if (aspects == null) return false;
                String tag = aspects.getAspects()[0].getTag();
                if (crystals.contains(tag) || crystals.size() >= 3) return false;
                crystals.add(tag);
            }
        }
        return bowl && redstone && flint && crystals.size() == 3;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        return new ItemStack(ConfigItems.salisMundus.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 6;
    }

    /** The flint and the bowl are kept. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for (int i = 0; i < ret.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(Items.FLINT) || stack.is(Items.BOWL)) {
                ret.set(i, stack.copyWithCount(1));
            } else if (stack.hasCraftingRemainingItem()) {
                ret.set(i, stack.getCraftingRemainingItem());
            }
        }
        return ret;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TCRecipes.SALIS_MUNDUS.get();
    }
}
