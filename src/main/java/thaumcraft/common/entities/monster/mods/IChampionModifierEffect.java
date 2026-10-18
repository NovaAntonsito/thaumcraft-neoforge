package thaumcraft.common.entities.monster.mods;

import javax.annotation.Nullable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface IChampionModifierEffect {
    float performEffect(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount);

    /** Client only. */
    void showFX(LivingEntity mob);
}
