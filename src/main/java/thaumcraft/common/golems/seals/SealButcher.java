package thaumcraft.common.golems.seals;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.tasks.TaskHandler;

/** Port of SealButcher: golems kill adult animals in the area while there are at least 3 of that kind. */
public class SealButcher implements ISeal, ISealGui, ISealConfigArea {
    int delay;
    boolean wait;
    ResourceLocation icon;

    public SealButcher() {
        delay = new Random(System.nanoTime()).nextInt(200);
        wait = false;
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_butcher");
    }

    @Override
    public String getKey() {
        return "thaumcraft:butcher";
    }

    @Override
    public void tickSeal(Level world, ISealEntity seal) {
        if (delay++ % 200 != 0 || wait) {
            return;
        }
        AABB area = GolemHelper.getBoundsForArea(seal);
        List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : list) {
            if (isValidTarget(target)) {
                // kept from 1.12: this counts up to 3 entities of the same class, valid or not (babies included)
                List<? extends LivingEntity> var55 = world.getEntitiesOfClass(target.getClass(), area);
                Iterator<? extends LivingEntity> var56 = var55.iterator();
                int count;
                for (count = 0; var56.hasNext() && count < 3; ++count) {
                    var56.next();
                }
                if (count > 2) {
                    Task task = new Task(seal.getSealPos(), target);
                    task.setPriority(seal.getPriority());
                    task.setLifespan((short) 10);
                    TaskHandler.addTask(world.dimension(), task);
                    wait = true;
                    break;
                }
            }
        }
    }

    private boolean isValidTarget(LivingEntity target) {
        return SealGuard.isIAnimals(target) && !(target instanceof Enemy) && (!(target instanceof TamableAnimal t) || !t.isTame())
                && !(target instanceof AbstractGolem) && (!(target instanceof Animal) || !target.isBaby());
    }

    @Override
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
        if (task.getEntity() instanceof LivingEntity target && isValidTarget(target) && golem.getGolemEntity() instanceof Mob mob) {
            mob.setTarget(target);
            golem.addRankXp(1);
        }
        task.setSuspended(true);
        wait = false;
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        task.setSuspended(true);
        wait = false;
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return true;
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
        return new int[] {2, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.FIGHTER, EnumGolemTrait.SMART};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskSuspension(Level world, Task task) {
        wait = false;
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
    }

    @Override
    public void onRemoval(Level world, BlockPos pos, Direction side) {
        wait = false;
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
}
