package thaumcraft.common.golems.seals;

import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.tasks.TaskHandler;

/** Port of SealGuard: fighting golems attack the mobs (and optionally animals or players) in the area. */
public class SealGuard implements ISeal, ISealGui, ISealConfigArea {
    int delay;
    protected ISealConfigToggles.SealToggle[] props;
    ResourceLocation icon;

    public SealGuard() {
        delay = new Random(System.nanoTime()).nextInt(22);
        props = new ISealConfigToggles.SealToggle[] {
                new ISealConfigToggles.SealToggle(true, "pmob", "golem.prop.mob"),
                new ISealConfigToggles.SealToggle(false, "panimal", "golem.prop.animal"),
                new ISealConfigToggles.SealToggle(false, "pplayer", "golem.prop.player")};
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_guard");
    }

    @Override
    public String getKey() {
        return "thaumcraft:guard";
    }

    /**
     * 1.12 IAnimals: the marker of animals, ambient and water creatures, golems, and (through IMob) the monsters too.
     */
    static boolean isIAnimals(LivingEntity target) {
        return target instanceof Animal || target instanceof AmbientCreature || target instanceof WaterAnimal
                || target instanceof AbstractGolem || target instanceof Enemy;
    }

    @Override
    public void tickSeal(Level world, ISealEntity seal) {
        if (delay++ % 20 != 0) {
            return;
        }
        AABB area = GolemHelper.getBoundsForArea(seal);
        List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : list) {
            if (isValidTarget(target)) {
                Task task = new Task(seal.getSealPos(), target);
                task.setPriority(seal.getPriority());
                task.setLifespan((short) 10);
                TaskHandler.addTask(world.dimension(), task);
            }
        }
    }

    private boolean isValidTarget(LivingEntity target) {
        boolean valid = false;
        if (props[0].value && (target instanceof Enemy || target instanceof Monster)) {
            valid = true;
        }
        if (props[1].value && (target instanceof Animal || isIAnimals(target))) {
            valid = true;
        }
        if (props[2].value && ServerLifecycleHooks.getCurrentServer() != null && ServerLifecycleHooks.getCurrentServer().isPvpAllowed() && target instanceof Player) {
            valid = true;
        }
        return valid;
    }

    @Override
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
        if (task.getEntity() instanceof LivingEntity target && isValidTarget(target) && golem.getGolemEntity() instanceof Mob mob) {
            mob.setTarget(target);
            golem.addRankXp(1);
        }
        task.setSuspended(true);
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return !golem.getGolemEntity().isAlliedTo(task.getEntity());
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
        return new EnumGolemTrait[] {EnumGolemTrait.FIGHTER};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskSuspension(Level world, Task task) {
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
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
}
