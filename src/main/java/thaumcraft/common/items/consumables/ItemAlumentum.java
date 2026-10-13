package thaumcraft.common.items.consumables;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import thaumcraft.common.entities.projectile.EntityAlumentum;

/**
 * Port of ItemAlumentum. Burns as a fuel that speeds up essentia smelteries, and can be thrown to explode.
 * Dispensers shoot it too (BehaviorDispenseAlumetum).
 */
public class ItemAlumentum extends Item implements ProjectileItem {
    public ItemAlumentum(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.playSound(SoundEvents.EGG_THROW, 0.3f, 0.4f / (world.random.nextFloat() * 0.4f + 0.8f));
        if (!world.isClientSide) {
            EntityAlumentum alumentum = new EntityAlumentum(world, player);
            alumentum.shootFromRotation(player, player.getXRot(), player.getYRot(), -5.0f, 0.4f, 2.0f);
            world.addFreshEntity(alumentum);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    /** CraftingEvents.getBurnTime. */
    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return 4800;
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        return new EntityAlumentum(world, pos.x(), pos.y(), pos.z());
    }
}
