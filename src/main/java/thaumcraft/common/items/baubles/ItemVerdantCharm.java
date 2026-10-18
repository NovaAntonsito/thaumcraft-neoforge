package thaumcraft.common.items.baubles;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.RechargeHelper;
import thaumcraft.common.items.ISubItems;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.registry.TCMobEffects;

/**
 * The verdant heart charm: once a second it spends charge to clear wither (20), poison (10) or flux taint (5).
 * The lifegiver also heals 1 for 5 charge, the sustainer refills air (under 100) or food for 1 charge.
 * 1.21: the "type" tag is the verdant_type component.
 */
public class ItemVerdantCharm extends Item implements ICurioItem, IRechargable, ISubItems {
    public ItemVerdantCharm(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    public static int getType(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.VERDANT_TYPE.get(), 0);
    }

    @Override
    public void fillSubItems(Consumer<ItemStack> output) {
        output.accept(new ItemStack(this));
        for (int t = 1; t <= 2; ++t) {
            ItemStack stack = new ItemStack(this);
            stack.set(TCDataComponents.VERDANT_TYPE.get(), t);
            output.accept(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (getType(stack) == 1) {
            tooltip.add(Component.translatable("item.verdant_charm.life.text").withStyle(ChatFormatting.GOLD));
        }
        if (getType(stack) == 2) {
            tooltip.add(Component.translatable("item.verdant_charm.sustain.text").withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack itemstack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide || entity.tickCount % 20 != 0 || !(entity instanceof Player player)) {
            return;
        }
        if (player.hasEffect(MobEffects.WITHER) && RechargeHelper.consumeCharge(itemstack, player, 20)) {
            player.removeEffect(MobEffects.WITHER);
            return;
        }
        if (player.hasEffect(MobEffects.POISON) && RechargeHelper.consumeCharge(itemstack, player, 10)) {
            player.removeEffect(MobEffects.POISON);
            return;
        }
        if (player.hasEffect(TCMobEffects.FLUX_TAINT) && RechargeHelper.consumeCharge(itemstack, player, 5)) {
            player.removeEffect(TCMobEffects.FLUX_TAINT);
            return;
        }
        if (getType(itemstack) == 1 && player.getHealth() < player.getMaxHealth() && RechargeHelper.consumeCharge(itemstack, player, 5)) {
            player.heal(1.0f);
            return;
        }
        if (getType(itemstack) == 2) {
            if (player.getAirSupply() < 100 && RechargeHelper.consumeCharge(itemstack, player, 1)) {
                player.setAirSupply(300);
                return;
            }
            if (player.canEat(false) && RechargeHelper.consumeCharge(itemstack, player, 1)) {
                player.getFoodData().eat(1, 0.3f);
            }
        }
    }

    @Override
    public int getMaxCharge(ItemStack stack, @Nullable LivingEntity player) {
        return 200;
    }

    @Override
    public EnumChargeDisplay showInHud(ItemStack stack, @Nullable LivingEntity player) {
        return EnumChargeDisplay.NORMAL;
    }
}
