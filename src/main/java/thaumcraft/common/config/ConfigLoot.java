package thaumcraft.common.config;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import thaumcraft.api.ThaumcraftApi;

/** ModConfig.postInitLoot of the original: what the loot bags (and later the loot urns and crates) can hold. */
public final class ConfigLoot {
    private ConfigLoot() {
    }

    public static void postInitLoot() {
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLD_NUGGET, 1), 2500, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLD_NUGGET, 2), 2250, 1);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLD_NUGGET, 3), 2000, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.salisMundus.get()), 3, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.salisMundus.get()), 6, 1);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.salisMundus.get()), 9, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.CHORUS_FRUIT), 5, 0, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.COMPASS), 5, 0, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.COOKIE), 5, 0, 1, 2);
        ThaumcraftApi.addLootBagItem(pearl(7), 1, 0);
        ThaumcraftApi.addLootBagItem(pearl(7), 3, 1);
        ThaumcraftApi.addLootBagItem(pearl(6), 1, 1);
        ThaumcraftApi.addLootBagItem(pearl(5), 9, 2);
        ThaumcraftApi.addLootBagItem(pearl(3), 3, 2);
        ThaumcraftApi.addLootBagItem(pearl(0), 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.NETHER_STAR), 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.DIAMOND), 10, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.DIAMOND), 50, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EMERALD), 15, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EMERALD), 75, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLD_INGOT), 100, 0, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.ENDER_PEARL), 100, 0, 1, 2);
        // TODO: amuletVis (6, uncommon and rare) and the 7 baubles (10 common for 0-2, 5 for 3-6), not ported yet.
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 5, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 10, 1);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 20, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 1, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 2, 1);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 3, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE), 3, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE), 6, 1);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE), 9, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.BOOK), 10, 0, 1, 2);
        BuiltInRegistries.POTION.holders().forEach(pt -> {
            ThaumcraftApi.addLootBagItem(potion(Items.POTION, pt), 2, 0, 1, 2);
            ThaumcraftApi.addLootBagItem(potion(Items.SPLASH_POTION, pt), 2, 0, 1, 2);
            ThaumcraftApi.addLootBagItem(potion(Items.LINGERING_POTION, pt), 2, 1, 2);
        });
    }

    /** The primordial pearl with that many uses spent. */
    private static ItemStack pearl(int damage) {
        ItemStack stack = new ItemStack(ConfigItems.primordialPearl.get());
        stack.setDamageValue(damage);
        return stack;
    }

    private static ItemStack potion(Item item, Holder<Potion> potion) {
        return PotionContents.createItemStack(item, potion);
    }
}
