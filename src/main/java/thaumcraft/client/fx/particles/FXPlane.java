package thaumcraft.client.fx.particles;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

/** Port of FXPlane: the flat swoosh of a slash, flying from one point to another. Drawn from both sides. */
public class FXPlane extends FXBase {
    float angle;
    float angleYaw;
    float anglePitch;

    public FXPlane(ClientLevel world, double d, double d1, double d2, double m, double m1, double m2, int life) {
        super(world, d, d1, d2, 0.0, 0.0, 0.0);
        rCol = 1.0f;
        gCol = 1.0f;
        bCol = 1.0f;
        gravity = 0.0f;
        lifetime = life;
        setSize(0.01f, 0.01f);
        xo = x;
        yo = y;
        zo = z;
        particleScale = 1.0f;
        alpha = 0.0f;
        double dx = m - x;
        double dy = m1 - y;
        double dz = m2 - z;
        xd = dx / lifetime;
        yd = dy / lifetime;
        zd = dz / lifetime;
        particleTextureIndexX = 22;
        particleTextureIndexY = 10;
        double d3 = Math.sqrt(dx * dx + dz * dz);
        angleYaw = 0.0f;
        anglePitch = 0.0f;
        if (d3 >= 1.0E-7) {
            angleYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            anglePitch = (float) -(Math.atan2(dy, d3) * 180.0 / Math.PI);
        }
        angle = (float) (random.nextGaussian() * 20.0);
    }

    @Override
    public int getFXLayer() {
        return 0;
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float f) {
        Matrix4f m = new Matrix4f().translate(relX(cam, f), relY(cam, f), relZ(cam, f)).rotateY(rad(-angleYaw + 90.0f)).rotateZ(rad(anglePitch + 90.0f))
                .rotateY(rad(angle));
        particleTextureIndexX = 22 + Math.round((age + f) / lifetime * 8.0f);
        float var16 = particleTextureIndexX / 32.0f;
        float var17 = var16 + 0.03125f;
        float var18 = particleTextureIndexY / 32.0f;
        float var19 = var18 + 0.03125f;
        float var20 = particleScale * (0.5f + (age + f) / lifetime);
        float a = alpha / 2.0f;
        // The original turned face culling off for this one; both windings stand in for that.
        vertex(wr, m, -0.5f * var20, 0.5f * var20, 0.0f, var17, var19, rCol, gCol, bCol, a, 240);
        vertex(wr, m, 0.5f * var20, 0.5f * var20, 0.0f, var17, var18, rCol, gCol, bCol, a, 240);
        vertex(wr, m, 0.5f * var20, -0.5f * var20, 0.0f, var16, var18, rCol, gCol, bCol, a, 240);
        vertex(wr, m, -0.5f * var20, -0.5f * var20, 0.0f, var16, var19, rCol, gCol, bCol, a, 240);
        vertex(wr, m, -0.5f * var20, -0.5f * var20, 0.0f, var16, var19, rCol, gCol, bCol, a, 240);
        vertex(wr, m, 0.5f * var20, -0.5f * var20, 0.0f, var16, var18, rCol, gCol, bCol, a, 240);
        vertex(wr, m, 0.5f * var20, 0.5f * var20, 0.0f, var17, var18, rCol, gCol, bCol, a, 240);
        vertex(wr, m, -0.5f * var20, 0.5f * var20, 0.0f, var17, var19, rCol, gCol, bCol, a, 240);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        float threshold = lifetime / 5.0f;
        if (age <= threshold) {
            alpha = age / threshold;
        } else {
            alpha = (lifetime - age) / (float) lifetime;
        }
        if (age++ >= lifetime) {
            remove();
        }
        x += xd;
        y += yd;
        z += zd;
    }
}
