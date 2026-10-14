package thaumcraft.common.golems.seals;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.utils.InventoryUtils;

/** Port of SealFill (Store): golems put what they carry into the container the seal is on, or drop it there. */
public class SealFill extends SealFiltered {
    int delay;
    int watchedTask;
    protected ISealConfigToggles.SealToggle[] props;
    ResourceLocation icon;

    public SealFill() {
        delay = new Random(System.nanoTime()).nextInt(50);
        watchedTask = Integer.MIN_VALUE;
        props = new ISealConfigToggles.SealToggle[] {
                new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"),
                new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
                new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"),
                new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod"),
                new ISealConfigToggles.SealToggle(false, "pexist", "golem.prop.exist")};
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_fill");
    }

    @Override
    public String getKey() {
        return "thaumcraft:fill";
    }

    private ThaumcraftInvHelper.InvFilter invFilter() {
        return new ThaumcraftInvHelper.InvFilter(!props[0].value, !props[1].value, props[2].value, props[3].value);
    }

    @Override
    public void tickSeal(Level world, ISealEntity seal) {
        if (delay++ % 20 != 0) {
            return;
        }
        Task oldTask = TaskHandler.getTask(world.dimension(), watchedTask);
        if (oldTask == null || oldTask.isReserved() || oldTask.isSuspended() || oldTask.isCompleted()) {
            Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(world.dimension(), task);
            watchedTask = task.getId();
        }
    }

    @Override
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
        ISealEntity se = SealHandler.getSealEntity(world.dimension(), task.getSealPos());
        if (se != null && !se.isStoppedByRedstone(world)) {
            Task newTask = new Task(task.getSealPos(), task.getSealPos().pos);
            newTask.setPriority(se.getPriority());
            TaskHandler.addTask(world.dimension(), newTask);
            watchedTask = newTask.getId();
        }
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        ThaumcraftInvHelper.InvFilter filter = invFilter();
        Tuple<ItemStack, Integer> tuple = InventoryUtils.findFirstMatchFromFilterTuple(getInv(), getSizes(), isBlacklist(), golem.getCarrying(), filter);
        if (tuple.getA() != null && !tuple.getA().isEmpty()) {
            IItemHandler inv = ThaumcraftInvHelper.getItemHandlerAt(world, task.getSealPos().pos, task.getSealPos().face);
            int limit = tuple.getA().getCount();
            if (hasStacksizeLimiters() && tuple.getB() != null && tuple.getB() > 0) {
                int c = (inv == null)
                        ? InventoryUtils.countStackInWorld(golem.getGolemWorld(), task.getSealPos().pos, tuple.getA(), 1.5, filter)
                        : ThaumcraftInvHelper.countTotalItemsIn(inv, tuple.getA(), filter);
                limit = c < tuple.getB() ? tuple.getB() - c : 0;
            }
            if (limit > 0) {
                ItemStack t = tuple.getA().copy();
                t.setCount(limit);
                ItemStack s = golem.dropItem(t);
                if (inv == null) {
                    Direction face = task.getSealPos().face;
                    BlockPos p = task.getSealPos().pos;
                    ItemEntity ie = new ItemEntity(world, p.getX() + 0.5 + face.getStepX(), p.getY() + 0.5 + face.getStepY(),
                            p.getZ() + 0.5 + face.getStepZ(), s);
                    ie.setDeltaMovement(ie.getDeltaMovement().multiply(1 / 5.0, 1 / 2.0, 1 / 5.0));
                    world.addFreshEntity(ie);
                } else {
                    golem.holdItem(ItemHandlerHelper.insertItemStacked(inv, s, false));
                }
                ((Entity) golem).playSound(SoundEvents.ITEM_PICKUP, 0.125f, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7f + 1.0f) * 1.0f);
                golem.addRankXp(1);
                golem.swingArm();
            }
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ThaumcraftInvHelper.InvFilter filter = invFilter();
        Tuple<ItemStack, Integer> tuple = InventoryUtils.findFirstMatchFromFilterTuple(getInv(), getSizes(), isBlacklist(), golem.getCarrying(), filter);
        ItemStack first = tuple.getA();
        if (first != null && !first.isEmpty()) {
            Level world = golem.getGolemWorld();
            IItemHandler inv = ThaumcraftInvHelper.getItemHandlerAt(world, task.getSealPos().pos, task.getSealPos().face);
            if (inv != null) {
                if (props[4].value && ThaumcraftInvHelper.countTotalItemsIn(inv, first, filter) <= 0) {
                    return false;
                }
                if (ThaumcraftInvHelper.hasRoomForSome(world, task.getSealPos().pos, task.getSealPos().face, first)) {
                    if (!hasStacksizeLimiters() || tuple.getB() == null || tuple.getB() <= 0) {
                        return true;
                    }
                    if (ThaumcraftInvHelper.countTotalItemsIn(inv, first, filter) < tuple.getB()) {
                        return true;
                    }
                }
            } else {
                return !hasStacksizeLimiters() || tuple.getB() == null || tuple.getB() <= 0
                        || InventoryUtils.countStackInWorld(world, task.getSealPos().pos, first, 1.5, filter) < tuple.getB();
            }
        }
        return false;
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
        return new int[] {1, 0, 4};
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
    public void onTaskSuspension(Level world, Task task) {
    }

    @Override
    public void onRemoval(Level world, BlockPos pos, Direction side) {
    }

    @Override
    public boolean hasStacksizeLimiters() {
        return !isBlacklist();
    }
}
