package thaumcraft.common.lib.crafting;

import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.crafting.ArcaneCraftingInput;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.common.aspects.ObjectAspects;
import thaumcraft.common.config.ConfigAspects;
import thaumcraft.common.registry.TCRecipes;

/** Partial port of ThaumcraftCraftingManager. */
public final class ThaumcraftCraftingManager {
    private ThaumcraftCraftingManager() {
    }

    /**
     * The matching crucible recipe that uses the most aspects, or null.
     * As in the original, a recipe needs a player (the thrower) who knows its research.
     */
    @Nullable
    public static CrucibleRecipe findMatchingCrucibleRecipe(Level level, @Nullable Player player, AspectList aspects, ItemStack lastDrop) {
        if (player == null) return null;
        int highest = 0;
        CrucibleRecipe out = null;
        ItemStack temp = lastDrop.copyWithCount(1);
        for (RecipeHolder<CrucibleRecipe> holder : level.getRecipeManager().getAllRecipesFor(TCRecipes.CRUCIBLE_TYPE.get())) {
            CrucibleRecipe recipe = holder.value();
            if (!ThaumcraftCapabilities.knowsResearchStrict(player, recipe.getResearch()) || !recipe.matches(aspects, temp)) continue;
            int result = recipe.getAspects().visSize();
            if (result > highest) {
                highest = result;
                out = recipe;
            }
        }
        return out;
    }

    /**
     * The arcane recipe matching the workbench grid and crystals, or null. Works on both sides.
     * As in the original, the research is checked by the container, which falls back to normal crafting.
     */
    @Nullable
    public static RecipeHolder<IArcaneRecipe> findMatchingArcaneRecipe(Level level, @Nullable Player player, ArcaneCraftingInput input) {
        return level.getRecipeManager().getRecipeFor(TCRecipes.ARCANE_TYPE.get(), input, level).orElse(null);
    }

    /**
     * The first infusion recipe matching the central item and the pedestal items, or null.
     * The recipes check the player's research themselves.
     */
    @Nullable
    public static InfusionRecipe findMatchingInfusionRecipe(Level level, List<ItemStack> items, ItemStack input, @Nullable Player player) {
        for (RecipeHolder<InfusionRecipe> holder : level.getRecipeManager().getAllRecipesFor(TCRecipes.INFUSION_TYPE.get())) {
            if (holder.value().matches(items, input, level, player)) {
                return holder.value();
            }
        }
        return null;
    }

    // ---- Item aspects -------------------------------------------------------------------------------
    // Port of getObjectTags / generateTags / getBonusTags. The 1.12 "history" list held stack NBT strings;
    // without metadata an Item is enough.

    /** The aspects of a stack: its data entry, or generated from recipes, plus bonus aspects. */
    public static AspectList getObjectTags(ItemStack itemstack) {
        return getObjectTags(itemstack, new ArrayList<>());
    }

    public static AspectList getObjectTags(ItemStack itemstack, List<Item> history) {
        if (itemstack.isEmpty()) {
            return new AspectList();
        }
        Item item = itemstack.getItem();
        AspectList tmp = null;
        // Essentia containers report only what they hold (getBonusTags clears the rest), so skip the lookup.
        if (!(item instanceof IEssentiaContainerItem container) || container.ignoreContainedAspects()) {
            tmp = ConfigAspects.getPotionTags(itemstack);
            if (tmp == null) tmp = ObjectAspects.getCached(item);
            if (tmp == null) {
                ObjectAspects.Entry entry = ObjectAspects.findEntry(itemstack);
                if (entry != null) {
                    tmp = resolveEntry(itemstack, entry, history);
                } else {
                    tmp = generateTags(itemstack, history);
                }
            }
        }
        return capAspects(getBonusTags(itemstack, tmp), 500);
    }

