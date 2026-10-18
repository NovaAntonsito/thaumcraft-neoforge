package thaumcraft.common.lib.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import thaumcraft.Thaumcraft;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.registry.TCAttributes;
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

    /**
     * hasGoggles: the held item or a worn piece of armor reveals essentia contents and the like.
     * The bauble slots are checked once Curios is in (stage 9e-3).
     */
    public static boolean hasGoggles(@Nullable Entity e) {
        if (!(e instanceof net.minecraft.world.entity.player.Player viewer)) {
            return false;
        }
        if (viewer.getMainHandItem().getItem() instanceof thaumcraft.api.items.IGoggles g && g.showIngamePopups(viewer.getMainHandItem(), viewer)) {
            return true;
        }
        for (net.minecraft.world.item.ItemStack stack : viewer.getInventory().armor) {
            if (stack.getItem() instanceof thaumcraft.api.items.IGoggles g && g.showIngamePopups(stack, viewer)) {
                return true;
            }
        }
        return false;
    }

    /** hasRevealer: the held items or a worn piece of armor would show nodes. */
    @SuppressWarnings("deprecation")
    public static boolean hasRevealer(@Nullable Entity e) {
        if (!(e instanceof net.minecraft.world.entity.player.Player viewer)) {
            return false;
        }
        if (viewer.getMainHandItem().getItem() instanceof thaumcraft.api.items.IRevealer r && r.showNodes(viewer.getMainHandItem(), viewer)) {
            return true;
        }
        if (viewer.getOffhandItem().getItem() instanceof thaumcraft.api.items.IRevealer r && r.showNodes(viewer.getOffhandItem(), viewer)) {
            return true;
        }
        for (net.minecraft.world.item.ItemStack stack : viewer.getInventory().armor) {
            if (stack.getItem() instanceof thaumcraft.api.items.IRevealer r && r.showNodes(stack, viewer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * resetFloatCounter: keeps the server from kicking a player for flying, while a tool holds them up.
     * 1.21: the counter is private, so it is set by reflection.
     */
    public static void resetFloatCounter(net.minecraft.server.level.ServerPlayer player) {
        try {
            net.neoforged.fml.util.ObfuscationReflectionHelper.setPrivateValue(net.minecraft.server.network.ServerGamePacketListenerImpl.class,
                    player.connection, 0, "aboveGroundTickCount");
        } catch (Exception ignored) {
        }
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
    public static final AttributeModifier CHAMPION_HEALTH = new AttributeModifier(rl("champion_health"), 100.0, AttributeModifier.Operation.ADD_VALUE);
    public static final AttributeModifier CHAMPION_DAMAGE = new AttributeModifier(rl("champion_damage"), 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final AttributeModifier BOLDBUFF = new AttributeModifier(rl("bold_speed"), 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    public static final AttributeModifier MIGHTYBUFF = new AttributeModifier(rl("mighty_damage"), 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    /** HPBUFF[5] and DMGBUFF[0] of the original, the only ones it used. */
    public static final AttributeModifier TAINTED_HEALTH = new AttributeModifier(rl("tainted_health"), 25.0, AttributeModifier.Operation.ADD_VALUE);
    public static final AttributeModifier TAINTED_DAMAGE = new AttributeModifier(rl("tainted_damage"), 0.5, AttributeModifier.Operation.ADD_VALUE);

    /** HPBUFF[0-4] and DMGBUFF[0-4]: the extra health and damage a boss gets for each player past the first. */
    public static final AttributeModifier[] HPBUFF = new AttributeModifier[5];
    public static final AttributeModifier[] DMGBUFF = new AttributeModifier[5];

    static {
        for (int a = 0; a < 5; ++a) {
            HPBUFF[a] = new AttributeModifier(rl("health_buff_" + (a + 1)), 50.0, AttributeModifier.Operation.ADD_VALUE);
            DMGBUFF[a] = new AttributeModifier(rl("damage_buff_" + (a + 1)), 0.5, AttributeModifier.Operation.ADD_VALUE);
        }
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, path);
    }

    /** The champion modifier of the mob: -2 not rolled yet, -1 normal, 0-12 champion, 13 tainted. */
    public static int getChampionMod(LivingEntity entity) {
        AttributeInstance cai = entity.getAttribute(TCAttributes.CHAMPION_MOD);
        return cai == null ? -2 : (int) cai.getValue();
    }

    private static void apply(@Nullable AttributeInstance ai, AttributeModifier mod) {
        if (ai == null) return;
        ai.removeModifier(mod.id());
        ai.addPermanentModifier(mod);
    }

    public static void makeChampion(Monster entity, boolean persist) {
        AttributeInstance modai = entity.getAttribute(TCAttributes.CHAMPION_MOD);
        if (modai == null || modai.getValue() > -2.0) {
            return;
        }
        int type = entity.getRandom().nextInt(ChampionModifier.mods.length);
        if (entity instanceof Creeper) {
            type = 0;
        }
        apply(modai, ChampionModifier.mods[type].attributeMod);
        if (!(entity instanceof thaumcraft.common.entities.monster.boss.EntityThaumcraftBoss boss)) {
            apply(entity.getAttribute(Attributes.MAX_HEALTH), CHAMPION_HEALTH);
            apply(entity.getAttribute(Attributes.ATTACK_DAMAGE), CHAMPION_DAMAGE);
            entity.heal(25.0f);
            entity.setCustomName(Component.empty().append(ChampionModifier.mods[type].getModNameLocalized()).append(" ").append(entity.getName()));
        } else {
            boss.generateName();
        }
        if (persist) {
            entity.setPersistenceRequired();
        }
        switch (type) {
            case 0 -> apply(entity.getAttribute(Attributes.MOVEMENT_SPEED), BOLDBUFF);
            case 3 -> apply(entity.getAttribute(Attributes.ATTACK_DAMAGE), MIGHTYBUFF);
            case 5 -> {
                int bh = (int) entity.getAttribute(Attributes.MAX_HEALTH).getBaseValue() / 2;
                entity.setAbsorptionAmount(entity.getAbsorptionAmount() + bh);
            }
            default -> {
            }
        }
    }

    public static void makeTainted(LivingEntity target) {
        AttributeInstance modai = target.getAttribute(TCAttributes.CHAMPION_MOD);
        if (modai == null || modai.getValue() > -1.0) {
            return;
        }
        int type = 13;
        if (modai.getValue() == -1.0) {
            apply(modai, ChampionModifier.ATTRIBUTE_MINUS_ONE);
        }
        apply(modai, ChampionModifier.mods[type].attributeMod);
        apply(target.getAttribute(Attributes.MAX_HEALTH), TAINTED_HEALTH);
        // Without an attack damage attribute the critter melee goal uses the size of the mob, like the original did.
        apply(target.getAttribute(Attributes.ATTACK_DAMAGE), TAINTED_DAMAGE);
        target.heal(25.0f);
    }
    /** The looting level of whoever caused the damage (LivingDropsEvent.getLootingLevel and dropLoot of 1.12). */
    public static int getLootingLevel(Level world, net.minecraft.world.damagesource.DamageSource source) {
        if (source.getEntity() instanceof LivingEntity le) {
            return net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(world.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), le);
        }
        return 0;
    }

    /** Whether ent2's middle lies in the cone of the given aperture (radians) and length along ent's line of sight. */
    public static boolean isVisibleTo(float fov, Entity ent, Entity ent2, float range) {
        double[] x = { ent2.getX(), ent2.getBoundingBox().minY + ent2.getBbHeight() / 2.0f, ent2.getZ() };
        double[] t = { ent.getX(), ent.getBoundingBox().minY + ent.getEyeHeight(), ent.getZ() };
        Vec3 q = ent.getLookAngle();
        q = new Vec3(q.x * range, q.y * range, q.z * range);
        Vec3 l = q.add(ent.getX(), ent.getBoundingBox().minY + ent.getEyeHeight(), ent.getZ());
        double[] b = { l.x, l.y, l.z };
        return Utils.isLyingInCone(x, t, b, fov);
    }
}
