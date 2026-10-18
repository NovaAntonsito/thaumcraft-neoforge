package thaumcraft.common.entities.monster.cult;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AICultistHurtByTarget;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.ai.misc.AIAltarFocus;
import thaumcraft.common.entities.projectile.EntityGolemOrb;
import thaumcraft.common.lib.SoundsTC;

/**
 * The crimson cleric: robes, and it throws fireballs or homing orbs from afar. A ritualist floats in front of its
 * altar (the home position) until something hurts it.
 */
public class EntityCultistCleric extends EntityCultist implements RangedAttackMob, IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> RITUALIST = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BOOLEAN);
    public int rage = 0;

    public EntityCultistCleric(EntityType<? extends EntityCultistCleric> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityCultist.createAttributes().add(Attributes.MAX_HEALTH, 24.0);
    }

    /** initEntityAI. 1.21 has no EntityAIRestrictOpenDoor (it kept villagers inside at night). */
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AIAltarFocus(this));
        goalSelector.addGoal(2, new AILongRangeAttack(this, 2.0, 1.0, 20, 40, 24.0f));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new AICultistHurtByTarget(this, true));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, thaumcraft.common.entities.monster.EntityEldritchGuardian.class, true));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance diff) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ConfigItems.crimsonRobeHelm.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ConfigItems.crimsonRobeChest.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ConfigItems.crimsonRobeLegs.get()));
        if (random.nextFloat() < ((level().getDifficulty() == Difficulty.HARD) ? 0.3f : 0.1f)) {
            setItemSlot(EquipmentSlot.FEET, new ItemStack(ConfigItems.crimsonBoots.get()));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float f) {
        double d0 = target.getX() - getX();
        double d2 = target.getBoundingBox().minY + target.getBbHeight() / 2.0f - (getY() + getBbHeight() / 2.0f);
        double d3 = target.getZ() - getZ();
        swing(InteractionHand.MAIN_HAND);
        float rf = random.nextFloat();
        if (rf > 0.66f) {
            EntityGolemOrb blast = new EntityGolemOrb(level(), this, target, true);
            Vec3 v = target.position().add(target.getDeltaMovement().scale(10.0)).subtract(position()).normalize();
            blast.setPos(blast.getX() + v.x, blast.getY() + v.y, blast.getZ() + v.z);
            blast.shoot(v.x, v.y, v.z, 0.66f, 3.0f);
            playSound(SoundsTC.EGATTACK.get(), 1.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(blast);
        } else {
            float f2 = Mth.sqrt(f) * 0.5f;
            level().levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, blockPosition(), 0);
            for (int i = 0; i < 3; ++i) {
                SmallFireball fireball = new SmallFireball(level(), this,
                        new Vec3(d0 + random.nextGaussian() * f2, d2, d3 + random.nextGaussian() * f2));
                fireball.setPos(fireball.getX(), getY() + getBbHeight() / 2.0f + 0.5, fireball.getZ());
                level().addFreshEntity(fireball);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !getIsRitualist();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RITUALIST, false);
    }

    public boolean getIsRitualist() {
        return entityData.get(RITUALIST);
    }

    public void setIsRitualist(boolean par1) {
        entityData.set(RITUALIST, par1);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        setIsRitualist(false);
        return super.hurt(source, amount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setIsRitualist(nbt.getBoolean("ritualist"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("ritualist", getIsRitualist());
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data) {
        data.writeInt(getRestrictCenter().getX());
        data.writeInt(getRestrictCenter().getY());
        data.writeInt(getRestrictCenter().getZ());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data) {
        restrictTo(new BlockPos(data.readInt(), data.readInt(), data.readInt()), 8);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && getIsRitualist()) {
            BlockPos home = getRestrictCenter();
            double d0 = home.getX() + 0.5 - getX();
            double d2 = home.getY() + 1.5 - (getY() + getEyeHeight());
            double d3 = home.getZ() + 0.5 - getZ();
            double d4 = Math.sqrt(d0 * d0 + d3 * d3);
            float f = (float) (Math.atan2(d3, d0) * 180.0 / Math.PI) - 90.0f;
            float f2 = (float) (-(Math.atan2(d2, d4) * 180.0 / Math.PI));
            setXRot(updateRotation(getXRot(), f2, 10.0f));
            yHeadRot = updateRotation(yHeadRot, f, (float) getMaxHeadXRot());
        }
        if (!level().isClientSide && getIsRitualist() && rage >= 5) {
            setIsRitualist(false);
        }
    }

    private float updateRotation(float from, float to, float max) {
        float f3 = Mth.wrapDegrees(to - from);
        if (f3 > max) {
            f3 = max;
        }
        if (f3 < -max) {
            f3 = -max;
        }
        return from + f3;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.CHANT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    @Override
    public void handleEntityEvent(byte par1) {
        if (par1 == 19) {
            for (int i = 0; i < 3; ++i) {
                double d0 = random.nextGaussian() * 0.02;
                double d2 = random.nextGaussian() * 0.02;
                double d3 = random.nextGaussian() * 0.02;
                level().addParticle(ParticleTypes.ANGRY_VILLAGER, getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                        getY() + 0.5 + random.nextFloat() * getBbHeight(), getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(), d0, d2, d3);
            }
        } else {
            super.handleEntityEvent(par1);
        }
    }
}
