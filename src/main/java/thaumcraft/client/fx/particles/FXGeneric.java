package thaumcraft.client.fx.particles;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/**
 * Port of FXGeneric: the all purpose particle on the particle sheet. Alpha and scale follow keyframes spread over
 * its life, the frame can loop or run once, the color fades to a second one, and it can lie on a fixed plane
 * (setAngles) instead of facing the camera.
 */
public class FXGeneric extends FXBase {
    boolean doneFrames = false;
    boolean flipped = false;
    double windX;
    double windZ;
    int layer = 0;
    float dr = 0.0f;
    float dg = 0.0f;
    float db = 0.0f;
    boolean loop = false;
    float rotationSpeed = 0.0f;
    int startParticle = 0;
    int numParticles = 1;
    int particleInc = 1;
    float[] scaleKeys = { 1.0f };
    float[] scaleFrames = { 0.0f };
    float[] alphaKeys = { 1.0f };
    float[] alphaFrames = { 0.0f };
    double slowDown = 0.9800000190734863;
    float randomX;
    float randomY;
    float randomZ;
    int[] finalFrames = null;
    boolean angled = false;
    float angleYaw;
    float anglePitch;
    int gridSize = 64;

    public FXGeneric(ClientLevel world, double x, double y, double z, double xx, double yy, double zz) {
        super(world, x, y, z, xx, yy, zz);
        setSize(0.1f, 0.1f);
        xo = x;
        yo = y;
        zo = z;
        xd = xx;
        yd = yy;
        zd = zz;
    }

