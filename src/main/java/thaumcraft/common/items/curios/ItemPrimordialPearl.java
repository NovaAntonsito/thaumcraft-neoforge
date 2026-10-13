package thaumcraft.common.items.curios;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Port of ItemPrimordialPearl. It has 8 uses: every craft hands it back one point more damaged, a pearl (0-2)
 * shrinking to a nodule (3-5) and then a mote (6-7). It cannot be repaired or enchanted.
 */
public class ItemPrimordialPearl extends Item {
    public ItemPrimordialPearl(Item.Properties properties) {
        super(properties.durability(8).rarity(Rarity.UNCOMMON).setNoRepair());
    }

    /** The "type" model property of the original: 0 pearl, 1 nodule, 2 mote. */
    public static float getType(ItemStack stack) {
        if (stack.getDamageValue() < 3) {
            return 0.0f;
        }
        if (stack.getDamageValue() < 6) {
            return 1.0f;
        }
        return 2.0f;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (stack.getDamageValue() < 3) {
            return getDescriptionId() + ".pearl";
        }
        if (stack.getDamageValue() < 6) {
            return getDescriptionId() + ".nodule";
        }
        return getDescriptionId() + ".mote";
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        if (!hasCraftingRemainingItem(itemStack)) {
            return ItemStack.EMPTY;
        }
        ItemStack out = itemStack.copyWithCount(1);
        out.setDamageValue(itemStack.getDamageValue() + 1);
        return out;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return stack.getDamageValue() < 7;
    }

    /** The durability bar would show its uses as wear; the original had none either (its damage was metadata). */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}
