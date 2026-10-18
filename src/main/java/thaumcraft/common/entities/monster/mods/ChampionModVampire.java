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
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextFloat() > 0.2f) return;
        ClientFX.championMote(mob, 1.0f, 0.0, 0.9f + rand.nextFloat() * 0.1f, 0.0f, 0.0f, 0.5f + rand.nextFloat() * 0.2f);
    }
}
