package thaumcraft.client.fx.particles;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

/** Port of FXBlockRunes: a rune glyph that fades in and out on one side of a block. */
public class FXBlockRunes extends FXBase {
    double ofx = 0.0;
    double ofy = 0.0;
    float rotation = 0.0f;
    int runeIndex = 0;

    public FXBlockRunes(ClientLevel world, double d, double d1, double d2, float f1, float f2, float f3, int m) {
        super(world, d, d1, d2, 0.0, 0.0, 0.0);
        if (f1 == 0.0f) {
            f1 = 1.0f;
        }
        rotation = random.nextInt(4) * 90;
        rCol = f1;
        gCol = f2;
        bCol = f3;
        gravity = 0.0f;
        xd = 0.0;
        yd = 0.0;
        zd = 0.0;
        lifetime = 3 * m;
        setSize(0.01f, 0.01f);
        xo = x;
        yo = y;
        zo = z;
        runeIndex = (int) (Math.random() * 16.0 + 224.0);
        ofx = random.nextFloat() * 0.2;
        ofy = -0.3 + random.nextFloat() * 0.6;
        particleScale = (float) (1.0 + random.nextGaussian() * 0.10000000149011612);
        alpha = 0.0f;
    }

    public void setScale(float s) {
        particleScale = s;
    }

    public void setOffsetX(double f) {
        ofx = f;
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float f) {
        Matrix4f m = new Matrix4f().translate(relX(cam, f), relY(cam, f), relZ(cam, f)).rotateY(rad(rotation)).rotateZ(rad(90.0f))
                .translate((float) ofx, (float) ofy, -0.51f);
        float var16 = runeIndex % 16 / 64.0f;
        float var17 = var16 + 0.015625f;
        float var18 = 0.09375f;
        float var19 = var18 + 0.015625f;
        float var20 = 0.3f * particleScale;
        float a = alpha / 2.0f;
        vertex(wr, m, -0.5f * var20, 0.5f * var20, 0.0f, var17, var19, rCol, gCol, bCol, a, 240);
        vertex(wr, m, 0.5f * var20, 0.5f * var20, 0.0f, var17, var18, rCol, gCol, bCol, a, 240);
        vertex(wr, m, 0.5f * var20, -0.5f * var20, 0.0f, var16, var18, rCol, gCol, bCol, a, 240);
        vertex(wr, m, -0.5f * var20, -0.5f * var20, 0.0f, var16, var19, rCol, gCol, bCol, a, 240);
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
        yd -= 0.04 * gravity;
        x += xd;
        y += yd;
        z += zd;
    }
}
