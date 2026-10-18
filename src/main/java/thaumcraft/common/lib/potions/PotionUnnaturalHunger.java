package thaumcraft.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Port of PotionUnnaturalHunger: every tick the player gets hungrier (0.025 exhaustion per level). Normal food does
 * not help; rotten flesh and zombie brains ease it (PlayerEvents.finishedUsingItem). It tints the screen red.
 * Its icon is cell (7, 1) of textures/misc/potions.png.
 */
public class PotionUnnaturalHunger extends MobEffect {
    public PotionUnnaturalHunger() {
        super(MobEffectCategory.HARMFUL, 4482611);
    }

    @Override
    public boolean applyEffectTick(LivingEntity target, int amplifier) {
        if (!target.level().isClientSide && target instanceof Player player) {
            player.causeFoodExhaustion(0.025f * (amplifier + 1));
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
