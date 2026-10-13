package thaumcraft.common.items.consumables;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of ItemBathSalts. Dropped salts dissolve after 10 seconds; if that happens in a still water source, it turns
 * into purifying fluid (EntityEvents.itemExpire).
 */
public class ItemBathSalts extends Item {
    public ItemBathSalts(Properties properties) {
        super(properties);
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return 200;
    }
}
