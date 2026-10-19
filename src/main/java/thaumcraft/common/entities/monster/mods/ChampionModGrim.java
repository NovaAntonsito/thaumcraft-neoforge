package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class ChampionModGrim implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.getRandom().nextFloat() < 0.4f) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity boss) {
        if (boss.level().random.nextBoolean()) {
            return;
        }
        float w = boss.level().random.nextFloat() * boss.getBbWidth();
        float d = boss.level().random.nextFloat() * boss.getBbWidth();
        float h = boss.level().random.nextFloat() * boss.getBbHeight();
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, -0.02, 0.0, boss.level().random.nextFloat() * 0.2f, boss.level().random.nextFloat() * 0.2f, boss.level().random.nextFloat() * 0.2f, 0.8f, false, 640, 10, 1, 8 + boss.level().random.nextInt(4), 0, 0.6f + boss.level().random.nextFloat() * 0.4f, 0.0f, 0);
    }
}
