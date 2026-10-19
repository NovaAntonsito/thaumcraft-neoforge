package thaumcraft.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

/** Port of FXWispEG: the dark wisps that sink below an eldritch guardian and drift along with it. */
public class FXWispEG extends FXBase {
    Entity target = null;
    public int blendmode = 1;

    public FXWispEG(ClientLevel world, double posX, double posY, double posZ, Entity target2) {
        super(world, posX, posY, posZ, 0.0, 0.0, 0.0);
        target = target2;
        xd = random.nextGaussian() * 0.03;
        yd = -0.05;
        zd = random.nextGaussian() * 0.03;
        particleScale *= 0.4f;
        lifetime = (int) (40.0 / (Math.random() * 0.3 + 0.7));
        setSize(0.01f, 0.01f);
        xo = x;
        yo = y;
        zo = z;
        blendmode = 771;
        rCol = random.nextFloat() * 0.05f;
        gCol = random.nextFloat() * 0.05f;
        bCol = random.nextFloat() * 0.05f;
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float f) {
        float agescale = 1.0f - age / (float) lifetime;
        float d6 = 1024.0f;
        double dist = cam.getPosition().distanceToSqr(x, y, z);
        float base = (float) (1.0 - Math.min(d6, dist) / d6);
        float f6 = 0.5f * particleScale;
        float var8 = age % 13 / 64.0f;
        float var9 = var8 + 0.015625f;
        float var10 = 0.046875f;
        float var11 = var10 + 0.015625f;
        billboard(wr, cam, relX(cam, f), relY(cam, f), relZ(cam, f), f6, 0.0f, var8, var9, var10, var11, rCol, gCol, bCol, 0.2f * agescale * base, 240);
    }

    @Override
    public int getFXLayer() {
        return blendmode != 1 ? 1 : 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (target != null && !onGround) {
            x += target.getDeltaMovement().x;
            z += target.getDeltaMovement().z;
        }
    }
}
