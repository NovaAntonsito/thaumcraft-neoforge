package thaumcraft.common.lib.utils;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import thaumcraft.api.internal.WeightedRandomLoot;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Partial port of Utils: what the golem seals and the loot bags need.
 * TODO: the rest of the original helpers when something needs them.
 */
public final class Utils {
    private Utils() {
    }

    /**
     * Block.isWood, canSustainLeaves or the "logWood" ore dictionary in 1.12. The minecraft:logs tag covers all three
     * (it includes the modded logs that are tagged c:/minecraft logs).
     */
    public static boolean isWoodLog(BlockGetter world, BlockPos pos) {
        BlockState bs = world.getBlockState(pos);
        return bs.is(BlockTags.LOGS);
    }
    /** One item out of a loot bag (or a loot urn) of the given rarity: 0 common, 1 uncommon, 2 rare. */
    public static ItemStack generateLoot(int rarity, RandomSource rand, RegistryAccess registries) {
        ItemStack is = ItemStack.EMPTY;
        if (rarity > 0 && rand.nextFloat() < 0.025f * rarity) {
            is = genGear(rarity, rand, registries);
            if (is.isEmpty()) {
                is = generateLoot(rarity, rand, registries);
            }
        } else {
            List<WeightedRandomLoot> list = switch (rarity) {
                case 1 -> WeightedRandomLoot.lootBagUncommon;
                case 2 -> WeightedRandomLoot.lootBagRare;
                default -> WeightedRandomLoot.lootBagCommon;
            };
            Optional<WeightedRandomLoot> loot = WeightedRandom.getRandomItem(rand, list);
            if (loot.isPresent()) {
                is = loot.get().item;
            }
        }
        if (is.is(Items.BOOK)) {
            is = EnchantmentHelper.enchantItem(rand, is.copy(), (int) (5.0f + rarity * 0.75f * rand.nextInt(18)), registries, Optional.empty());
        }
        return is.copy();
    }

    private static ItemStack genGear(int rarity, RandomSource rand, RegistryAccess registries) {
        int quality = rand.nextInt(2);
        if (rand.nextFloat() < 0.2f) {
            ++quality;
        }
        if (rand.nextFloat() < 0.15f) {
            ++quality;
        }
        if (rand.nextFloat() < 0.1f) {
            ++quality;
        }
        if (rand.nextFloat() < 0.095f) {
            ++quality;
        }
        if (rand.nextFloat() < 0.095f) {
            ++quality;
        }
        Item item = getGearItemForSlot(rand.nextInt(5), quality);
        if (item != null) {
            ItemStack is = new ItemStack(item);
            is.setDamageValue(rand.nextInt(1 + is.getMaxDamage() / 6));
            if (rand.nextInt(4) < rarity) {
                is = EnchantmentHelper.enchantItem(rand, is, (int) (5.0f + rarity * 0.75f * rand.nextInt(18)), registries, Optional.empty());
            }
            return is.copy();
        }
        return ItemStack.EMPTY;
    }

    /** Quality 4 is thaumium gear and 6 void metal gear. TODO: those items are not ported yet, so they roll again. */
    @Nullable
    private static Item getGearItemForSlot(int slot, int quality) {
        Item[] items = switch (slot) {
            case 4 -> new Item[] { Items.LEATHER_HELMET, Items.GOLDEN_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, null, Items.DIAMOND_HELMET, null };
            case 3 -> new Item[] { Items.LEATHER_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, null,
                    Items.DIAMOND_CHESTPLATE, null };
            case 2 -> new Item[] { Items.LEATHER_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, null,
                    Items.DIAMOND_LEGGINGS, null };
            case 1 -> new Item[] { Items.LEATHER_BOOTS, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, null, Items.DIAMOND_BOOTS, null };
            default -> new Item[] { Items.IRON_AXE, Items.IRON_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_SWORD, null, Items.DIAMOND_SWORD, null };
        };
        return quality < items.length ? items[quality] : null;
    }

    /** Whether point x lies in the cone with apex t, axis towards b (its length is the cone's) and the given aperture. */
    public static boolean isLyingInCone(double[] x, double[] t, double[] b, float aperture) {
        double halfAperture = aperture / 2.0f;
        double[] apexToXVect = dif(t, x);
        double[] axisVect = dif(t, b);
        boolean isInInfiniteCone = dotProd(apexToXVect, axisVect) / magn(apexToXVect) / magn(axisVect) > Math.cos(halfAperture);
        if (!isInInfiniteCone) {
            return false;
        }
        return dotProd(apexToXVect, axisVect) / magn(axisVect) < magn(axisVect);
    }

    public static double dotProd(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    public static double[] dif(double[] a, double[] b) {
        return new double[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
    }

    public static double magn(double[] a) {
        return Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
    }
}
