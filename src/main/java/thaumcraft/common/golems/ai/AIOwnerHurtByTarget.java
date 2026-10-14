package thaumcraft.common.golems.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import thaumcraft.common.entities.construct.EntityOwnedConstruct;

/** Port of AIOwnerHurtByTarget: attacks whatever hurt the owner. */
public class AIOwnerHurtByTarget extends TargetGoal {
    EntityOwnedConstruct theDefendingTameable;
    LivingEntity theOwnerAttacker;
    private int timestamp;

    public AIOwnerHurtByTarget(EntityOwnedConstruct construct) {
        super(construct, false);
        theDefendingTameable = construct;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!theDefendingTameable.isOwned()) {
            return false;
        }
        LivingEntity owner = theDefendingTameable.getOwnerEntity();
        if (owner == null) {
            return false;
        }
        theOwnerAttacker = owner.getLastHurtByMob();
        int i = owner.getLastHurtByMobTimestamp();
        return i != timestamp && canAttack(theOwnerAttacker, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        mob.setTarget(theOwnerAttacker);
        LivingEntity owner = theDefendingTameable.getOwnerEntity();
        if (owner != null) {
            timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
