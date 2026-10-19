package thaumcraft.common.entities.monster.boss;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.blocks.world.BlockLoot;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.projectile.EntityGolemOrb;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCAttributes;

/**
 * Port of EntityEldritchGolem, the eldritch construct: a huge armored golem and a boss. It wakes up over 5 seconds,
 * healing 2 a tick meanwhile, tramples loot crates, urns and soft blocks in its way, and hits hard (three quarters of
 * its 10 damage) while tossing its target up. The blow that would kill it instead blows its head off: it explodes,
 * wakes up again, and from then on its arms knock targets far back and it charges and fires energy orbs from its
 * open neck, crackling with arcs. As a champion it is named "[modifier] Eldritch Construct".
 * As in the original, the client never learns the wake-up timer (the sync event fires at 150, but the golem only
 * counts from 100), so its head never bows while waking; and the arc picks the block under it from a fixed spot.
 */
public class EntityEldritchGolem extends EntityThaumcraftBoss implements IEldritchMob, RangedAttackMob {
    private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(EntityEldritchGolem.class, EntityDataSerializers.BOOLEAN);
    int beamCharge = 0;
    boolean chargingBeam = false;
    int arcing = 0;
    int ax = 0;
    int ay = 0;
    int az = 0;
    private int attackTimer;

    public EntityEldritchGolem(EntityType<? extends EntityEldritchGolem> type, Level world) {
        super(type, world);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void generateName() {
        int t = (int) getAttributeValue(TCAttributes.CHAMPION_MOD);
        if (t >= 0) {
            setCustomName(Component.translatable("entity.thaumcraft.eldritch_golem.custom", ChampionModifier.mods[t].getModNameLocalized()));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEADLESS, false);
    }

    public boolean isHeadless() {
        return entityData.get(HEADLESS);
    }

    public void setHeadless(boolean par1) {
        entityData.set(HEADLESS, par1);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (HEADLESS.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("headless", isHeadless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setHeadless(nbt.getBoolean("headless"));
        if (isHeadless()) {
            makeHeadless();
        }
    }

    /** getEyeHeight: the eyes sit higher once the head is gone (the orbs come out of the neck). */
    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(isHeadless() ? 3.33f : 3.0f);
    }

    @Override
    public int getArmorValue() {
        return super.getArmorValue() + 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityThaumcraftBoss.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MAX_HEALTH, 400.0);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        spawnTimer = 100;
        return super.finalizeSpawn(world, diff, reason, data);
    }

    /** getPosition of 1.12: the block half a block above the feet. */
    private BlockPos feet() {
        return BlockPos.containing(getX(), getY() + 0.5, getZ());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTimer > 0) {
            --attackTimer;
        }
        Vec3 m = getDeltaMovement();
        if (m.x * m.x + m.z * m.z > 2.500000277905201E-7 && random.nextInt(5) == 0) {
            BlockState bs = level().getBlockState(feet());
            if (!bs.isAir()) {
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, bs), getX() + (random.nextFloat() - 0.5) * getBbWidth(),
                        getBoundingBox().minY + 0.1, getZ() + (random.nextFloat() - 0.5) * getBbWidth(), 4.0 * (random.nextFloat() - 0.5), 0.5,
                        (random.nextFloat() - 0.5) * 4.0);
            }
            if (!level().isClientSide && bs.getBlock() instanceof BlockLoot) {
                level().destroyBlock(feet(), true);
            }
        }
        if (!level().isClientSide) {
            BlockState bs = level().getBlockState(feet());
            float h = bs.getDestroySpeed(level(), feet());
            if (h >= 0.0f && h <= 0.15f) {
                level().destroyBlock(feet(), true);
            }
        }
    }

    /** The killing blow takes its head off instead: an explosion, and it wakes up again with the orb attack. */
    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (!level().isClientSide && damage > getHealth() && !isHeadless()) {
            setHeadless(true);
            spawnTimer = 100;
            double xx = Mth.cos(getYRot() % 360.0f / 180.0f * 3.1415927f) * 0.75f;
            double zz = Mth.sin(getYRot() % 360.0f / 180.0f * 3.1415927f) * 0.75f;
            level().explode(this, getX() + xx, getY() + getEyeHeight(), getZ() + zz, 2.0f, Level.ExplosionInteraction.NONE);
            makeHeadless();
            return false;
        }
        return super.hurt(source, damage);
    }

    void makeHeadless() {
        goalSelector.addGoal(2, new AILongRangeAttack(this, 3.0, 1.0, 5, 5, 24.0f));
    }

    /** attackEntityAsMob: three quarters of its damage, tossing the target up, and far back once headless. */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (attackTimer > 0) {
            return false;
        }
        attackTimer = 10;
        level().broadcastEntityEvent(this, (byte) 4);
        boolean flag = target.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75f);
        if (flag) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.2000000059604645, 0.0));
            if (isHeadless()) {
                target.push(-Mth.sin(getYRot() * 3.1415927f / 180.0f) * 1.5f, 0.1, Mth.cos(getYRot() * 3.1415927f / 180.0f) * 1.5f);
            }
        }
        return flag;
    }

    /** An energy orb from the neck, while the charge lasts (15 to 19 per shot out of 150). */
    @Override
    public void performRangedAttack(LivingEntity entitylivingbase, float f) {
        if (hasLineOfSight(entitylivingbase) && !chargingBeam && beamCharge > 0) {
            beamCharge -= 15 + random.nextInt(5);
            getLookControl().setLookAt(entitylivingbase.getX(), entitylivingbase.getBoundingBox().minY + entitylivingbase.getBbHeight() / 2.0f,
                    entitylivingbase.getZ(), 30.0f, 30.0f);
            Vec3 v = getViewVector(1.0f);
            EntityGolemOrb blast = new EntityGolemOrb(level(), this, entitylivingbase, false);
            blast.setPos(blast.getX() + v.x, blast.getY(), blast.getZ() + v.z);
            double d0 = entitylivingbase.getX() + entitylivingbase.getDeltaMovement().x - getX();
            double d2 = entitylivingbase.getY() - getY() - entitylivingbase.getBbHeight() / 2.0f;
            double d3 = entitylivingbase.getZ() + entitylivingbase.getDeltaMovement().z - getZ();
            blast.shoot(d0, d2, d3, 0.66f, 5.0f);
            playSound(SoundsTC.EGATTACK.get(), 1.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(blast);
        }
    }

    @Override
    public void handleEntityEvent(byte p_70103_1_) {
        if (p_70103_1_ == 4) {
            attackTimer = 10;
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        } else if (p_70103_1_ == 18) {
            spawnTimer = 150;
        } else if (p_70103_1_ == 19) {
            if (arcing == 0) {
                float radius = 2.0f + random.nextFloat() * 2.0f;
                double radians = Math.toRadians(random.nextInt(360));
                double deltaX = radius * Math.cos(radians);
                double deltaZ = radius * Math.sin(radians);
                int bx = Mth.floor(getX() + deltaX);
                int by = Mth.floor(getY());
                int bz = Mth.floor(getZ() + deltaZ);
                BlockPos bp = new BlockPos(bx, by, bz);
                for (int c = 0; c < 5 && level().isEmptyBlock(bp); ++c, --by) {
                }
                if (level().isEmptyBlock(bp.above()) && !level().isEmptyBlock(bp)) {
                    ax = bx;
                    ay = by;
                    az = bz;
                    arcing = 8 + random.nextInt(5);
                    playSound(SoundsTC.JACOBS.get(), 0.8f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.05f);
                }
            }
        } else {
            super.handleEntityEvent(p_70103_1_);
        }
    }

    @Override
    public void tick() {
        if (getSpawnTimer() == 150) {
            level().broadcastEntityEvent(this, (byte) 18);
        }
        if (getSpawnTimer() > 0) {
            heal(2.0f);
        }
        super.tick();
        if (level().isClientSide) {
            if (isHeadless()) {
                setXRot(0.0f);
                float f1 = Mth.cos(-yBodyRot * 0.017453292f - 3.1415927f);
                float f2 = Mth.sin(-yBodyRot * 0.017453292f - 3.1415927f);
                float f3 = -Mth.cos(-getXRot() * 0.017453292f);
                float f4 = Mth.sin(-getXRot() * 0.017453292f);
                Vec3 v = new Vec3(f2 * f3, f4, f1 * f3);
                if (random.nextInt(20) == 0) {
                    float a = (random.nextFloat() - random.nextFloat()) / 3.0f;
                    float b = (random.nextFloat() - random.nextFloat()) / 3.0f;
                    thaumcraft.client.fx.FXDispatcher.spark((float) (getX() + v.x + a), (float) getY() + getEyeHeight() - 0.75f, (float) (getZ() + v.z + b), 3.0f,
                            0.65f + random.nextFloat() * 0.1f, 1.0f, 1.0f, 0.8f);
                }
                thaumcraft.client.fx.FXDispatcher.drawVentParticles((float) getX() + v.x * 0.4, (float) getY() + getEyeHeight() - 1.25f,
                        (float) getZ() + v.z * 0.4, 0.0, 0.001, 0.0, 5592405, 4.0f);
                if (arcing > 0) {
                    thaumcraft.client.fx.FXDispatcher.arcLightning(getX(), getY() + getBbHeight() / 2.0f, getZ(), ax + 0.5, ay + 1, az + 0.5,
                            0.65f + random.nextFloat() * 0.1f, 1.0f, 1.0f, 1.0f - arcing / 10.0f);
                    --arcing;
                }
            }
        } else {
            if (isHeadless() && beamCharge <= 0) {
                chargingBeam = true;
            }
            if (isHeadless() && chargingBeam) {
                ++beamCharge;
                level().broadcastEntityEvent(this, (byte) 19);
                if (beamCharge == 150) {
                    chargingBeam = false;
                }
            }
        }
    }

    public int getAttackTimer() {
        return attackTimer;
    }
}
