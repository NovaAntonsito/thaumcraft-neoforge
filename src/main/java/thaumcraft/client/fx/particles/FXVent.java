package thaumcraft.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Port of FXVent: a puff of colored gas that swells until it is gone. */
public class FXVent extends FXBase {
    float psm = 1.0f;

    public FXVent(ClientLevel par1World, double par2, double par4, double par6, double par8, double par10, double par12, int color) {
        super(par1World, par2, par4, par6, par8, par10, par12);
        setSize(0.02f, 0.02f);
        particleScale = random.nextFloat() * 0.1f + 0.05f;
        xd = par8;
        yd = par10;
        zd = par12;
        rCol = ((color >> 16) & 0xFF) / 255.0f;
        bCol = (color & 0xFF) / 255.0f;
        gCol = ((color >> 8) & 0xFF) / 255.0f;
        setHeading(xd, yd, zd, 0.125f, 5.0f);
        Minecraft mc = Minecraft.getInstance();
        Entity renderentity = mc.getCameraEntity();
        int visibleDistance = 50;
        if (mc.options.graphicsMode().get() == GraphicsStatus.FAST) {
            visibleDistance = 25;
        }
        if (renderentity != null && Math.sqrt(renderentity.distanceToSqr(x, y, z)) > visibleDistance) {
            lifetime = 0;
        }
        xo = x;
        yo = y;
        zo = z;
    }

    public void setScale(float f) {
        particleScale *= f;
        psm *= f;
    }

    public void setHeading(double par1, double par3, double par5, float par7, float par8) {
        float f2 = Mth.sqrt((float) (par1 * par1 + par3 * par3 + par5 * par5));
        par1 /= f2;
        par3 /= f2;
        par5 /= f2;
        par1 += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.007499999832361937 * par8;
        par3 += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.007499999832361937 * par8;
        par5 += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.007499999832361937 * par8;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        xd = par1;
        yd = par3;
        zd = par5;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        ++age;
        if (particleScale >= psm) {
            remove();
        }
        yd += 0.0025;
        move(xd, yd, zd);
        xd *= 0.8500000190734863;
        yd *= 0.8500000190734863;
        zd *= 0.8500000190734863;
        if (particleScale < psm) {
            particleScale *= (float) 1.15;
        }
        if (particleScale > psm) {
            particleScale = psm;
        }
        if (onGround) {
            xd *= 0.699999988079071;
            zd *= 0.699999988079071;
        }
    }

    public void setRGB(float r, float g, float b) {
        rCol = r;
        gCol = g;
        bCol = b;
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float f) {
        int part = (int) (1.0f + particleScale / psm * 4.0f);
        float var8 = part % 16 / 64.0f;
        float var9 = var8 + 0.015625f;
        float var10 = part / 64 / 64.0f;
        float var11 = var10 + 0.015625f;
        float var12 = 0.3f * particleScale;
        float a = alpha * ((psm - particleScale) / psm);
        billboard(wr, cam, relX(cam, f), relY(cam, f), relZ(cam, f), var12, 0.0f, var8, var9, var10, var11, rCol, gCol, bCol, a, getLightColor(f));
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
