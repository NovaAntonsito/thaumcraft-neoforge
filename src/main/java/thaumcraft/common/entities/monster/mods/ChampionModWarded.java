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
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextBoolean()) return;
        ClientFX.championMote(mob, 1.0f, 0.0, 0.5f + rand.nextFloat() * 0.1f, 0.5f + rand.nextFloat() * 0.1f, 0.5f + rand.nextFloat() * 0.1f,
                0.8f + rand.nextFloat() * 0.3f);
    }
}
