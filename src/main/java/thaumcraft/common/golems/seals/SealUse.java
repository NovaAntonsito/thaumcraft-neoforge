package thaumcraft.common.golems.seals;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.GolemInteractionHelper;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.utils.InventoryUtils;

/** Port of SealUse: a golem clicks the block the seal is on with the item it carries (or an empty hand). */
public class SealUse extends SealFiltered implements ISealConfigToggles {
    int delay;
    int watchedTask;
    ResourceLocation icon;
    protected SealToggle[] props;

    public SealUse() {
        delay = new Random(System.nanoTime()).nextInt(49);
        watchedTask = Integer.MIN_VALUE;
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_use");
        props = new SealToggle[] {
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(true, "pnbt", "golem.prop.nbt"),
                new SealToggle(false, "pore", "golem.prop.ore"),
                new SealToggle(false, "pmod", "golem.prop.mod"),
                new SealToggle(false, "pleft", "golem.prop.left"),
                new SealToggle(false, "pempty", "golem.prop.empty"),
                new SealToggle(false, "pemptyhand", "golem.prop.emptyhand"),
                new SealToggle(false, "psneak", "golem.prop.sneak"),
                new SealToggle(false, "ppro", "golem.prop.provision.wl")};
    }

    @Override
    public String getKey() {
        return "thaumcraft:use";
    }

    private ThaumcraftInvHelper.InvFilter invFilter() {
        return new ThaumcraftInvHelper.InvFilter(!props[0].value, !props[1].value, props[2].value, props[3].value);
    }

    @Override
    public void tickSeal(Level world, ISealEntity seal) {
        if (delay++ % 5 != 0) {
            return;
        }
        Task oldTask = TaskHandler.getTask(world.dimension(), watchedTask);
        if (oldTask == null || oldTask.isSuspended() || oldTask.isCompleted()) {
            if (getToggles()[5].value != world.isEmptyBlock(seal.getSealPos().pos)) {
                return;
            }
            Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(world.dimension(), task);
            watchedTask = task.getId();
        }
    }

    @Override
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        if (getToggles()[5].value == world.isEmptyBlock(task.getPos())) {
            ItemStack clickStack = golem.getCarrying().get(0);
            if (!filter.get(0).isEmpty()) {
                clickStack = InventoryUtils.findFirstMatchFromFilter(filter, filterSize, blacklist, golem.getCarrying(), invFilter());
            }
            if (!clickStack.isEmpty() || props[6].value) {
                ItemStack ss = ItemStack.EMPTY;
                if (!clickStack.isEmpty()) {
                    ss = clickStack.copy();
                    golem.dropItem(clickStack.copy());
                }
                GolemInteractionHelper.golemClick(world, golem, task.getPos(), task.getSealPos().face, props[6].value ? ItemStack.EMPTY : ss, props[7].value, !getToggles()[4].value);
            }
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (!props[6].value) {
            boolean found = !InventoryUtils.findFirstMatchFromFilter(filter, filterSize, blacklist, golem.getCarrying(), invFilter()).isEmpty();
            // 1.12 also set the requested stack's damage to the wildcard when "use metadata" was off; there is no
            // damage wildcard in 1.21. An empty filter slot requests nothing.
            if (!found && getToggles()[8].value && !blacklist && !getInv().get(0).isEmpty()) {
                ISealEntity se = SealHandler.getSealEntity(golem.getGolemWorld().dimension(), task.getSealPos());
                if (se != null) {
                    GolemHelper.requestProvisioning(golem.getGolemWorld(), se, getInv().get(0).copy());
                }
            }
            return found;
        }
        return true;
    }

    @Override
    public void onTaskSuspension(Level world, Task task) {
    }

    @Override
    public boolean canPlaceAt(Level world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public ResourceLocation getSealIcon() {
        return icon;
    }

    @Override
    public void onRemoval(Level world, BlockPos pos, Direction side) {
    }

    @Override
    public Object returnContainer(Level world, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return SealBaseContainer.provider(seal);
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {1, 3, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.DEFT, EnumGolemTrait.SMART};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int indx, boolean value) {
        props[indx].setValue(value);
    }
}
