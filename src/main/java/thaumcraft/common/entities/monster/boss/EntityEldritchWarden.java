package thaumcraft.common.entities.monster.boss;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
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
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.projectile.EntityEldritchOrb;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.fx.PacketFXBlockArc;
import thaumcraft.common.lib.network.fx.PacketFXSonic;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.registry.TCAttributes;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityEldritchWarden: a giant eldritch guardian, and a boss. It rises out of the ground over 7.5 seconds,
 * starts with a shield of absorption worth two thirds of its health (a second, blue boss bar) that grows back 1 every
 * 25 ticks while it is not being hit, and leaves a trail of sapping field wherever it walks. From afar it hurls
 * eldritch orbs from alternating hands or screeches (knockback, wither, weakness and temporary warp). The first time
 * the shield is gone it goes into a frenzy: it teleports home and, untouchable, sends out rings of sapping field.
 * As a champion it is named "[title] the [modifier]".
 * The outer lands are not ported. 1.21: the absorption needs a max absorption attribute; the sapping field under it
 * is only placed on the server (the client copy of 1.12 only made ghost blocks); the portal particles of the
 * teleport were spawned on the server in 1.12, which drew nothing, so they are left out.
 */
public class EntityEldritchWarden extends EntityThaumcraftBoss implements RangedAttackMob, IEldritchMob {
    private static final EntityDataAccessor<Byte> NAME = SynchedEntityData.defineId(EntityEldritchWarden.class, EntityDataSerializers.BYTE);
    static final String[] titles = new String[] { "Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha", "Oorn", "Shaikorth",
            "Rhan-Tegoth", "Rhogog", "Shudde M'ell", "Vulthoom", "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon" };
    protected final ServerBossEvent bossInfo2;
    boolean fieldFrenzy = false;
    int fieldFrenzyCounter = 0;
    boolean lastBlast = false;
    public float armLiftL = 0.0f;
    public float armLiftR = 0.0f;

    public EntityEldritchWarden(EntityType<? extends EntityEldritchWarden> type, Level world) {
        super(type, world);
        bossInfo2 = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossInfo2.removePlayer(player);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossInfo2.addPlayer(player);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new AILongRangeAttack(this, 3.0, 1.0, 20, 40, 24.0f));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    @Override
    public void generateName() {
        int t = (int) getAttributeValue(TCAttributes.CHAMPION_MOD);
        if (t >= 0) {
            setCustomName(Component.translatable("entity.thaumcraft.eldritch_warden.custom", getTitle(), ChampionModifier.mods[t].getModNameLocalized()));
        }
    }

    private String getTitle() {
        return titles[entityData.get(NAME)];
    }

    private void setTitle(int title) {
        entityData.set(NAME, (byte) title);
    }

    /** The absorption shield is two thirds of the base health, so the cap is raised to match. */
    public static AttributeSupplier.Builder createAttributes() {
        return EntityThaumcraftBoss.createAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MAX_ABSORPTION, 400.0 * 0.66);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(NAME, (byte) 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putByte("title", entityData.get(NAME));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setTitle(nbt.getByte("title"));
    }

    @Override
    public int getArmorValue() {
        return super.getArmorValue() + 4;
    }

