package thaumcraft.common.items.curios;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.Utils;

/** Port of ItemLootBag. The 1.12 item had one meta per rarity; now each rarity is its own item. */
public class ItemLootBag extends Item {
    private final int rarity;

    public ItemLootBag(int rarity, Item.Properties properties) {
        super(properties.stacksTo(16).rarity(rarity == 1 ? Rarity.UNCOMMON : rarity == 2 ? Rarity.RARE : Rarity.COMMON));
        this.rarity = rarity;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable("tc.lootbag"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            for (int q = 8 + world.random.nextInt(5), a = 0; a < q; ++a) {
                ItemStack is = Utils.generateLoot(rarity, world.random, world.registryAccess());
                if (!is.isEmpty()) {
                    world.addFreshEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), is.copy()));
                }
            }
            player.playSound(SoundsTC.COINS.get(), 0.75f, 1.0f);
        }
        player.getItemInHand(hand).shrink(1);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
