package thaumcraft.common.entities.ai.pech;

import java.util.EnumSet;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Node;
import thaumcraft.common.entities.monster.EntityPech;

/**
 * Port of AIPechItemEntityGoto: a pech walks over to the closest item within 16 blocks it wants, and picks it up.
 * Items a pech handed out in a trade (the "PechDrop" thrower of 1.12, now a tag on the item) are left alone.
 */
public class AIPechItemEntityGoto extends Goal {
    /** Marks the items dropped when a trade window closes. */
    public static final String PECH_DROP = "PechDrop";
    private final EntityPech pech;
    private Entity targetEntity;
    float maxTargetDistance = 16.0f;
    private int count;
    private int failedPathFindingPenalty;

    public AIPechItemEntityGoto(EntityPech pech) {
        this.pech = pech;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (--count > 0) {
            return false;
        }
        double range = Double.MAX_VALUE;
        for (Entity e : pech.level().getEntities(pech, pech.getBoundingBox().inflate(maxTargetDistance))) {
            if (e instanceof ItemEntity item && pech.canPickup(item.getItem())) {
                if (e.getPersistentData().getBoolean(PECH_DROP)) {
                    continue;
                }
                double distance = e.distanceToSqr(pech.getX(), pech.getY(), pech.getZ());
                if (distance >= range || distance > maxTargetDistance * maxTargetDistance) {
                    continue;
                }
                range = distance;
                targetEntity = e;
            }
        }
        return targetEntity != null;
    }

    @Override
    public boolean canContinueToUse() {
        return targetEntity != null && targetEntity.isAlive() && !pech.getNavigation().isDone()
                && targetEntity.distanceToSqr(pech) < maxTargetDistance * maxTargetDistance;
    }

    @Override
    public void stop() {
        targetEntity = null;
    }

    @Override
    public void start() {
        pech.getNavigation().moveTo(pech.getNavigation().createPath(targetEntity, 0), speed());
        count = 0;
    }

    private double speed() {
        return pech.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5;
    }

    @Override
    public void tick() {
        pech.getLookControl().setLookAt(targetEntity, 30.0f, 30.0f);
        if (pech.getSensing().hasLineOfSight(targetEntity) && --count <= 0) {
            count = failedPathFindingPenalty + 4 + pech.getRandom().nextInt(4);
            pech.getNavigation().moveTo(targetEntity, speed());
            if (pech.getNavigation().getPath() != null) {
                Node finalPathPoint = pech.getNavigation().getPath().getEndNode();
                if (finalPathPoint != null && targetEntity.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1.0) {
                    failedPathFindingPenalty = 0;
                } else {
                    failedPathFindingPenalty += 10;
                }
            } else {
                failedPathFindingPenalty += 10;
            }
        }
        double distance = pech.distanceToSqr(targetEntity.getX(), targetEntity.getBoundingBox().minY, targetEntity.getZ());
        if (distance <= 1.5) {
            count = 0;
            ItemEntity item = (ItemEntity) targetEntity;
            int am = item.getItem().getCount();
            ItemStack is = pech.pickupItem(item.getItem());
            if (!is.isEmpty()) {
                item.setItem(is);
            } else {
                targetEntity.discard();
            }
            if (is.isEmpty() || is.getCount() != am) {
                targetEntity.level().playSound(null, targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(), SoundEvents.ITEM_PICKUP,
                        SoundSource.NEUTRAL, 0.2f, ((targetEntity.level().random.nextFloat() - targetEntity.level().random.nextFloat()) * 0.7f + 1.0f) * 2.0f);
            }
        }
    }
}
