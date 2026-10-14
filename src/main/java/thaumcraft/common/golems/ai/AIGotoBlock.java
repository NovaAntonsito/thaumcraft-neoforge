package thaumcraft.common.golems.ai;

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import thaumcraft.Config;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.tasks.TaskHandler;

public class AIGotoBlock extends AIGoto {
    public AIGotoBlock(EntityThaumcraftGolem g) {
        super(g, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (golem.getTask() != null) {
            BlockPos p = golem.getTask().getPos();
            golem.getLookControl().setLookAt(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 10.0f, golem.getMaxHeadXRot());
        }
    }

    @Override
    protected void moveTo() {
        BlockPos p = targetBlock != null ? targetBlock : golem.getTask().getPos();
        golem.getNavigation().moveTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, golem.getGolemMoveSpeed());
    }

    @Override
    protected boolean findDestination() {
        ArrayList<Task> list = TaskHandler.getBlockTasksSorted(golem.level().dimension(), golem.getUUID(), golem);
        for (Task ticket : list) {
            if (areGolemTagsValidForTask(ticket) && ticket.canGolemPerformTask(golem) && golem.isWithinRestriction(ticket.getPos())
                    && isValidDestination(golem.level(), ticket.getPos()) && canEasilyReach(ticket.getPos())) {
                targetBlock = getAdjacentSpace(ticket.getPos());
                golem.setTask(ticket);
                golem.getTask().setReserved(true);
                if (Config.SHOW_GOLEM_EMOTES.getAsBoolean()) {
                    golem.level().broadcastEntityEvent(golem, (byte) 5);
                }
                return true;
            }
        }
        return false;
    }

    private BlockPos getAdjacentSpace(BlockPos pos) {
        double d = Double.MAX_VALUE;
        BlockPos closest = null;
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockState block = golem.level().getBlockState(pos.relative(face));
            if (!block.blocksMotion()) {
                double dist = pos.relative(face).getCenter().distanceToSqr(golem.position());
                if (dist < d) {
                    closest = pos.relative(face);
                    d = dist;
                }
            }
        }
        return closest;
    }

    private boolean canEasilyReach(BlockPos pos) {
        if (pos.getCenter().distanceToSqr(golem.position()) < minDist) {
            return true;
        }
        Path pathentity = golem.getNavigation().createPath(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0);
        if (pathentity == null) {
            return false;
        }
        Node pathpoint = pathentity.getEndNode();
        if (pathpoint == null) {
            return false;
        }
        int i = pathpoint.x - pos.getX();
        int j = pathpoint.z - pos.getZ();
        int k = pathpoint.y - pos.getY();
        if (i == 0 && j == 0 && k == 2) {
            --k;
        }
        return i * i + j * j + k * k < 2.25;
    }
}
