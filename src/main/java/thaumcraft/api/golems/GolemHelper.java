package thaumcraft.api.golems;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.golems.tasks.TaskHandler;

/**
 * Port of GolemHelper. The original went through ThaumcraftApi.internalMethods; here it calls the handlers directly.
 * Dimensions are level keys instead of 1.12 dimension ids.
 */
public class GolemHelper {
    /**
     * Make sure to register your seals during common setup.
     */
    public static void registerSeal(ISeal seal) {
        SealHandler.registerSeal(seal);
    }

    public static ISeal getSeal(String key) {
        return SealHandler.getSeal(key);
    }

    public static ItemStack getSealStack(String key) {
        return thaumcraft.common.golems.seals.ItemSealPlacer.getSealStack(key);
    }

    public static ISealEntity getSealEntity(ResourceKey<Level> dim, thaumcraft.api.golems.seals.SealPos pos) {
        return SealHandler.getSealEntity(dim, pos);
    }

    public static void addGolemTask(ResourceKey<Level> dim, Task task) {
        TaskHandler.addTask(dim, task);
    }

    public static HashMap<ResourceKey<Level>, ArrayList<ProvisionRequest>> provisionRequests = new HashMap<>();
    static int LISTLIMIT = 1000;

    private static void addRequest(Level world, ProvisionRequest pr) {
        ArrayList<ProvisionRequest> list = provisionRequests.computeIfAbsent(world.dimension(), k -> new ArrayList<>());
        if (!list.contains(pr)) {
            list.add(pr);
        }
        if (list.size() > LISTLIMIT) list.remove(0);
    }

    /**
     * @param stack the stack requested. Can accept wildcard values.
     */
    public static void requestProvisioning(Level world, ISealEntity seal, ItemStack stack) {
        addRequest(world, new ProvisionRequest(seal, stack.copy()));
    }

    /**
     * @param stack the stack requested. Can accept wildcard values.
     */
    public static void requestProvisioning(Level world, BlockPos pos, Direction side, ItemStack stack) {
        addRequest(world, new ProvisionRequest(pos, side, stack.copy()));
    }

    /**
     * @param stack the stack requested. Can accept wildcard values.
     */
    public static void requestProvisioning(Level world, Entity entity, ItemStack stack) {
        addRequest(world, new ProvisionRequest(entity, stack.copy()));
    }

    /**
     * @param stack the stack requested. Can accept wildcard values.
     * @param ui a unique number to make the request slightly more unique in case you want to add multiple similar requests
     */
    public static void requestProvisioning(Level world, BlockPos pos, Direction side, ItemStack stack, int ui) {
        ProvisionRequest pr = new ProvisionRequest(pos, side, stack.copy());
        pr.setUI(ui);
        addRequest(world, pr);
    }

    /**
     * @param stack the stack requested. Can accept wildcard values.
     * @param ui a unique number to make the request slightly more unique in case you want to add multiple similar requests
     */
    public static void requestProvisioning(Level world, Entity entity, ItemStack stack, int ui) {
        ProvisionRequest pr = new ProvisionRequest(entity, stack.copy());
        pr.setUI(ui);
        addRequest(world, pr);
    }

    /**
     * This method is used to get a single blockpos from within a designated seal area.
     * This method is best used if you want to increment through the blocks in the area.
     * @param count a value used to derive a specific pos
     */
    public static BlockPos getPosInArea(ISealEntity seal, int count) {
        Direction face = seal.getSealPos().face;
        int xx = 1 + (seal.getArea().getX() - 1) * (face.getStepX() == 0 ? 2 : 1);
        int yy = 1 + (seal.getArea().getY() - 1) * (face.getStepY() == 0 ? 2 : 1);
        int zz = 1 + (seal.getArea().getZ() - 1) * (face.getStepZ() == 0 ? 2 : 1);
        int qx = face.getStepX() != 0 ? face.getStepX() : 1;
        int qy = face.getStepY() != 0 ? face.getStepY() : 1;
        int qz = face.getStepZ() != 0 ? face.getStepZ() : 1;
        int y = qy * ((count / zz) / xx) % yy + face.getStepY();
        int x = qx * (count / zz) % xx + face.getStepX();
        int z = qz * count % zz + face.getStepZ();
        return seal.getSealPos().pos.offset(
                x - (face.getStepX() == 0 ? xx / 2 : 0),
                y - (face.getStepY() == 0 ? yy / 2 : 0),
                z - (face.getStepZ() == 0 ? zz / 2 : 0));
    }

    /**
     * Returns the designated seal area as an AABB
     */
    public static AABB getBoundsForArea(ISealEntity seal) {
        BlockPos p = seal.getSealPos().pos;
        Direction face = seal.getSealPos().face;
        return new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1)
                .move(face.getStepX(), face.getStepY(), face.getStepZ())
                .expandTowards(
                        face.getStepX() != 0 ? (seal.getArea().getX() - 1) * face.getStepX() : 0,
                        face.getStepY() != 0 ? (seal.getArea().getY() - 1) * face.getStepY() : 0,
                        face.getStepZ() != 0 ? (seal.getArea().getZ() - 1) * face.getStepZ() : 0)
                .inflate(
                        face.getStepX() == 0 ? seal.getArea().getX() - 1 : 0,
                        face.getStepY() == 0 ? seal.getArea().getY() - 1 : 0,
                        face.getStepZ() == 0 ? seal.getArea().getZ() - 1 : 0);
    }
}
