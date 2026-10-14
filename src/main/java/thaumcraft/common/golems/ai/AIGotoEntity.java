package thaumcraft.common.golems.ai;

import java.util.ArrayList;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import thaumcraft.Config;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.tasks.TaskHandler;

public class AIGotoEntity extends AIGoto {
    public AIGotoEntity(EntityThaumcraftGolem g) {
        super(g, (byte) 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (golem.getTask() != null && golem.getTask().getEntity() != null) {
            golem.getLookControl().setLookAt(golem.getTask().getEntity(), 10.0f, golem.getMaxHeadXRot());
        }
    }

    @Override
    protected void moveTo() {
        if (golem.getTask() != null && golem.getTask().getEntity() != null) {
            golem.getNavigation().moveTo(golem.getTask().getEntity(), golem.getGolemMoveSpeed());
        }
    }

    @Override
    protected boolean findDestination() {
        ArrayList<Task> list = TaskHandler.getEntityTasksSorted(golem.level().dimension(), golem.getUUID(), golem);
        for (Task ticket : list) {
            if (areGolemTagsValidForTask(ticket) && ticket.canGolemPerformTask(golem) && golem.isWithinRestriction(ticket.getEntity().blockPosition())
                    && isValidDestination(golem.level(), ticket.getEntity().blockPosition()) && canEasilyReach(ticket.getEntity())) {
                golem.setTask(ticket);
                golem.getTask().setReserved(true);
                float hw = golem.getTask().getEntity().getBbWidth() / 2.0f;
                minDist = 3.5 + hw * hw;
                if (Config.SHOW_GOLEM_EMOTES.getAsBoolean()) {
                    golem.level().broadcastEntityEvent(golem, (byte) 5);
                }
                return true;
            }
        }
        return false;
    }

    private boolean canEasilyReach(Entity e) {
        if (golem.distanceToSqr(e) < minDist) {
            return true;
        }
        Path pathentity = golem.getNavigation().createPath(e, 0);
        if (pathentity == null) {
            return false;
        }
        Node pathpoint = pathentity.getEndNode();
        if (pathpoint == null) {
            return false;
        }
        int i = pathpoint.x - Mth.floor(e.getX());
        int j = pathpoint.z - Mth.floor(e.getZ());
        return i * i + j * j < minDist;
    }
}
