package thaumcraft.common.golems.parts;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.GolemArm;
import thaumcraft.common.entities.projectile.EntityGolemDart;
import thaumcraft.common.golems.ai.AIArrowAttack;

public class GolemArmDart implements GolemArm.IArmFunction {
    @Override
    public void onMeleeAttack(IGolemAPI golem, Entity target) {
    }

    @Override
    public void onRangedAttack(IGolemAPI golem, LivingEntity target, float range) {
        LivingEntity g = golem.getGolemEntity();
        EntityGolemDart entityarrow = new EntityGolemDart(golem.getGolemWorld(), g);
        float dmg = (float) g.getAttributeValue(Attributes.ATTACK_DAMAGE) / 3.0f;
        entityarrow.setBaseDamage(dmg + range + golem.getGolemWorld().random.nextGaussian() * 0.25);
        double d0 = target.getX() - g.getX();
        double d2 = target.getBoundingBox().minY + target.getEyeHeight() + range * range - entityarrow.getY();
        double d3 = target.getZ() - g.getZ();
        entityarrow.shoot(d0, d2, d3, 1.6f, 3.0f);
        golem.getGolemWorld().addFreshEntity(entityarrow);
        g.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (golem.getGolemWorld().random.nextFloat() * 0.4f + 0.8f));
    }

    @Override
    public Goal getRangedAttackAI(RangedAttackMob golem) {
        return new AIArrowAttack(golem, 1.0, 20, 25, 16.0f);
    }

    @Override
    public void onUpdateTick(IGolemAPI golem) {
    }
}
