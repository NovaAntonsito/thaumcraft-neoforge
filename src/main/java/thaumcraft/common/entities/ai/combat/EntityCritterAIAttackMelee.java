package thaumcraft.common.entities.ai.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/** Melee attack for tainted critters, which usually have no attack damage attribute of their own. */
public class EntityCritterAIAttackMelee extends MeleeAttackGoal {
    public EntityCritterAIAttackMelee(PathfinderMob creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (canPerformAttack(target)) {
            resetAttackCooldown();
            mob.swing(InteractionHand.MAIN_HAND);
            attackEntityAsMob(mob, target);
        }
    }

    protected boolean attackEntityAsMob(PathfinderMob attacker, LivingEntity target) {
        float f = Math.max(2.0f, (attacker.getBbHeight() + attacker.getBbWidth()) * 2.0f);
        AttributeInstance dmg = attacker.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) {
            f = (float) dmg.getValue();
        }
        boolean flag = target.hurt(attacker.damageSources().mobAttack(attacker), f);
        if (flag) {
            attacker.setLastHurtMob(target);
        }
        return flag;
    }
}
