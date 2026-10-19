package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;

public class ChampionModBold implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        return 0.0f;
    }

    @Override
    public void showFX(LivingEntity boss) {
        if (boss.level().random.nextBoolean()) {
            return;
        }
        float w = boss.level().random.nextFloat() * boss.getBbWidth();
        float d = boss.level().random.nextFloat() * boss.getBbWidth();
        float h = boss.level().random.nextFloat() * boss.getBbHeight() / 3.0f;
        thaumcraft.client.fx.FXDispatcher.spark(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.2f, 0.3f - boss.level().random.nextFloat() * 0.1f, 0.0f, 0.8f + boss.level().random.nextFloat() * 0.2f, 1.0f);
    }
}
