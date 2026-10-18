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
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextBoolean()) return;
        ClientFX.championMote(mob, 1.0f, -0.02, rand.nextFloat() * 0.2f, rand.nextFloat() * 0.2f, rand.nextFloat() * 0.2f, 0.6f + rand.nextFloat() * 0.4f);
    }
}
