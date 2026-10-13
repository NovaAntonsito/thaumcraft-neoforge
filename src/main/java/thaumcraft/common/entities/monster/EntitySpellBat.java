package thaumcraft.common.entities.monster;

import java.awt.Color;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCEntities;

/**
 * The bat of the spell bat focus medium. It flutters around like a bat, chases the nearest enemy (or friend) and
 * casts the rest of the spell on it every 2 seconds, losing a heart each time. It lasts 30 seconds.
 * 1.21: updateAITasks is customServerAiStep and isOnSameTeam is isAlliedTo. The full-bright glow is in the renderer.
 */
public class EntitySpellBat extends Monster implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> FRIENDLY = SynchedEntityData.defineId(EntitySpellBat.class, EntityDataSerializers.BOOLEAN);

    private BlockPos currentFlightTarget;
    public LivingEntity owner = null;
    FocusPackage focusPackage;
    private UUID ownerUniqueId;
    public int damBonus = 0;
    private int attackTime;
    FocusEffect[] effects = null;
    public int color = 16777215;

    public EntitySpellBat(EntityType<? extends EntitySpellBat> type, Level level) {
        super(type, level);
    }

    public EntitySpellBat(FocusPackage pac, boolean friendly) {
        super(TCEntities.SPELL_BAT.get(), pac.world);
        focusPackage = pac;
        setOwner(pac.getCaster());
        setIsFriendly(friendly);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FRIENDLY, false);
    }

    public boolean getIsFriendly() {
        return entityData.get(FRIENDLY);
    }

    public void setIsFriendly(boolean par1) {
        entityData.set(FRIENDLY, par1);
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

    public void setOwner(@Nullable LivingEntity ownerIn) {
        owner = ownerIn;
        ownerUniqueId = ownerIn == null ? null : ownerIn.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (owner == null && ownerUniqueId != null && level() instanceof ServerLevel sl) {
            if (sl.getEntity(ownerUniqueId) instanceof LivingEntity living) {
                owner = living;
            }
        }
        return owner;
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
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

    @Nullable
    @Override
    public PlayerTeam getTeam() {
        LivingEntity entitylivingbase = getOwner();
        if (entitylivingbase != null) {
            return entitylivingbase.getTeam();
        }
        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity otherEntity) {
        LivingEntity owner = getOwner();
        if (otherEntity == owner) {
            return true;
        }
        if (owner != null) {
            return owner.isAlliedTo(otherEntity) || otherEntity.isAlliedTo(owner);
        }
        return super.isAlliedTo(otherEntity);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && (tickCount > 600 || getOwner() == null || focusPackage == null)) {
            discard();
        }
        Vec3 m = getDeltaMovement();
        setDeltaMovement(m.x, m.y * 0.6000000238418579, m.z);
        if (isAlive() && level().isClientSide && focusPackage != null) {
            if (effects == null) {
                effects = focusPackage.getFocusEffects();
                if (effects.length > 0) {
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    for (FocusEffect ef : effects) {
                        Color c = new Color(FocusEngine.getElementColor(ef.getKey()));
                        r += c.getRed();
                        g += c.getGreen();
                        b += c.getBlue();
                    }
                    r /= effects.length;
                    g /= effects.length;
                    b /= effects.length;
                    color = new Color(r, g, b).getRGB();
                }
            }
            if (effects.length > 0) {
                FocusEffect eff = effects[random.nextInt(effects.length)];
                eff.renderParticleFX(level(), getX() + random.nextGaussian() * 0.125, getY() + getBbHeight() / 2.0f + random.nextGaussian() * 0.125,
                        getZ() + random.nextGaussian() * 0.125, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (attackTime > 0) {
            --attackTime;
        }
        LivingEntity target = getTarget();
        double var1;
        double var2;
        double var3;
        if (target == null) {
            if (currentFlightTarget != null && (!level().isEmptyBlock(currentFlightTarget) || currentFlightTarget.getY() < level().getMinBuildHeight() + 1)) {
                currentFlightTarget = null;
            }
            if (currentFlightTarget == null || random.nextInt(30) == 0 || currentFlightTarget.distToCenterSqr(position()) < 4.0) {
                currentFlightTarget = new BlockPos((int) getX() + random.nextInt(7) - random.nextInt(7), (int) getY() + random.nextInt(6) - 2,
                        (int) getZ() + random.nextInt(7) - random.nextInt(7));
            }
            var1 = currentFlightTarget.getX() + 0.5 - getX();
            var2 = currentFlightTarget.getY() + 0.1 - getY();
            var3 = currentFlightTarget.getZ() + 0.5 - getZ();
        } else {
            var1 = target.getX() - getX();
            var2 = target.getY() + target.getEyeHeight() * 0.66f - getY();
            var3 = target.getZ() - getZ();
        }
        Vec3 m = getDeltaMovement();
        double mx = m.x + (Math.signum(var1) * 0.5 - m.x) * 0.10000000149011612;
        double my = m.y + (Math.signum(var2) * 0.699999988079071 - m.y) * 0.10000000149011612;
        double mz = m.z + (Math.signum(var3) * 0.5 - m.z) * 0.10000000149011612;
        setDeltaMovement(mx, my, mz);
        float var4 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float var5 = Mth.wrapDegrees(var4 - getYRot());
        zza = 0.5f;
        setYRot(getYRot() + var5);
        if (getTarget() == null) {
            setTarget(findTargetToAttack());
        } else if (getTarget().isAlive()) {
            float f = getTarget().distanceTo(this);
            if (isAlive() && hasLineOfSight(getTarget())) {
                attackEntity(getTarget(), f);
            }
        } else {
            setTarget(null);
        }
        if (!getIsFriendly() && getTarget() instanceof Player player && player.getAbilities().invulnerable) {
            setTarget(null);
        }
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

    protected void attackEntity(Entity target, float par2) {
        if (attackTime <= 0 && par2 < Math.max(2.5f, target.getBbWidth() * 1.1f) && target.getBoundingBox().maxY > getBoundingBox().minY
                && target.getBoundingBox().minY < getBoundingBox().maxY) {
            attackTime = 40;
            if (!level().isClientSide) {
                Vec3 hitVec = target.position().add(0.0, target.getBbHeight() / 2.0f, 0.0);
                HitResult ray = new EntityHitResult(target, hitVec);
                Trajectory tra = new Trajectory(position(), hitVec.subtract(position()));
                FocusEngine.runFocusPackage(focusPackage.copy(getOwner()), new Trajectory[] {tra}, new HitResult[] {ray});
                setHealth(getHealth() - 1.0f);
            }
            playSound(SoundEvents.BAT_HURT, 0.5f, 0.9f + random.nextFloat() * 0.2f);
        }
    }

    @Override
    protected void doPush(Entity entityIn) {
        if (getIsFriendly()) {
            return;
        }
        super.doPush(entityIn);
    }

    protected LivingEntity findTargetToAttack() {
        double var1 = 12.0;
        List<LivingEntity> list = EntityUtils.getEntitiesInRange(level(), getX(), getY(), getZ(), this, LivingEntity.class, var1);
        double d = Double.MAX_VALUE;
        LivingEntity ret = null;
        for (LivingEntity e : list) {
            if (!e.isAlive()) {
                continue;
            }
            if (getIsFriendly()) {
                if (!EntityUtils.isFriendly(getOwner(), e)) {
                    continue;
                }
            } else {
                if (EntityUtils.isFriendly(getOwner(), e)) {
                    continue;
                }
                if (isAlliedTo(e)) {
                    continue;
                }
            }
            double ed = distanceToSqr(e);
            if (ed >= d) {
                continue;
            }
            d = ed;
            ret = e;
        }
        return ret;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.hasUUID("OwnerUUID")) {
            ownerUniqueId = nbt.getUUID("OwnerUUID");
        }
        setIsFriendly(nbt.getBoolean("friendly"));
        try {
            (focusPackage = new FocusPackage()).deserialize(nbt.getCompound("pack"));
        } catch (Exception ex) {
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (ownerUniqueId != null) {
            nbt.putUUID("OwnerUUID", ownerUniqueId);
        }
        if (focusPackage != null) {
            nbt.put("pack", focusPackage.serialize());
        }
        nbt.putBoolean("friendly", getIsFriendly());
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }
}
