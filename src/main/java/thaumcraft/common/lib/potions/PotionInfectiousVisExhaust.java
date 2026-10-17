package thaumcraft.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Port of PotionInfectiousVisExhaust ("Flux Phage"). Works like Flux Flu, and every 40 ticks it passes itself on,
 * one level lower, to every creature within 4 blocks that does not have it. At level I it passes on plain Flux Flu.
 * Its icon is cell (6, 1) of textures/misc/potions.png.
 */
public class PotionInfectiousVisExhaust extends MobEffect {
    public PotionInfectiousVisExhaust() {
        super(MobEffectCategory.HARMFUL, 6706551);
    }

    @Override
    public boolean applyEffectTick(LivingEntity target, int amplifier) {
        for (LivingEntity e : target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0))) {
            if (!e.hasEffect(TCMobEffects.INFECTIOUS_VIS_EXHAUST)) {
                if (amplifier > 0) {
                    e.addEffect(new MobEffectInstance(TCMobEffects.INFECTIOUS_VIS_EXHAUST, 6000, amplifier - 1, false, true));
                } else {
                    e.addEffect(new MobEffectInstance(TCMobEffects.VIS_EXHAUST, 6000, 0, false, true));
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
