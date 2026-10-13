package thaumcraft.common.entities.projectile;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
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
 * The mine of the mine focus medium. It drops slowly, arms itself on the first thing it touches and, two seconds
 * later, goes off on the first enemies (or friends) that come within a block of it.
 * 1.21: pushOutOfBlocks is Entity.moveTowardsClosestSpace.
 */
public class EntityFocusMine extends ThrowableProjectile implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> ARMED = SynchedEntityData.defineId(EntityFocusMine.class, EntityDataSerializers.BOOLEAN);

    FocusPackage focusPackage;
    boolean friendly = false;
    public int counter = 40;
    FocusEffect[] effects = null;

    public EntityFocusMine(EntityType<? extends EntityFocusMine> type, Level level) {
        super(type, level);
    }

    public EntityFocusMine(FocusPackage pack, Trajectory trajectory, boolean friendly) {
        super(TCEntities.FOCUS_MINE.get(), pack.getCaster(), pack.world);
        focusPackage = pack;
        this.friendly = friendly;
        setPos(trajectory.source.x, trajectory.source.y, trajectory.source.z);
        shoot(trajectory.direction.x, trajectory.direction.y, trajectory.direction.z, 0.0f, 0.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ARMED, false);
    }

    public boolean getIsArmed() {
        return entityData.get(ARMED);
    }

    public void setIsArmed(boolean par1) {
        entityData.set(ARMED, par1);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
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
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("armed", getIsArmed());
        if (focusPackage != null) {
            nbt.put("pack", focusPackage.serialize());
        }
        nbt.putBoolean("friendly", friendly);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        friendly = nbt.getBoolean("friendly");
        setIsArmed(nbt.getBoolean("armed"));
        if (getIsArmed()) {
            counter = 0;
        }
        try {
            (focusPackage = new FocusPackage()).deserialize(nbt.getCompound("pack"));
        } catch (Exception ex) {
        }
    }

    @Override
    protected void onHit(HitResult mop) {
        if (mop != null && mop.getType() != HitResult.Type.MISS && getOwner() != null) {
            setIsArmed(true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().noCollision(this, getBoundingBox())) {
            moveTowardsClosestSpace(getX(), (getBoundingBox().minY + getBoundingBox().maxY) / 2.0, getZ());
            setDeltaMovement(getDeltaMovement().scale(0.25));
        }
        Level world = level();
        if (tickCount > 1200 || (!world.isClientSide && (getOwner() == null || focusPackage == null))) {
            discard();
        }
        if (!isAlive() || !getIsArmed()) {
            return;
        }
        if (counter > 0) {
            --counter;
        }
        if (counter > 0 || tickCount % 5 != 0) {
            return;
        }
        if (world.isClientSide) {
            if (effects == null && focusPackage != null) {
                effects = focusPackage.getFocusEffects();
            }
            if (effects != null && effects.length > 0) {
                FocusEffect eff = effects[random.nextInt(effects.length)];
                eff.renderParticleFX(world, getX() + random.nextGaussian() * 0.1, getY() + random.nextGaussian() * 0.1, getZ() + random.nextGaussian() * 0.1,
                        random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01);
            }
            return;
        }
        List<LivingEntity> list2 = EntityUtils.getEntitiesInRange(world, getX(), getY(), getZ(), this, LivingEntity.class, 1.0);
        int d = 0;
        for (LivingEntity e : list2) {
            if (!e.isAlive()) {
                continue;
            }
            if (friendly) {
                if (!EntityUtils.isFriendly(focusPackage.getCaster(), e)) {
                    continue;
                }
            } else if (EntityUtils.isFriendly(focusPackage.getCaster(), e)) {
                continue;
            }
            Vec3 epv = e.position().add(0.0, e.getBbHeight() / 2.0f, 0.0);
            Vec3 here = position();
            ServerEvents.addRunnableServer(world, () -> {
                HitResult ray = new EntityHitResult(e, e.position().add(0.0, e.getBbHeight() / 2.0f, 0.0));
                FocusEngine.runFocusPackage(focusPackage.copy(getOwner() instanceof LivingEntity le ? le : null), new Trajectory[] {new Trajectory(here, epv.subtract(here).normalize())},
                        new HitResult[] {ray});
            }, d++);
        }
        if (d > 0) {
            discard();
        }
    }
}
