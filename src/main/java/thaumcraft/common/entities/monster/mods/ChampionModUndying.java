package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;

public class ChampionModUndying implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.tickCount % 20 == 0) {
            mob.heal(1.0f);
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextBoolean()) return;
        ClientFX.championMote(mob, 1.0f, 0.03, 0.1f + rand.nextFloat() * 0.1f, 0.8f + rand.nextFloat() * 0.2f, 0.1f + rand.nextFloat() * 0.1f,
                0.5f + rand.nextFloat() * 0.2f);
    }
}