    /** Applies a data entry: registerObjectTag / registerComplexObjectTag / new AspectList(new ItemStack(base)). */
    @Nullable
    private static AspectList resolveEntry(ItemStack stack, ObjectAspects.Entry entry, List<Item> history) {
        Item item = stack.getItem();
        boolean recursive = entry.complex() || entry.base().isPresent();
        if (recursive) {
            if (history.contains(item)) {
                return null;
            }
            history.add(item);
        }
        AspectList out = new AspectList();
        if (entry.base().isPresent()) {
            ResourceLocation baseId = ResourceLocation.tryParse(entry.base().get());
            if (baseId != null && BuiltInRegistries.ITEM.containsKey(baseId)) {
                out.add(getObjectTags(new ItemStack(BuiltInRegistries.ITEM.get(baseId)), history));
            }
        }
        out.add(entry.aspects());
        entry.merge().ifPresent(out::merge);
        if (entry.complex()) {
            AspectList generated = generateTagsFromRecipes(stack.copyWithCount(1), history);
            if (generated != null) out.add(generated);
        }
        ObjectAspects.cache(item, out);
        return out;
    }

    private static AspectList capAspects(@Nullable AspectList sourcetags, int amount) {
        AspectList out = new AspectList();
        if (sourcetags == null) {
            return out;
        }
        for (Aspect aspect : sourcetags.getAspects()) {
            out.merge(aspect, Math.min(amount, sourcetags.getAmount(aspect)));
        }
        return out;
    }

    private static AspectList getBonusTags(ItemStack itemstack, @Nullable AspectList sourcetags) {
        AspectList tmp = new AspectList();
        Item item = itemstack.getItem();
        if (item instanceof IEssentiaContainerItem container && !container.ignoreContainedAspects()) {
            sourcetags = null;
            AspectList held = container.getAspects(itemstack);
            if (held != null) {
                for (Aspect tag : held.getAspects()) {
                    if (held.getAmount(tag) > 0) tmp.add(tag, held.getAmount(tag));
                }
            }
        }
        if (sourcetags != null) {
            tmp.add(sourcetags);
        }
        // ItemHorseArmor was not ItemArmor in 1.12; its aspects come from the data entry only.
        if (item instanceof ArmorItem armor && !(item instanceof AnimalArmorItem)) {
            tmp.merge(Aspect.PROTECT, armor.getDefense() * 4);
        } else if (item instanceof SwordItem sword) {
            // 1.12 ItemSword.getAttackDamage() = 3 + material damage, the same base SwordItem uses for vanilla tiers.
            float damage = 3.0f + sword.getTier().getAttackDamageBonus();
            if (damage + 1.0f > 0.0f) tmp.merge(Aspect.AVERSION, (int) (damage + 1.0f) * 4);
        } else if (item instanceof BowItem) {
            tmp.merge(Aspect.AVERSION, 10).merge(Aspect.FLIGHT, 5);
        } else if (item instanceof DiggerItem tool && !(item instanceof HoeItem)) {
            tmp.merge(Aspect.TOOL, (harvestLevel(tool.getTier()) + 1) * 4);
        } else if (item instanceof ShearsItem || item instanceof HoeItem) {
            // Durability of the 1.12 tool materials: wood 59, stone 131, gold 32, iron 250.
            int max = itemstack.getMaxDamage();
            if (max <= 59) {
                tmp.merge(Aspect.TOOL, 4);
            } else if (max <= 131) {
                tmp.merge(Aspect.TOOL, 8);
            } else if (max <= 250) {
                tmp.merge(Aspect.TOOL, 12);
            } else {
                tmp.merge(Aspect.TOOL, 16);
            }
        }
        if (itemstack.is(Tags.Items.DYES)) {
            tmp.merge(Aspect.SENSES, 5);
        }
        addEnchantmentTags(EnchantmentHelper.getEnchantmentsForCrafting(itemstack), tmp);
        return AspectHelper.cullTags(tmp);
    }

