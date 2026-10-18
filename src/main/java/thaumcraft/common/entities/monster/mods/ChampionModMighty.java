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
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextFloat() > 0.3f) return;
        ClientFX.championMote(mob, 1.0f, 0.0, 0.8f + rand.nextFloat() * 0.2f, 0.8f + rand.nextFloat() * 0.2f, 0.8f + rand.nextFloat() * 0.2f,
                1.0f + rand.nextFloat() * 0.3f);
    }
}
