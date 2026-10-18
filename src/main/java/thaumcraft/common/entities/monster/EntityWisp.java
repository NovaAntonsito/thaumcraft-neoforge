package thaumcraft.common.entities.monster;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.fx.PacketFXZap;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityWisp: a floating ball of one aspect (a primal nine times in ten). It drifts around and now and then
 * picks on the nearest player within 16 blocks, or whoever hurts it, zapping them every two seconds or so. Moving
 * targets are hit less often but standing ones take a point more. It drops a crystal of its aspect.
 */
public class EntityWisp extends FlyingMob implements Enemy {
    private static final EntityDataAccessor<String> TYPE = SynchedEntityData.defineId(EntityWisp.class, EntityDataSerializers.STRING);

    public int courseChangeCooldown = 0;
    public double waypointX;
    public double waypointY;
    public double waypointZ;
    private int aggroCooldown = 0;
    public int prevAttackCounter = 0;
    public int attackCounter = 0;
    private BlockPos currentFlightTarget;

    public EntityWisp(EntityType<? extends EntityWisp> type, Level world) {
        super(type, world);
        xpReward = 5;
    }

    public EntityWisp(Level world) {
        this(TCEntities.WISP.get(), world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 22.0).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean hurt(DamageSource damagesource, float i) {
        if (damagesource.getEntity() instanceof LivingEntity attacker) {
            setTarget(attacker);
            aggroCooldown = 200;
        }
        return super.hurt(damagesource, i);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, "");
    }

