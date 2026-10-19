package thaumcraft.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

/** Port of FXSlimyBubble: a bubble that swells, rises and pops on liquid death. */
public class FXSlimyBubble extends FXBase {
    int particle = 144;

    public FXSlimyBubble(ClientLevel world, double d, double d1, double d2, float f) {
        super(world, d, d1, d2, 0.0, 0.0, 0.0);
        rCol = 1.0f;
        gCol = 1.0f;
        bCol = 1.0f;
        gravity = 0.0f;
        xd = 0.0;
        yd = 0.0;
        zd = 0.0;
        particleScale = f;
        lifetime = 15 + random.nextInt(5);
        setSize(0.01f, 0.01f);
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float f) {
        float var8 = particle % 16 / 64.0f;
        float var9 = var8 + 0.015625f;
        float var10 = particle / 16 / 64.0f;
        float var11 = var10 + 0.015625f;
        billboard(wr, cam, relX(cam, f), relY(cam, f), relZ(cam, f), particleScale, 0.0f, var8, var9, var10, var11, rCol, gCol, bCol, alpha,
                getLightColor(f));
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        }
        if (age - 1 < 6) {
            particle = 144 + age / 2;
            if (age == 5) {
                y += 0.1;
            }
        } else if (age < lifetime - 4) {
            yd += 0.005;
            particle = 147 + age % 4 / 2;
        } else {
            yd /= 2.0;
            particle = 150 - (lifetime - age) / 2;
        }
        y += yd;
    }
}
