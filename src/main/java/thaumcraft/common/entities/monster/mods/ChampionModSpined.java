package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;

public class ChampionModSpined implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (target == null || source.is(DamageTypes.THORNS)) {
            return amount;
        }
        target.hurt(mob.damageSources().thorns(mob), (float) (1 + mob.getRandom().nextInt(3)));
        target.playSound(SoundEvents.THORNS_HIT, 0.5f, 1.0f);
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
        int p = 704 + boss.level().random.nextInt(4) * 3;
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, 0.0, 0.0, 0.5f + boss.level().random.nextFloat() * 0.2f, 0.1f + boss.level().random.nextFloat() * 0.2f, 0.1f + boss.level().random.nextFloat() * 0.2f, 0.7f, false, p, 3, 1, 3, 0, 1.2f + boss.level().random.nextFloat() * 0.3f, 0.0f, 0);
    }
}
