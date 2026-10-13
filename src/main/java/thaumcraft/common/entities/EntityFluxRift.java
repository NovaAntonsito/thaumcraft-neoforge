package thaumcraft.common.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityFluxRift: a tear in reality that grows on flux. It is a chain of points (from its seed and size)
 * that eats blocks and hurts entities along it. It grows unstable over time; unstable rifts fire flux events and
 * eventually collapse, dropping void seeds and sometimes a primordial pearl.
 */
public class EntityFluxRift extends Entity {
    private static final EntityDataAccessor<Integer> SEED = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> STABILITY = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> COLLAPSE = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.BOOLEAN);

    /** event, weight, stability cost, allowed near a taint seed. */
    private static final int[][] EVENTS = {{0, 50, 5, 1}, {1, 10, 0, 0}, {2, 20, 10, 1}, {3, 20, 10, 1}, {4, 1, 0, 1}};

    int maxSize = 0;
    int lastSize = -1;
    public final List<Vec3> points = new ArrayList<>();
    public final List<Float> pointsWidth = new ArrayList<>();

    public EntityFluxRift(EntityType<? extends EntityFluxRift> type, Level world) {
        super(type, world);
    }

    public EntityFluxRift(Level world) {
        this(TCEntities.FLUX_RIFT.get(), world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SEED, 0);
        builder.define(SIZE, 5);
        builder.define(STABILITY, 0.0f);
        builder.define(COLLAPSE, false);
    }

    public boolean getCollapse() {
        return entityData.get(COLLAPSE);
    }

    public void setCollapse(boolean b) {
        if (b) {
            maxSize = getRiftSize();
        }
        entityData.set(COLLAPSE, b);
    }

    public float getRiftStability() {
        return entityData.get(STABILITY);
    }

    public void setRiftStability(float s) {
        entityData.set(STABILITY, Mth.clamp(s, -100.0f, 100.0f));
    }

    public int getRiftSize() {
        return entityData.get(SIZE);
    }

    public void setRiftSize(int s) {
        entityData.set(SIZE, s);
        setSize();
    }

    public int getRiftSeed() {
        return entityData.get(SEED);
    }

    public void setRiftSeed(int s) {
        entityData.set(SEED, s);
    }

    /** The points (and so the bounding box) change with the seed and the size, on both sides. */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (SEED.equals(key) || SIZE.equals(key)) {
            setSize();
        }
    }

    protected void setSize() {
        calcSteps(points, pointsWidth, new Random(getRiftSeed()));
        lastSize = getRiftSize();
        setBoundingBox(makeBoundingBox());
    }

    /** The box around the points, instead of the 2x2 entity size. */
    @Override
    protected AABB makeBoundingBox() {
        if (points == null || points.isEmpty()) {
            return super.makeBoundingBox();
        }
        double x0 = Double.MAX_VALUE, y0 = Double.MAX_VALUE, z0 = Double.MAX_VALUE;
        double x2 = -Double.MAX_VALUE, y2 = -Double.MAX_VALUE, z2 = -Double.MAX_VALUE;
        for (Vec3 v : points) {
            x0 = Math.min(x0, v.x);
            x2 = Math.max(x2, v.x);
            y0 = Math.min(y0, v.y);
            y2 = Math.max(y2, v.y);
            z0 = Math.min(z0, v.z);
            z2 = Math.max(z2, v.z);
        }
        return new AABB(getX() + x0, getY() + y0, getZ() + z0, getX() + x2, getY() + y2, getZ() + z2);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("MaxSize", maxSize);
        tag.putInt("RiftSize", getRiftSize());
        tag.putInt("RiftSeed", getRiftSeed());
        tag.putFloat("Stability", getRiftStability());
        tag.putBoolean("collapse", getCollapse());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        maxSize = tag.getInt("MaxSize");
        setRiftSeed(tag.getInt("RiftSeed"));
        setRiftSize(tag.getInt("RiftSize"));
        // The original read the stability back with getInteger, dropping the fraction.
        setRiftStability((int) tag.getFloat("Stability"));
        setCollapse(tag.getBoolean("collapse"));
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
    }

    @Override
    public void tick() {
        super.tick();
        if (lastSize != getRiftSize()) {
            setSize();
        }
        Level level = level();
        if (!level.isClientSide) {
            if (getRiftSeed() == 0) {
                setRiftSeed(random.nextInt());
            }
            if (points.size() > 1) {
                int pi = random.nextInt(points.size() - 1);
                Vec3 v1 = points.get(pi).add(getX(), getY(), getZ());
                Vec3 v2 = points.get(pi + 1).add(getX(), getY(), getZ());
                BlockHitResult rt = level.clip(new ClipContext(v1, v2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (rt.getType() == HitResult.Type.BLOCK) {
                    BlockPos p = rt.getBlockPos();
                    BlockState bs = level.getBlockState(p);
                    if (!bs.isAir() && bs.getDestroySpeed(level, p) >= 0.0f) {
                        level.levelEvent(2001, p, Block.getId(bs));
                        level.removeBlock(p, false);
                    }
                }
                for (Entity e : level.getEntities(this, new AABB(v1, v1).inflate(0.5), e -> e.distanceToSqr(v1) < 0.25)) {
                    if (!e.isAlive() || (e instanceof Player player && player.isCreative())) {
                        continue;
                    }
                    e.hurt(damageSources().fellOutOfWorld(), 2.0f);
                    if (e instanceof ItemEntity) {
                        e.discard();
                    }
                }
            }
            if (points.size() < 3 && !getCollapse()) {
                setCollapse(true);
            }
            if (getCollapse()) {
                setRiftSize(getRiftSize() - 1);
                if (random.nextBoolean()) {
                    AuraHelper.addVis(level, blockPosition(), 1.0f);
                } else {
                    AuraHelper.polluteAura(level, blockPosition(), 1.0f, false);
                }
                if (random.nextInt(10) == 0) {
                    level.explode(this, getX() + random.nextGaussian() * 2.0, getY() + random.nextGaussian() * 2.0,
                            getZ() + random.nextGaussian() * 2.0, random.nextFloat() / 2.0f, Level.ExplosionInteraction.NONE);
                }
                if (getRiftSize() <= 1) {
                    completeCollapse();
                    return;
                }
            }
            if (tickCount % 120 == 0) {
                setRiftStability(getRiftStability() - 0.2f);
            }
            if (tickCount % 600 == getId() % 600) {
                float flux = AuraHelper.getFlux(level, blockPosition());
                double size = Math.sqrt(getRiftSize() * 2);
                if (flux >= size && getRiftSize() < 100 && getStability() != EnumStability.VERY_STABLE) {
                    AuraHelper.drainFlux(level, blockPosition(), (float) size, false);
                    setRiftSize(getRiftSize() + 1);
                }
                if (getRiftStability() < 0.0f && random.nextInt(1000) < Math.abs(getRiftStability()) + getRiftSize()) {
                    executeRiftEvent();
                }
            }
            if (isAlive() && tickCount % 300 == 0) {
                playSound(SoundsTC.EVILPORTAL.get(), (float) (0.15 + random.nextGaussian() * 0.066), (float) (0.75 + random.nextGaussian() * 0.1));
            }
        } else if (points.size() > 2) {
            if (!getCollapse() && getRiftStability() < 0.0f && random.nextInt(150) < Math.abs(getRiftStability())) {
                int pi = 1 + random.nextInt(points.size() - 2);
                Vec3 v1 = points.get(pi).add(getX(), getY(), getZ());
                ClientFX.drawCurlyWisp(level, v1.x, v1.y, v1.z, 0.1f + pointsWidth.get(pi) * 3.0f, 1.0f, 1.0f, 1.0f);
            }
            if (getCollapse()) {
                int pi = 1 + random.nextInt(points.size() - 2);
                Vec3 v1 = points.get(pi).add(getX(), getY(), getZ());
                ClientFX.drawCurlyWisp(level, v1.x, v1.y, v1.z, 0.1f + pointsWidth.get(pi) * 3.0f, 1.0f,
                        0.3f + random.nextFloat() * 0.1f, 0.3f + random.nextFloat() * 0.1f);
            }
        }
    }

    /** Called by AuraEvents when a chunk's flux goes over the limit: a rift somewhere in that chunk. */
    public static void createRift(Level world, BlockPos pos) {
        pos = pos.offset(world.random.nextInt(16), 0, world.random.nextInt(16));
        BlockPos p2 = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
        int top = world.getMinBuildHeight() + world.dimensionType().logicalHeight();
        if (!world.dimensionType().hasSkyLight()) {
            for (p2 = new BlockPos(p2.getX(), world.getMinBuildHeight() + 10, p2.getZ()); !world.isEmptyBlock(p2);
                 p2 = p2.above(world.random.nextInt(5) + 1)) {
                if (p2.getY() > top - 5) {
                    return;
                }
            }
        }
        if (p2.getY() >= top - 4) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(p2);
        if (!world.getEntitiesOfClass(EntityFluxRift.class, new AABB(p2).inflate(32.0), e -> e.distanceToSqr(c) < 1024.0).isEmpty()) {
            return;
        }
        EntityFluxRift rift = new EntityFluxRift(world);
        rift.setRiftSeed(world.random.nextInt());
        rift.moveTo(c.x, c.y, c.z, world.random.nextInt(360), 0.0f);
        float flux = AuraHelper.getFlux(world, p2);
        double size = Math.sqrt(flux * 3.0f);
        if (size > 5.0 && world.addFreshEntity(rift)) {
            rift.setRiftSize((int) size);
            AuraHelper.drainFlux(world, p2, (float) size, false);
            for (Player target : world.getEntitiesOfClass(Player.class, new AABB(p2).inflate(32.0))) {
                if (!ThaumcraftCapabilities.knowsResearch(target, "f_toomuchflux")) {
                    target.displayClientMessage(Component.translatable("tc.fluxevent.3").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
                    ResearchManager.completeResearch(target, "f_toomuchflux");
                }
            }
        }
    }

    /**
     * RandomItemChooser over the flux events. Only the collapse is ported: wisps (0), taint seeds (1), infectious
     * vis exhaustion (2) and the flux cloud focus (3) need entities, effects and foci that come later.
     * TODO: those events, and the "not near a taint seed" check of event 1.
     */
    private void executeRiftEvent() {
        int total = 0;
        for (int[] e : EVENTS) total += e[1];
        int roll = random.nextInt(total);
        int[] ei = EVENTS[EVENTS.length - 1];
        for (int[] e : EVENTS) {
            roll -= e[1];
            if (roll < 0) {
                ei = e;
                break;
            }
        }
        boolean didit = false;
        if (ei[0] == 4) {
            setCollapse(true);
        }
        if (didit) {
            setRiftStability(getRiftStability() + ei[2]);
        }
    }

    private void calcSteps(List<Vec3> pp, List<Float> ww, Random rr) {
        pp.clear();
        ww.clear();
        Vec3 right = new Vec3(rr.nextGaussian(), rr.nextGaussian(), rr.nextGaussian()).normalize();
        Vec3 left = right.scale(-1.0);
        Vec3 lr = Vec3.ZERO;
        Vec3 ll = Vec3.ZERO;
        int steps = Mth.ceil(getRiftSize() / 3.0f);
        float girth = getRiftSize() / 300.0f;
        double angle = 0.33;
        float dec = girth / steps;
        for (int a = 0; a < steps; ++a) {
            girth -= dec;
            right = right.xRot((float) (rr.nextGaussian() * angle));
            right = right.yRot((float) (rr.nextGaussian() * angle));
            lr = lr.add(right.scale(0.2));
            pp.add(lr);
            ww.add(girth);
            left = left.xRot((float) (rr.nextGaussian() * angle));
            left = left.yRot((float) (rr.nextGaussian() * angle));
            ll = ll.add(left.scale(0.2));
            pp.add(0, ll);
            ww.add(0, girth);
        }
        lr = lr.add(right.scale(0.1));
        pp.add(lr);
        ww.add(0.0f);
        ll = ll.add(left.scale(0.1));
        pp.add(0, ll);
        ww.add(0, 0.0f);
    }

    public void addStability() {
        setRiftStability(getRiftStability() + 0.125f);
    }

    public EnumStability getStability() {
        float s = getRiftStability();
        return s > 50.0f ? EnumStability.VERY_STABLE : s >= 0.0f ? EnumStability.STABLE : s > -25.0f ? EnumStability.UNSTABLE : EnumStability.VERY_UNSTABLE;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    private void completeCollapse() {
        int qq = (int) Math.sqrt(maxSize);
        if (random.nextInt(100) < qq) {
            ItemStack pearl = new ItemStack(ConfigItems.primordialPearl.get());
            pearl.setDamageValue(4 + random.nextInt(4));
            spawnAtLocation(pearl, 0.0f);
        }
        for (int a = 0; a < qq; ++a) {
            spawnAtLocation(new ItemStack(ConfigItems.voidSeed.get()), 0.0f);
        }
        // TODO: PacketFXBlockBamf (stage 10). A vanilla burst for now.
        if (level() instanceof ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, getX(), getY(), getZ(), 40, 0.5, 0.5, 0.5, 0.3);
        }
        List<LivingEntity> list = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(32.0),
                e -> e.distanceToSqr(this) < 1024.0);
        // The original's switch falls through: very unstable also gets the weakness and the warp.
        EnumStability stability = getStability();
        if (stability == EnumStability.VERY_UNSTABLE) {
            // TODO: PotionFluxTaint (flux taint effect, not ported yet) for w * 20 ticks.
        }
        if (stability == EnumStability.VERY_UNSTABLE || stability == EnumStability.UNSTABLE) {
            for (LivingEntity p : list) {
                int w = (int) ((1.0 - p.distanceToSqr(this) / 32.0) * 300.0);
                if (w > 0) {
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, w * 20, 0));
                }
            }
        }
        if (stability != EnumStability.VERY_STABLE) {
            for (LivingEntity p : list) {
                if (p instanceof Player player) {
                    int w = (int) ((1.0 - p.distanceToSqr(this) / 32.0) * 25.0);
                    if (w > 0) {
                        InternalMethodHandler.addWarpToPlayer(player, w, PlayerWarp.EnumWarpType.NORMAL);
                        InternalMethodHandler.addWarpToPlayer(player, w, PlayerWarp.EnumWarpType.TEMPORARY);
                    }
                }
            }
        }
        discard();
    }

    public enum EnumStability {
        VERY_STABLE,
        STABLE,
        UNSTABLE,
        VERY_UNSTABLE
    }
}
