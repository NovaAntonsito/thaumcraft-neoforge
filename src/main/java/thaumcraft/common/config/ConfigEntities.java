package thaumcraft.common.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.EnchantedBookItem;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.entities.monster.EntityPech;

/**
 * The pech part of ConfigEntities.initEntities: what pechs value and what each kind trades. A trade is {value 1 to 5,
 * stack}; enchanted books are made when traded (a Function of the registries), since enchantments are data now.
 * <p>
 * TODO: native clusters, thaumium tools, cloth robes, the knowledge fragment curio, the vis amulet, the bauble and the
 * eldritch eye, once they are ported. Until then forager trades of value 1 and mage trades of value 4 give nothing.
 */
public final class ConfigEntities {
    private ConfigEntities() {
    }

    public static void initPechTrades() {
        EntityPech.valuedItems.put(Items.ENDER_PEARL, 15);
        ArrayList<List<Object>> forInv = new ArrayList<>();
        forInv.add(Arrays.asList(2, new ItemStack(Items.BLAZE_ROD)));
        forInv.add(Arrays.asList(2, new ItemStack(ConfigBlocks.saplingGreatwood.get())));
        forInv.add(Arrays.asList(2, new ItemStack(Items.DRAGON_BREATH)));
        forInv.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forInv.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forInv.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE)));
        forInv.add(Arrays.asList(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
        forInv.add(Arrays.asList(5, new ItemStack(ConfigBlocks.saplingSilverwood.get())));
        EntityPech.tradeInventory.put(0, forInv);
        ArrayList<List<Object>> forMag = new ArrayList<>();
        forMag.add(Arrays.asList(1, crystal(Aspect.AIR)));
        forMag.add(Arrays.asList(1, crystal(Aspect.EARTH)));
        forMag.add(Arrays.asList(1, crystal(Aspect.FIRE)));
        forMag.add(Arrays.asList(1, crystal(Aspect.WATER)));
        forMag.add(Arrays.asList(1, crystal(Aspect.ORDER)));
        forMag.add(Arrays.asList(1, crystal(Aspect.ENTROPY)));
        // Potion damage values 8193 and 8261 of the old potions: Regeneration and Healing II.
        forMag.add(Arrays.asList(2, PotionContents.createItemStack(Items.POTION, Potions.REGENERATION)));
        forMag.add(Arrays.asList(2, PotionContents.createItemStack(Items.POTION, Potions.STRONG_HEALING)));
        forMag.add(Arrays.asList(2, crystal(Aspect.FLUX)));
        forMag.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forMag.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forMag.add(Arrays.asList(3, crystal(Aspect.AURA)));
        forMag.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE)));
        forMag.add(Arrays.asList(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
        forMag.add(Arrays.asList(5, new ItemStack(ConfigItems.pechWand.get())));
        // As in the original, the totem went to the forager list.
        forInv.add(Arrays.asList(5, new ItemStack(Items.TOTEM_OF_UNDYING)));
        EntityPech.tradeInventory.put(1, forMag);
        ArrayList<List<Object>> forArc = new ArrayList<>();
        // EnumDyeColor.byDyeDamage(0..14): every candle but the white one.
        for (DyeColor color : new DyeColor[] {DyeColor.BLACK, DyeColor.RED, DyeColor.GREEN, DyeColor.BROWN, DyeColor.BLUE, DyeColor.PURPLE,
                DyeColor.CYAN, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.PINK, DyeColor.LIME, DyeColor.YELLOW, DyeColor.LIGHT_BLUE,
                DyeColor.MAGENTA, DyeColor.ORANGE}) {
            forArc.add(Arrays.asList(1, new ItemStack(ConfigBlocks.candles.get(color).get())));
        }
        forArc.add(Arrays.asList(2, new ItemStack(Items.GHAST_TEAR)));
        // So did the compass and the spectral arrow.
        forInv.add(Arrays.asList(2, new ItemStack(Items.COMPASS)));
        forArc.add(Arrays.asList(2, book(Enchantments.POWER)));
        forArc.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forArc.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forArc.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE)));
        forArc.add(Arrays.asList(4, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
        forInv.add(Arrays.asList(4, new ItemStack(Items.SPECTRAL_ARROW)));
        forArc.add(Arrays.asList(5, book(Enchantments.FLAME)));
        forArc.add(Arrays.asList(5, book(Enchantments.INFINITY)));
        EntityPech.tradeInventory.put(2, forArc);
    }

    private static ItemStack crystal(Aspect aspect) {
        return ConfigItems.crystalEssence.get().makeCrystal(aspect, 1);
    }

    /** ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment, 1)). */
    private static Function<RegistryAccess, ItemStack> book(ResourceKey<Enchantment> enchantment) {
        return registries -> EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(registries.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(enchantment), 1));
    }

    /** The stack a trade gives. */
    @SuppressWarnings("unchecked")
    public static ItemStack tradeStack(Object trade, RegistryAccess registries) {
        if (trade instanceof Function<?, ?> f) {
            return ((Function<RegistryAccess, ItemStack>) f).apply(registries);
        }
        return ((ItemStack) trade).copy();
    }
}
