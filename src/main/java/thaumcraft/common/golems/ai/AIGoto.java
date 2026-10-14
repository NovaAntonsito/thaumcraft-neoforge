package thaumcraft.common.golems.ai;

import java.util.Arrays;
import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Config;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.golems.tasks.TaskHandler;

/** Port of AIGoto: takes the best open task, walks to it and completes it when close enough. */
public abstract class AIGoto extends Goal {
    protected EntityThaumcraftGolem golem;
    protected int taskCounter = -1;
    protected byte type;
    protected int cooldown;
    protected double minDist = 4.0;
    private BlockPos prevRamble;
    protected BlockPos targetBlock;
    int pause = 0;

    public AIGoto(EntityThaumcraftGolem g, byte type) {
        golem = g;
        this.type = type;
        // setMutexBits(5): move and jump.
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            --cooldown;
            return false;
        }
        cooldown = 5;
        if (golem.getTask() != null && !golem.getTask().isSuspended()) {
            return false;
        }
        targetBlock = null;
        boolean start = findDestination();
        if (start && golem.getTask() != null && golem.getTask().getSealPos() != null) {
            ISealEntity se = GolemHelper.getSealEntity(golem.level().dimension(), golem.getTask().getSealPos());
            if (se != null) {
                se.getSeal().onTaskStarted(golem.level(), golem, golem.getTask());
            }
        }
        return start;
    }

    @Override
    public void start() {
        moveTo();
        taskCounter = 0;
    }

    protected abstract void moveTo();

    @Override
    public boolean canContinueToUse() {
        return taskCounter >= 0 && taskCounter <= 1000 && golem.getTask() != null && !golem.getTask().isSuspended()
                && isValidDestination(golem.level(), golem.getTask().getPos());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Task task = golem.getTask();
        if (task == null) {
            return;
        }
        if (pause-- <= 0) {
            double dist = task.getType() == 0 ? (targetBlock == null ? task.getPos() : targetBlock).getCenter().distanceToSqr(golem.position())
                    : golem.distanceToSqr(task.getEntity());
            if (dist > minDist) {
                task.setCompletion(false);
                ++taskCounter;
                if (taskCounter % 5 == 0) {
                    if (prevRamble != null && prevRamble.equals(golem.blockPosition())) {
                        Vec3 vec3 = DefaultRandomPos.getPosTowards(golem, 6, 4, Vec3.atLowerCornerOf(task.getPos()), Math.PI / 2);
                        if (vec3 != null) {
                            golem.getNavigation().moveTo(vec3.x + 0.5, vec3.y + 0.5, vec3.z + 0.5, golem.getGolemMoveSpeed());
                        }
                    } else {
                        moveTo();
                    }
                    prevRamble = golem.blockPosition();
                }
            } else {
                TaskHandler.completeTask(task, golem);
                if (golem.getTask() != null && golem.getTask().isCompleted()) {
                    if (taskCounter >= 0) {
                        taskCounter = 0;
                    }
                    pause = 0;
                } else {
                    pause = 10;
                    ++taskCounter;
                }
                --taskCounter;
            }
        }
    }

    @Override
    public void stop() {
        Task task = golem.getTask();
        if (task != null) {
            if (!task.isCompleted() && task.isReserved() && Config.SHOW_GOLEM_EMOTES.getAsBoolean()) {
                golem.level().broadcastEntityEvent(golem, (byte) 6);
            }
            if (task.isCompleted() && !task.isSuspended()) {
                task.setSuspended(true);
            }
            task.setReserved(false);
        }
    }

    protected abstract boolean findDestination();

    protected boolean isValidDestination(Level world, BlockPos pos) {
        return true;
    }

    protected boolean areGolemTagsValidForTask(Task ticket) {
        ISealEntity se = SealHandler.getSealEntity(golem.level().dimension(), ticket.getSealPos());
        if (se == null || se.getSeal() == null) {
            return true;
        }
        if (se.isLocked() && (golem.getOwnerUUID() == null || !golem.getOwnerUUID().toString().equals(se.getOwner()))) {
            return false;
        }
        if (se.getSeal().getRequiredTags() != null && !golem.getProperties().getTraits().containsAll(Arrays.asList(se.getSeal().getRequiredTags()))) {
            return false;
        }
        if (se.getSeal().getForbiddenTags() != null) {
            for (EnumGolemTrait tag : se.getSeal().getForbiddenTags()) {
                if (golem.getProperties().getTraits().contains(tag)) {
                    return false;
                }
            }
        }
        return true;
    }
}
