package thaumcraft.common.entities.ai.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/** A ranged attack that is only used while the target is at least min blocks away. */
public class AILongRangeAttack extends RangedAttackGoal {
    private final Mob wielder;
    double minDistance;

    public AILongRangeAttack(RangedAttackMob mob, double min, double speed, int minInterval, int maxInterval, float radius) {
        super(mob, speed, minInterval, maxInterval, radius);
        minDistance = min;
        wielder = (Mob) mob;
    }

    @Override
    public boolean canUse() {
        boolean ex = super.canUse();
        if (ex) {
            LivingEntity var1 = wielder.getTarget();
            if (var1 == null) {
                return false;
            }
            if (!var1.isAlive()) {
                wielder.setTarget(null);
                return false;
            }
            double ra = wielder.distanceToSqr(var1.getX(), var1.getBoundingBox().minY, var1.getZ());
            if (ra < minDistance * minDistance) {
                return false;
            }
        }
        return ex;
    }
}
