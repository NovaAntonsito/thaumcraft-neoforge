package thaumcraft.common.golems.ai;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import thaumcraft.common.entities.construct.EntityOwnedConstruct;

/** Port of AIFollowOwner: walks after the owner and teleports next to them when 12 blocks behind with no path. */
public class AIFollowOwner extends Goal {
    private final EntityOwnedConstruct thePet;
    private LivingEntity theOwner;
    Level theWorld;
    private final double followSpeed;
    private final PathNavigation petPathfinder;
    private int timeToRecalcPath;
    float maxDist;
    float minDist;
    private float oldWaterCost;

    public AIFollowOwner(EntityOwnedConstruct pet, double speed, float minDist, float maxDist) {
        thePet = pet;
        theWorld = pet.level();
        followSpeed = speed;
        petPathfinder = pet.getNavigation();
        this.minDist = minDist;
        this.maxDist = maxDist;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(pet.getNavigation() instanceof GroundPathNavigation) && !(pet.getNavigation() instanceof PathNavigateGolemAir)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = thePet.getOwnerEntity();
        if (owner == null) {
            return false;
        }
        if (thePet.distanceToSqr(owner) < minDist * minDist) {
            return false;
        }
        theOwner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !petPathfinder.isDone() && thePet.distanceToSqr(theOwner) > maxDist * maxDist;
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = thePet.getPathfindingMalus(PathType.WATER);
        thePet.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    public void stop() {
        theOwner = null;
        petPathfinder.stop();
        thePet.setPathfindingMalus(PathType.WATER, oldWaterCost);
    }

    /** Air or a block that is not a full cube. */
    private boolean isEmptyBlock(BlockPos pos) {
        BlockState state = theWorld.getBlockState(pos);
        return state.isAir() || !state.isCollisionShapeFullBlock(theWorld, pos);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        thePet.getLookControl().setLookAt(theOwner, 10.0f, thePet.getMaxHeadXRot());
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = 10;
            if (!petPathfinder.moveTo(theOwner, followSpeed) && !thePet.isLeashed() && thePet.distanceToSqr(theOwner) >= 144.0) {
                int i = Mth.floor(theOwner.getX()) - 2;
                int j = Mth.floor(theOwner.getZ()) - 2;
                int k = Mth.floor(theOwner.getBoundingBox().minY);
                for (int l = 0; l <= 4; ++l) {
                    for (int i2 = 0; i2 <= 4; ++i2) {
                        BlockPos below = new BlockPos(i + l, k - 1, j + i2);
                        if ((l < 1 || i2 < 1 || l > 3 || i2 > 3) && theWorld.getBlockState(below).isCollisionShapeFullBlock(theWorld, below)
                                && isEmptyBlock(new BlockPos(i + l, k, j + i2)) && isEmptyBlock(new BlockPos(i + l, k + 1, j + i2))) {
                            thePet.moveTo(i + l + 0.5f, k, j + i2 + 0.5f, thePet.getYRot(), thePet.getXRot());
                            petPathfinder.stop();
                            return;
                        }
                    }
                }
            }
        }
    }
}
