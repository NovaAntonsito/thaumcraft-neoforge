package thaumcraft.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;

public class ChampionModFire implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.getRandom().nextFloat() < 0.4f) {
            target.igniteForSeconds(4);
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity boss) {
        float w = boss.level().random.nextFloat() * boss.getBbWidth();
        float d = boss.level().random.nextFloat() * boss.getBbWidth();
        float h = boss.level().random.nextFloat() * boss.getBbHeight();
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, 0.03, 0.0, 0.9f + boss.level().random.nextFloat() * 0.1f, 1.0f, 1.0f, 0.7f, false, 640, 10, 1, 8 + boss.level().random.nextInt(4), 0, 0.7f + boss.level().random.nextFloat() * 0.2f, 0.0f, 0);
    }
}
