package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;

public class ChampionModUndying implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.tickCount % 20 == 0) {
            mob.heal(1.0f);
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
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, 0.03, 0.0, 0.1f + boss.level().random.nextFloat() * 0.1f, 0.8f + boss.level().random.nextFloat() * 0.2f, 0.1f + boss.level().random.nextFloat() * 0.1f, 0.9f, true, 69, 4, 1, 4 + boss.level().random.nextInt(4), 0, 0.5f + boss.level().random.nextFloat() * 0.2f, 0.0f, 0);
    }
}
