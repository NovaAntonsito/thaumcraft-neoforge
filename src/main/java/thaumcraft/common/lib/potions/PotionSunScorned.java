package thaumcraft.common.lib.potions;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Port of PotionSunScorned: every 2 seconds, bright light under the open sky may set the target on fire, and darkness
 * may heal it a little. The screen glows. Its icon is cell (6, 2) of textures/misc/potions.png.
 */
public class PotionSunScorned extends MobEffect {
    public PotionSunScorned() {
        super(MobEffectCategory.HARMFUL, 16308330);
    }

    @Override
    public boolean applyEffectTick(LivingEntity target, int amplifier) {
        if (!target.level().isClientSide) {
            float f = target.getLightLevelDependentMagicValue();
            if (f > 0.5f && target.level().random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f
                    && target.level().canSeeSky(new BlockPos(Mth.floor(target.getX()), Mth.floor(target.getY()), Mth.floor(target.getZ())))) {
                target.igniteForSeconds(4.0f);
            } else if (f < 0.25f && target.level().random.nextFloat() > f * 2.0f) {
                target.heal(1.0f);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
