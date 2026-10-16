package thaumcraft.common.golems.seals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.GolemInteractionHelper;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;

/** Port of SealLumber: golems chop down the trees in the area, one log at a time from the top. */
public class SealLumber implements ISeal, ISealGui, ISealConfigArea {
    int delay;
    HashMap<Integer, Long> cache;
    ResourceLocation icon;

    public SealLumber() {
        delay = new Random(System.nanoTime()).nextInt(33);
        cache = new HashMap<>();
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_lumber");
    }

    @Override
    public String getKey() {
        return "thaumcraft:lumber";
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
        ++delay;
        BlockPos p = GolemHelper.getPosInArea(seal, delay);
        if (!cache.containsValue(p.asLong()) && Utils.isWoodLog(world, p)) {
            Task task = new Task(seal.getSealPos(), p);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(world.dimension(), task);
            cache.put(task.getId(), p.asLong());
        }
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && Utils.isWoodLog(world, task.getPos())) {
            FakePlayer fp = GolemInteractionHelper.getFakePlayer(world, golem);
            BlockState bs = world.getBlockState(task.getPos());
            golem.swingArm();
            if (BlockUtils.breakFurthestBlock(world, task.getPos(), bs, fp)) {
                task.setLifespan((short) Math.max(task.getLifespan(), 10L));
                golem.addRankXp(1);
                return false;
            }
            cache.remove(task.getId());
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && Utils.isWoodLog(golem.getGolemWorld(), task.getPos())) {
            return true;
        }
        task.setSuspended(true);
        return false;
    }

    @Override
    public void onTaskSuspension(Level world, Task task) {
        cache.remove(task.getId());
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
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
    public void onRemoval(Level world, BlockPos pos, Direction side) {
    }

    @Override
    public Object returnContainer(Level world, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return SealBaseContainer.provider(seal);
    }

    /** Not used: the screen (SealBaseGUI) comes from the menu type registered in ThaumcraftClient. */
    @Override
    public Object returnGui(Level world, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {2, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.BREAKER, EnumGolemTrait.SMART};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
    }
}
