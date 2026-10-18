package thaumcraft.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.Part;
import thaumcraft.api.internal.CommonInternals;

import javax.annotation.Nullable;

/**
 * Partial port of ThaumcraftApi: the thaumonomicon recipe catalog and the entity aspects. Normal recipes are datapack
 * recipes; only multiblock blueprints and "fake" display recipes are registered here.
 */
public class ThaumcraftApi {

    /**
     * An extra item the infernal furnace may give when it smelts something. The input is an ItemStack (matched by
     * item) or an item tag, which stands in for the original ore dictionary name.
     */
    public static class SmeltBonus {
        public Object in;
        public ItemStack out;
        public float chance;

        public SmeltBonus(Object in, ItemStack out, float chance) {
            this.in = in;
            this.out = out;
            this.chance = chance;
        }
    }

    /** Adds a smelting bonus: in is an ItemStack or a TagKey of items. */
    public static void addSmeltingBonus(Object in, ItemStack out, float chance) {
        if (in instanceof ItemStack || in instanceof TagKey<?>) {
            CommonInternals.smeltingBonus.add(new SmeltBonus(in, out, chance));
        }
    }

    public static void addSmeltingBonus(Object in, ItemStack out) {
        addSmeltingBonus(in, out, 0.33f);
    }

    /** A multiblock built with salis mundus, shown in the thaumonomicon as a 3D blueprint. */
    public static class BluePrint {
        Part[][][] parts;
        String research;
        @Nullable
        ItemStack displayStack;
        ItemStack[] ingredientList;

        public BluePrint(String research, Part[][][] parts, ItemStack... ingredientList) {
            this.parts = parts;
            this.research = research;
            this.ingredientList = ingredientList;
        }

        public BluePrint(String research, ItemStack display, Part[][][] parts, ItemStack... ingredientList) {
            this.parts = parts;
            this.research = research;
            displayStack = display;
            this.ingredientList = ingredientList;
        }

        public Part[][][] getParts() {
            return parts;
        }

        public String getResearch() {
            return research;
        }

        /** The items needed to build it, listed in the thaumonomicon only. */
        public ItemStack[] getIngredientList() {
            return ingredientList;
        }

        /** Shown instead of the multiblock on the recipe bookmark. */
        @Nullable
        public ItemStack getDisplayStack() {
            return displayStack;
        }
    }

    /** Adds a multiblock blueprint to the thaumonomicon recipe catalog. */
    public static void addMultiblockRecipeToCatalog(ResourceLocation registry, BluePrint recipe) {
        CommonInternals.craftingRecipeCatalog.put(registry, recipe);
    }

    /**
     * Adds a recipe that is only shown in the thaumonomicon. Since recipes now come from datapacks, it can also be
     * the id of a recipe or recipe group to show under this name, or a Function&lt;RecipeManager, Object&gt; that
     * builds the recipe when the book needs it.
     */
    public static void addFakeCraftingRecipe(ResourceLocation registry, Object recipe) {
        CommonInternals.craftingRecipeCatalogFake.put(registry, recipe);
    }

    // ENTITY ASPECTS ////////////////////////////////////

    /** CommonInternals.scanEntities: the aspects registered for each entity type. */
    public static final List<EntityTags> scanEntities = new ArrayList<>();

    /**
     * Assigns aspects to an entity type, which you can then scan using a thaumometer. With NBT values, the tags only
     * apply when the entity's saved data has them; when several entries match, the last one registered wins.
     * The 1.12 entity name strings became entity types.
     * Example: registerEntityTag(EntityType.SKELETON, new AspectList().add(Aspect.DEATH, 5));
     */
    public static void registerEntityTag(EntityType<?> entity, AspectList aspects, EntityTagsNBT... nbt) {
        scanEntities.add(new EntityTags(entity, aspects, nbt));
    }

    public static class EntityTagsNBT {
        public String name;
        public Object value;

        public EntityTagsNBT(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        /** getNBTDataFromId(...).equals(value): booleans and numbers compare by value, anything else as text. */
        public boolean matches(CompoundTag tag) {
            if (!tag.contains(name)) return false;
            if (value instanceof Boolean b) return tag.getBoolean(name) == b;
            if (value instanceof Number n) return tag.getInt(name) == n.intValue();
            return tag.getString(name).equals(String.valueOf(value));
        }
    }

    public static class EntityTags {
        public EntityType<?> entityType;
        public EntityTagsNBT[] nbts;
        public AspectList aspects;

        public EntityTags(EntityType<?> entityType, AspectList aspects, EntityTagsNBT... nbts) {
            this.entityType = entityType;
            this.nbts = nbts;
            this.aspects = aspects;
        }
    }

    /**
     * This method is used to register seeds for crops that the harvesting golems can replant.
     * @param block the crop block
     * @param seed the item that plants it
     */
    public static void registerSeed(net.minecraft.world.level.block.Block block, net.minecraft.world.item.ItemStack seed) {
        CommonInternals.seedList.put(block, seed);
    }

    public static net.minecraft.world.item.ItemStack getSeed(net.minecraft.world.level.block.Block block) {
        return CommonInternals.seedList.get(block);
    }
    /**
     * Adds an item to the loot bags. Weight is how often it shows up (gold nuggets are 2500, diamonds 10 to 50);
     * bagTypes are the bags it goes in: 0 common, 1 uncommon, 2 rare. Without bagTypes it goes in the common bag.
     */
    public static void addLootBagItem(ItemStack item, int weight, int... bagTypes) {
        if (bagTypes == null || bagTypes.length == 0) {
            thaumcraft.api.internal.WeightedRandomLoot.lootBagCommon.add(new thaumcraft.api.internal.WeightedRandomLoot(item, weight));
        } else {
            for (int rarity : bagTypes) {
                switch (rarity) {
                    case 0 -> thaumcraft.api.internal.WeightedRandomLoot.lootBagCommon.add(new thaumcraft.api.internal.WeightedRandomLoot(item, weight));
                    case 1 -> thaumcraft.api.internal.WeightedRandomLoot.lootBagUncommon.add(new thaumcraft.api.internal.WeightedRandomLoot(item, weight));
                    case 2 -> thaumcraft.api.internal.WeightedRandomLoot.lootBagRare.add(new thaumcraft.api.internal.WeightedRandomLoot(item, weight));
                    default -> {
                    }
                }
            }
        }
    }
}
