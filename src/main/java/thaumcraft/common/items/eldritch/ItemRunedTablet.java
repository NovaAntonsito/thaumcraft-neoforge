package thaumcraft.common.items.eldritch;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * ItemEldritchObject meta 2 of TC4: the Runed Tablet. It floats in the key room of the Outer Lands maze, and opens
 * the Ancient Locking Mechanism of the boss room.
 */
public class ItemRunedTablet extends Item {
    public ItemRunedTablet(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.runed_tablet.text").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
