package thaumcraft.common.items.tools;

import javax.annotation.Nullable;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.RechargeHelper;
import thaumcraft.common.entities.projectile.EntityGrapple;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Port of ItemGrappleGun: fires a grappling hook for 1 point of charge (100 max). "loaded" is set while the hook is
 * out, which shows the gun without its head (the "thaumcraft:type" model property).
 */
public class ItemGrappleGun extends Item implements IRechargable {

    public ItemGrappleGun(Properties properties) {
        super(properties);
    }

    public static boolean isLoaded(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.GRAPPLE_LOADED.get(), false);
    }

    @Override
    public int getMaxCharge(ItemStack stack, @Nullable LivingEntity player) {
        return 100;
    }

    @Override
    public EnumChargeDisplay showInHud(ItemStack stack, @Nullable LivingEntity player) {
        return EnumChargeDisplay.NORMAL;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!EntityGrapple.grapples.containsKey(entityIn.getId()) && isLoaded(stack)) {
            stack.set(TCDataComponents.GRAPPLE_LOADED.get(), false);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.playSound(SoundsTC.ICE.get(), 3.0f, 0.8f + world.random.nextFloat() * 0.1f);
        if (!world.isClientSide && RechargeHelper.getCharge(stack) > 0) {
            EntityGrapple grapple = new EntityGrapple(world, player, hand);
            grapple.shootFromRotation(player, player.getXRot(), player.getYRot(), -5.0f, 1.5f, 0.0f);
            double px = -Mth.cos((player.getYRot() - 0.5f) / 180.0f * 3.141593f) * 0.2f * (grapple.hand == InteractionHand.MAIN_HAND ? 1 : -1);
            double pz = -Mth.sin((player.getYRot() - 0.5f) / 180.0f * 3.141593f) * 0.3f * (grapple.hand == InteractionHand.MAIN_HAND ? 1 : -1);
            Vec3 vl = player.getLookAngle();
            // As in the original, the look's y goes into z.
            grapple.setPos(grapple.getX() + px + vl.x, grapple.getY(), grapple.getZ() + pz + vl.y);
            if (world.addFreshEntity(grapple)) {
                RechargeHelper.consumeCharge(stack, player, 1);
                stack.set(TCDataComponents.GRAPPLE_LOADED.get(), true);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItem() == this && newStack.getItem() == this && !ItemStack.isSameItemSameComponents(oldStack, newStack)) {
            return isLoaded(oldStack) != isLoaded(newStack);
        }
        return newStack.getItem() != oldStack.getItem();
    }
}
