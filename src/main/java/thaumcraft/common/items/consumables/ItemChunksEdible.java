package thaumcraft.common.items.consumables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port of ItemChunksEdible: the meat nuggets the infernal furnace gives off when cooking meat (1 hunger, 0.3
 * saturation), eaten in half the time. 1.21: one item per old metadata (beef, chicken, pork, fish, rabbit, mutton).
 */
public class ItemChunksEdible extends Item {
    public static final String[] VARIANTS = {"beef", "chicken", "pork", "fish", "rabbit", "mutton"};
    public static final FoodProperties FOOD = new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).build();

    public ItemChunksEdible(Item.Properties properties) {
        super(properties.food(FOOD));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 10;
    }
}
