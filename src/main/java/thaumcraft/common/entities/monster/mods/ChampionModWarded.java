package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ChampionModWarded implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.invulnerableTime <= 0 && mob.tickCount % 25 == 0) {
            int bh = (int) mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue() / 2;
            if (mob.getAbsorptionAmount() < bh) {
                mob.setAbsorptionAmount(mob.getAbsorptionAmount() + 1.0f);
            }
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
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, 0.0, 0.0, 0.5f + boss.level().random.nextFloat() * 0.1f, 0.5f + boss.level().random.nextFloat() * 0.1f, 0.5f + boss.level().random.nextFloat() * 0.1f, 0.6f, true, 69, 4, 1, 4 + boss.level().random.nextInt(4), 0, 0.8f + boss.level().random.nextFloat() * 0.3f, 0.0f, 0);
    }
}
