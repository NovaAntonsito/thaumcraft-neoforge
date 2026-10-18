package thaumcraft.common.entities.monster.boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.EntityUtils;

/**
 * Port of EntityThaumcraftBoss: the base of the Thaumcraft bosses. A purple boss bar that darkens the sky, a home
 * 24 blocks around where it spawned, 1 health back every 1.5 seconds, and a cap of 35 on any single hit, which
 * enrages it for 10 seconds (regeneration, strength, haste). It keeps a tally of who hurt it most to switch targets
 * and grows (50 health, 0.5 damage) for each extra player it is fighting, up to 5. It never despawns, breathes
 * under water (entity type tag), ignores cobwebs and is friends with the eldritch. It drops a primordial pearl
 * and a rare treasure.
 * As in the original, the target switch looks up the current target instead of the new one, so it never switches
 * and the player bonus only counts players that pass that check.
 */
public class EntityThaumcraftBoss extends Monster {
    private static final EntityDataAccessor<Integer> AGGRO = SynchedEntityData.defineId(EntityThaumcraftBoss.class, EntityDataSerializers.INT);
    protected final ServerBossEvent bossInfo;
    HashMap<Integer, Integer> aggro = new HashMap<>();
    int spawnTimer = 0;

    public EntityThaumcraftBoss(EntityType<? extends EntityThaumcraftBoss> type, Level world) {
        super(type, world);
        bossInfo = (ServerBossEvent) new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)
                .setDarkenScreen(true);
        xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("HomeD")) {
            restrictTo(new BlockPos(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ")), nbt.getInt("HomeD"));
        }
        if (hasCustomName()) {
            bossInfo.setName(getDisplayName());
        }
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AGGRO, 0);
    }

    /** updateAITasks ran the AI only once the spawn timer was over. */
    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || getSpawnTimer() > 0;
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

    /** onInitialSpawn: home where it spawned, and its name. */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        restrictTo(blockPosition(), 24);
        generateName();
        bossInfo.setName(getDisplayName());
        return data;
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossInfo.setName(getDisplayName());
    }

    public int getAnger() {
        return entityData.get(AGGRO);
    }

    public void setAnger(int par1) {
        entityData.set(AGGRO, par1);
    }

    public int getSpawnTimer() {
        return spawnTimer;
    }

    @Override
    public void tick() {
        super.tick();
        if (getSpawnTimer() > 0) {
            --spawnTimer;
        }
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
        if (!level().isClientSide) {
            if (tickCount % 30 == 0) {
                heal(1.0f);
            }
            if (getTarget() != null && tickCount % 20 == 0) {
                List<Integer> dl = new ArrayList<>();
                int players = 0;
                int hei = getTarget().getId();
                int ld;
                int ad = ld = aggro.getOrDefault(hei, 0);
                Entity newTarget = null;
                for (Integer ei : aggro.keySet()) {
                    int ca = aggro.get(ei);
                    if (ca > ad + 25 && ca > ad * 1.1 && ca > ld) {
                        newTarget = level().getEntity(hei);
                        if (newTarget == null || !newTarget.isAlive() || distanceToSqr(newTarget) > 16384.0) {
                            dl.add(ei);
                        } else {
                            hei = ei;
                            ld = ei;
                            if (newTarget instanceof Player) {
                                ++players;
                            }
                        }
                    }
                }
                for (Integer ei : dl) {
                    aggro.remove(ei);
                }
                if (newTarget instanceof LivingEntity le && hei != getTarget().getId()) {
                    setTarget(le);
                }
                float om = getMaxHealth();
                AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
                AttributeInstance damage = getAttribute(Attributes.ATTACK_DAMAGE);
                for (int a = 0; a < 5; ++a) {
                    if (damage != null) {
                        damage.removeModifier(EntityUtils.DMGBUFF[a].id());
                    }
                    health.removeModifier(EntityUtils.HPBUFF[a].id());
                }
                for (int a = 0; a < Math.min(5, players - 1); ++a) {
                    health.addTransientModifier(EntityUtils.HPBUFF[a]);
                    if (damage != null) {
                        damage.addTransientModifier(EntityUtils.DMGBUFF[a]);
                    }
                }
                double mm = getMaxHealth() / om;
                setHealth((float) (getHealth() * mm));
            }
            if (getTarget() != null && !getTarget().isAlive()) {
                setTarget(null);
            }
            bossInfo.setProgress(getHealth() / getMaxHealth());
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource ds) {
        return super.isInvulnerableTo(ds) || getSpawnTimer() > 0;
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && getSpawnTimer() <= 0;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    /** setInWeb did nothing. */
    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** isOnSameTeam. */
    @Override
    public boolean isAlliedTo(Entity el) {
        return el instanceof IEldritchMob;
    }

    /**
     * dropFewItems: the primordial pearl, and a rare treasure.
     * TODO: the original dropped the pearl as an EntitySpecialItem (a floating item); a plain item that floats up for now.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        dropPearl(this);
        spawnAtLocation(new ItemStack(ConfigItems.lootBagRare.get()), 1.5f);
    }

    /** EntityUtils.entityDropSpecialItem with the primordial pearl, at half the height of the mob. */
    static void dropPearl(LivingEntity entity) {
        ItemEntity item = entity.spawnAtLocation(new ItemStack(ConfigItems.primordialPearl.get()), entity.getBbHeight() / 2.0f);
        if (item != null) {
            item.setDeltaMovement(0.0, 0.1, 0.0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (!level().isClientSide) {
            if (source.getEntity() instanceof LivingEntity) {
                int target = source.getEntity().getId();
                int ad = (int) damage;
                if (aggro.containsKey(target)) {
                    ad += aggro.get(target);
                }
                aggro.put(target, ad);
            }
            if (damage > 35.0f) {
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
        }
        return super.hurt(source, damage);
    }

    public void generateName() {
    }
}
