package thaumcraft.common.items.eldritch;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/** ItemEldritchObject meta 0 of TC4: the Eldritch Eye, placed four at a time on an eldritch altar keystone. */
public class ItemEldritchEye extends Item {
    public ItemEldritchEye(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.eldritch_eye.text").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
