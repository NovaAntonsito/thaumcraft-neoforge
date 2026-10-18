package thaumcraft.common.entities.projectile;

import java.util.HashMap;

import javax.annotation.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityGrapple: the hook of the arcane grappler. It sticks to whatever it hits and reels the thrower in
 * until they sneak. One hook per thrower ({@link #grapples}: thrower id to hook id); an unattached hook is gone after
 * 30 ticks. The pull runs on both sides, so the client moves its own player.
 */
public class EntityGrapple extends ThrowableProjectile implements IEntityWithComplexSpawn {
    public static HashMap<Integer, Integer> grapples = new HashMap<>();
    public InteractionHand hand = InteractionHand.MAIN_HAND;
    @Nullable
    LivingEntity cthrower;
    boolean p = false;
    boolean boost;
    int prevDist = 0;
    int count = 0;
    boolean added = false;
    public float ampl = 0.0f;

    public EntityGrapple(EntityType<? extends EntityGrapple> type, Level world) {
        super(type, world);
    }

    public EntityGrapple(Level world, LivingEntity thrower, InteractionHand hand) {
        super(TCEntities.GRAPPLE.get(), thrower, world);
        this.hand = hand;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    /** The original's shoot always had no inaccuracy. */
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, 0.0f);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data) {
        int id = -1;
        if (getThrower() != null) {
            id = getThrower().getId();
        }
        data.writeInt(id);
        data.writeBoolean(hand == InteractionHand.MAIN_HAND);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data) {
        int id = data.readInt();
        hand = data.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (id >= 0 && level().getEntity(id) instanceof LivingEntity thrower) {
            cthrower = thrower;
        }
    }

    @Nullable
    public LivingEntity getThrower() {
        if (cthrower != null) {
            return cthrower;
        }
        return getOwner() instanceof LivingEntity owner ? owner : null;
    }

    @Override
    protected double getDefaultGravity() {
        return getPulling() ? 0.0 : 0.03;
    }

    public void setPulling() {
        p = true;
    }

    public boolean getPulling() {
        return p;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity thrower = getThrower();
        if (!getPulling() && !isRemoved() && (tickCount > 30 || thrower == null)) {
            if (thrower != null) {
                grapples.remove(thrower.getId());
            }
            discard();
        }
        if (thrower != null) {
            if (!level().isClientSide && !isRemoved() && !added) {
                if (grapples.containsKey(thrower.getId())) {
                    int ii = grapples.get(thrower.getId());
                    if (ii != getId()) {
                        Entity e = level().getEntity(ii);
                        if (e != null) {
                            e.discard();
                        }
                    }
                }
                grapples.put(thrower.getId(), getId());
                added = true;
            }
            Integer current = grapples.get(thrower.getId());
            if (current != null && current != getId()) {
                discard();
            }
            double dis = thrower.distanceTo(this);
            if (getPulling() && !isRemoved()) {
                if (thrower.isShiftKeyDown()) {
                    grapples.remove(thrower.getId());
                    discard();
                } else {
                    if (!level().isClientSide && thrower instanceof ServerPlayer sp) {
                        EntityUtils.resetFloatCounter(sp);
                    }
                    thrower.fallDistance = 0.0f;
                    double mx = getX() - thrower.getX();
                    double my = getY() - thrower.getY();
                    double mz = getZ() - thrower.getZ();
                    double dd = dis;
                    if (dis < 8.0) {
                        dd = dis * (8.0 - dis);
                    }
                    dd = Math.max(1.0E-9, dd);
                    mx /= dd * 5.0;
                    my /= dd * 5.0;
                    mz /= dd * 5.0;
                    Vec3 v2 = new Vec3(mx, my, mz);
                    if (v2.length() > 0.25) {
                        v2 = v2.normalize();
                        mx = v2.x / 4.0;
                        my = v2.y / 4.0;
                        mz = v2.z / 4.0;
                    }
                    double ym = my + 0.033;
                    if (!boost) {
                        ym += 0.4000000059604645;
                        boost = true;
                    }
                    thrower.setDeltaMovement(thrower.getDeltaMovement().add(mx, ym, mz));
                    int d = (int) (dis / 2.0);
                    if (d == prevDist) {
                        ++count;
                    } else {
                        count = 0;
                    }
                    prevDist = d;
                }
            }
            if (level().isClientSide) {
                if (!getPulling()) {
                    ampl += 0.02f;
                } else {
                    ampl *= 0.66f;
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 6) {
            setPulling();
            setDeltaMovement(Vec3.ZERO);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void onHit(HitResult mop) {
        if (!level().isClientSide) {
            setPulling();
            setDeltaMovement(Vec3.ZERO);
            setPos(mop.getLocation());
            level().broadcastEntityEvent(this, (byte) 6);
        }
    }
}
