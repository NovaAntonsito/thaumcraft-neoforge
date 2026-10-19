package thaumcraft.common.entities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.entities.projectile.EntityEldritchOrb;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.fx.PacketFXSonic;
import thaumcraft.common.lib.network.misc.PacketMiscEvent;
import thaumcraft.common.network.TCNetwork;

/**
 * The eldritch guardian: a ghostly warden of the places where reality is thin. Up close it strikes hard; from afar it
 * hurls eldritch orbs or, now and then, screeches (wither and temporary warp). While one is around, nearby players
 * are wrapped in fog. It takes half damage from magic.
 * In the Outer Lands it spawns with absorption of half its health, which slowly comes back, and brings no fog.
 * 1.21: absorption is capped by an attribute, so its cap is its health (Outer Lands plus warded champion). Undead and smite come from entity type
 * tags, and the eye height (2.1) is set on the entity type. Its melee attack was meant to set the target on fire
 * when it burned empty handed, but the original compared the held item with null, which never happens, so it never did.
 */
public class EntityEldritchGuardian extends Monster implements RangedAttackMob, IEldritchMob {
    public float armLiftL = 0.0f;
    public float armLiftR = 0.0f;
    boolean lastBlast = false;

    public EntityEldritchGuardian(EntityType<? extends EntityEldritchGuardian> type, Level world) {
        super(type, world);
        xpReward = 20;
        if (getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new AILongRangeAttack(this, 8.0, 1.0, 20, 40, 24.0f));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MAX_ABSORPTION, 50.0);
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(ServerLevelAccessor world, net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType reason, @javax.annotation.Nullable net.minecraft.world.entity.SpawnGroupData data) {
        data = super.finalizeSpawn(world, difficulty, reason, data);
        if (thaumcraft.common.world.dim.OuterLands.is(world.getLevel())) {
            int bh = (int) getAttribute(Attributes.MAX_HEALTH).getBaseValue() / 2;
            setAbsorptionAmount(getAbsorptionAmount() + bh);
        }
        return data;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (thaumcraft.common.world.dim.OuterLands.is(level()) && invulnerableTime <= 0 && tickCount % 25 == 0) {
            int bh = (int) getAttribute(Attributes.MAX_HEALTH).getBaseValue() / 2;
            if (getAbsorptionAmount() < bh) {
                setAbsorptionAmount(getAbsorptionAmount() + 1.0f);
            }
        }
    }

    @Override
    public int getArmorValue() {
        return 4;
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    /** isMagicDamage was set on magic, indirect magic and thorns, the damage types witches resist. */
    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            damage /= 2.0f;
        }
        return super.hurt(source, damage);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (armLiftL > 0.0f) {
                armLiftL -= 0.05f;
            }
            if (armLiftR > 0.0f) {
                armLiftR -= 0.05f;
            }
            float x = (float) (getX() + (random.nextFloat() - random.nextFloat()) * 0.2f);
            float z = (float) (getZ() + (random.nextFloat() - random.nextFloat()) * 0.2f);
            thaumcraft.client.fx.FXDispatcher.wispFXEG(x, (float) (getY() + 0.22 * getBbHeight()), z, this);
        } else if (!thaumcraft.common.world.dim.OuterLands.is(level()) && (tickCount == 0 || tickCount % 100 == 0)
                && level().getDifficulty() != Difficulty.EASY) {
            double d6 = (level().getDifficulty() == Difficulty.HARD) ? 576.0 : 256.0;
            for (Player entityplayer1 : level().players()) {
                if (entityplayer1.isAlive() && entityplayer1 instanceof ServerPlayer sp && entityplayer1.distanceToSqr(getX(), getY(), getZ()) < d6) {
                    PacketDistributor.sendToPlayer(sp, new PacketMiscEvent(PacketMiscEvent.MIST_EVENT_SHORT));
                }
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.EGIDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsTC.EGDEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (hasRestriction() && getRestrictRadius() > 0.0f) {
            nbt.putInt("HomeD", (int) getRestrictRadius());
            nbt.putInt("HomeX", getRestrictCenter().getX());
            nbt.putInt("HomeY", getRestrictCenter().getY());
            nbt.putInt("HomeZ", getRestrictCenter().getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("HomeD")) {
            restrictTo(new BlockPos(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ")), nbt.getInt("HomeD"));
        }
    }

    @Override
    public void handleEntityEvent(byte p_70103_1_) {
        if (p_70103_1_ == 15) {
            armLiftL = 0.5f;
        } else if (p_70103_1_ == 16) {
            armLiftR = 0.5f;
        } else if (p_70103_1_ == 17) {
            armLiftL = 0.9f;
            armLiftR = 0.9f;
        } else {
            super.handleEntityEvent(p_70103_1_);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !hasRestriction();
    }

    /** getCanSpawnHere: no other guardian nearby, and any light level will do (isValidLightLevel). */
    public static boolean checkGuardianSpawnRules(EntityType<EntityEldritchGuardian> type, ServerLevelAccessor world,
            MobSpawnType reason, BlockPos pos, RandomSource rand) {
        return world.getEntitiesOfClass(EntityEldritchGuardian.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty()
                && Monster.checkAnyLightMonsterSpawnRules(type, world, reason, pos, rand);
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** Mostly an orb from alternating sides; otherwise, if it can see the target, the screech. */
    @Override
    public void performRangedAttack(LivingEntity entitylivingbase, float f) {
        if (random.nextFloat() > 0.15f) {
            EntityEldritchOrb blast = new EntityEldritchOrb(level(), this);
            lastBlast = !lastBlast;
            level().broadcastEntityEvent(this, (byte) (lastBlast ? 16 : 15));
            int rr = lastBlast ? 90 : 180;
            double xx = Mth.cos((getYRot() + rr) % 360.0f / 180.0f * 3.1415927f) * 0.5f;
            double zz = Mth.sin((getYRot() + rr) % 360.0f / 180.0f * 3.1415927f) * 0.5f;
            blast.setPos(blast.getX() - xx, blast.getY(), blast.getZ() - zz);
            Vec3 v = entitylivingbase.position().add(entitylivingbase.getDeltaMovement().scale(10.0)).subtract(position()).normalize();
            blast.shoot(v.x, v.y, v.z, 1.1f, 2.0f);
            playSound(SoundsTC.EGATTACK.get(), 2.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(blast);
        } else if (hasLineOfSight(entitylivingbase)) {
            TCNetwork.sendToAllAround(level(), getX(), getY(), getZ(), 32.0, new PacketFXSonic(getId()));
            entitylivingbase.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
            if (entitylivingbase instanceof Player p) {
                InternalMethodHandler.addWarpToPlayer(p, 1 + level().random.nextInt(3), PlayerWarp.EnumWarpType.TEMPORARY);
            }
            playSound(SoundsTC.EGSCREECH.get(), 3.0f, 1.0f + random.nextFloat() * 0.1f);
        }
    }

    @Override
    public boolean isAlliedTo(Entity el) {
        return el instanceof IEldritchMob;
    }
}
