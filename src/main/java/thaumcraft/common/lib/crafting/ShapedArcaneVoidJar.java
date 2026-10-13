package thaumcraft.common.lib.crafting;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ArcaneCraftingInput;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.registry.TCRecipes;

/** Port of ShapedArcaneVoidJar: the result keeps the data (essentia, label) of the warded jar in the grid. */
public class ShapedArcaneVoidJar extends ShapedArcaneRecipe {
    public ShapedArcaneVoidJar(String group, String research, int vis, @Nullable AspectList crystals, ShapedRecipePattern pattern, ItemStack result) {
        super(group, research, vis, crystals, pattern, result);
    }

    @Override
    public ItemStack assemble(ArcaneCraftingInput input, HolderLookup.Provider registries) {
        ItemStack jar = ItemStack.EMPTY;
        for (int a = 0; a < ArcaneCraftingInput.GRID; ++a) {
            if (input.getItem(a).is(ConfigBlocks.jarNormal.get().asItem())) {
                jar = input.getItem(a);
                break;
            }
        }
        ItemStack res = super.assemble(input, registries);
        if (!jar.isEmpty()) {
            res.applyComponents(jar.getComponentsPatch());
        }
        return res;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TCRecipes.ARCANE_VOID_JAR.get();
    }
}
