package thaumcraft.common.entities.monster.boss;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.fx.PacketFXBlockArc;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.tiles.misc.TileBanner;

/**
 * Port of EntityCultistPortalGreater: the greater crimson portal, a red boss bar in six notches. On arrival it raises
 * four crimson cult banners around it and scatters loot crates. Then, while a player is within 48 blocks, it lets
 * out wave after wave of knights and clerics, healing itself until the twelfth wave brings out the crimson praetor.
 * After that every cultist it lets out hurts it. It never moves, shrugs off potions, zaps whoever touches it, drops
 * a primordial pearl and explodes when it dies. It only comes from its spawn egg.
 * 1.21: the boss bar is updated in tick. The original updated it with the AI, which this portal never ran, so its
 * bar stayed full.
 */
public class EntityCultistPortalGreater extends Monster {
    protected final ServerBossEvent bossInfo;
    int stage = 0;
    int stagecounter = 200;
    public int pulse = 0;

    public EntityCultistPortalGreater(EntityType<? extends EntityCultistPortalGreater> type, Level world) {
        super(type, world);
        bossInfo = (ServerBossEvent) new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6)
                .setDarkenScreen(true);
        xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public int getArmorValue() {
        return 5;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("stage", stage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        stage = nbt.getInt("stage");
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
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (stagecounter > 0) {
                --stagecounter;
                if (stagecounter == 160 && stage == 0) {
                    level().broadcastEntityEvent(this, (byte) 16);
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos bp = new BlockPos((int) getX() - dir.getStepX() * 6, (int) getY(), (int) getZ() + dir.getStepZ() * 6);
                        level().setBlock(bp, ConfigBlocks.bannerCrimsonCult.get().defaultBlockState(), 3);
                        if (level().getBlockEntity(bp) instanceof TileBanner te) {
                            int face = switch (dir) {
                                case NORTH -> 8;
                                case SOUTH -> 0;
                                case WEST -> 12;
                                case EAST -> 4;
                                default -> 0;
                            };
                            te.setBannerFacing((byte) face);
                            te.syncTile(false);
                            TCNetwork.sendToAllAround(level(), getX(), getY(), getZ(), 32.0,
                                    new PacketFXBlockArc(bp, this, 0.5f + random.nextFloat() * 0.2f, 0.0f, 0.0f));
                            playSound(SoundsTC.WANDFAIL.get(), 1.0f, 1.0f);
                        }
                    }
                }
                if (stagecounter > 20 && stagecounter < 150 && stage == 0 && stagecounter % 13 == 0) {
                    int a = (int) getX() + random.nextInt(5) - random.nextInt(5);
                    int b = (int) getZ() + random.nextInt(5) - random.nextInt(5);
                    BlockPos bp2 = new BlockPos(a, (int) getY(), b);
                    if (a != (int) getX() && b != (int) getZ() && level().isEmptyBlock(bp2)) {
                        level().broadcastEntityEvent(this, (byte) 16);
                        float rr = random.nextFloat();
                        int md = (rr < 0.05f) ? 2 : ((rr < 0.2f) ? 1 : 0);
                        Block bb = switch (md) {
                            case 1 -> ConfigBlocks.lootCrateUncommon.get();
                            case 2 -> ConfigBlocks.lootCrateRare.get();
                            default -> ConfigBlocks.lootCrateCommon.get();
                        };
                        level().setBlockAndUpdate(bp2, bb.defaultBlockState());
                        TCNetwork.sendToAllAround(level(), getX(), getY(), getZ(), 32.0,
                                new PacketFXBlockArc(bp2, this, 0.5f + random.nextFloat() * 0.2f, 0.0f, 0.0f));
                        playSound(SoundsTC.WANDFAIL.get(), 1.0f, 1.0f);
                    }
                }
            } else if (level().getNearestPlayer(this, 48.0) != null) {
                level().broadcastEntityEvent(this, (byte) 16);
                switch (stage) {
                    case 0, 1, 2, 3, 4 -> {
                        stagecounter = 15 + random.nextInt(10 - stage) - stage;
                        spawnMinions();
                    }
                    case 12 -> {
                        stagecounter = 50 + getTiming() * 2 + random.nextInt(50);
                        spawnBoss();
                    }
                    default -> {
                        int t = getTiming();
                        stagecounter = t + random.nextInt(5 + t / 3);
                        spawnMinions();
                    }
                }
                ++stage;
            } else {
                stagecounter = 30 + random.nextInt(30);
            }
            if (stage < 12) {
                heal(1.0f);
            }
            bossInfo.setProgress(getHealth() / getMaxHealth());
        }
        if (pulse > 0) {
            --pulse;
        }
    }

    /** 20 ticks for each cultist within 32 blocks. */
    int getTiming() {
        List<EntityCultist> l = EntityUtils.getEntitiesInRange(level(), getX(), getY(), getZ(), this, EntityCultist.class, 32.0);
        return l.size() * 20;
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
        cultist.restrictTo(blockPosition(), 32);
        server.addFreshEntity(cultist);
        cultist.spawnAnim();
        cultist.playSound(SoundsTC.WANDFAIL.get(), 1.0f, 1.0f);
        if (stage > 12) {
            hurt(damageSources().fellOutOfWorld(), (float) (5 + random.nextInt(5)));
        }
    }

    void spawnBoss() {
        EntityCultistLeader cultist = TCEntities.CULTIST_LEADER.get().create(level());
        if (cultist == null || !(level() instanceof ServerLevel server)) {
            return;
        }
        cultist.setPos(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25, getZ() + random.nextFloat() - random.nextFloat());
        cultist.finalizeSpawn(server, server.getCurrentDifficultyAt(cultist.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        cultist.restrictTo(blockPosition(), 32);
        server.addFreshEntity(cultist);
        cultist.spawnAnim();
        cultist.playSound(SoundsTC.WANDFAIL.get(), 1.0f, 1.0f);
    }

    /** onCollideWithPlayer: a zap to whoever touches it. */
    @Override
    public void playerTouch(Player p) {
        if (distanceToSqr(p) < 3.0 && p.hurt(damageSources().indirectMagic(this, this), 8.0f)) {
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

    /**
     * dropFewItems: the primordial pearl.
     * TODO: the original dropped it as an EntitySpecialItem (a floating item); a plain item that floats up for now.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        EntityThaumcraftBoss.dropPearl(this);
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
            level().explode(this, getX(), getY(), getZ(), 2.0f, Level.ExplosionInteraction.NONE);
        }
        super.die(source);
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
