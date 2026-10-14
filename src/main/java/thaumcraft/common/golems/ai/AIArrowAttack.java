package thaumcraft.common.golems.ai;

import java.util.EnumSet;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/** Port of AIArrowAttack: keeps shooting while it has a target, closing in until it can see it within range. */
public class AIArrowAttack extends Goal {
    private final Mob entityHost;
    private final RangedAttackMob rangedAttackEntityHost;
    private int rangedAttackTime = -1;
    private final double entityMoveSpeed;
    private int seeTime;
    private final int attackIntervalMin;
    private final int maxRangedAttackTime;
    private final float attackRadius;
    private final float maxAttackDistance;

    public AIArrowAttack(RangedAttackMob attacker, double movespeed, int attackIntervalMin, int maxAttackTime, float maxAttackDistanceIn) {
        if (!(attacker instanceof LivingEntity)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        rangedAttackEntityHost = attacker;
        entityHost = (Mob) attacker;
        entityMoveSpeed = movespeed;
        this.attackIntervalMin = attackIntervalMin;
        maxRangedAttackTime = maxAttackTime;
        attackRadius = maxAttackDistanceIn;
        maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return entityHost.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !entityHost.getNavigation().isDone();
    }

    @Override
    public void stop() {
        seeTime = 0;
        rangedAttackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = entityHost.getTarget();
        if (target == null) {
            return;
        }
        double d0 = entityHost.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
        boolean flag = entityHost.getSensing().hasLineOfSight(target);
        if (flag) {
            ++seeTime;
        } else {
            seeTime = 0;
        }
        if (d0 <= maxAttackDistance && seeTime >= 20) {
            entityHost.getNavigation().stop();
        } else {
            entityHost.getNavigation().moveTo(target, entityMoveSpeed);
        }
        entityHost.getLookControl().setLookAt(target, 10.0f, 30.0f);
        if (--rangedAttackTime == 0) {
            if (d0 > maxAttackDistance || !flag) {
                return;
            }
            float f = (float) Math.sqrt(d0) / attackRadius;
            float lvt_5_1_ = Mth.clamp(f, 0.1f, 1.0f);
            rangedAttackEntityHost.performRangedAttack(target, lvt_5_1_);
            rangedAttackTime = Mth.floor(f * (maxRangedAttackTime - attackIntervalMin) + attackIntervalMin);
        } else if (rangedAttackTime < 0) {
            float f2 = (float) Math.sqrt(d0) / attackRadius;
            rangedAttackTime = Mth.floor(f2 * (maxRangedAttackTime - attackIntervalMin) + attackIntervalMin);
        }
    }
}
