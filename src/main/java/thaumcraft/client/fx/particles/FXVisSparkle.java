package thaumcraft.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/** Port of FXVisSparkle: a twinkling mote that flies to a point and vanishes there. */
public class FXVisSparkle extends FXBase {
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    float sizeMod = 0.0f;

    public FXVisSparkle(ClientLevel par1World, double par2, double par4, double par6, double tx, double ty, double tz) {
        super(par1World, par2, par4, par6, 0.0, 0.0, 0.0);
        particleScale = 0.0f;
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        lifetime = 1000;
        float f3 = 0.01f;
        xd = (float) random.nextGaussian() * f3;
        yd = (float) random.nextGaussian() * f3;
        zd = (float) random.nextGaussian() * f3;
        sizeMod = 45 + random.nextInt(15);
        rCol = 0.2f;
        gCol = 0.6f + random.nextFloat() * 0.3f;
        bCol = 0.2f;
        gravity = 0.2f;
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float f) {
        float bob = Mth.sin(age / 3.0f) * 0.3f + 6.0f;
        int part = age % 16;
        float var8 = part / 64.0f;
        float var9 = var8 + 0.015625f;
        float var10 = 0.125f;
        float var11 = var10 + 0.015625f;
        float var12 = 0.1f * particleScale * bob;
        billboard(wr, cam, relX(cam, f), relY(cam, f), relZ(cam, f), var12, 0.0f, var8, var9, var10, var11, rCol, gCol, bCol, 0.5f, 240);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.985;
        zd *= 0.985;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double d13 = 0.10000000149011612;
        double d14 = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (d14 < 2.0) {
            particleScale *= 0.95f;
        }
        if (d14 < 0.2) {
            lifetime = age;
        }
        if (age < 10) {
            particleScale = age / sizeMod;
        }
        dx /= d14;
        dy /= d14;
        dz /= d14;
        xd += dx * d13;
        yd += dy * d13;
        zd += dz * d13;
        xd = Mth.clamp((float) xd, -0.1f, 0.1f);
        yd = Mth.clamp((float) yd, -0.1f, 0.1f);
        zd = Mth.clamp((float) zd, -0.1f, 0.1f);
        if (age++ >= lifetime) {
            remove();
        }
    }
}