    /** updateAITasks: no AI during the frenzy. */
    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || fieldFrenzyCounter > 0;
    }

    @Override
    public void tick() {
        if (getSpawnTimer() == 150) {
            level().broadcastEntityEvent(this, (byte) 18);
        }
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
            ClientFX.wispFXEG(level(), x, (float) (getY() + 0.25 * getBbHeight()), z);
            if (spawnTimer > 0) {
                float he = Math.max(1.0f, getBbHeight() * ((150 - spawnTimer) / 150.0f));
                for (int a = 0; a < 33; ++a) {
                    ClientFX.smokeSpiral(level(), getX(), getBoundingBox().minY + he / 2.0f, getZ(), he, random.nextInt(360),
                            Mth.floor(getBoundingBox().minY) - 1, 2232623);
                }
            }
        } else {
            // updateAITasks: the shield grows back, and its bar follows it.
            int bh = (int) (getAttributeBaseValue(Attributes.MAX_HEALTH) * 0.66);
            if (invulnerableTime <= 0 && tickCount % 25 == 0 && getAbsorptionAmount() < bh) {
                setAbsorptionAmount(getAbsorptionAmount() + 1.0f);
            }
            bossInfo2.setProgress(getAbsorptionAmount() / bh);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            for (int l = 0; l < 4; ++l) {
                int i = Mth.floor(getX() + (l % 2 * 2 - 1) * 0.25f);
                int j = Mth.floor(getY());
                int k = Mth.floor(getZ() + (l / 2 % 2 * 2 - 1) * 0.25f);
                BlockPos bp = new BlockPos(i, j, k);
                if (level().isEmptyBlock(bp)) {
                    level().setBlockAndUpdate(bp, ConfigBlocks.effectSap.get().defaultBlockState());
                }
            }
        }
        if (!level().isClientSide && fieldFrenzyCounter > 0) {
            if (fieldFrenzyCounter == 150) {
                teleportHome();
            }
            performFieldFrenzy();
        }
    }

    /** Every half second, a wider ring of sapping field that fades in 12.5 to 20 seconds, with sparks to some of it. */
    private void performFieldFrenzy() {
        if (fieldFrenzyCounter < 121 && fieldFrenzyCounter % 10 == 0) {
            level().broadcastEntityEvent(this, (byte) 17);
            double radius = (150 - fieldFrenzyCounter) / 8.0;
            int d = 1 + fieldFrenzyCounter / 8;
            int i = Mth.floor(getX());
            int j = Mth.floor(getY());
            int k = Mth.floor(getZ());
            for (int q = 0; q < 180 / d; ++q) {
                double radians = Math.toRadians(q * 2 * d);
                int deltaX = (int) (radius * Math.cos(radians));
                int deltaZ = (int) (radius * Math.sin(radians));
                BlockPos bp = new BlockPos(i + deltaX, j, k + deltaZ);
                if (level().isEmptyBlock(bp) && level().getBlockState(bp.below()).isRedstoneConductor(level(), bp.below())) {
                    level().setBlockAndUpdate(bp, ConfigBlocks.effectSap.get().defaultBlockState());
                    level().scheduleTick(bp, ConfigBlocks.effectSap.get(), 250 + random.nextInt(150));
                    if (random.nextFloat() < 0.3f) {
                        TCNetwork.sendToAllAround(level(), getX(), getY(), getZ(), 32.0,
                                new PacketFXBlockArc(bp, this, 0.5f + random.nextFloat() * 0.2f, 0.0f, 0.5f + random.nextFloat() * 0.2f));
                    }
                }
            }
            playSound(SoundsTC.ZAP.get(), 1.0f, 0.9f + random.nextFloat() * 0.1f);
        }
        --fieldFrenzyCounter;
    }

    /**
     * Back home, if there is floor and room there. As in the original, the retries pick new columns but keep testing
     * the home block, so it only works when home itself is clear.
     */
    protected void teleportHome() {
        BlockPos home = getRestrictCenter();
        EntityTeleportEvent.EnderEntity event = EventHooks.onEnderTeleport(this, home.getX(), home.getY(), home.getZ());
        if (event.isCanceled()) {
            return;
        }
        double d3 = getX();
        double d4 = getY();
        double d5 = getZ();
        setPosRaw(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        boolean flag = false;
        int i = Mth.floor(getX());
        int j = Mth.floor(getY());
        int k = Mth.floor(getZ());
        BlockPos bp = new BlockPos(i, j, k);
        if (level().hasChunkAt(bp)) {
            boolean flag2 = false;
            int tries = 20;
            while (!flag2 && tries > 0) {
                BlockState block = level().getBlockState(bp.below());
                BlockState block2 = level().getBlockState(bp);
                if (block.blocksMotion() && !block2.blocksMotion()) {
                    flag2 = true;
                } else {
                    i = Mth.floor(getX()) + random.nextInt(8) - random.nextInt(8);
                    k = Mth.floor(getZ()) + random.nextInt(8) - random.nextInt(8);
                    --tries;
                }
            }
            if (flag2) {
                setPos(i + 0.5, j + 0.1, k + 0.5);
                if (level().noCollision(this)) {
                    flag = true;
                }
            }
        }
        if (!flag) {
            setPos(d3, d4, d5);
            return;
        }
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource ds) {
        return fieldFrenzyCounter > 0 || super.isInvulnerableTo(ds);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (isInvulnerableTo(source) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER)) {
            return false;
        }
        boolean aef = super.hurt(source, damage);
        if (!level().isClientSide && aef && !fieldFrenzy && getAbsorptionAmount() <= 0.0f) {
            fieldFrenzy = true;
            fieldFrenzyCounter = 150;
        }
        return aef;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        spawnTimer = 150;
        setTitle(random.nextInt(titles.length));
        setAbsorptionAmount((float) (getAbsorptionAmount() + getAttributeBaseValue(Attributes.MAX_HEALTH) * 0.66));
        return super.finalizeSpawn(world, diff, reason, data);
    }

    /** Mostly an orb from alternating hands; otherwise, if it can see the target, the screech. */
    @Override
    public void performRangedAttack(LivingEntity entitylivingbase, float f) {
        if (random.nextFloat() > 0.2f) {
            EntityEldritchOrb blast = new EntityEldritchOrb(level(), this);
            lastBlast = !lastBlast;
            level().broadcastEntityEvent(this, (byte) (lastBlast ? 16 : 15));
            int rr = lastBlast ? 90 : 180;
            double xx = Mth.cos((getYRot() + rr) % 360.0f / 180.0f * 3.1415927f) * 0.5f;
            double yy = 0.13;
            double zz = Mth.sin((getYRot() + rr) % 360.0f / 180.0f * 3.1415927f) * 0.5f;
            blast.setPos(blast.getX() - xx, blast.getY() - yy, blast.getZ() - zz);
            double d0 = entitylivingbase.getX() + entitylivingbase.getDeltaMovement().x - getX();
            double d2 = entitylivingbase.getY() - getY() - entitylivingbase.getBbHeight() / 2.0f;
            double d3 = entitylivingbase.getZ() + entitylivingbase.getDeltaMovement().z - getZ();
            blast.shoot(d0, d2, d3, 1.0f, 2.0f);
            playSound(SoundsTC.EGATTACK.get(), 2.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(blast);
        } else if (hasLineOfSight(entitylivingbase)) {
            TCNetwork.sendToAllAround(level(), getX(), getY(), getZ(), 32.0, new PacketFXSonic(getId()));
            entitylivingbase.push(-Mth.sin(getYRot() * 3.1415927f / 180.0f) * 1.5f, 0.1, Mth.cos(getYRot() * 3.1415927f / 180.0f) * 1.5f);
            entitylivingbase.hurtMarked = true;
            entitylivingbase.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
            entitylivingbase.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
            if (entitylivingbase instanceof Player p) {
                InternalMethodHandler.addWarpToPlayer(p, 3 + level().random.nextInt(3), PlayerWarp.EnumWarpType.TEMPORARY);
            }
            playSound(SoundsTC.EGSCREECH.get(), 4.0f, 1.0f + random.nextFloat() * 0.1f);
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
        } else if (p_70103_1_ == 18) {
            spawnTimer = 150;
        } else {
            super.handleEntityEvent(p_70103_1_);
        }
    }

    /** canAttackClass. */
    @Override
    public boolean canAttackType(EntityType<?> type) {
        return type != TCEntities.ELDRITCH_GUARDIAN.get() && super.canAttackType(type);
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
}
