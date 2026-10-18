package thaumcraft.common.entities.monster.tainted;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityTaintacle: a tentacle rooted in taint. It never moves and starves off taint. When its target is
 * between 4 and 16 blocks away and stands on taint, it raises taint tendrils (EntityTaintacleSmall) under it.
 */
public class EntityTaintacle extends Monster implements ITaintedMob {
    public float flailIntensity = 1.0f;

    public EntityTaintacle(EntityType<? extends EntityTaintacle> type, Level world) {
        super(type, world);
        xpReward = 8;
    }

    public EntityTaintacle(Level world) {
        this(TCEntities.TAINTACLE.get(), world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof ITaintedMob) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof ITaintedMob || super.isAlliedTo(other);
    }

    /** Taint where the entity stands or right under it. */
    public static boolean isOnTaint(Level world, BlockPos pos) {
        return TaintHelper.isTaint(world.getBlockState(pos)) || TaintHelper.isTaint(world.getBlockState(pos.below()));
    }

    /** Rooted: no sideways motion and nothing pushes it up. */
    @Override
    public void move(MoverType type, Vec3 motion) {
        super.move(type, new Vec3(0.0, Math.min(motion.y, 0.0), 0.0));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount % 20 == 0) {
            if (!isOnTaint(level(), blockPosition())) {
                hurt(damageSources().starve(), 1.0f);
            }
            LivingEntity target = getTarget();
            if (!(this instanceof EntityTaintacleSmall) && tickCount % 40 == 0 && target != null && distanceToSqr(target) > 16.0
                    && distanceToSqr(target) < 256.0 && getSensing().hasLineOfSight(target)) {
                spawnTentacles(this, target, getSoundVolume(), getVoicePitch());
            }
        }
        if (level().isClientSide) {
            if (flailIntensity > 1.0f) {
                flailIntensity -= 0.01f;
            }
            if (tickCount < getBbHeight() * 10.0f && onGround()) {
                ClientFX.tentacleArise(this);
            }
        }
    }

    /**
     * Raises a taint tendril next to the target when it stands on taint. Shared with the taint seeds.
     * TODO: the eldritch biome also allows it, and turns the air under the target into taint fibre (Outer Lands).
     */
    public static void spawnTentacles(Mob source, Entity entity, float volume, float pitch) {
        Level world = source.level();
        if (isOnTaint(world, entity.blockPosition())) {
            EntityTaintacleSmall taintlet = new EntityTaintacleSmall(world);
            taintlet.moveTo(entity.getX() + world.random.nextFloat() - world.random.nextFloat(), entity.getY(),
                    entity.getZ() + world.random.nextFloat() - world.random.nextFloat(), 0.0f, 0.0f);
            world.addFreshEntity(taintlet);
            source.playSound(SoundsTC.TENTACLE.get(), volume, pitch);
        }
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
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            flailIntensity = 3.0f;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        level().broadcastEntityEvent(this, (byte) 16);
        playSound(SoundsTC.TENTACLE.get(), getSoundVolume(), getVoicePitch());
        return super.doHurtTarget(target);
    }
}
