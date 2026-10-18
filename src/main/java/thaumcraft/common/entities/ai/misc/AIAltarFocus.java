package thaumcraft.common.entities.ai.misc;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import thaumcraft.common.entities.monster.cult.EntityCultistCleric;

/**
 * Keeps a ritualist cleric still in front of its altar. It stops being a ritualist once it is more than 4 blocks
 * from home. TODO: also when the home block is no longer the eldritch altar (BlocksTC.eldritch, not ported yet).
 */
public class AIAltarFocus extends Goal {
    private final EntityCultistCleric entity;

    public AIAltarFocus(EntityCultistCleric par1EntityLiving) {
        entity = par1EntityLiving;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return entity.getIsRitualist() && entity.hasRestriction();
    }

    @Override
    public boolean canContinueToUse() {
        return entity.getIsRitualist() && entity.hasRestriction();
    }

    @Override
    public void tick() {
        if (entity.hasRestriction() && entity.tickCount % 40 == 0
                && entity.getRestrictCenter().distToCenterSqr(entity.getX(), entity.getY(), entity.getZ()) > 16.0) {
            entity.setIsRitualist(false);
        }
    }
}
