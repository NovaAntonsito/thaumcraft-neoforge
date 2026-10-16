package thaumcraft.common.golems.seals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.GolemInteractionHelper;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.utils.BlockUtils;

/** Port of SealBreaker: golems break the blocks in the area that pass the filter. */
public class SealBreaker extends SealFiltered implements ISealConfigArea, ISealConfigToggles {
    int delay;
    HashMap<Integer, Long> cache;
    ResourceLocation icon;
    protected SealToggle[] props;

    public SealBreaker() {
        delay = new Random(System.nanoTime()).nextInt(42);
        cache = new HashMap<>();
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_breaker");
        props = new SealToggle[] {new SealToggle(true, "pmeta", "golem.prop.meta")};
    }

    @Override
    public String getKey() {
        return "thaumcraft:breaker";
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
        if (!cache.containsValue(p.asLong()) && isValidBlock(world, p)) {
            Task task = new Task(seal.getSealPos(), p);
            task.setPriority(seal.getPriority());
            task.setData((int) (world.getBlockState(p).getDestroySpeed(world, p) * 10.0f));
            TaskHandler.addTask(world.dimension(), task);
            cache.put(task.getId(), p.asLong());
        }
    }

    /**
     * Kept from 1.12: every filter stack must match for a whitelist (so a whitelist with two different blocks lets
     * nothing through), and any match rejects for a blacklist. The "use metadata" toggle has nothing left to compare
     * now that block states are flattened into separate items.
     */
    private boolean isValidBlock(Level world, BlockPos p) {
        BlockState bs = world.getBlockState(p);
        if (!world.isEmptyBlock(p) && bs.getDestroySpeed(world, p) >= 0.0f) {
            for (ItemStack ts : getInv()) {
                if (ts != null && !ts.isEmpty()) {
                    ItemStack fs = BlockUtils.getSilkTouchDrop(bs);
                    boolean match = !fs.isEmpty() && ItemStack.isSameItem(fs, ts);
                    if (isBlacklist()) {
                        if (match) {
                            return false;
                        }
                    } else if (!match) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        BlockState bs = world.getBlockState(task.getPos());
        if (cache.containsKey(task.getId()) && isValidBlock(world, task.getPos())) {
            FakePlayer fp = GolemInteractionHelper.getFakePlayer(world, golem);
            golem.swingArm();
            boolean silky = getToggles().length > 1 && getToggles()[1].value;
            int bspd = silky ? 7 : 21;
            if (task.getData() > bspd) {
                float bh = bs.getDestroySpeed(world, task.getPos()) * 10.0f;
                task.setLifespan((short) Math.max(task.getLifespan(), 10L));
                task.setData(task.getData() - bspd);
                int progress = (int) (9.0f * (1.0f - task.getData() / bh));
                SoundType st = bs.getSoundType(world, task.getPos(), golem.getGolemEntity());
                world.playSound(null, task.getPos(), st.getBreakSound(), SoundSource.BLOCKS, (st.getVolume() + 0.7f) / 8.0f, st.getPitch() * 0.5f);
                BlockUtils.destroyBlockPartially(world, golem.getGolemEntity().getId(), task.getPos(), progress);
                return false;
            }
            BlockUtils.destroyBlockPartially(world, golem.getGolemEntity().getId(), task.getPos(), 10);
            BlockUtils.harvestBlock(world, fp, task.getPos(), true, silky, 0, true);
            golem.addRankXp(1);
            cache.remove(task.getId());
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && isValidBlock(golem.getGolemWorld(), task.getPos())) {
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

    @Override
    public int[] getGuiCategories() {
        return new int[] {2, 1, 3, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.BREAKER};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
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
