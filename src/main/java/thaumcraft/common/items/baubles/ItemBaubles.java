package thaumcraft.common.items.baubles;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.items.IVisDiscountGear;

/**
 * The plain baubles: mundane amulet, ring and belt, the apprentice's ring and the fancy amulet, ring and belt.
 * 1.21: one item per old metadata (variant); the Curios slot comes from the curios item tags.
 * Only the apprentice's ring lowers vis costs (5%).
 */
public class ItemBaubles extends Item implements IVisDiscountGear {
    public static final String[] VARIANTS = {"amulet_mundane", "ring_mundane", "girdle_mundane", "ring_apprentice", "amulet_fancy", "ring_fancy", "girdle_fancy"};

    private final int variant;

    public ItemBaubles(int variant, Item.Properties properties) {
        super(properties.stacksTo(1));
        this.variant = variant;
    }

    public int getVariant() {
        return variant;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return variant == 3 ? 5 : 0;
    }
}
