package thaumcraft.common.entities.projectile;

import java.awt.Color;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.joml.Vector3f;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.lib.events.ServerEvents;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCEntities;

/**
 * The orb thrown by the projectile focus medium. Special: 0 none, 1 bouncy, 2 seeks hostiles, 3 seeks friendlies.
 * 1.21: the owner is the vanilla projectile owner (synced in the spawn packet), so the OWNER data parameter is gone.
 * The fire mote trail uses colored dust until the FX stage.
 */
public class EntityFocusProjectile extends ThrowableProjectile implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Integer> SPECIAL = SynchedEntityData.defineId(EntityFocusProjectile.class, EntityDataSerializers.INT);

    FocusPackage focusPackage;
    private Entity target;
    boolean firstParticle = false;
    public float lastRenderTick = 0.0f;
    FocusEffect[] effects = null;

    public EntityFocusProjectile(EntityType<? extends EntityFocusProjectile> type, Level level) {
        super(type, level);
    }

    public EntityFocusProjectile(FocusPackage pack, float speed, Trajectory trajectory, int special) {
        super(TCEntities.FOCUS_PROJECTILE.get(), pack.getCaster(), pack.getCaster().level());
        focusPackage = pack;
        LivingEntity caster = pack.getCaster();
        setPos(trajectory.source.x + trajectory.direction.x * caster.getBbWidth() * 2.1,
                trajectory.source.y + trajectory.direction.y * caster.getBbWidth() * 2.1,
                trajectory.source.z + trajectory.direction.z * caster.getBbWidth() * 2.1);
        shoot(trajectory.direction.x, trajectory.direction.y, trajectory.direction.z, speed, 0.0f);
        setSpecial(special);
    }

    @Override
    protected double getDefaultGravity() {
        return (getSpecial() > 1) ? 0.005 : 0.01;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPECIAL, 0);
    }

    public void setSpecial(int s) {
        entityData.set(SPECIAL, s);
    }

    public int getSpecial() {
        return entityData.get(SPECIAL);
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
        if (focusPackage != null) {
            nbt.put("pack", focusPackage.serialize());
        }
        nbt.putInt("special", getSpecial());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setSpecial(nbt.getInt("special"));
        try {
            (focusPackage = new FocusPackage()).deserialize(nbt.getCompound("pack"));
        } catch (Exception ex) {
        }
    }

    @Override
    protected void onHit(HitResult mop) {
        if (mop == null) {
            return;
        }
        Level world = level();
        if (getSpecial() == 1 && mop instanceof BlockHitResult bhr && mop.getType() == HitResult.Type.BLOCK) {
            if (world.getBlockState(bhr.getBlockPos()).getCollisionShape(world, bhr.getBlockPos()).isEmpty()) {
                return;
            }
            Vec3 m = getDeltaMovement();
            double mx = m.x;
            double my = m.y;
            double mz = m.z;
            double px = getX() - mx;
            double py = getY() - my;
            double pz = getZ() - mz;
            Direction side = bhr.getDirection();
            if (side.getStepZ() != 0) {
                mz *= -1.0;
            }
            if (side.getStepX() != 0) {
                mx *= -1.0;
            }
            if (side.getStepY() != 0) {
                my *= -0.9;
            }
            mx *= 0.9;
            my *= 0.9;
            mz *= 0.9;
            float var20 = Mth.sqrt((float) (mx * mx + my * my + mz * mz));
            px -= mx / var20 * 0.05;
            py -= my / var20 * 0.05;
            pz -= mz / var20 * 0.05;
            setPos(px, py, pz);
            setDeltaMovement(mx, my, mz);
            if (!world.isClientSide) {
                playSound(SoundEvents.LEASH_KNOT_PLACE, 0.25f, 1.0f);
            }
            if (!world.isClientSide && new Vec3(mx, my, mz).length() < 0.2) {
                discard();
            }
        } else if (!world.isClientSide) {
            HitResult hit = mop;
            if (mop instanceof EntityHitResult ehr) {
                hit = new EntityHitResult(ehr.getEntity(), position());
            }
            final HitResult target = hit;
            Vec3 pv = new Vec3(xo, yo, zo);
            Vec3 vf = getDeltaMovement();
            FocusPackage pack = focusPackage;
            if (pack != null) {
                ServerEvents.addRunnableServer(world, () -> FocusEngine.runFocusPackage(pack, new Trajectory[] {new Trajectory(pv, vf.normalize())}, new HitResult[] {target}), 0);
            }
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > 1200 || (!level().isClientSide && (getOwner() == null || focusPackage == null))) {
            discard();
        }
        firstParticle = true;
        if (target == null && tickCount % 5 == 0 && getSpecial() > 1) {
            List<LivingEntity> list = EntityUtils.getEntitiesInRangeSorted(level(), this, LivingEntity.class, 16.0);
            for (LivingEntity pt : list) {
                if (pt.isAlive() && isVisibleTo(1.75f, pt, 16.0f)) {
                    if (!canSee(pt)) {
                        continue;
                    }
                    boolean f = EntityUtils.isFriendly(getOwner(), pt);
                    if (f && getSpecial() == 3) {
                        target = pt;
                        break;
                    }
                    if (!f && getSpecial() == 2) {
                        target = pt;
                        break;
                    }
                }
            }
        }
        if (target != null) {
            double dx = target.getX() - getX();
            double dy = target.getBoundingBox().minY + target.getBbHeight() * 0.6 - getY();
            double dz = target.getZ() - getZ();
            Vec3 v = new Vec3(dx, dy, dz).normalize();
            Vec3 mv = getDeltaMovement();
            double lv = mv.length();
            mv = mv.normalize().add(v.scale(0.275));
            mv = mv.normalize().scale(lv);
            setDeltaMovement(mv);
            if (tickCount % 5 == 0 && (!target.isAlive() || !isVisibleTo(1.75f, target, 16.0f) || !canSee(target))) {
                target = null;
            }
        }
    }

    /** EntityUtils.isVisibleTo: whether the target lies in a cone along the direction of flight. */
    private boolean isVisibleTo(float fov, Entity ent2, float range) {
        double[] x = {ent2.getX(), ent2.getBoundingBox().minY + ent2.getBbHeight() / 2.0f, ent2.getZ()};
        double[] t = {getX(), getBoundingBox().minY + getEyeHeight(), getZ()};
        Vec3 q = getLookAngle().scale(range);
        Vec3 l = q.add(getX(), getBoundingBox().minY + getEyeHeight(), getZ());
        double[] b = {l.x, l.y, l.z};
        double halfAperture = fov / 2.0f;
        double[] apexToXVect = {t[0] - x[0], t[1] - x[1], t[2] - x[2]};
        double[] axisVect = {t[0] - b[0], t[1] - b[1], t[2] - b[2]};
        double dot = apexToXVect[0] * axisVect[0] + apexToXVect[1] * axisVect[1] + apexToXVect[2] * axisVect[2];
        double ma = Math.sqrt(apexToXVect[0] * apexToXVect[0] + apexToXVect[1] * apexToXVect[1] + apexToXVect[2] * apexToXVect[2]);
        double mb = Math.sqrt(axisVect[0] * axisVect[0] + axisVect[1] * axisVect[1] + axisVect[2] * axisVect[2]);
        if (!(dot / ma / mb > Math.cos(halfAperture))) {
            return false;
        }
        return dot / mb < mb;
    }

    /** EntityUtils.canEntityBeSeen: no block between the middles of both entities. */
    private boolean canSee(Entity targetEntity) {
        Vec3 a = new Vec3(getX(), getY() + getBbHeight() / 2.0f, getZ());
        Vec3 b = new Vec3(targetEntity.getX(), targetEntity.getY() + targetEntity.getBbHeight() / 2.0f, targetEntity.getZ());
        return level().clip(new ClipContext(a, b, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public Vec3 getLookAngle() {
        return getDeltaMovement().normalize();
    }

    /** Called by the renderer: the trail of the projectile. */
    public void renderParticle(float coeff) {
        lastRenderTick = coeff;
        if (effects == null && focusPackage != null) {
            effects = focusPackage.getFocusEffects();
        }
        if (effects != null && effects.length > 0) {
            Level world = level();
            FocusEffect eff = effects[random.nextInt(effects.length)];
            Color c1 = new Color(FocusEngine.getElementColor(eff.getKey()));
            double x = xo + (getX() - xo) * coeff;
            double y = yo + (getY() - yo) * coeff + getBbHeight() / 2.0f;
            double z = zo + (getZ() - zo) * coeff;
            world.addParticle(new DustParticleOptions(new Vector3f(c1.getRed() / 255.0f, c1.getGreen() / 255.0f, c1.getBlue() / 255.0f), 1.0f),
                    x, y, z, 0.0125f * (random.nextFloat() - 0.5f), 0.0125f * (random.nextFloat() - 0.5f), 0.0125f * (random.nextFloat() - 0.5f));
            if (firstParticle) {
                firstParticle = false;
                eff.renderParticleFX(world, x + random.nextGaussian() * 0.1, y + random.nextGaussian() * 0.1, z + random.nextGaussian() * 0.1,
                        random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01);
            }
        }
    }
}
