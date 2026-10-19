package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;

public class ChampionModVampire implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        mob.heal(Math.max(2.0f, amount / 2.0f));
        return amount;
    }

    @Override
    public void showFX(LivingEntity boss) {
        if (boss.level().random.nextFloat() > 0.2f) {
            return;
        }
        float w = boss.level().random.nextFloat() * boss.getBbWidth();
        float d = boss.level().random.nextFloat() * boss.getBbWidth();
        float h = boss.level().random.nextFloat() * boss.getBbHeight();
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, 0.0, 0.0, 0.9f + boss.level().random.nextFloat() * 0.1f, 0.0f, 0.0f, 0.9f, false, 579, 4, 1, 4 + boss.level().random.nextInt(4), 0, 0.5f + boss.level().random.nextFloat() * 0.2f, 0.0f, 0);
    }
}
