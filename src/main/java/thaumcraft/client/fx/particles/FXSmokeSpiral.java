package thaumcraft.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/** Port of FXSmokeSpiral: a puff that winds twice around a point over its life, never dipping below miny. */
public class FXSmokeSpiral extends FXBase {
    private float radius = 1.0f;
    private int start = 0;
    private int miny = 0;

    public FXSmokeSpiral(ClientLevel world, double d, double d1, double d2, float radius, int start, int miny) {
        super(world, d, d1, d2, 0.0, 0.0, 0.0);
        gravity = -0.01f;
        xd = 0.0;
        yd = 0.0;
        zd = 0.0;
        lifetime = 20 + random.nextInt(10);
        setSize(0.01f, 0.01f);
        xo = x;
        yo = y;
        zo = z;
        this.radius = radius;
        this.start = start;
        this.miny = miny;
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float f) {
        int particle = (int) (1.0f + age / (float) lifetime * 4.0f);
        float r1 = start + 720.0f * ((age + f) / lifetime);
        float r2 = 90.0f - 180.0f * ((age + f) / lifetime);
        float mX = -Mth.sin(r1 / 180.0f * 3.1415927f) * Mth.cos(r2 / 180.0f * 3.1415927f);
        float mZ = Mth.cos(r1 / 180.0f * 3.1415927f) * Mth.cos(r2 / 180.0f * 3.1415927f);
        float mY = -Mth.sin(r2 / 180.0f * 3.1415927f);
        mX *= radius;
        mY *= radius;
        mZ *= radius;
        float var8 = particle % 16 / 64.0f;
        float var9 = var8 + 0.015625f;
        float var10 = particle / 16 / 64.0f;
        float var11 = var10 + 0.015625f;
        float var12 = 0.15f * particleScale;
        float var13 = (float) (x + mX - cam.getPosition().x);
        float var14 = (float) (Math.max(y + mY, miny + 0.1f) - cam.getPosition().y);
        float var15 = (float) (z + mZ - cam.getPosition().z);
        billboard(wr, cam, var13, var14, var15, var12, 0.0f, var8, var9, var10, var11, rCol, gCol, bCol, 0.66f * alpha, getLightColor(f));
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void tick() {
        setAlphaF((lifetime - age) / (float) lifetime);
        if (age++ >= lifetime) {
            remove();
        }
    }
}
