package thaumcraft.common.entities.projectile;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.lib.events.ServerEvents;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCEntities;

/**
 * The lingering cloud of the cloud focus medium. Every 5 ticks it runs the rest of the spell on the living things
 * inside it and on a few random blocks around it (each block at most every 2 seconds).
 * 1.21: the size follows the synced radius through getDimensions, like vanilla's AreaEffectCloud.
 * The big faded cloud particles use colored dust until the FX stage.
 */
public class EntityFocusCloud extends Entity implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(EntityFocusCloud.class, EntityDataSerializers.FLOAT);
    static HashMap<Long, Long> cooldownMap = new HashMap<>();

    FocusPackage focusPackage;
    private LivingEntity owner;
    private UUID ownerUniqueId;
    private int duration;
    FocusEffect[] effects = null;

    public EntityFocusCloud(EntityType<? extends EntityFocusCloud> type, Level level) {
        super(type, level);
    }

    public EntityFocusCloud(FocusPackage pack, Trajectory trajectory, float rad, int dur) {
        super(TCEntities.FOCUS_CLOUD.get(), pack.world);
        focusPackage = pack;
        setPos(trajectory.source.x, trajectory.source.y, trajectory.source.z);
        setOwner(pack.getCaster());
        setRadius(rad);
        setDuration(dur);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int durationIn) {
        duration = durationIn;
    }

    public void setOwner(@Nullable LivingEntity ownerIn) {
        owner = ownerIn;
        ownerUniqueId = ownerIn == null ? null : ownerIn.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (owner == null && ownerUniqueId != null && level() instanceof ServerLevel sl) {
            if (sl.getEntity(ownerUniqueId) instanceof LivingEntity living) {
                owner = living;
            }
        }
        return owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 0.5f);
    }

    public void setRadius(float radiusIn) {
        if (!level().isClientSide) {
            entityData.set(RADIUS, radiusIn);
        }
    }

    public float getRadius() {
        return entityData.get(RADIUS);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (RADIUS.equals(key)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(getRadius() * 2.0f, 0.5f);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data) {
        data.writeNbt(focusPackage != null ? focusPackage.serialize() : new CompoundTag());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data) {
        try {
            (focusPackage = new FocusPackage()).deserialize(data.readNbt());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putInt("Duration", duration);
        nbt.putFloat("Radius", getRadius());
        if (ownerUniqueId != null) {
            nbt.putUUID("OwnerUUID", ownerUniqueId);
        }
        if (focusPackage != null) {
            nbt.put("pack", focusPackage.serialize());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        tickCount = nbt.getInt("Age");
        duration = nbt.getInt("Duration");
        setRadius(nbt.getFloat("Radius"));
        if (nbt.hasUUID("OwnerUUID")) {
            ownerUniqueId = nbt.getUUID("OwnerUUID");
        }
        try {
            (focusPackage = new FocusPackage()).deserialize(nbt.getCompound("pack"));
        } catch (Exception ex) {
        }
    }

    @Override
    public void tick() {
        super.tick();
        float rad = getRadius();
        int dur = getDuration();
        Level world = level();
        if (!world.isClientSide && (tickCount > dur * 20 || getOwner() == null || focusPackage == null)) {
            discard();
        }
        if (!isAlive()) {
            return;
        }
        if (world.isClientSide) {
            if (effects == null && focusPackage != null) {
                effects = focusPackage.getFocusEffects();
            }
            if (effects != null && effects.length > 0) {
                for (int a = 0; a < rad; ++a) {
                    FocusEffect eff = effects[random.nextInt(effects.length)];
                    drawFocusCloudParticle(world, getX() + random.nextGaussian() * rad / 2.0 * 0.85, getY() + random.nextGaussian() * rad / 2.0 * 0.85,
                            getZ() + random.nextGaussian() * rad / 2.0 * 0.85, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01,
                            random.nextGaussian() * 0.01, FocusEngine.getElementColor(eff.getKey()));
                    eff.renderParticleFX(world, getX() + random.nextGaussian() * rad / 2.0, getY() + random.nextGaussian() * rad / 2.0, getZ() + random.nextGaussian() * rad / 2.0,
                            random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01);
                }
            }
        } else if (tickCount % 5 == 0) {
            long t = System.currentTimeMillis();
            ArrayList<Trajectory> trajectories = new ArrayList<>();
            ArrayList<HitResult> targets = new ArrayList<>();
            List<Entity> list = EntityUtils.getEntitiesInRange(world, getX(), getY(), getZ(), this, Entity.class, rad);
            for (Entity e : list) {
                if (!e.isAlive()) {
                    continue;
                }
                if (e instanceof EntityFocusCloud cloud) {
                    Vec3 v = e.position().subtract(position());
                    e.move(MoverType.SELF, new Vec3(v.x / 50.0, v.y / 50.0, v.z / 50.0));
                    cloud.moveTowardsClosestSpace(getX(), getY(), getZ());
                }
                if (!(e instanceof LivingEntity)) {
                    continue;
                }
                // The original looked the entity id up as an Integer in this Long map, so the check never matched and
                // living things are hit every 5 ticks. Kept that way; only the put remains.
                cooldownMap.put((long) e.getId(), t + 2000L);
                Vec3 hitVec = e.position().add(0.0, e.getBbHeight() / 2.0f, 0.0);
                targets.add(new EntityHitResult(e, hitVec));
                trajectories.add(new Trajectory(position(), hitVec.subtract(position())));
            }
            for (int a2 = 0; a2 < rad; ++a2) {
                Vec3 dV = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
                BlockHitResult br = world.clip(new ClipContext(position(), position().add(dV.scale(rad)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (br.getType() != HitResult.Type.BLOCK) {
                    continue;
                }
                long bl = br.getBlockPos().asLong();
                if (cooldownMap.containsKey(bl)) {
                    if (cooldownMap.get(bl) <= t) {
                        cooldownMap.remove(bl);
                    } else {
                        continue;
                    }
                }
                targets.add(br);
                trajectories.add(new Trajectory(position(), dV));
                cooldownMap.put(bl, t + 2000L);
            }
            if (!targets.isEmpty()) {
                FocusPackage pack = focusPackage.copy(getOwner());
                ServerEvents.addRunnableServer(world, () -> FocusEngine.runFocusPackage(pack, trajectories.toArray(new Trajectory[0]), targets.toArray(new HitResult[0])), 0);
            }
        }
    }

    /** FXDispatcher.drawFocusCloudParticle: a big faded puff in the effect's color. */
    private void drawFocusCloudParticle(Level world, double x, double y, double z, double mx, double my, double mz, int c) {
        Color color = new Color(c);
        world.addParticle(new DustParticleOptions(new Vector3f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f), 4.0f), x, y, z, mx, my, mz);
    }
}
