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
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextBoolean()) return;
        ClientFX.championMote(mob, 1.0f, 0.0, 0.5f + rand.nextFloat() * 0.2f, 0.1f + rand.nextFloat() * 0.2f, 0.1f + rand.nextFloat() * 0.2f,
                1.2f + rand.nextFloat() * 0.3f);
    }
}
