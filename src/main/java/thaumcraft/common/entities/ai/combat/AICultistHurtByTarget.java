package thaumcraft.common.entities.ai.combat;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import thaumcraft.common.entities.monster.cult.EntityCultist;

/** Revenge on whatever hurt it, calling every idle cultist around to help. */
public class AICultistHurtByTarget extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    boolean entityCallsForHelp;
    private int revengeTimerOld;

    public AICultistHurtByTarget(PathfinderMob owner, boolean callsHelp) {
        super(owner, false);
        entityCallsForHelp = callsHelp;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        int i = mob.getLastHurtByMobTimestamp();
        LivingEntity entitylivingbase = mob.getLastHurtByMob();
        return i != revengeTimerOld && entitylivingbase != null && canAttack(entitylivingbase, HURT_BY_TARGETING);
    }

    @Override
    public void start() {
        mob.setTarget(mob.getLastHurtByMob());
        targetMob = mob.getTarget();
        revengeTimerOld = mob.getLastHurtByMobTimestamp();
        unseenMemoryTicks = 300;
        if (entityCallsForHelp) {
            alertOthers();
        }
        super.start();
    }

    protected void alertOthers() {
        double d0 = getFollowDistance();
        AABB box = new AABB(mob.getX(), mob.getY(), mob.getZ(), mob.getX() + 1.0, mob.getY() + 1.0, mob.getZ() + 1.0).inflate(d0, 10.0, d0);
        for (EntityCultist creature : mob.level().getEntitiesOfClass(EntityCultist.class, box)) {
            if (mob != creature && creature.getTarget() == null && !creature.isAlliedTo(mob.getLastHurtByMob())) {
                setEntityAttackTarget(creature, mob.getLastHurtByMob());
            }
        }
    }

    protected void setEntityAttackTarget(PathfinderMob creatureIn, LivingEntity entityLivingBaseIn) {
        creatureIn.setTarget(entityLivingBaseIn);
    }
}
