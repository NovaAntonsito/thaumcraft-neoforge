package thaumcraft.common.golems.seals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.common.aspects.ObjectAspects;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.GolemInteractionHelper;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.CropUtils;

/** Port of SealHarvest: golems harvest the grown crops in the area and replant them. */
public class SealHarvest implements ISeal, ISealGui, ISealConfigArea, ISealConfigToggles {
    int delay;
    int count;
    HashMap<Long, ReplantInfo> replantTasks;
    ResourceLocation icon;
    protected SealToggle[] props;

    public SealHarvest() {
        delay = new Random(System.nanoTime()).nextInt(33);
        count = 0;
        replantTasks = new HashMap<>();
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_harvest");
        props = new SealToggle[] {new SealToggle(true, "prep", "golem.prop.replant"), new SealToggle(false, "ppro", "golem.prop.provision")};
    }

    @Override
    public String getKey() {
        return "thaumcraft:harvest";
    }

    @Override
    public void tickSeal(Level world, ISealEntity seal) {
        if (delay % 100 == 0) {
            AABB area = GolemHelper.getBoundsForArea(seal);
            Iterator<Long> rt = replantTasks.keySet().iterator();
            while (rt.hasNext()) {
                BlockPos pp = BlockPos.of(rt.next());
                if (!area.contains(new Vec3(pp.getX() + 0.5, pp.getY() + 0.5, pp.getZ() + 0.5))) {
                    // 1.12 also tried to suspend the replant task here, but looked it up with the iterator as the key,
                    // so it never found one
                    rt.remove();
                }
            }
        }
        if (delay++ % 5 != 0) {
            return;
        }
        BlockPos p = GolemHelper.getPosInArea(seal, count++);
        if (CropUtils.isGrownCrop(world, p)) {
            Task task = new Task(seal.getSealPos(), p);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(world.dimension(), task);
        } else if (getToggles()[0].value && replantTasks.containsKey(p.asLong()) && world.isEmptyBlock(p)) {
            Task t = TaskHandler.getTask(world.dimension(), replantTasks.get(p.asLong()).taskid);
            if (t == null) {
                Task tt2 = new Task(seal.getSealPos(), replantTasks.get(p.asLong()).pos);
                tt2.setPriority(seal.getPriority());
                TaskHandler.addTask(world.dimension(), tt2);
                replantTasks.get(p.asLong()).taskid = tt2.getId();
            }
        }
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        if (CropUtils.isGrownCrop(world, task.getPos())) {
            FakePlayer fp = GolemInteractionHelper.getFakePlayer(world, golem);
            Direction face = golem.getGolemEntity().getDirection().getOpposite();
            BlockState bs = world.getBlockState(task.getPos());
            if (CropUtils.clickableCrops.contains(bs)) {
                bs.useWithoutItem(world, fp, new BlockHitResult(Vec3.atLowerCornerOf(task.getPos()), face, task.getPos(), false));
                golem.addRankXp(1);
                golem.swingArm();
            } else {
                GolemInteractionHelper.golemClick(world, golem, task.getPos(), task.getSealPos().face, ItemStack.EMPTY, false, true);
                if (CropUtils.isGrownCrop(world, task.getPos())) {
                    BlockUtils.harvestBlock(world, fp, task.getPos(), true, false, 0, true);
                    golem.addRankXp(1);
                    golem.swingArm();
                    if (getToggles()[0].value) {
                        ItemStack seed = ThaumcraftApi.getSeed(bs.getBlock());
                        if (seed != null && !seed.isEmpty()) {
                            BlockState bb = world.getBlockState(task.getPos().below());
                            Direction rf = null;
                            if (seed.getItem() instanceof BlockItem bi && isPlantable(bi.getBlock()) && bi.getBlock().defaultBlockState().canSurvive(world, task.getPos())) {
                                rf = Direction.DOWN;
                            } else if (!(seed.getItem() instanceof BlockItem bi2 && isPlantable(bi2.getBlock())) && bs.hasProperty(BlockStateProperties.FACING)) {
                                rf = bs.getValue(BlockStateProperties.FACING);
                            }
                            if (rf != null) {
                                Task tt = new Task(task.getSealPos(), task.getPos());
                                tt.setPriority(task.getPriority());
                                tt.setLifespan((short) 300);
                                replantTasks.put(tt.getPos().asLong(), new ReplantInfo(tt.getPos(), rf, tt.getId(), seed.copy(), bb.getBlock() instanceof FarmBlock));
                                TaskHandler.addTask(world.dimension(), tt);
                            }
                        }
                    }
                }
            }
        } else if (replantTasks.containsKey(task.getPos().asLong()) && replantTasks.get(task.getPos().asLong()).taskid == task.getId()
                && world.isEmptyBlock(task.getPos()) && golem.isCarrying(replantTasks.get(task.getPos().asLong()).stack)) {
            FakePlayer fp = GolemInteractionHelper.getFakePlayer(world, golem);
            BlockState bb2 = world.getBlockState(task.getPos().below());
            ReplantInfo ri = replantTasks.get(task.getPos().asLong());
            if ((bb2.is(net.minecraft.world.level.block.Blocks.DIRT) || bb2.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) && ri.farmland) {
                fp.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
                Items.DIAMOND_HOE.useOn(new UseOnContext(fp, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(task.getPos().below()), Direction.UP, task.getPos().below(), false)));
            }
            ItemStack seed = ri.stack.copy();
            seed.setCount(1);
            fp.setItemInHand(InteractionHand.MAIN_HAND, seed.copy());
            BlockPos at = task.getPos().relative(ri.face);
            boolean placed = fp.getMainHandItem().useOn(new UseOnContext(fp, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(at), ri.face.getOpposite(), at, false))).consumesAction();
            fp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            if (placed) {
                world.levelEvent(2001, task.getPos(), Block.getId(world.getBlockState(task.getPos())));
                golem.dropItem(seed);
                golem.addRankXp(1);
                golem.swingArm();
            }
        }
        task.setSuspended(true);
        return true;
    }

    /** 1.12 IPlantable: the plants that go on top of a soil block (not cocoa, which hangs on a log). */
    private static boolean isPlantable(Block block) {
        return block instanceof net.minecraft.world.level.block.BushBlock;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (replantTasks.containsKey(task.getPos().asLong()) && replantTasks.get(task.getPos().asLong()).taskid == task.getId()) {
            boolean carry = golem.isCarrying(replantTasks.get(task.getPos().asLong()).stack);
            if (!carry && getToggles()[1].value) {
                ISealEntity se = SealHandler.getSealEntity(golem.getGolemWorld().dimension(), task.getSealPos());
                if (se != null) {
                    GolemHelper.requestProvisioning(golem.getGolemWorld(), se, replantTasks.get(task.getPos().asLong()).stack);
                }
            }
            return carry;
        }
        return true;
    }

    @Override
    public void onTaskSuspension(Level world, Task task) {
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
        ListTag nbttaglist = nbt.getList("replant", Tag.TAG_COMPOUND);
        for (int i = 0; i < nbttaglist.size(); ++i) {
            CompoundTag nbttagcompound1 = nbttaglist.getCompound(i);
            long loc = nbttagcompound1.getLong("taskloc");
            byte face = nbttagcompound1.getByte("taskface");
            boolean farmland = nbttagcompound1.getBoolean("farmland");
            ItemStack stack = ItemStack.parseOptional(ObjectAspects.registries(), nbttagcompound1);
            replantTasks.put(loc, new ReplantInfo(BlockPos.of(loc), Direction.values()[face], 0, stack, farmland));
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
        if (getToggles()[0].value) {
            ListTag nbttaglist = new ListTag();
            for (Long key : replantTasks.keySet()) {
                ReplantInfo info = replantTasks.get(key);
                if (info.stack.isEmpty()) {
                    continue;
                }
                CompoundTag nbttagcompound1 = new CompoundTag();
                nbttagcompound1.putLong("taskloc", info.pos.asLong());
                nbttagcompound1.putByte("taskface", (byte) info.face.ordinal());
                nbttagcompound1.putBoolean("farmland", info.farmland);
                info.stack.save(ObjectAspects.registries(), nbttagcompound1);
                nbttaglist.add(nbttagcompound1);
            }
            nbt.put("replant", nbttaglist);
        }
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
        return new int[] {2, 3, 0, 4};
    }

    @Override
    public SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int indx, boolean value) {
        props[indx].setValue(value);
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
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
    }

    private static class ReplantInfo {
        Direction face;
        BlockPos pos;
        int taskid;
        ItemStack stack;
        boolean farmland;

        public ReplantInfo(BlockPos pos, Direction face, int taskid, ItemStack stack, boolean farmland) {
            this.pos = pos;
            this.face = face;
            this.taskid = taskid;
            this.stack = stack;
            this.farmland = farmland;
        }
    }
}