    /** Without speeds the original kept the random motion of the base particle. */
    public FXGeneric(ClientLevel world, double x, double y, double z) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        setSize(0.1f, 0.1f);
        xo = x;
        yo = y;
        zo = z;
    }

    void calculateFrames() {
        doneFrames = true;
        if (alphaKeys == null) {
            setAlphaF(1.0f);
        }
        alphaFrames = new float[lifetime + 1];
        float inc = (alphaKeys.length - 1) / (float) lifetime;
        float is = 0.0f;
        for (int a = 0; a <= lifetime; ++a) {
            int isF = Mth.floor(is);
            float diff = isF < alphaKeys.length - 1 ? alphaKeys[isF + 1] - alphaKeys[isF] : 0.0f;
            float pa = is - isF;
            alphaFrames[a] = alphaKeys[isF] + diff * pa;
            is += inc;
        }
        if (scaleKeys == null) {
            setScale(1.0f);
        }
        scaleFrames = new float[lifetime + 1];
        inc = (scaleKeys.length - 1) / (float) lifetime;
        is = 0.0f;
        for (int a = 0; a <= lifetime; ++a) {
            int isF = Mth.floor(is);
            float diff = isF < scaleKeys.length - 1 ? scaleKeys[isF + 1] - scaleKeys[isF] : 0.0f;
            float pa = is - isF;
            scaleFrames[a] = scaleKeys[isF] + diff * pa;
            is += inc;
        }
    }

    @Override
    public void tick() {
        if (!doneFrames) {
            calculateFrames();
        }
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        }
        oRoll = roll;
        roll += (float) Math.PI * rotationSpeed * 2.0f;
        yd -= 0.04 * gravity;
        move(xd, yd, zd);
        xd *= slowDown;
        yd *= slowDown;
        zd *= slowDown;
        xd += random.nextGaussian() * randomX;
        yd += random.nextGaussian() * randomY;
        zd += random.nextGaussian() * randomZ;
        xd += windX;
        zd += windZ;
        if (onGround && slowDown != 1.0) {
            xd *= 0.699999988079071;
            zd *= 0.699999988079071;
        }
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float partialTicks) {
        if (loop) {
            setParticleTextureIndex(startParticle + age / particleInc % numParticles);
        } else {
            float fs = age / (float) lifetime;
            setParticleTextureIndex((int) (startParticle + Math.min(numParticles * fs, (float) (numParticles - 1))));
        }
        if (finalFrames != null && finalFrames.length > 0 && age > lifetime - finalFrames.length) {
            int frame = lifetime - age;
            if (frame < 0) {
                frame = 0;
            }
            setParticleTextureIndex(finalFrames[frame]);
        }
        alpha = alphaFrames.length <= 0 ? 0.0f : alphaFrames[Math.min(age, alphaFrames.length - 1)];
        particleScale = scaleFrames.length <= 0 ? 0.0f : scaleFrames[Math.min(age, scaleFrames.length - 1)];
        draw(wr, cam, partialTicks);
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flip) {
        flipped = flip;
    }

    public void draw(VertexConsumer wr, Camera cam, float partialTicks) {
        float tx1 = particleTextureIndexX / (float) gridSize;
        float tx2 = tx1 + 1.0f / gridSize;
        float ty1 = particleTextureIndexY / (float) gridSize;
        float ty2 = ty1 + 1.0f / gridSize;
        float ts = 0.1f * particleScale;
        if (flipped) {
            float t = tx1;
            tx1 = tx2;
            tx2 = t;
        }
        float fs = Mth.clamp((age + partialTicks) / lifetime, 0.0f, 1.0f);
        float pr = rCol + (dr - rCol) * fs;
        float pg = gCol + (dg - gCol) * fs;
        float pb = bCol + (db - bCol) * fs;
        int light = getLightColor(partialTicks);
        float f5 = relX(cam, partialTicks);
        float f6 = relY(cam, partialTicks);
        float f7 = relZ(cam, partialTicks);
        // The original eased the angle by (angle - prevAngle) * partialTicks past the current one, not between.
        float f8 = roll + (roll - oRoll) * partialTicks;
        if (angled) {
            Matrix4f m = new Matrix4f().translate(f5, f6, f7).rotateY(rad(-angleYaw + 90.0f)).rotateX(rad(anglePitch + 90.0f));
            if (roll != 0.0f) {
                m.rotateZ(f8);
            }
            vertex(wr, m, -ts, -ts, 0.0f, tx2, ty2, pr, pg, pb, alpha, light);
            vertex(wr, m, -ts, ts, 0.0f, tx2, ty1, pr, pg, pb, alpha, light);
            vertex(wr, m, ts, ts, 0.0f, tx1, ty1, pr, pg, pb, alpha, light);
            vertex(wr, m, ts, -ts, 0.0f, tx1, ty2, pr, pg, pb, alpha, light);
        } else {
            billboard(wr, cam, f5, f6, f7, ts, roll != 0.0f ? f8 : 0.0f, tx1, tx2, ty1, ty2, pr, pg, pb, alpha, light);
        }
    }

    /** A drift that turns with the moon phase. */
    public void setWind(double d) {
        int m = level.getMoonPhase();
        double ang = m * (40 + random.nextInt(10)) / 180.0f * (float) Math.PI;
        // Utils.rotateAroundY of (0.1, 0, 0).
        double vx = 0.1 * Math.cos(ang);
        double vz = -0.1 * Math.sin(ang);
        windX = vx * d;
        windZ = vz * d;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public void setRBGColorF(float particleRedIn, float particleGreenIn, float particleBlueIn) {
        super.setRBGColorF(particleRedIn, particleGreenIn, particleBlueIn);
        dr = particleRedIn;
        dg = particleGreenIn;
        db = particleBlueIn;
    }

    public void setRBGColorF(float particleRedIn, float particleGreenIn, float particleBlueIn, float r2, float g2, float b2) {
        super.setRBGColorF(particleRedIn, particleGreenIn, particleBlueIn);
        dr = r2;
        dg = g2;
        db = b2;
    }

    @Override
    public int getFXLayer() {
        return layer;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setRotationSpeed(float rot) {
        rotationSpeed = (float) (rot * 0.017453292519943);
    }

    public void setRotationSpeed(float start, float rot) {
        roll = (float) (start * Math.PI * 2.0);
        oRoll = roll;
        rotationSpeed = (float) (rot * 0.017453292519943);
    }

    public void setParticles(int startParticle, int numParticles, int particleInc) {
        this.numParticles = numParticles;
        this.particleInc = particleInc;
        setParticleTextureIndex(this.startParticle = startParticle);
    }

    public void setParticle(int startParticle) {
        numParticles = 1;
        particleInc = 1;
        setParticleTextureIndex(this.startParticle = startParticle);
    }

    public void setScale(float... scale) {
        particleScale = scale[0];
        scaleKeys = scale;
    }

    public void setAlphaF(float... a1) {
        super.setAlphaF(a1[0]);
        alphaKeys = a1;
    }

    @Override
    public void setAlphaF(float a1) {
        super.setAlphaF(a1);
        alphaKeys = new float[] { a1 };
    }

    public void setSlowDown(double slowDown) {
        this.slowDown = slowDown;
    }

    public void setRandomMovementScale(float x, float y, float z) {
        randomX = x;
        randomY = y;
        randomZ = z;
    }

    public void setFinalFrames(int... frames) {
        finalFrames = frames;
    }

    public void setAngles(float yaw, float pitch) {
        angleYaw = yaw;
        anglePitch = pitch;
        angled = true;
    }

    public void setParticleTextureIndex(int index) {
        if (index < 0) {
            index = 0;
        }
        particleTextureIndexX = index % gridSize;
        particleTextureIndexY = index / gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    /** Named as in the original: false turns collisions off. */
    public void setNoClip(boolean clip) {
        hasPhysics = clip;
    }
}
