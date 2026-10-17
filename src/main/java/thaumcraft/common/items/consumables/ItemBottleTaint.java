package thaumcraft.common.items.consumables;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.common.entities.projectile.EntityBottleTaint;

/** Port of ItemBottleTaint: a bottle of taint to throw. Stacks to 8. */
public class ItemBottleTaint extends Item {
    public ItemBottleTaint(Item.Properties properties) {
        super(properties.stacksTo(8));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.playSound(SoundEvents.EGG_THROW, 0.5f, 0.4f / (world.random.nextFloat() * 0.4f + 0.8f));
        if (!world.isClientSide) {
            EntityBottleTaint bottle = new EntityBottleTaint(world, player);
            bottle.shootFromRotation(player, player.getXRot(), player.getYRot(), -5.0f, 0.66f, 1.0f);
            world.addFreshEntity(bottle);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }
}
