package thaumcraft.common.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.common.lib.InternalMethodHandler;

/**
 * Port of ItemZombieBrain: wolf food (4 hunger, 0.2 saturation) with an 80% chance of Hunger. Eating it gives warp:
 * one normal warp 10% of the time, otherwise 1 to 3 temporary warp.
 * TODO: brainy zombies drop it (entities, a later stage).
 */
public class ItemZombieBrain extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder().nutrition(4).saturationModifier(0.2f)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 30, 0), 0.8f).build();

    public ItemZombieBrain(Properties properties) {
        super(properties.food(FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof Player player) {
            if (world.random.nextFloat() < 0.1f) {
                InternalMethodHandler.addWarpToPlayer(player, 1, PlayerWarp.EnumWarpType.NORMAL);
            } else {
                InternalMethodHandler.addWarpToPlayer(player, 1 + world.random.nextInt(3), PlayerWarp.EnumWarpType.TEMPORARY);
            }
        }
        return super.finishUsingItem(stack, world, entity);
    }
}
