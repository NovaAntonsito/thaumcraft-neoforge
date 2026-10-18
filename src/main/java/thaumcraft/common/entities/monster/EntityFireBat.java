package thaumcraft.common.entities.monster;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of EntityFireBat: a bat of fire from the nether. It hangs from ceilings until a player comes within 4 blocks,
 * then flutters at the nearest player within 12. One bite in ten it blows up (1.5, no fire, no block damage). It does
 * not burn, explosions do not hurt it and water does. Drops gunpowder (loot table).
 * 1.21: updateAITasks is customServerAiStep, like the spell bat. The full-bright glow is in the renderer.
 */
public class EntityFireBat extends Monster {
    private static final EntityDataAccessor<Boolean> HANGING = SynchedEntityData.defineId(EntityFireBat.class, EntityDataSerializers.BOOLEAN);

    private BlockPos currentFlightTarget;
    @Nullable
    public LivingEntity owner = null;
    public int damBonus = 0;
    private int attackTime;

    public EntityFireBat(EntityType<? extends EntityFireBat> type, Level world) {
        super(type, world);
        setIsBatHanging(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HANGING, false);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return (getIsBatHanging() && random.nextInt(4) != 0) ? null : SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public boolean getIsBatHanging() {
        return entityData.get(HANGING);
    }

    public void setIsBatHanging(boolean par1) {
        entityData.set(HANGING, par1);
    }

    @Override
    public void aiStep() {
        if (isInWaterRainOrBubble()) {
            hurt(damageSources().drown(), 1.0f);
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (getIsBatHanging()) {
            setDeltaMovement(Vec3.ZERO);
            setPos(getX(), Mth.floor(getY()) + 1.0 - getBbHeight(), getZ());
        } else {
            Vec3 m = getDeltaMovement();
            setDeltaMovement(m.x, m.y * 0.6000000238418579, m.z);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (attackTime > 0) {
            --attackTime;
        }
        BlockPos blockpos = blockPosition();
        BlockPos blockpos2 = blockpos.above();
        if (getIsBatHanging()) {
            if (!level().getBlockState(blockpos2).isRedstoneConductor(level(), blockpos2)) {
                setIsBatHanging(false);
                level().levelEvent(null, 1025, blockpos, 0);
            } else {
                if (random.nextInt(200) == 0) {
                    yHeadRot = (float) random.nextInt(360);
                }
                if (level().getNearestPlayer(this, 4.0) != null) {
                    setIsBatHanging(false);
                    level().levelEvent(null, 1025, blockpos, 0);
                }
            }
        } else if (getTarget() == null) {
            if (currentFlightTarget != null && (!level().isEmptyBlock(currentFlightTarget) || currentFlightTarget.getY() < level().getMinBuildHeight() + 1)) {
                currentFlightTarget = null;
            }
            if (currentFlightTarget == null || random.nextInt(30) == 0 || currentFlightTarget.distToCenterSqr(position()) < 4.0) {
                currentFlightTarget = new BlockPos((int) getX() + random.nextInt(7) - random.nextInt(7), (int) getY() + random.nextInt(6) - 2,
                        (int) getZ() + random.nextInt(7) - random.nextInt(7));
            }
            flyTowards(currentFlightTarget.getX() + 0.5 - getX(), currentFlightTarget.getY() + 0.1 - getY(), currentFlightTarget.getZ() + 0.5 - getZ());
            if (random.nextInt(100) == 0 && level().getBlockState(blockpos2).isRedstoneConductor(level(), blockpos2)) {
                setIsBatHanging(true);
            }
        } else {
            LivingEntity target = getTarget();
            flyTowards(target.getX() - getX(), target.getY() + target.getEyeHeight() * 0.66f - getY(), target.getZ() - getZ());
        }
        if (getTarget() == null) {
            setTarget(findPlayerToAttack());
        } else if (getTarget().isAlive()) {
            float f = getTarget().distanceTo(this);
            if (isAlive() && hasLineOfSight(getTarget())) {
                attackEntity(getTarget(), f);
            }
        } else {
            setTarget(null);
        }
        if (getTarget() instanceof Player player && player.getAbilities().invulnerable) {
            setTarget(null);
        }
    }

    private void flyTowards(double var1, double var2, double var3) {
        Vec3 m = getDeltaMovement();
        double mx = m.x + (Math.signum(var1) * 0.5 - m.x) * 0.10000000149011612;
        double my = m.y + (Math.signum(var2) * 0.699999988079071 - m.y) * 0.10000000149011612;
        double mz = m.z + (Math.signum(var3) * 0.5 - m.z) * 0.10000000149011612;
        setDeltaMovement(mx, my, mz);
        float var4 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float var5 = Mth.wrapDegrees(var4 - getYRot());
        zza = 0.5f;
        setYRot(getYRot() + var5);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        if (!level().isClientSide && getIsBatHanging()) {
            setIsBatHanging(false);
        }
        return super.hurt(source, amount);
    }

    protected void attackEntity(Entity entity, float par2) {
        if (attackTime <= 0 && par2 < Math.max(2.5f, entity.getBbWidth() * 1.1f) && entity.getBoundingBox().maxY > getBoundingBox().minY
                && entity.getBoundingBox().minY < getBoundingBox().maxY) {
            attackTime = 20 + random.nextInt(20);
            if (random.nextInt(10) == 0 && !level().isClientSide) {
                entity.invulnerableTime = 0;
                level().explode(this, getX(), getY(), getZ(), 1.5f, false, Level.ExplosionInteraction.NONE);
                discard();
            }
            playSound(SoundEvents.BAT_HURT, 0.5f, 0.9f + random.nextFloat() * 0.2f);
            doHurtTarget(entity);
        }
    }

    @Nullable
    protected LivingEntity findPlayerToAttack() {
        return level().getNearestPlayer(this, 12.0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setIsBatHanging(nbt.getBoolean("hang"));
        damBonus = nbt.getByte("damBonus");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("hang", getIsBatHanging());
        nbt.putByte("damBonus", (byte) damBonus);
    }

    /** getCanSpawnHere: dark enough (light at most 0 to 6) and room to spawn; isValidLightLevel was always true. */
    public static boolean checkFireBatSpawnRules(EntityType<EntityFireBat> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos,
            RandomSource rand) {
        return world.getDifficulty() != Difficulty.PEACEFUL && world.getMaxLocalRawBrightness(pos) <= rand.nextInt(7)
                && Mob.checkMobSpawnRules(type, world, reason, pos, rand);
    }
}
