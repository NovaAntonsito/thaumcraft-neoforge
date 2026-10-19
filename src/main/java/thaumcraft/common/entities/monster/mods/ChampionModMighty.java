package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;

public class ChampionModMighty implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        return 0.0f;
    }

    @Override
    public void showFX(LivingEntity boss) {
        if (boss.level().random.nextFloat() > 0.3f) {
            return;
        }
        float w = boss.level().random.nextFloat() * boss.getBbWidth();
        float d = boss.level().random.nextFloat() * boss.getBbWidth();
        float h = boss.level().random.nextFloat() * boss.getBbHeight();
        int p = 704 + boss.level().random.nextInt(4) * 3;
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, 0.0, 0.0, 0.8f + boss.level().random.nextFloat() * 0.2f, 0.8f + boss.level().random.nextFloat() * 0.2f, 0.8f + boss.level().random.nextFloat() * 0.2f, 0.7f, false, p, 3, 1, 4 + boss.level().random.nextInt(3), 0, 1.0f + boss.level().random.nextFloat() * 0.3f, 0.0f, 0);
    }
}
