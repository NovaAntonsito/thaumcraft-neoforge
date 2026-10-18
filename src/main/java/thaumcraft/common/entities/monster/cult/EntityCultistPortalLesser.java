package thaumcraft.common.entities.monster.cult;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCEntities;

/**
 * The lesser crimson portal. It wakes up when a player comes within 32 blocks and, while the player is in sight,
 * lets out knights and clerics (up to 2, 4 or 6 cultists around, by difficulty), hurting itself each time.
 * It does not move, shrugs off potions and explodes when it dies.
 */
public class EntityCultistPortalLesser extends Monster {
    private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(EntityCultistPortalLesser.class, EntityDataSerializers.BOOLEAN);
    int stagecounter = 100;
    public int activeCounter = 0;
    public int pulse = 0;

    public EntityCultistPortalLesser(EntityType<? extends EntityCultistPortalLesser> type, Level world) {
        super(type, world);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public int getArmorValue() {
        return 4;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVE, false);
    }

    public boolean isActive() {
        return entityData.get(ACTIVE);
    }

    public void setActive(boolean active) {
        entityData.set(ACTIVE, active);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("active", isActive());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setActive(nbt.getBoolean("active"));
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void move(MoverType type, Vec3 pos) {
    }

    /** onLivingUpdate did nothing: no AI and no movement. */
    @Override
    public void aiStep() {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (isActive()) {
            ++activeCounter;
        }
        if (!level().isClientSide) {
            if (!isActive()) {
                if (tickCount % 10 == 0) {
                    Player p = level().getNearestPlayer(this, 32.0);
                    if (p != null) {
                        setActive(true);
                        playSound(SoundsTC.CRAFTSTART.get(), 1.0f, 1.0f);
                    }
                }
            } else if (stagecounter-- <= 0) {
                Player p = level().getNearestPlayer(this, 32.0);
                if (p != null && hasLineOfSight(p)) {
                    int count = (level().getDifficulty() == Difficulty.HARD) ? 6 : ((level().getDifficulty() == Difficulty.NORMAL) ? 4 : 2);
                    List<EntityCultist> l = level().getEntitiesOfClass(EntityCultist.class, getBoundingBox().inflate(32.0, 32.0, 32.0));
                    count -= l.size();
                    if (count > 0) {
                        level().broadcastEntityEvent(this, (byte) 16);
                        spawnMinions();
                    }
                }
                stagecounter = 50 + random.nextInt(50);
            }
        }
        if (pulse > 0) {
            --pulse;
        }
    }

    void spawnMinions() {
        EntityCultist cultist;
        if (random.nextFloat() > 0.33) {
            cultist = TCEntities.CULTIST_KNIGHT.get().create(level());
        } else {
            cultist = TCEntities.CULTIST_CLERIC.get().create(level());
        }
        if (cultist == null || !(level() instanceof ServerLevel server)) {
            return;
        }
        cultist.setPos(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25, getZ() + random.nextFloat() - random.nextFloat());
        cultist.finalizeSpawn(server, server.getCurrentDifficultyAt(cultist.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(cultist);
        cultist.spawnAnim();
        cultist.playSound(SoundsTC.WANDFAIL.get(), 1.0f, 1.0f);
        hurt(damageSources().fellOutOfWorld(), (float) (5 + random.nextInt(5)));
    }

    /** onCollideWithPlayer: a zap to whoever touches it. */
    @Override
    public void playerTouch(Player p) {
        if (distanceToSqr(p) < 3.0 && p.hurt(damageSources().indirectMagic(this, this), 4.0f)) {
            playSound(SoundsTC.ZAP.get(), 1.0f, (random.nextFloat() - random.nextFloat()) * 0.1f + 1.0f);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 540;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.MONOLITH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundsTC.ZAP.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsTC.SHOCK.get();
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    public void handleEntityEvent(byte msg) {
        if (msg == 16) {
            pulse = 10;
        } else {
            super.handleEntityEvent(msg);
        }
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, Entity source) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 1.5f, Level.ExplosionInteraction.NONE);
        }
        super.die(source);
    }
}
