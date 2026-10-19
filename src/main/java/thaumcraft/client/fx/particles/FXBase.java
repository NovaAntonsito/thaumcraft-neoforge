package thaumcraft.client.fx.particles;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Base of the particles drawn by Thaumcraft's own ParticleEngine. Carries what the 1.12 Particle had and the 1.21
 * one lacks (the random particleScale, the texture index on the sheet, the layer) and the quad helpers that stand in
 * for the original's tessellator and GL matrix calls.
 */
public abstract class FXBase extends Particle {
    protected float particleScale;
    protected int particleTextureIndexX;
    protected int particleTextureIndexY;

    protected FXBase(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        particleScale = (random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    /** Like the 1.12 constructor with speeds: the motion gets the vanilla random spread. */
    protected FXBase(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        particleScale = (random.nextFloat() * 0.5f + 0.5f) * 2.0f;
    }

    /** 0 additive, 1 alpha blended, 2 additive through walls, 3 alpha blended through walls. */
    public int getFXLayer() {
        return 0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public void setAlphaF(float a) {
        alpha = a;
    }

    public void setRBGColorF(float r, float g, float b) {
        setColor(r, g, b);
    }

    public void setMaxAge(int max) {
        lifetime = max;
    }

    public void setGravity(float g) {
        gravity = g;
    }

    protected float relX(Camera cam, float pt) {
        return (float) (Mth.lerp(pt, xo, x) - cam.getPosition().x);
    }

    protected float relY(Camera cam, float pt) {
        return (float) (Mth.lerp(pt, yo, y) - cam.getPosition().y);
    }

    protected float relZ(Camera cam, float pt) {
        return (float) (Mth.lerp(pt, zo, z) - cam.getPosition().z);
    }

    protected static double distanceToCamera(Camera cam, double x, double y, double z) {
        Vec3 c = cam.getPosition();
        return Math.sqrt(c.distanceToSqr(x, y, z));
    }

    /**
     * A camera facing quad of half size s at (px, py, pz) relative to the camera, turned by roll around the view.
     * Corners and UVs as the original's rotationX/rotationXY quads.
     */
    protected static void billboard(VertexConsumer vc, Camera cam, float px, float py, float pz, float s, float roll, float u0, float u1, float v0,
            float v1, float r, float g, float b, float a, int light) {
        Quaternionf q = new Quaternionf(cam.rotation());
        if (roll != 0.0f) q.rotateZ(roll);
        corner(vc, q, px, py, pz, 1.0f, -1.0f, s, u1, v1, r, g, b, a, light);
        corner(vc, q, px, py, pz, 1.0f, 1.0f, s, u1, v0, r, g, b, a, light);
        corner(vc, q, px, py, pz, -1.0f, 1.0f, s, u0, v0, r, g, b, a, light);
        corner(vc, q, px, py, pz, -1.0f, -1.0f, s, u0, v1, r, g, b, a, light);
    }

    private static void corner(VertexConsumer vc, Quaternionf q, float px, float py, float pz, float cx, float cy, float s, float u, float v, float r,
            float g, float b, float a, int light) {
        Vector3f p = new Vector3f(cx, cy, 0.0f).rotate(q).mul(s).add(px, py, pz);
        vc.addVertex(p.x(), p.y(), p.z()).setUv(u, v).setColor(r, g, b, a).setLight(light);
    }

    /** One vertex of a quad placed with a matrix, for the particles the original drew with glTranslate/glRotate. */
    protected static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float z, float u, float v, float r, float g, float b, float a, int light) {
        Vector3f p = m.transformPosition(x, y, z, new Vector3f());
        vc.addVertex(p.x(), p.y(), p.z()).setUv(u, v).setColor(r, g, b, a).setLight(light);
    }

    protected static float rad(float degrees) {
        return degrees * Mth.DEG_TO_RAD;
    }
}
