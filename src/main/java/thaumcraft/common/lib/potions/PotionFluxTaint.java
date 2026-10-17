package thaumcraft.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.lib.DamageSourceThaumcraft;

/**
 * Port of PotionFluxTaint ("Taint Poison"). Tainted mobs are healed, everything else that is not undead takes one
 * point of taint damage, every 40 ticks at level I and twice as often per level. Its icon is cell (3, 1) of
 * textures/misc/potions.png.
 * TODO: the champion "tainted" modifier (CHAMPION_MOD 13) also heals; champions come with the mobs.
 */
public class PotionFluxTaint extends MobEffect {
    public PotionFluxTaint() {
        super(MobEffectCategory.HARMFUL, 6697847);
    }

    @Override
    public boolean applyEffectTick(LivingEntity target, int strength) {
        if (target instanceof ITaintedMob) {
            target.heal(1.0f);
        } else if (!target.isInvertedHealAndHarm()) {
            target.hurt(DamageSourceThaumcraft.taint(target.level()), 1.0f);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int k = 40 >> amplifier;
        return k <= 0 || duration % k == 0;
    }
}
