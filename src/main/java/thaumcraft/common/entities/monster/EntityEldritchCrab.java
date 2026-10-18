package thaumcraft.common.entities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.lib.SoundsTC;

/**
 * The eldritch (helmed) crab. Without its helm it leaps onto the head of its target and claws at it from there. Its
 * helm gives it armor and breaks off once it is down to half health. It is what lives inside an inhabited zombie.
 * The ender pearl it may drop is in its loot table. 1.21: arthropod and bane of arthropods come from entity type tags.
 */
public class EntityEldritchCrab extends Monster implements IEldritchMob {
    private static final EntityDataAccessor<Boolean> HELM = SynchedEntityData.defineId(EntityEldritchCrab.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RIDING = SynchedEntityData.defineId(EntityEldritchCrab.class, EntityDataSerializers.INT);
    private int attackTime = 0;

    public EntityEldritchCrab(EntityType<? extends EntityEldritchCrab> type, Level world) {
        super(type, world);
        xpReward = 6;
        if (getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.63f));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    /**
     * applyEntityAttributes. The original picked 0.275 speed with a helm and 0.3 without, but it ran before the helm
     * was set, so every crab moves at 0.3.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HELM, false);
        builder.define(RIDING, -1);
    }

    public int getRiding() {
        return entityData.get(RIDING);
    }

    public void setRiding(int s) {
        entityData.set(RIDING, s);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public int getArmorValue() {
        return hasHelm() ? 5 : 0;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (level().getDifficulty() == Difficulty.HARD) {
            setHelm(true);
        } else {
            setHelm(random.nextFloat() < 0.33f);
        }
        if (data == null) {
            data = new Spider.SpiderEffectsGroupData();
            if (level().getDifficulty() == Difficulty.HARD && level.getRandom().nextFloat() < 0.1f * diff.getSpecialMultiplier()) {
                ((Spider.SpiderEffectsGroupData) data).setRandomEffect(level.getRandom());
            }
        }
        if (data instanceof Spider.SpiderEffectsGroupData spiderData && spiderData.effect != null) {
            addEffect(new MobEffectInstance(spiderData.effect, -1));
        }
        return super.finalizeSpawn(level, diff, reason, data);
    }

    public boolean hasHelm() {
        return entityData.get(HELM);
    }

    public void setHelm(boolean par1) {
        entityData.set(HELM, par1);
    }

    @Override
    public void tick() {
        super.tick();
        --attackTime;
        if (tickCount < 20) {
            fallDistance = 0.0f;
        }
        if (!level().isClientSide) {
            Entity target = getTarget();
            if (getVehicle() == null && target != null && !target.isVehicle() && !onGround() && !hasHelm() && target.isAlive()
                    && getY() - target.getY() >= target.getBbHeight() / 2.0f && distanceToSqr(target) < 4.0) {
                startRiding(target);
                setRiding(target.getId());
            }
            if (getVehicle() != null && isAlive() && attackTime <= 0) {
                attackTime = 10 + random.nextInt(10);
                doHurtTarget(getVehicle());
                if (random.nextFloat() < 0.2) {
                    stopRiding();
                    setRiding(-1);
                }
            }
            if (getVehicle() == null && getRiding() != -1) {
                setRiding(-1);
            }
        } else if (getVehicle() == null && getRiding() != -1) {
            Entity e = level().getEntity(getRiding());
            if (e != null) {
                startRiding(e);
            }
        } else if (getVehicle() != null && getRiding() == -1) {
            stopRiding();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            playSound(SoundsTC.CRABCLAW.get(), 1.0f, 0.9f + level().random.nextFloat() * 0.2f);
            return true;
        }
        return false;
    }

    /** The helm shatters once the crab is down to half health. Only the break sound of renderBrokenItemStack was heard. */
    @Override
    public boolean hurt(DamageSource source, float damage) {
        boolean b = super.hurt(source, damage);
        if (hasHelm() && getHealth() / getMaxHealth() <= 0.5f) {
            setHelm(false);
            playSound(SoundEvents.ITEM_BREAK, 0.8f, 0.8f + level().random.nextFloat() * 0.4f);
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);
        }
        return b;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setHelm(nbt.getBoolean("helm"));
        if (!hasHelm()) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("helm", hasHelm());
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.CRABTALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsTC.CRABDEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        playSound(SoundEvents.SPIDER_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    public boolean isAlliedTo(Entity el) {
        return el instanceof EntityEldritchCrab;
    }
}
