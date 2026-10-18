package thaumcraft.common.entities.monster.boss;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.monster.tainted.EntityTaintacle;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityTaintacleGiant: the taint boss of the eldritch dungeons. A huge taintacle with a purple boss bar that
 * slowly heals. A single hit over 35 is capped at 35 and enrages it for 10 seconds (regeneration, strength, haste).
 * The last one alive within 48 blocks drops a primordial pearl. It never despawns and breathes under water (tag).
 * It spawns as a champion. TODO: it has no spawn place until the Outer Lands.
 */
public class EntityTaintacleGiant extends EntityTaintacle implements IEldritchMob {
    private static final EntityDataAccessor<Integer> AGGRO = SynchedEntityData.defineId(EntityTaintacleGiant.class, EntityDataSerializers.INT);
    protected final ServerBossEvent bossInfo;

    public EntityTaintacleGiant(EntityType<? extends EntityTaintacleGiant> type, Level world) {
        super(type, world);
        bossInfo = (ServerBossEvent) new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)
                .setDarkenScreen(true);
        xpReward = 20;
    }

    public EntityTaintacleGiant(Level world) {
        this(TCEntities.TAINTACLE_GIANT.get(), world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 175.0).add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AGGRO, 0);
    }

    public int getAnger() {
        return entityData.get(AGGRO);
    }

    public void setAnger(int anger) {
        entityData.set(AGGRO, anger);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        EntityUtils.makeChampion(this, true);
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (getAnger() > 0) {
            setAnger(getAnger() - 1);
        }
        if (level().isClientSide && random.nextInt(15) == 0 && getAnger() > 0) {
            double d0 = random.nextGaussian() * 0.02;
            double d2 = random.nextGaussian() * 0.02;
            double d3 = random.nextGaussian() * 0.02;
            level().addParticle(ParticleTypes.ANGRY_VILLAGER, getX() + random.nextFloat() * getBbWidth() - getBbWidth() / 2.0,
                    getBoundingBox().minY + getBbHeight() + random.nextFloat() * 0.5, getZ() + random.nextFloat() * getBbWidth() - getBbWidth() / 2.0,
                    d0, d2, d3);
        }
        if (!level().isClientSide && tickCount % 30 == 0) {
            heal(1.0f);
        }
    }

    /**
     * dropFewItems: the primordial pearl, if no other giant taintacle is within 48 blocks.
     * TODO: the original dropped it as an EntitySpecialItem (a floating item); a plain item that floats up for now.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        List<EntityTaintacleGiant> ents = EntityUtils.getEntitiesInRange(level(), getX(), getY(), getZ(), this, EntityTaintacleGiant.class, 48.0);
        if (ents == null || ents.isEmpty()) {
            ItemEntity item = spawnAtLocation(new ItemStack(ConfigItems.primordialPearl.get()), getBbHeight() / 2.0f);
            if (item != null) {
                item.setDeltaMovement(0.0, 0.1, 0.0);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (!level().isClientSide && damage > 35.0f) {
            if (getAnger() == 0) {
                addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, (int) (damage / 15.0f)));
                addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, (int) (damage / 10.0f)));
                addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, (int) (damage / 40.0f)));
                setAnger(200);
                if (source.getEntity() instanceof Player player) {
                    player.displayClientMessage(Component.empty().append(getName()).append(" ").append(Component.translatable("tc.boss.enrage")), true);
                }
            }
            damage = 35.0f;
        }
        return super.hurt(source, damage);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossInfo.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossInfo.removePlayer(player);
    }
}
