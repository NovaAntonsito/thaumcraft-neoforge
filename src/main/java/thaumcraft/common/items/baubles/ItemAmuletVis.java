package thaumcraft.common.items.baubles;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import thaumcraft.api.items.RechargeHelper;
import thaumcraft.common.lib.utils.BaublesHelper;

/**
 * The vis stone (found) and the amulet of vis (crafted). Worn, they recharge one point into the first item that takes
 * it: hotbar, then baubles, then armor. The stone every 2 seconds, the amulet 4 times a second.
 * 1.21: the two old metadata values are two items.
 */
public class ItemAmuletVis extends Item implements ICurioItem {
    private final boolean crafted;

    public ItemAmuletVis(boolean crafted, Item.Properties properties) {
        super(properties.stacksTo(1));
        this.crafted = crafted;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide || player.tickCount % (crafted ? 5 : 40) != 0) {
            return;
        }
        for (int a = 0; a < Inventory.getSelectionSize(); ++a) {
            if (RechargeHelper.rechargeItem(player.level(), player.getInventory().items.get(a), player.blockPosition(), player, 1) > 0.0f) {
                return;
            }
        }
        for (ItemStack bauble : BaublesHelper.getBaubles(player)) {
            if (RechargeHelper.rechargeItem(player.level(), bauble, player.blockPosition(), player, 1) > 0.0f) {
                return;
            }
        }
        for (ItemStack armor : player.getInventory().armor) {
            if (RechargeHelper.rechargeItem(player.level(), armor, player.blockPosition(), player, 1) > 0.0f) {
                return;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.amulet_vis.text").withStyle(ChatFormatting.AQUA));
    }
}
