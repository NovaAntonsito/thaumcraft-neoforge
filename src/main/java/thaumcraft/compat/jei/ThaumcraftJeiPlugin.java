package thaumcraft.compat.jei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.aspects.ObjectAspects;
import thaumcraft.common.config.ConfigItems;

/**
 * JEI (and EMI, which loads JEI plugins): aspects are ingredients, so they show in the list and can be searched by
 * name. Showing the recipes of an aspect lists every item that has it ("What has Ignis?"); showing its uses lists
 * the compounds it makes. Showing the uses of an item lists its aspects. Not in Thaumcraft.
 */
@JeiPlugin
public class ThaumcraftJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = Thaumcraft.id("jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(AspectIngredient.TYPE, List.copyOf(Aspect.aspects.values()), new AspectIngredient.Helper(),
                new AspectIngredient.Renderer());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AspectSourcesCategory(gui), new AspectCombinationCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(AspectSourcesCategory.TYPE, sources());
        List<AspectCombinationCategory.Combination> combos = new ArrayList<>();
        for (Aspect a : Aspect.getCompoundAspects()) {
            Aspect[] parts = a.getComponents();
            if (parts != null && parts.length == 2) {
                combos.add(new AspectCombinationCategory.Combination(parts[0], parts[1], a));
            }
        }
        registration.addRecipes(AspectCombinationCategory.TYPE, combos);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ConfigItems.thaumometer.get()), AspectSourcesCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ConfigItems.thaumonomicon.get()), AspectCombinationCategory.TYPE);
    }

    /** Every item's aspects, grouped by aspect and sorted by how much each item holds; split into pages. */
    private static List<AspectSourcesCategory.Page> sources() {
        Map<Aspect, List<Map.Entry<ItemStack, Integer>>> byAspect = new LinkedHashMap<>();
        for (Aspect a : Aspect.aspects.values()) {
            byAspect.put(a, new ArrayList<>());
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            ItemStack stack = new ItemStack(item);
            AspectList tags;
            try {
                tags = ObjectAspects.get(stack);
            } catch (RuntimeException e) {
                continue;
            }
            if (tags == null) continue;
            for (Aspect a : tags.getAspects()) {
                if (a != null && byAspect.containsKey(a)) {
                    byAspect.get(a).add(Map.entry(stack, tags.getAmount(a)));
                }
            }
        }
        int per = AspectSourcesCategory.COLUMNS * AspectSourcesCategory.ROWS;
        List<AspectSourcesCategory.Page> pages = new ArrayList<>();
        for (Map.Entry<Aspect, List<Map.Entry<ItemStack, Integer>>> e : byAspect.entrySet()) {
            List<Map.Entry<ItemStack, Integer>> list = e.getValue();
            if (list.isEmpty()) continue;
            list.sort(Comparator.comparingInt((Map.Entry<ItemStack, Integer> x) -> -x.getValue()));
            int count = (list.size() + per - 1) / per;
            for (int p = 0; p < count; p++) {
                List<Map.Entry<ItemStack, Integer>> part = list.subList(p * per, Math.min(list.size(), (p + 1) * per));
                pages.add(new AspectSourcesCategory.Page(e.getKey(), part.stream().map(Map.Entry::getKey).toList(),
                        part.stream().map(Map.Entry::getValue).toList(), p, count));
            }
        }
        return pages;
    }
}
