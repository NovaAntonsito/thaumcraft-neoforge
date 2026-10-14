package thaumcraft.common.golems.seals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thaumcraft.Config;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.utils.InventoryUtils;

/** Port of SealPickup (Collect): golems pick up the dropped items in the seal's area. */
public class SealPickup extends SealFiltered implements ISealConfigArea {
    int delay;
    HashMap<Integer, Integer> itemEntities;
    ResourceLocation icon;
    protected ISealConfigToggles.SealToggle[] props;

    public SealPickup() {
        delay = new Random(System.nanoTime()).nextInt(100);
        itemEntities = new HashMap<>();
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_pickup");
        props = new ISealConfigToggles.SealToggle[] {
                new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"),
                new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
                new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"),
                new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod")};
    }

    @Override
    public String getKey() {
        return "thaumcraft:pickup";
    }

    private ThaumcraftInvHelper.InvFilter invFilter() {
        return new ThaumcraftInvHelper.InvFilter(!props[0].value, !props[1].value, props[2].value, props[3].value);
    }

    @Override
    public void tickSeal(Level world, ISealEntity seal) {
        if (delay++ % 5 != 0) {
            return;
        }
        AABB area = GolemHelper.getBoundsForArea(seal);
        List<ItemEntity> list = world.getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity ent : list) {
            if (ent != null && ent.onGround() && !ent.hasPickUpDelay() && !ent.getItem().isEmpty() && !itemEntities.containsValue(ent.getId())) {
                ItemStack stack = InventoryUtils.findFirstMatchFromFilter(filter, filterSize, isBlacklist(), NonNullList.withSize(1, ent.getItem()), invFilter());
                if (stack != null && !stack.isEmpty()) {
                    Task task = new Task(seal.getSealPos(), ent);
                    task.setPriority(seal.getPriority());
                    itemEntities.put(task.getId(), ent.getId());
                    TaskHandler.addTask(world.dimension(), task);
                    break;
                }
            }
        }
        if (delay % 100 != 0) {
            Iterator<Integer> it = itemEntities.values().iterator();
            while (it.hasNext()) {
                Entity e2 = world.getEntity(it.next());
                if (e2 != null && e2.isAlive()) {
                    continue;
                }
                try {
                    it.remove();
                } catch (Exception ex) {
                }
            }
        }
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        ItemEntity ei = getItemEntity(world, task);
        if (ei != null && !ei.getItem().isEmpty()) {
            ItemStack stack = InventoryUtils.findFirstMatchFromFilter(filter, filterSize, isBlacklist(), NonNullList.withSize(1, ei.getItem()), invFilter());
            if (stack != null && !stack.isEmpty()) {
                ItemStack is = golem.holdItem(ei.getItem());
                if (is != null && !is.isEmpty() && is.getCount() > 0) {
                    ei.setItem(is);
                }
                if (is == null || is.isEmpty() || is.getCount() <= 0) {
                    ei.discard();
                }
                ((Entity) golem).playSound(SoundEvents.ITEM_PICKUP, 0.125f, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7f + 1.0f) * 2.0f);
                golem.swingArm();
            }
        }
        task.setSuspended(true);
        itemEntities.remove(task.getId());
        ArrayList<Task> localTasks = TaskHandler.getEntityTasksSorted(world.dimension(), null, (Entity) golem);
        for (Task ticket : localTasks) {
            if (itemEntities.containsKey(ticket.getId()) && ticket.canGolemPerformTask(golem)
                    && ((EntityThaumcraftGolem) golem).isWithinRestriction(ticket.getEntity().blockPosition())) {
                ((EntityThaumcraftGolem) golem).setTask(ticket);
                ((EntityThaumcraftGolem) golem).getTask().setReserved(true);
                if (Config.SHOW_GOLEM_EMOTES.getAsBoolean()) {
                    world.broadcastEntityEvent((Entity) golem, (byte) 5);
                }
                break;
            }
        }
        return true;
    }

    protected ItemEntity getItemEntity(Level world, Task task) {
        Integer ei = itemEntities.get(task.getId());
        if (ei != null) {
            Entity ent = world.getEntity(ei);
            if (ent instanceof ItemEntity item) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ItemEntity ei = getItemEntity(golem.getGolemWorld(), task);
        if (ei == null) {
            return false;
        }
        if (!ei.isAlive()) {
            task.setSuspended(true);
            return false;
        }
        return golem.canCarry(ei.getItem(), true);
    }

    @Override
    public boolean canPlaceAt(Level world, BlockPos pos, Direction side) {
        return !world.isEmptyBlock(pos);
    }

    @Override
    public ResourceLocation getSealIcon() {
        return icon;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {2, 1, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return null;
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.CLUMSY};
    }

    @Override
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
    }

    @Override
    public void onTaskSuspension(Level world, Task task) {
    }

    @Override
    public void onRemoval(Level world, BlockPos pos, Direction side) {
    }
}
