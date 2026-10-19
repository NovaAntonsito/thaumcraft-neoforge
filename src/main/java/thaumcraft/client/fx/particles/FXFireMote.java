package thaumcraft.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

/** Port of FXFireMote: a bright spinning mote that shrinks and fades over 16 ticks. */
public class FXFireMote extends FXBase {
    float baseScale = 0.0f;
    float baseAlpha = 1.0f;
    int glowlayer = 0;

    public FXFireMote(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, float scale, int layer) {
        super(worldIn, x, y, z, 0.0, 0.0, 0.0);
        float colorR = r;
        float colorG = g;
        float colorB = b;
        if (colorR > 1.0) {
            colorR /= 255.0f;
        }
        if (colorG > 1.0) {
            colorG /= 255.0f;
        }
        if (colorB > 1.0) {
            colorB /= 255.0f;
        }
        glowlayer = layer;
        setColor(colorR, colorG, colorB);
        lifetime = 16;
        particleScale = scale;
        baseScale = scale;
        xd = vx;
        yd = vy;
        zd = vz;
        roll = 6.2831855f;
        oRoll = roll;
        particleTextureIndexX = 7 % 64;
        particleTextureIndexY = 7 / 64;
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float partialTicks) {
        float f = particleTextureIndexX / 64.0f;
        float f2 = f + 0.015625f;
        float f3 = particleTextureIndexY / 64.0f;
        float f4 = f3 + 0.015625f;
        float f5 = 0.1f * particleScale;
        float f9 = roll + (roll - oRoll) * partialTicks;
        billboard(wr, cam, relX(cam, partialTicks), relY(cam, partialTicks), relZ(cam, partialTicks), f5, f9, f, f2, f3, f4, rCol, gCol, bCol,
                alpha * baseAlpha, 240);
    }

    @Override
    public int getFXLayer() {
        return glowlayer;
    }

    @Override
    public void tick() {
        super.tick();
        if (random.nextInt(6) == 0) {
            ++age;
        }
        if (age >= lifetime) {
            remove();
        }
        float lifespan = age / (float) lifetime;
        particleScale = baseScale - baseScale * lifespan;
        baseAlpha = 1.0f - lifespan;
        oRoll = roll;
        ++roll;
    }
}
