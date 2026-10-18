package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;
import net.minecraft.tags.DamageTypeTags;

public class ChampionModArmored implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            float f1 = amount * 19.0f;
            amount = f1 / 25.0f;
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextInt(4) != 0) return;
        ClientFX.championMote(mob, 1.0f, 0.0, 0.9f, 0.9f, 0.9f + rand.nextFloat() * 0.1f, 0.6f + rand.nextFloat() * 0.2f);
    }
}
