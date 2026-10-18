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
    public void showFX(LivingEntity mob) {
        ClientFX.championFlame(mob);
    }
}