    /** 1.12 ToolMaterial.getHarvestLevel(). 1.21 tiers keep it as the "incorrect blocks" tag. */
    private static int harvestLevel(Tier tier) {
        if (tier instanceof Tiers vanilla) {
            return switch (vanilla) {
                case WOOD, GOLD -> 0;
                case STONE -> 1;
                case IRON -> 2;
                case DIAMOND -> 3;
                case NETHERITE -> 4;
            };
        }
        var incorrect = tier.getIncorrectBlocksForDrops();
        if (incorrect == BlockTags.INCORRECT_FOR_STONE_TOOL) return 1;
        if (incorrect == BlockTags.INCORRECT_FOR_IRON_TOOL) return 2;
        if (incorrect == BlockTags.INCORRECT_FOR_DIAMOND_TOOL) return 3;
        if (incorrect == BlockTags.INCORRECT_FOR_NETHERITE_TOOL) return 4;
        return 0;
    }

    private static void addEnchantmentTags(ItemEnchantments enchantments, AspectList tmp) {
        if (enchantments.isEmpty()) return;
        int magic = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> e = entry.getKey();
            int lvl = entry.getIntValue() * 3;
            if (is(e, Enchantments.AQUA_AFFINITY)) {
                tmp.merge(Aspect.WATER, lvl);
            } else if (is(e, Enchantments.BANE_OF_ARTHROPODS)) {
                tmp.merge(Aspect.BEAST, lvl / 2).merge(Aspect.AVERSION, lvl / 2);
            } else if (is(e, Enchantments.BLAST_PROTECTION)) {
                tmp.merge(Aspect.PROTECT, lvl / 2).merge(Aspect.ENTROPY, lvl / 2);
            } else if (is(e, Enchantments.EFFICIENCY)) {
                tmp.merge(Aspect.TOOL, lvl);
            } else if (is(e, Enchantments.FEATHER_FALLING)) {
                tmp.merge(Aspect.FLIGHT, lvl);
            } else if (is(e, Enchantments.FIRE_ASPECT)) {
                tmp.merge(Aspect.FIRE, lvl / 2).merge(Aspect.AVERSION, lvl / 2);
            } else if (is(e, Enchantments.FIRE_PROTECTION)) {
                tmp.merge(Aspect.PROTECT, lvl / 2).merge(Aspect.FIRE, lvl / 2);
            } else if (is(e, Enchantments.FLAME)) {
                tmp.merge(Aspect.FIRE, lvl);
            } else if (is(e, Enchantments.FORTUNE)) {
                tmp.merge(Aspect.DESIRE, lvl);
            } else if (is(e, Enchantments.INFINITY)) {
                tmp.merge(Aspect.CRAFT, lvl);
            } else if (is(e, Enchantments.KNOCKBACK)) {
                tmp.merge(Aspect.AIR, lvl);
            } else if (is(e, Enchantments.LOOTING)) {
                tmp.merge(Aspect.DESIRE, lvl);
            } else if (is(e, Enchantments.POWER)) {
                tmp.merge(Aspect.AVERSION, lvl);
            } else if (is(e, Enchantments.PROJECTILE_PROTECTION)) {
                tmp.merge(Aspect.PROTECT, lvl);
            } else if (is(e, Enchantments.PROTECTION)) {
                tmp.merge(Aspect.PROTECT, lvl);
            } else if (is(e, Enchantments.PUNCH)) {
                tmp.merge(Aspect.AIR, lvl);
            } else if (is(e, Enchantments.RESPIRATION)) {
                tmp.merge(Aspect.AIR, lvl);
            } else if (is(e, Enchantments.SHARPNESS)) {
                tmp.merge(Aspect.AVERSION, lvl);
            } else if (is(e, Enchantments.SILK_TOUCH)) {
                tmp.merge(Aspect.EXCHANGE, lvl);
            } else if (is(e, Enchantments.THORNS)) {
                tmp.merge(Aspect.AVERSION, lvl);
            } else if (is(e, Enchantments.SMITE)) {
                tmp.merge(Aspect.UNDEAD, lvl / 2).merge(Aspect.AVERSION, lvl / 2);
            } else if (is(e, Enchantments.UNBREAKING)) {
                tmp.merge(Aspect.EARTH, lvl);
            } else if (is(e, Enchantments.DEPTH_STRIDER)) {
                tmp.merge(Aspect.WATER, lvl);
            } else if (is(e, Enchantments.LUCK_OF_THE_SEA)) {
                tmp.merge(Aspect.DESIRE, lvl);
            } else if (is(e, Enchantments.LURE)) {
                tmp.merge(Aspect.BEAST, lvl);
            } else if (is(e, Enchantments.FROST_WALKER)) {
                tmp.merge(Aspect.COLD, lvl);
            } else if (is(e, Enchantments.MENDING)) {
                tmp.merge(Aspect.CRAFT, lvl);
            }
            // 1.12 rarity is the 1.21 weight: common 10, uncommon 5, rare 2, very rare 1.
            int weight = e.value().definition().weight();
            if (weight < 10) {
                magic += weight >= 5 ? 2 : weight >= 2 ? 4 : 6;
            }
            magic += lvl;
        }
        if (magic > 0) {
            tmp.merge(Aspect.MAGIC, magic);
        }
    }

    private static boolean is(Holder<Enchantment> holder, ResourceKey<Enchantment> key) {
        return holder.is(key);
    }

    /** Aspects from recipes, cached. Null when nothing could be generated. */
    @Nullable
    public static AspectList generateTags(ItemStack is, List<Item> history) {
        Item item = is.getItem();
        AspectList known = ObjectAspects.getCached(item);
        if (known != null) {
            return known;
        }
        if (history.contains(item)) {
            return null;
        }
        history.add(item);
        if (history.size() < 100) {
            AspectList ret = generateTagsFromRecipes(is.copyWithCount(1), history);
            ret = capAspects(ret, 500);
            ObjectAspects.cache(item, ret);
            return ret;
        }
        return null;
    }

    @Nullable
    private static AspectList generateTagsFromRecipes(ItemStack stack, List<Item> history) {
        RecipeManager recipes = ObjectAspects.recipes();
        if (recipes == null) {
            return null;
        }
        try {
            AspectList ret = generateTagsFromCrucibleRecipes(recipes, stack, history);
            if (ret != null) {
                return ret;
            }
            ret = generateTagsFromInfusionRecipes(recipes, stack, history);
            if (ret != null) {
                return ret;
            }
            return generateTagsFromCraftingRecipes(recipes, stack, history);
        } catch (RuntimeException e) {
            Thaumcraft.LOGGER.debug("Could not generate aspects for {}", stack, e);
            return null;
        }
    }

    @Nullable
    private static AspectList generateTagsFromCrucibleRecipes(RecipeManager recipes, ItemStack stack, List<Item> history) {
        CrucibleRecipe cr = null;
        for (RecipeHolder<CrucibleRecipe> holder : recipes.getAllRecipesFor(TCRecipes.CRUCIBLE_TYPE.get())) {
            if (holder.value().getRecipeOutput().is(stack.getItem())) {
                cr = holder.value();
                break;
            }
        }
        if (cr == null) {
            return null;
        }
        AspectList ot = cr.getAspects();
        int ss = cr.getRecipeOutput().getCount();
        ItemStack[] cats = cr.getCatalyst().getItems();
        if (cats.length == 0 || cats[0].isEmpty()) {
            return null;
        }
        AspectList out = new AspectList();
        out.add(getObjectTags(cats[0], history));
        for (Aspect tt : ot.getAspects()) {
            out.add(tt, (int) (Math.sqrt(ot.getAmount(tt)) / ss));
        }
        removeEmpty(out);
        return out;
    }

    @Nullable
    private static AspectList generateTagsFromInfusionRecipes(RecipeManager recipes, ItemStack stack, List<Item> history) {
        InfusionRecipe cr = null;
        for (RecipeHolder<InfusionRecipe> holder : recipes.getAllRecipesFor(TCRecipes.INFUSION_TYPE.get())) {
            if (!holder.value().getRecipeOutput().isEmpty() && holder.value().getRecipeOutput().is(stack.getItem())) {
                cr = holder.value();
                break;
            }
        }
        if (cr == null) {
            return null;
        }
        AspectList ot = cr.getAspects().copy();
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(cr.getRecipeInput());
        ingredients.addAll(cr.getComponents());
        AspectList out = new AspectList();
        AspectList ot2 = getAspectsFromIngredients(ingredients, cr.getRecipeOutput(), null, history);
        for (Aspect tt : ot2.getAspects()) {
            out.add(tt, ot2.getAmount(tt));
        }
        for (Aspect tt : ot.getAspects()) {
            out.add(tt, (int) (Math.sqrt(ot.getAmount(tt)) / cr.getRecipeOutput().getCount()));
        }
        removeEmpty(out);
        return out;
    }

    @Nullable
    private static AspectList generateTagsFromCraftingRecipes(RecipeManager recipes, ItemStack stack, List<Item> history) {
        AspectList ret = null;
        int value = Integer.MAX_VALUE;
        for (RecipeHolder<CraftingRecipe> holder : recipes.getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            ItemStack output = recipe.getResultItem(ObjectAspects.registries());
            if (output.isEmpty() || !output.is(stack.getItem()) || recipe.getIngredients().isEmpty()) {
                continue;
            }
            AspectList ph = getAspectsFromIngredients(recipe.getIngredients(), output, recipe, history);
            removeEmpty(ph);
            if (ph.visSize() >= value || ph.visSize() <= 0) {
                continue;
            }
            ret = ph;
            value = ph.visSize();
        }
        // Arcane recipes were part of the 1.12 crafting registry; their vis cost adds praecantatio.
        for (RecipeHolder<IArcaneRecipe> holder : recipes.getAllRecipesFor(TCRecipes.ARCANE_TYPE.get())) {
            IArcaneRecipe ar = holder.value();
            ItemStack output = ar.getResultItem(ObjectAspects.registries());
            if (output.isEmpty() || !output.is(stack.getItem())) {
                continue;
            }
            AspectList ph = getAspectsFromIngredients(ar.getIngredients(), output, ar.getCraftingRecipe(), history);
            if (ar.getVis() > 0) {
                ph.add(Aspect.MAGIC, (int) (Math.sqrt(1 + ar.getVis() / 2) / (float) output.getCount()));
            }
            removeEmpty(ph);
            if (ph.visSize() >= value || ph.visSize() <= 0) {
                continue;
            }
            ret = ph;
            value = ph.visSize();
        }
        return ret;
    }

    private static AspectList getAspectsFromIngredients(List<Ingredient> ingredients, ItemStack recipeOut,
                                                        @Nullable CraftingRecipe recipe, List<Item> history) {
        AspectList out = new AspectList();
        AspectList mid = new AspectList();
        List<ItemStack> exlist = List.of();
        if (recipe != null && ingredients.size() <= 9) {
            List<ItemStack> grid = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                ItemStack[] options = i < ingredients.size() ? ingredients.get(i).getItems() : new ItemStack[0];
                grid.add(options.length > 0 ? options[0].copy() : ItemStack.EMPTY);
            }
            try {
                exlist = recipe.getRemainingItems(CraftingInput.of(3, 3, grid));
            } catch (RuntimeException ignored) {
            }
        }
        for (Ingredient is : ingredients) {
            ItemStack[] options = is.getItems();
            if (options.length == 0) {
                continue;
            }
            mid.add(getObjectTags(options[0], history));
        }
        for (ItemStack ri : exlist) {
            if (!ri.isEmpty()) {
                AspectList obj = getObjectTags(ri, history);
                for (Aspect as : obj.getAspects()) {
                    mid.reduce(as, obj.getAmount(as));
                }
            }
        }
        for (Aspect as : mid.getAspects()) {
            float v = mid.getAmount(as) * 0.75f / recipeOut.getCount();
            if (v < 1.0f && v > 0.75f) {
                v = 1.0f;
            }
            out.add(as, (int) v);
        }
        removeEmpty(out);
        return out;
    }

    private static void removeEmpty(AspectList list) {
        for (Aspect as : list.getAspects()) {
            if (list.getAmount(as) <= 0) list.remove(as);
        }
    }
}
