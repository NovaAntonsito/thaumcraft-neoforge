package thaumcraft.common.entities.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.level.Level;

/**
 * Port of EntityGiantBrainyZombie ("Furious Zombie"): every hit makes it angrier (up to 2), and while its anger is
 * over 1 it grows and hits harder, calming down slowly. It leaps at its target.
 * The 1.12 dropFewItems (rotten flesh) never ran, because zombies use their loot table; it is left out.
 */
public class EntityGiantBrainyZombie extends EntityBrainyZombie {
    private static final EntityDataAccessor<Float> ANGER = SynchedEntityData.defineId(EntityGiantBrainyZombie.class, EntityDataSerializers.FLOAT);
    /** The anger the size was last set with: in 1.12 setSize only ran while the anger was over 1. */
    private float sizeAnger = 0.0f;

    public EntityGiantBrainyZombie(EntityType<? extends EntityGiantBrainyZombie> type, Level world) {
        super(type, world);
        xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityBrainyZombie.createAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER, 0.0f);
    }

    public float getAnger() {
        return entityData.get(ANGER);
    }

    public void setAnger(float par1) {
        entityData.set(ANGER, par1);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (getAnger() > 1.0f) {
            if (!level().isClientSide) {
                setAnger(getAnger() - 0.002f);
            }
            sizeAnger = getAnger();
            refreshDimensions();
        }
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.0f + (getAnger() - 1.0f) * 5.0f);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(0.6f + sizeAnger * 0.6f, 1.95f + sizeAnger * 1.95f).withEyeHeight(1.74f + sizeAnger * 1.74f);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        setAnger(Math.min(2.0f, getAnger() + 0.1f));
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("anger", getAnger());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setAnger(nbt.getFloat("anger"));
    }
}
