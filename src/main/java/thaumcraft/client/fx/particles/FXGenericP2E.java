package thaumcraft.client.fx.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Port of FXGenericP2E: an FXGeneric that homes in on an entity and dies when it gets there. */
public class FXGenericP2E extends FXGeneric {
    private final Entity target;

    public FXGenericP2E(ClientLevel world, double x, double y, double z, Entity target) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        setSize(0.1f, 0.1f);
        this.target = target;
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double dz = target.getZ() - z;
        int base = (int) (Mth.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 5.0f);
        if (base < 1) {
            base = 1;
        }
        lifetime = base;
        xo = x;
        yo = y;
        zo = z;
        float f3 = 0.01f;
        xd = (float) random.nextGaussian() * f3;
        yd = (float) random.nextGaussian() * f3;
        zd = (float) random.nextGaussian() * f3;
        gravity = 0.2f;
    }

    @Override
    public void tick() {
        super.tick();
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double dz = target.getZ() - z;
        double d13 = 0.3;
        double d14 = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (d14 < 4.0) {
            particleScale *= 0.9f;
            d13 = 0.6;
        }
        if (d14 < 0.25) {
            remove();
        }
        dx /= d14;
        dy /= d14;
        dz /= d14;
        xd += dx * d13;
        yd += dy * d13;
        zd += dz * d13;
        xd = Mth.clamp((float) xd, -0.35f, 0.35f);
        yd = Mth.clamp((float) yd, -0.35f, 0.35f);
        zd = Mth.clamp((float) zd, -0.35f, 0.35f);
    }
}
