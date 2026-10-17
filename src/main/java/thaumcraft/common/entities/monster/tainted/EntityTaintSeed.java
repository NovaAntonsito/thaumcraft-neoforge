package thaumcraft.common.entities.monster.tainted;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Config;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.registry.TCMobEffects;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * Port of EntityTaintSeed: the rooted heart of a taint outbreak. It never moves. While the aura has flux it keeps
 * spreading fibres around itself (every tick while boosted by a flux rift), and it poisons everything close with
 * taint. Without flux it starves. Taint blocks only survive near a seed (TaintHelper keeps the list).
 * TODO: the small taintacles it raises under its target (stage 9b-2), and the eldritch biome checks.
 */
public class EntityTaintSeed extends Monster implements ITaintedMob {
    public int boost = 0;
    boolean firstRun = false;
    public float attackAnim = 0.0f;

    public EntityTaintSeed(EntityType<? extends EntityTaintSeed> type, Level world) {
        super(type, world);
        xpReward = 8;
    }

    public EntityTaintSeed(Level world) {
        this(TCEntities.TAINT_SEED.get(), world);
    }

    protected int getArea() {
        return 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 75.0).add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        boost = nbt.getInt("boost");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("boost", boost);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        level().broadcastEntityEvent(this, (byte) 16);
        playSound(SoundsTC.TENTACLE.get(), getSoundVolume(), getVoicePitch());
        return super.doHurtTarget(target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            attackAnim = 0.5f;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof ITaintedMob) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof ITaintedMob || super.isAlliedTo(other);
    }

    /** Not in peaceful, not inside anything, and no other seed within 80% of the spread range. */
    public boolean getCanSpawnHere() {
        return level().getDifficulty() != Difficulty.PEACEFUL && isNotColliding()
                && EntityUtils.getEntitiesInRange(level(), blockPosition(), null, EntityTaintSeed.class, Config.TAINT_SPREAD_AREA.get() * 0.8).isEmpty();
    }

    public boolean isNotColliding() {
        return !level().containsAnyLiquid(getBoundingBox()) && level().isUnobstructed(this);
    }

    @Override
    public void die(DamageSource cause) {
        TaintHelper.removeTaintSeed(level(), blockPosition());
        super.die(cause);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (!firstRun || tickCount % 1200 == 0) {
                TaintHelper.removeTaintSeed(level(), blockPosition());
                TaintHelper.addTaintSeed(level(), blockPosition());
                firstRun = true;
            }
            if (isAlive()) {
                boolean tickFlag = tickCount % 20 == 0;
                if (boost > 0 || tickFlag) {
                    float mod = (boost > 0) ? 1.0f : AuraHandler.getFluxSaturation(level(), blockPosition());
                    if (boost > 0) {
                        --boost;
                    }
                    if (mod <= 0.0f) {
                        hurt(damageSources().starve(), 0.5f);
                        AuraHelper.polluteAura(level(), blockPosition(), 0.1f, false);
                    } else {
                        int a = getArea();
                        TaintHelper.spreadFibres(level(), blockPosition().offset(Mth.nextInt(getRandom(), -a * 3, a * 3),
                                Mth.nextInt(getRandom(), -a, a), Mth.nextInt(getRandom(), -a * 3, a * 3)), true);
                    }
                }
                if (tickFlag) {
                    LivingEntity target = getTarget();
                    if (target != null && distanceToSqr(target) < getArea() * 256 && getSensing().hasLineOfSight(target)) {
                        spawnTentacles(target);
                    }
                    List<LivingEntity> list = EntityUtils.getEntitiesInRange(level(), blockPosition(), this, LivingEntity.class, getArea() * 4);
                    for (LivingEntity elb : list) {
                        elb.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, 100, getArea() - 1, false, true));
                    }
                }
            }
        } else {
            if (attackAnim > 0.0f) {
                attackAnim *= 0.75f;
            }
            if (attackAnim < 0.001) {
                attackAnim = 0.0f;
            }
            float xx = 1.0f * Mth.sin(tickCount * 0.05f - 0.5f) / 5.0f;
            float zz = 1.0f * Mth.sin(tickCount * 0.06f - 0.5f) / 5.0f + hurtTime / 200.0f + attackAnim;
            if (random.nextFloat() < 0.033) {
                ClientFX.drawLightningFlash(level(), getX() + xx, getY() + getBbHeight() + 0.25f, getZ() + zz, 0.7f, 0.1f, 0.9f, 0.5f,
                        1.5f + random.nextFloat());
            } else {
                ClientFX.drawTaintParticles(level(), getX() + xx, getY() + getBbHeight() + 0.25f, getZ() + zz, random.nextGaussian() * 0.05f,
                        0.1f + 0.01f * random.nextFloat(), random.nextGaussian() * 0.05f, 2.0f);
            }
        }
    }

    /**
     * Raises a small taintacle under the target when it stands on taint.
     * TODO stage 9b-2: EntityTaintacleSmall, the tentacle sound, and the eldritch biome variant.
     */
    protected void spawnTentacles(Entity entity) {
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHORUS_FLOWER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 1.3f - getBbHeight() / 10.0f;
    }

    @Override
    protected float getSoundVolume() {
        return getBbHeight() / 8.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundsTC.TENTACLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsTC.TENTACLE.get();
    }

    /** dropFewItems: one flux crystal. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(world, source, recentlyHit);
        spawnAtLocation(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1), getBbHeight() / 2.0f);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void moveRelative(float amount, Vec3 relative) {
    }

    /** Rooted: no sideways motion and nothing pushes it up. */
    @Override
    public void move(MoverType type, Vec3 motion) {
        super.move(type, new Vec3(0.0, Math.min(motion.y, 0.0), 0.0));
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
