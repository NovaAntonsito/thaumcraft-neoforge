package thaumcraft.common.entities.monster.tainted;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityTaintSwarm: a cloud of tiny biting things that taint geysers let out. The entity itself is invisible;
 * what you see are its particles (FXSwarm). It roams near taint seeds and goes after players within 8 blocks, biting
 * with weakness. A summoned swarm wastes away without a target.
 */
public class EntityTaintSwarm extends Monster implements ITaintedMob {
    private static final EntityDataAccessor<Boolean> SUMMONED = SynchedEntityData.defineId(EntityTaintSwarm.class, EntityDataSerializers.BOOLEAN);
    private BlockPos currentFlightTarget;
    public int damBonus = 0;
    /** The FXSwarm particles of this swarm, client side only. */
    public final List<Object> swarm = new ArrayList<>();
    private int attackTime;

    public EntityTaintSwarm(EntityType<? extends EntityTaintSwarm> type, Level world) {
        super(type, world);
    }

    public EntityTaintSwarm(Level world) {
        this(TCEntities.TAINT_SWARM.get(), world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SUMMONED, false);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof ITaintedMob) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof ITaintedMob || super.isAlliedTo(other);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.SWARM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundsTC.SWARMATTACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsTC.SWARMATTACK.get();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public boolean getIsSummoned() {
        return entityData.get(SUMMONED);
    }

    public void setIsSummoned(boolean summoned) {
        entityData.set(SUMMONED, summoned);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 m = getDeltaMovement();
        setDeltaMovement(m.x, m.y * 0.6f, m.z);
        if (level().isClientSide) {
            for (int a = 0; a < swarm.size(); ++a) {
                if (swarm.get(a) == null || !ClientFX.isParticleAlive(swarm.get(a))) {
                    swarm.remove(a);
                    break;
                }
            }
            if (swarm.size() < 30) {
                swarm.add(ClientFX.swarmParticle(this, 0.22f, 15.0f, 0.08f));
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTime > 0) {
            --attackTime;
        }
        if (level().isClientSide) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null) {
            if (getIsSummoned()) {
                hurt(damageSources().generic(), 5.0f);
            }
            if (currentFlightTarget != null && (!level().isEmptyBlock(currentFlightTarget) || currentFlightTarget.getY() < 1
                    || currentFlightTarget.getY() > level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, currentFlightTarget).above(2).getY()
                    || !TaintHelper.isNearTaintSeed(level(), currentFlightTarget))) {
                currentFlightTarget = null;
            }
            if (currentFlightTarget == null || random.nextInt(30) == 0 || distanceToSqr(Vec3.atCenterOf(currentFlightTarget)) < 4.0) {
                currentFlightTarget = BlockPos.containing((int) getX() + random.nextInt(7) - random.nextInt(7),
                        (int) getY() + random.nextInt(6) - 2, (int) getZ() + random.nextInt(7) - random.nextInt(7));
            }
            steer(currentFlightTarget.getX() + 0.5 - getX(), currentFlightTarget.getY() + 0.1 - getY(), currentFlightTarget.getZ() + 0.5 - getZ(), 0.015f);
        } else {
            steer(target.getX() - getX(), target.getY() + target.getEyeHeight() - getY(), target.getZ() - getZ(), 0.025f);
        }
        target = getTarget();
        if (target == null) {
            setTarget(findPlayerToAttack());
        } else if (isAlive() && target.isAlive()) {
            float f = target.distanceTo(this);
            if (hasLineOfSight(target)) {
                attackEntity(target, f);
            }
        } else {
            setTarget(null);
        }
        if (getTarget() instanceof Player p && p.getAbilities().invulnerable) {
            setTarget(null);
        }
    }

    private void steer(double dx, double dy, double dz, float pull) {
        Vec3 m = getDeltaMovement();
        double mx = m.x + (Math.signum(dx) * 0.5 - m.x) * pull;
        double my = m.y + (Math.signum(dy) * 0.7 - m.y) * 0.1;
        double mz = m.z + (Math.signum(dz) * 0.5 - m.z) * pull;
        setDeltaMovement(mx, my, mz);
        float var4 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float var5 = Mth.wrapDegrees(var4 - getYRot());
        zza = 0.1f;
        setYRot(getYRot() + var5);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * Bites when touching. The victim keeps its motion (no knockback).
     * TODO: a summoned swarm also marked the victim as hit by a player (recentlyHit), for player drops.
     */
    protected void attackEntity(Entity victim, float dist) {
        if (attackTime <= 0 && dist < 3.0f && victim.getBoundingBox().maxY > getBoundingBox().minY
                && victim.getBoundingBox().minY < getBoundingBox().maxY) {
            attackTime = 15 + random.nextInt(10);
            Vec3 motion = victim.getDeltaMovement();
            if (doHurtTarget(victim) && victim instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            }
            victim.hasImpulse = false;
            victim.setDeltaMovement(motion);
            playSound(SoundsTC.SWARMATTACK.get(), 0.3f, 0.9f + level().random.nextFloat() * 0.2f);
        }
    }

    protected LivingEntity findPlayerToAttack() {
        return getIsSummoned() ? null : level().getNearestPlayer(this, 8.0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setIsSummoned(nbt.getBoolean("summoned"));
        damBonus = nbt.getByte("damBonus");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("summoned", getIsSummoned());
        nbt.putByte("damBonus", (byte) damBonus);
    }

    /** dropFewItems: a flux crystal half the time. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(world, source, recentlyHit);
        if (random.nextBoolean()) {
            spawnAtLocation(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1), getBbHeight() / 2.0f);
        }
    }
}
