package thaumcraft.common.golems.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.seals.SealHandler;

/** Port of TaskHandler: the open golem tasks of each dimension. */
public class TaskHandler {
    static int TASK_LIMIT = 10000;
    public static ConcurrentHashMap<ResourceKey<Level>, ConcurrentHashMap<Integer, Task>> tasks = new ConcurrentHashMap<>();

    public static void addTask(ResourceKey<Level> dim, Task ticket) {
        ConcurrentHashMap<Integer, Task> dc = getTasks(dim);
        if (dc.size() > TASK_LIMIT) {
            try {
                Iterator<Task> i = dc.values().iterator();
                if (i.hasNext()) {
                    i.next();
                    i.remove();
                }
            } catch (Exception ex) {
            }
        }
        dc.put(ticket.getId(), ticket);
    }

    public static Task getTask(ResourceKey<Level> dim, int id) {
        return getTasks(dim).get(id);
    }

    public static ConcurrentHashMap<Integer, Task> getTasks(ResourceKey<Level> dim) {
        return tasks.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
    }

    /** Distance to the golem minus 256 per priority point, closest first. */
    private static double weight(Task ticket, Entity golem) {
        return ticket.getPos().getCenter().distanceToSqr(golem.getX(), golem.getY(), golem.getZ()) - ticket.getPriority() * 256;
    }

    private static void insertSorted(ArrayList<Task> out, Task ticket, Entity golem) {
        double d = weight(ticket, golem);
        for (int a = 0; a < out.size(); ++a) {
            if (d < weight(out.get(a), golem)) {
                out.add(a, ticket);
                return;
            }
        }
        out.add(ticket);
    }

    public static ArrayList<Task> getBlockTasksSorted(ResourceKey<Level> dim, UUID uuid, Entity golem) {
        ArrayList<Task> out = new ArrayList<>();
        for (Task ticket : getTasks(dim).values()) {
            if (ticket.isReserved() || ticket.getType() != 0) {
                continue;
            }
            if (uuid != null && ticket.getGolemUUID() != null && !uuid.equals(ticket.getGolemUUID())) {
                continue;
            }
            insertSorted(out, ticket, golem);
        }
        return out;
    }

    public static ArrayList<Task> getEntityTasksSorted(ResourceKey<Level> dim, UUID uuid, Entity golem) {
        ArrayList<Task> out = new ArrayList<>();
        for (Task ticket : getTasks(dim).values()) {
            if (ticket.isReserved() || ticket.getType() != 1) {
                continue;
            }
            if (uuid != null && ticket.getGolemUUID() != null && !uuid.equals(ticket.getGolemUUID())) {
                continue;
            }
            if (ticket.getEntity() == null || !ticket.getEntity().isAlive()) {
                ticket.setSuspended(true);
            } else {
                insertSorted(out, ticket, golem);
            }
        }
        return out;
    }

    public static void completeTask(Task task, EntityThaumcraftGolem golem) {
        if (task.isCompleted() || task.isSuspended()) {
            return;
        }
        ISealEntity se = SealHandler.getSealEntity(golem.level().dimension(), task.getSealPos());
        if (se != null) {
            task.setCompletion(se.getSeal().onTaskCompletion(golem.level(), golem, task));
        } else {
            task.setCompletion(true);
        }
    }

    /** Runs once a second: ages the tasks and drops the suspended or expired ones. */
    public static void clearSuspendedOrExpiredTasks(Level world) {
        ConcurrentHashMap<Integer, Task> tickets = getTasks(world.dimension());
        ConcurrentHashMap<Integer, Task> temp = new ConcurrentHashMap<>();
        for (Task ticket : tickets.values()) {
            if (!ticket.isSuspended() && ticket.getLifespan() > 0L) {
                ticket.setLifespan((short) (ticket.getLifespan() - 1L));
                temp.put(ticket.getId(), ticket);
            } else {
                ISealEntity sEnt = SealHandler.getSealEntity(world.dimension(), ticket.getSealPos());
                if (sEnt == null) {
                    continue;
                }
                sEnt.getSeal().onTaskSuspension(world, ticket);
            }
        }
        tasks.put(world.dimension(), temp);
    }
}