    /** onDeath on the client: the 1.21 client only learns of the death through entity event 3. */
    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 3 && level().isClientSide) {
            ClientFX.burst(getX(), getY() + 0.44999998807907104, getZ(), 1.0f);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && tickCount <= 1) {
            ClientFX.burst(getX(), getY(), getZ(), 10.0f);
        }
        Aspect aspect = Aspect.getAspect(getWispType());
        if (level().isClientSide && random.nextBoolean() && aspect != null) {
            ClientFX.drawWispParticles(level(), getX() + (random.nextFloat() - random.nextFloat()) * 0.7f,
                    getY() + (random.nextFloat() - random.nextFloat()) * 0.7f, getZ() + (random.nextFloat() - random.nextFloat()) * 0.7f,
                    aspect.getColor());
        }
        Vec3 m = getDeltaMovement();
        setDeltaMovement(m.x, m.y * 0.6000000238418579, m.z);
    }

    /** getType of the original (the name is taken by Entity.getType in 1.21). */
    public String getWispType() {
        return entityData.get(TYPE);
    }

    public void setWispType(String t) {
        entityData.set(TYPE, String.valueOf(t));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!isEffectiveAi() || level().isClientSide) {
            return;
        }
        if (Aspect.getAspect(getWispType()) == null) {
            if (random.nextInt(10) != 0) {
                List<Aspect> as = Aspect.getPrimalAspects();
                setWispType(as.get(random.nextInt(as.size())).getTag());
            } else {
                List<Aspect> as = Aspect.getCompoundAspects();
                setWispType(as.get(random.nextInt(as.size())).getTag());
            }
        }
        if (level().getDifficulty() == Difficulty.PEACEFUL) {
            discard();
        }
        prevAttackCounter = attackCounter;
        double attackrange = 16.0;
        LivingEntity target = getTarget();
        if (target == null || !hasLineOfSight(target)) {
            if (currentFlightTarget != null && (!level().isEmptyBlock(currentFlightTarget) || currentFlightTarget.getY() < level().getMinBuildHeight() + 1
                    || currentFlightTarget.getY() > level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, currentFlightTarget).above(8).getY())) {
                currentFlightTarget = null;
            }
            if (currentFlightTarget == null || random.nextInt(30) == 0 || currentFlightTarget.distToCenterSqr(position()) < 4.0) {
                currentFlightTarget = new BlockPos((int) getX() + random.nextInt(7) - random.nextInt(7), (int) getY() + random.nextInt(6) - 2,
                        (int) getZ() + random.nextInt(7) - random.nextInt(7));
            }
            flyTowards(currentFlightTarget.getX() + 0.5 - getX(), currentFlightTarget.getY() + 0.1 - getY(), currentFlightTarget.getZ() + 0.5 - getZ(), 0.15f);
        } else if (distanceToSqr(target) > attackrange * attackrange / 2.0 && hasLineOfSight(target)) {
            flyTowards(target.getX() - getX(), target.getY() + target.getEyeHeight() * 0.66f - getY(), target.getZ() - getZ(), 0.5f);
        }
        if (getTarget() instanceof Player player && player.getAbilities().invulnerable) {
            setTarget(null);
        }
        if (getTarget() != null && !getTarget().isAlive()) {
            setTarget(null);
        }
        --aggroCooldown;
        if (random.nextInt(1000) == 0 && (getTarget() == null || aggroCooldown-- <= 0)) {
            setTarget(level().getNearestPlayer(this, 16.0));
            if (getTarget() != null) {
                aggroCooldown = 50;
            }
        }
        target = getTarget();
        if (isAlive() && target != null && target.distanceToSqr(this) < attackrange * attackrange) {
            double d5 = target.getX() - getX();
            double d7 = target.getZ() - getZ();
            float n = -(float) Math.atan2(d5, d7) * 180.0f / 3.141593f;
            setYRot(n);
            yBodyRot = n;
            if (hasLineOfSight(target)) {
                ++attackCounter;
                if (attackCounter == 20) {
                    playSound(SoundsTC.ZAP.get(), 1.0f, 1.1f);
                    Aspect aspect = Aspect.getAspect(getWispType());
                    TCNetwork.sendToAllAround(level(), getX(), getY(), getZ(), 32.0, new PacketFXZap(position(), target.position(),
                            aspect != null ? aspect.getColor() : 0xFFFFFF, 0.6f));
                    float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
                    Vec3 tm = target.getDeltaMovement();
                    if (Math.abs(tm.x) > 0.10000000149011612 || Math.abs(tm.y) > 0.10000000149011612 || Math.abs(tm.z) > 0.10000000149011612) {
                        if (random.nextFloat() < 0.4f) {
                            target.hurt(damageSources().mobAttack(this), damage);
                        }
                    } else if (random.nextFloat() < 0.66f) {
                        target.hurt(damageSources().mobAttack(this), damage + 1.0f);
                    }
                    attackCounter = -20 + random.nextInt(20);
                }
            } else if (attackCounter > 0) {
                --attackCounter;
            }
        }
    }

    private void flyTowards(double var1, double var2, double var3, float forward) {
        Vec3 m = getDeltaMovement();
        double mx = m.x + (Math.signum(var1) * 0.5 - m.x) * 0.10000000149011612;
        double my = m.y + (Math.signum(var2) * 0.699999988079071 - m.y) * 0.10000000149011612;
        double mz = m.z + (Math.signum(var3) * 0.5 - m.z) * 0.10000000149011612;
        setDeltaMovement(mx, my, mz);
        float var4 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float var5 = Mth.wrapDegrees(var4 - getYRot());
        zza = forward;
        setYRot(getYRot() + var5);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.WISPLIVE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.LAVA_EXTINGUISH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsTC.WISPDEAD.get();
    }

    /** dropFewItems: a crystal of its aspect. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(world, source, recentlyHit);
        Aspect aspect = Aspect.getAspect(getWispType());
        if (aspect != null) {
            spawnAtLocation(ConfigItems.crystalEssence.get().makeCrystal(aspect, 1), 0.0f);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }

    /** getCanSpawnHere: fewer than 8 wisps within 16 blocks, not peaceful, dark enough and room to spawn. */
    public static boolean checkWispSpawnRules(EntityType<EntityWisp> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos,
            RandomSource rand) {
        int count = world.getEntitiesOfClass(EntityWisp.class, new net.minecraft.world.phys.AABB(pos).inflate(16.0)).size();
        return count < 8 && world.getDifficulty() != Difficulty.PEACEFUL && isValidLightLevel(world, pos, rand)
                && Mob.checkMobSpawnRules(type, world, reason, pos, rand);
    }

    /** The 1.12 EntityMob.isValidLightLevel, also used when a flux rift makes a wisp. */
    public static boolean isValidLightLevel(ServerLevelAccessor world, BlockPos blockpos, RandomSource rand) {
        if (world.getBrightness(LightLayer.SKY, blockpos) > rand.nextInt(32)) {
            return false;
        }
        int i = world.getLevel().isThundering() ? world.getMaxLocalRawBrightness(blockpos, 10) : world.getMaxLocalRawBrightness(blockpos);
        return i <= rand.nextInt(8);
    }

    /** getCanSpawnHere for a wisp made in code (the flux rift event). */
    public boolean getCanSpawnHere() {
        return level() instanceof ServerLevel sl && checkWispSpawnRules(TCEntities.WISP.get(), sl, MobSpawnType.EVENT, blockPosition(), random);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 2;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putString("Type", getWispType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        setWispType(nbttagcompound.getString("Type"));
    }
}
