package thaumcraft.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Port of FXBoreSparkle: a sparkle that flies to a point or an entity's eyes (infusion, the arcane bore). */
public class FXBoreSparkle extends FXBase {
    private Entity target;
    private double targetX;
    private double targetY;
    private double targetZ;
    public int particle = 24;

    public FXBoreSparkle(ClientLevel par1World, double par2, double par4, double par6, double tx, double ty, double tz) {
        super(par1World, par2, par4, par6, 0.0, 0.0, 0.0);
        particleScale = random.nextFloat() * 0.5f + 0.5f;
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int) (Mth.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 10.0f);
        if (base < 1) {
            base = 1;
        }
        lifetime = base / 2 + random.nextInt(base);
        float f3 = 0.01f;
        xd = (float) random.nextGaussian() * f3;
        yd = (float) random.nextGaussian() * f3;
        zd = (float) random.nextGaussian() * f3;
        rCol = 0.2f;
        gCol = 0.6f + random.nextFloat() * 0.3f;
        bCol = 0.2f;
        gravity = 0.2f;
        Minecraft mc = Minecraft.getInstance();
        Entity renderentity = mc.getCameraEntity();
        int visibleDistance = 64;
        if (mc.options.graphicsMode().get() == GraphicsStatus.FAST) {
            visibleDistance = 32;
        }
        if (renderentity != null && Math.sqrt(renderentity.distanceToSqr(x, y, z)) > visibleDistance) {
            lifetime = 0;
        }
    }

    public FXBoreSparkle(ClientLevel par1World, double par2, double par4, double par6, Entity t) {
        this(par1World, par2, par4, par6, t.getX(), t.getY() + t.getEyeHeight(), t.getZ());
        target = t;
    }

    @Override
    public void render(VertexConsumer wr, Camera cam, float f) {
        float bob = Mth.sin(age / 3.0f) * 0.5f + 1.0f;
        int part = age % 4;
        float var8 = part / 64.0f;
        float var9 = var8 + 0.015625f;
        float var10 = 0.0625f;
        float var11 = var10 + 0.015625f;
        float var12 = 0.1f * particleScale * bob;
        billboard(wr, cam, relX(cam, f), relY(cam, f), relZ(cam, f), var12, 0.0f, var8, var9, var10, var11, rCol, gCol, bCol, 1.0f, 240);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (target != null) {
            targetX = target.getX();
            targetY = target.getY() + target.getEyeHeight();
            targetZ = target.getZ();
        }
        if (age++ >= lifetime || (Mth.floor(x) == Mth.floor(targetX) && Mth.floor(y) == Mth.floor(targetY) && Mth.floor(z) == Mth.floor(targetZ))) {
            remove();
            return;
        }
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.95;
        zd *= 0.985;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double d11 = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double clamp = Math.min(0.25, d11 / 15.0);
        if (d11 < 2.0) {
            particleScale *= 0.9f;
        }
        dx /= d11;
        dy /= d11;
        dz /= d11;
        xd += dx * clamp;
        yd += dy * clamp;
        zd += dz * clamp;
        xd = Mth.clamp((float) xd, -clamp, clamp);
        yd = Mth.clamp((float) yd, -clamp, clamp);
        zd = Mth.clamp((float) zd, -clamp, clamp);
        xd += random.nextGaussian() * 0.01;
        yd += random.nextGaussian() * 0.01;
        zd += random.nextGaussian() * 0.01;
    }
}
