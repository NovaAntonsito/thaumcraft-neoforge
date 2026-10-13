package thaumcraft.common.lib.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Partial port of EntityUtils. */
public final class EntityUtils {
    private EntityUtils() {
    }

    /** The entity the given one is looking at, from its eyes, between minrange and range blocks away. */
    @Nullable
    public static Entity getPointedEntity(Level world, Entity entity, double minrange, double range, float padding, boolean nonCollide) {
        return getPointedEntity(world, entity, entity.getEyePosition(), entity.getLookAngle(), minrange, range, padding, nonCollide);
    }

    /**
     * Searches the box swept along the look vector for the closest entity whose (padded) box the ray hits, with no
     * block in between. Entities that cannot be collided with only count with nonCollide.
     */
    @Nullable
    public static Entity getPointedEntity(Level world, @Nullable Entity ignore, Vec3 start, Vec3 lookVec, double minrange, double range,
                                          float padding, boolean nonCollide) {
        Entity pointedEntity = null;
        double d = range;
        Vec3 end = start.add(lookVec.x * d, lookVec.y * d, lookVec.z * d);
        AABB bb = ignore != null ? ignore.getBoundingBox() : new AABB(start, start).inflate(0.5);
        List<Entity> list = world.getEntities(ignore, bb.expandTowards(lookVec.x * d, lookVec.y * d, lookVec.z * d).inflate(padding));
        double d2 = 0.0;
        for (Entity entity : list) {
            if (start.distanceTo(entity.position()) < minrange) continue;
            if (!entity.isPickable() && !nonCollide) continue;
            Vec3 eyes = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
            if (world.clip(new ClipContext(start, eyes, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ignore)).getType() != HitResult.Type.MISS) {
                continue;
            }
            float f2 = Math.max(0.8f, entity.getPickRadius());
            AABB box = entity.getBoundingBox().inflate(f2);
            Optional<Vec3> hit = box.clip(start, end);
            if (box.contains(start)) {
                if (0.0 < d2 || d2 == 0.0) {
                    pointedEntity = entity;
                    d2 = 0.0;
                }
            } else if (hit.isPresent()) {
                double d3 = start.distanceTo(hit.get());
                if (d3 < d2 || d2 == 0.0) {
                    pointedEntity = entity;
                    d2 = d3;
                }
            }
        }
        return pointedEntity;
    }
    /** As getPointedEntity, but returns where the ray hit the entity's padded box. */
    @Nullable
    public static EntityHitResult getPointedEntityRay(Level world, @Nullable Entity ignore, Vec3 start, Vec3 lookVec, double minrange, double range,
                                                      float padding, boolean nonCollide) {
        EntityHitResult pointedEntityRay = null;
        double d = range;
        Vec3 end = start.add(lookVec.x * d, lookVec.y * d, lookVec.z * d);
        AABB bb = ignore != null ? ignore.getBoundingBox() : new AABB(start, start).inflate(0.5);
        List<Entity> list = world.getEntities(ignore, bb.expandTowards(lookVec.x * d, lookVec.y * d, lookVec.z * d).inflate(padding));
        double d2 = 0.0;
        for (Entity entity : list) {
            if (start.distanceTo(entity.position()) < minrange) continue;
            if (!entity.isPickable() && !nonCollide) continue;
            Vec3 eyes = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
            if (world.clip(new ClipContext(start, eyes, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ignore)).getType() != HitResult.Type.MISS) {
                continue;
            }
            float f2 = Math.max(0.8f, entity.getPickRadius());
            AABB box = entity.getBoundingBox().inflate(f2);
            Optional<Vec3> hit = box.clip(start, end);
            if (box.contains(start)) {
                if (0.0 < d2 || d2 == 0.0) {
                    pointedEntityRay = new EntityHitResult(entity, hit.orElse(start));
                    d2 = 0.0;
                }
            } else if (hit.isPresent()) {
                double d3 = start.distanceTo(hit.get());
                if (d3 < d2 || d2 == 0.0) {
                    pointedEntityRay = new EntityHitResult(entity, hit.get());
                    d2 = d3;
                }
            }
        }
        return pointedEntityRay;
    }

    public static boolean isFriendly(@Nullable Entity source, @Nullable Entity target) {
        if (source == null || target == null) {
            return false;
        }
        if (source.getId() == target.getId()) {
            return true;
        }
        if (source.hasPassenger(target) || target.hasPassenger(source) || source.isPassengerOfSameVehicle(target)) {
            return true;
        }
        if (source.isAlliedTo(target)) {
            return true;
        }
        if (target instanceof OwnableEntity oe && oe.getOwner() != null && oe.getOwner().equals(source)) {
            return true;
        }
        if (!target.level().isClientSide && target instanceof Player && target.getServer() != null && !target.getServer().isPvpAllowed()) {
            return true;
        }
        return false;
    }

    public static <T extends Entity> List<T> getEntitiesInRange(Level world, BlockPos pos, @Nullable Entity entity, Class<? extends T> classEntity, double range) {
        return getEntitiesInRange(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, entity, classEntity, range);
    }

    public static <T extends Entity> List<T> getEntitiesInRange(Level world, double x, double y, double z, @Nullable Entity entity, Class<? extends T> classEntity, double range) {
        ArrayList<T> out = new ArrayList<>();
        for (T ent : world.getEntitiesOfClass(classEntity, new AABB(x, y, z, x, y, z).inflate(range))) {
            if (entity != null && entity.getId() == ent.getId()) {
                continue;
            }
            out.add(ent);
        }
        return out;
    }

    public static <T extends Entity> List<T> getEntitiesInRangeSorted(Level world, Entity entity, Class<? extends T> classEntity, double range) {
        List<T> list = getEntitiesInRange(world, entity.getX(), entity.getY(), entity.getZ(), entity, classEntity, range);
        list.sort(Comparator.comparingDouble(e -> entity.position().distanceToSqr(e.position())));
        return list;
    }
}
