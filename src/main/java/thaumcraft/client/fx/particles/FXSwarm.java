package thaumcraft.client.fx.particles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.lib.SoundsTC;

/**
 * Port of FXSwarm: one of the tiny biting things of a taint swarm. It steers towards its swarm, scatters while the
 * swarm is hurt, and falls and fades when the swarm dies. A few of them buzz near players. Full bright.
 */
public class FXSwarm extends TextureSheetParticle {
    /** Set when the particle sprites load (ThaumcraftClient). */
    public static SpriteSet sprites;
    private static final List<Long> buzzcount = new ArrayList<>();

    private final Entity target;
    private float turnSpeed = 10.0f;
    private float speed = 0.2f;
    int deathtimer = 0;
    float rotationPitch;
    float rotationYaw;
    private final float particleScale;
    private final float baseG;
    private final float baseB;

    public FXSwarm(ClientLevel world, double x, double y, double z, Entity target, float r, float g, float b, float sp, float ts, float pg) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        rCol = r;
        gCol = g;
        bCol = b;
        baseG = g;
        baseB = b;
        particleScale = random.nextFloat() * 0.5f + 1.0f;
        this.target = target;
        float f3 = 0.2f;
        xd = (random.nextFloat() - random.nextFloat()) * f3;
        yd = (random.nextFloat() - random.nextFloat()) * f3;
        zd = (random.nextFloat() - random.nextFloat()) * f3;
        speed = sp;
        turnSpeed = ts;
        gravity = pg;
        lifetime = Integer.MAX_VALUE;
        if (sprites != null) {
            setSprite(sprites.get(0, 7));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float bob = Mth.sin(age / 3.0f) * 0.25f + 1.0f;
        return 0.1f * particleScale * bob;
    }

    private boolean targetAlive() {
        return target != null && target.isAlive() && !(target instanceof LivingEntity l && l.deathTime > 0);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        ++age;
        if (sprites != null) {
            setSprite(sprites.get(age % 8, 7));
        }
        if (!targetAlive()) {
            ++deathtimer;
            xd *= 0.9;
            zd *= 0.9;
            yd -= gravity / 2.0f;
            if (deathtimer > 50) {
                remove();
            }
        } else {
            yd += gravity;
        }
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.985;
        zd *= 0.985;
        boolean hurt = target instanceof LivingEntity l && l.hurtTime > 0;
        if (targetAlive()) {
            Vec3 v1 = new Vec3(x, y, z);
            int half = (int) (turnSpeed / 2.0f);
            if (v1.distanceToSqr(target.getX(), target.getY(), target.getZ()) > target.getBbWidth() && !hurt) {
                faceEntity(target, turnSpeed / 2.0f + random.nextInt(half), turnSpeed / 2.0f + random.nextInt(half));
            } else {
                faceEntity(target, -(turnSpeed / 2.0f + random.nextInt(half)), -(turnSpeed / 2.0f + random.nextInt(half)));
            }
            double mx = -Mth.sin(rotationYaw / 180.0f * (float) Math.PI) * Mth.cos(rotationPitch / 180.0f * (float) Math.PI);
            double mz = Mth.cos(rotationYaw / 180.0f * (float) Math.PI) * Mth.cos(rotationPitch / 180.0f * (float) Math.PI);
            double my = -Mth.sin(rotationPitch / 180.0f * (float) Math.PI);
            setHeading(mx, my, mz, speed, 15.0f);
        }
        // renderParticle: greener and bluer halved while the swarm is hurt, fading out as it dies.
        float dd = hurt ? 2.0f : 1.0f;
        gCol = baseG / dd;
        bCol = baseB / dd;
        alpha = (50.0f - deathtimer) / 50.0f;
        if (buzzcount.size() < 3 && random.nextInt(50) == 0 && level.getNearestPlayer(x, y, z, 8.0, false) != null) {
            level.playLocalSound(x, y, z, SoundsTC.FLY.get(), SoundSource.HOSTILE, 0.03f, 0.5f + random.nextFloat() * 0.4f, false);
            buzzcount.add(System.nanoTime() + 1500000L);
        }
        if (buzzcount.size() >= 3 && buzzcount.get(0) < System.nanoTime()) {
            buzzcount.remove(0);
        }
    }

    public void faceEntity(Entity e, float yawStep, float pitchStep) {
        double d0 = e.getX() - x;
        double d2 = e.getZ() - z;
        AABB bb = getBoundingBox();
        double d3 = (e.getBoundingBox().minY + e.getBoundingBox().maxY) / 2.0 - (bb.minY + bb.maxY) / 2.0;
        double d4 = Math.sqrt(d0 * d0 + d2 * d2);
        float f2 = (float) (Math.atan2(d2, d0) * 180.0 / Math.PI) - 90.0f;
        float f3 = (float) (-(Math.atan2(d3, d4) * 180.0 / Math.PI));
        rotationPitch = updateRotation(rotationPitch, f3, pitchStep);
        rotationYaw = updateRotation(rotationYaw, f2, yawStep);
    }

    private float updateRotation(float current, float wanted, float step) {
        float f3 = Mth.wrapDegrees(wanted - current);
        if (f3 > step) {
            f3 = step;
        }
        if (f3 < -step) {
            f3 = -step;
        }
        return current + f3;
    }

    public void setHeading(double hx, double hy, double hz, float sp, float spread) {
        double f2 = Math.sqrt(hx * hx + hy * hy + hz * hz);
        hx /= f2;
        hy /= f2;
        hz /= f2;
        hx += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.007499999832361937 * spread;
        hy += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.007499999832361937 * spread;
        hz += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.007499999832361937 * spread;
        xd = hx * sp;
        yd = hy * sp;
        zd = hz * sp;
    }
}
