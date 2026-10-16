package thaumcraft.common.golems.seals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.utils.InventoryUtils;

/** Port of SealEmpty: golems take items out of the inventory the seal is on and carry them off. */
public class SealEmpty extends SealFiltered {
    int delay;
    int filterInc;
    HashMap<Integer, ItemStack> cache;
    ResourceLocation icon;
    protected ISealConfigToggles.SealToggle[] props;

    public SealEmpty() {
        delay = new Random(System.nanoTime()).nextInt(30);
        filterInc = 0;
        cache = new HashMap<>();
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_empty");
        props = new ISealConfigToggles.SealToggle[] {
                new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"),
                new ISealConfigToggles.SealToggle(true, "pnbt", "golem.prop.nbt"),
                new ISealConfigToggles.SealToggle(false, "pore", "golem.prop.ore"),
                new ISealConfigToggles.SealToggle(false, "pmod", "golem.prop.mod"),
                new ISealConfigToggles.SealToggle(false, "pcycle", "golem.prop.cycle"),
                new ISealConfigToggles.SealToggle(false, "pleave", "golem.prop.leave")};
    }

    @Override
    public String getKey() {
        return "thaumcraft:empty";
    }

    private ThaumcraftInvHelper.InvFilter invFilter() {
        return new ThaumcraftInvHelper.InvFilter(!props[0].value, !props[1].value, props[2].value, props[3].value);
    }

    @Override
    public void tickSeal(Level world, ISealEntity seal) {
        if (delay % 100 == 0) {
            Iterator<Integer> it = cache.keySet().iterator();
            while (it.hasNext()) {
                Task t = TaskHandler.getTask(world.dimension(), it.next());
                if (t == null) {
                    it.remove();
                }
            }
        }
        if (delay++ % 20 != 0) {
            return;
        }
        IItemHandler inv = ThaumcraftInvHelper.getItemHandlerAt(world, seal.getSealPos().pos, seal.getSealPos().face);
        if (inv == null) {
            return;
        }
        ItemStack stack = InventoryUtils.findFirstMatchFromFilter(getInv(filterInc), isBlacklist(), inv, seal.getSealPos().face, invFilter(), props[5].value);
        if (stack != null && !stack.isEmpty()) {
            Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
            task.setPriority(seal.getPriority());
            task.setLifespan((short) 5);
            TaskHandler.addTask(world.dimension(), task);
            cache.put(task.getId(), stack);
        }
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        ItemStack stack = cache.get(task.getId());
        int sa = stack == null ? 0 : ThaumcraftInvHelper.countTotalItemsIn(world, task.getSealPos().pos, task.getSealPos().face, stack, invFilter());
        if (stack != null && !stack.isEmpty() && props[5].value && sa <= stack.getCount()) {
            stack = stack.copy();
            stack.setCount(sa - 1);
        }
        if (stack != null && !stack.isEmpty()) {
            int limit = golem.canCarryAmount(stack);
            if (limit > 0) {
                ItemStack s = golem.holdItem(InventoryUtils.removeStackFrom(world, task.getSealPos().pos, task.getSealPos().face,
                        InventoryUtils.copyLimitedStack(stack, limit), invFilter(), false));
                if (!s.isEmpty()) {
                    InventoryUtils.ejectStackAt(world, task.getSealPos().pos.relative(task.getSealPos().face), task.getSealPos().face.getOpposite(), s);
                }
                golem.getGolemEntity().playSound(SoundEvents.ITEM_PICKUP, 0.125f, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7f + 1.0f) * 2.0f);
                golem.swingArm();
            }
        }
        cache.remove(task.getId());
        ++filterInc;
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ItemStack stack = cache.get(task.getId());
        return stack != null && !stack.isEmpty() && golem.canCarry(stack, true);
    }

    @Override
    public boolean canPlaceAt(Level world, BlockPos pos, Direction side) {
        IItemHandler inv = ThaumcraftInvHelper.getItemHandlerAt(world, pos, side);
        return inv != null;
    }

    public NonNullList<ItemStack> getInv(int c) {
        return super.getInv();
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
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
    }

    @Override
    public void onTaskSuspension(Level world, Task task) {
        cache.remove(task.getId());
    }

    @Override
    public void onRemoval(Level world, BlockPos pos, Direction side) {
    }
}
