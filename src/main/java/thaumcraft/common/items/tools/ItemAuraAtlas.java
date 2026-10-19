package thaumcraft.common.items.tools;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.common.lib.network.misc.PacketAuraAtlasToClient;

/**
 * Not in Thaumcraft 4 (dream branch): the aura atlas. Using it opens a map of the chunks around the player with their
 * vis and flux, the aura nodes known to the world and the local anomalies.
 */
public class ItemAuraAtlas extends Item {
    public ItemAuraAtlas(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static boolean isHolding(Player player) {
        return player.getMainHandItem().getItem() instanceof ItemAuraAtlas || player.getOffhandItem().getItem() instanceof ItemAuraAtlas;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, PacketAuraAtlasToClient.around(sp, true));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.aura_atlas.text").withStyle(ChatFormatting.GRAY));
    }
}
