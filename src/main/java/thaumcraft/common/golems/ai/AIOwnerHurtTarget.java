package thaumcraft.common.golems.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import thaumcraft.common.entities.construct.EntityOwnedConstruct;

/** Port of AIOwnerHurtTarget: attacks whatever the owner hits. */
public class AIOwnerHurtTarget extends TargetGoal {
    EntityOwnedConstruct theEntityTameable;
    LivingEntity theTarget;
    private int timestamp;

    public AIOwnerHurtTarget(EntityOwnedConstruct construct) {
        super(construct, false);
        theEntityTameable = construct;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!theEntityTameable.isOwned()) {
            return false;
        }
        LivingEntity owner = theEntityTameable.getOwnerEntity();
        if (owner == null) {
            return false;
        }
        theTarget = owner.getLastHurtMob();
        int i = owner.getLastHurtMobTimestamp();
        return i != timestamp && canAttack(theTarget, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        mob.setTarget(theTarget);
        LivingEntity owner = theEntityTameable.getOwnerEntity();
        if (owner != null) {
            timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
