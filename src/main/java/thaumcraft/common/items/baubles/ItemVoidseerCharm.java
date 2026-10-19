package thaumcraft.common.items.baubles;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.items.IVisDiscountGear;
import thaumcraft.api.items.IWarpingGear;

/** The voidseer's pearl: up to 25% off vis costs, by permanent warp (100 gives the most), and a fifth of that as warp. */
public class ItemVoidseerCharm extends Item implements IVisDiscountGear, IWarpingGear {
    public ItemVoidseerCharm(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.voidseer_charm.text").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        int q = 0;
        // No player while the creative search tab builds its tooltips.
        if (player == null) return 0;
        PlayerWarp warp = ThaumcraftCapabilities.getWarp(player);
        if (warp != null) {
            int pw = Math.min(100, warp.get(PlayerWarp.EnumWarpType.PERMANENT));
            q = (int) (pw / 100.0f * 25.0f);
        }
        return q;
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return getVisDiscount(itemstack, player) / 5;
    }
}
