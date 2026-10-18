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
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextBoolean()) return;
        ClientFX.championSpark(mob, 1.0f / 3.0f, 0.2f, 0.3f - rand.nextFloat() * 0.1f, 0.0f);
    }
}
