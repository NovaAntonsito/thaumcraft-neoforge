package thaumcraft.client.fx.other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.fx.beams.PolyCone;
import thaumcraft.client.fx.particles.FXBase;
import thaumcraft.client.lib.render.TCRenderTypes;

/**
 * Port of FXEssentiaStream: a wobbling tube of essentia (gle polycone, 8 sides, textures/misc/essentia.png) that
 * grows from its source toward the target, drips, and then shrinks into the target. A new stream between the same
 * two blocks with the same color makes the running one longer instead.
 * Also used for the water of the everfull urn.
 */
public class FXEssentiaStream extends FXBase {
    private static final ResourceLocation TEX0 = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/misc/essentia.png");
    static final HashMap<String, FXEssentiaStream> pt = new HashMap<>();
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final int count;
    public int length;
    private String key = "";
    int layer = 1;
    double[][] points;
    float[][] colours;
    double[] radii;
    int growing = -1;
    final List<double[]> vecs = new ArrayList<>();

    public FXEssentiaStream(ClientLevel w, double x, double y, double z, double tx, double ty, double tz, int count, int color, float scale, int extend,
                            double my) {
        super(w, x, y, z);
        particleScale = (float) (scale * (1.0 + random.nextGaussian() * 0.15000000596046448));
        length = Math.max(20, extend);
        this.count = count;
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        BlockPos bp1 = BlockPos.containing(this.x, this.y, this.z);
        BlockPos bp2 = BlockPos.containing(targetX, targetY, targetZ);
        // TODO: the original nudged the start out of an essentia input or output block (not ported yet).
        double dx = tx - this.x;
        double dy = ty - this.y;
        double dz = tz - this.z;
        int base = (int) (Mth.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 21.0f);
        if (base < 1) {
            base = 1;
        }
        lifetime = base;
        String k = bp1.asLong() + "" + bp2.asLong() + "" + color;
        if (pt.containsKey(k)) {
            FXEssentiaStream trail2 = pt.get(k);
            if (trail2.isAlive() && trail2.vecs.size() < trail2.length) {
                trail2.length += Math.max(extend, 5);
                trail2.lifetime += Math.max(extend, 5);
                lifetime = 0;
            }
        }
        if (lifetime > 0) {
            pt.put(k, this);
            key = k;
        }
        xd = Mth.sin(count / 4.0f) * 0.015f;
        yd = my + Mth.sin(count / 3.0f) * 0.015f;
        zd = Mth.sin(count / 2.0f) * 0.015f;
        rCol = ((color >> 16) & 0xFF) / 255.0f;
        gCol = ((color >> 8) & 0xFF) / 255.0f;
        bCol = (color & 0xFF) / 255.0f;
        gravity = 0.2f;
        vecs.add(new double[] { 0.0, 0.0, 0.0, 0.001 });
        vecs.add(new double[] { 0.0, 0.0, 0.0, 0.001 });
        startX = this.x;
        startY = this.y;
        startZ = this.z;
    }

    public void setFXLayer(int l) {
        layer = l;
    }

    @Override
    public int getFXLayer() {
        return layer;
    }

    @Override
    public boolean isCustomRender() {
        return true;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
    }

    @Override
    public void renderCustom(MultiBufferSource buffers, Camera cam, float f) {
        if (points != null && points.length > 2) {
            VertexConsumer vc = buffers.getBuffer(TCRenderTypes.beam(TEX0, false));
            PolyCone.draw(vc, startX - cam.getPosition().x, startY - cam.getPosition().y, startZ - cam.getPosition().z, points, colours, radii, 8, 0.075f,
                    growing < 0 ? 0.0f : 0.075f * (age - growing + f));
        }
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime || length < 1) {
            remove();
            if (pt.containsKey(key) && !pt.get(key).isAlive()) {
                pt.remove(key);
            }
            return;
        }
        yd += 0.01 * gravity;
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.985;
        zd *= 0.985;
        xd = Mth.clamp((float) xd, -0.05f, 0.05f);
        yd = Mth.clamp((float) yd, -0.05f, 0.05f);
        zd = Mth.clamp((float) zd, -0.05f, 0.05f);
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double d13 = 0.01;
        double d14 = Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));
        dx /= d14;
        dy /= d14;
        dz /= d14;
        xd += dx * (d13 / Math.min(1.0, d14));
        yd += dy * (d13 / Math.min(1.0, d14));
        zd += dz * (d13 / Math.min(1.0, d14));
        float scale = particleScale * (0.75f + Mth.sin((count + age) / 2.0f) * 0.25f);
        if (d14 < 1.0) {
            float f = Mth.sin((float) (d14 * 1.5707963267948966));
            scale *= f;
            particleScale *= f;
        }
        if (particleScale > 0.001) {
            vecs.add(new double[] { x - startX, y - startY, z - startZ, scale });
        } else {
            if (growing < 0) {
                growing = age;
            }
            --length;
            FXDispatcher.essentiaDropFx(targetX + random.nextGaussian() * 0.07500000298023224, targetY + random.nextGaussian() * 0.07500000298023224,
                    targetZ + random.nextGaussian() * 0.07500000298023224, rCol, gCol, bCol, 0.5f);
        }
        if (vecs.size() > length) {
            vecs.remove(0);
        }
        points = new double[vecs.size()][3];
        colours = new float[vecs.size()][4];
        radii = new double[vecs.size()];
        int c = vecs.size();
        for (double[] v : vecs) {
            --c;
            float variance = 1.0f + Mth.sin((c + age) / 3.0f) * 0.2f;
            float xx = Mth.sin((c + age) / 6.0f) * 0.03f;
            float yy = Mth.sin((c + age) / 7.0f) * 0.03f;
            float zz = Mth.sin((c + age) / 8.0f) * 0.03f;
            points[c][0] = v[0] + xx;
            points[c][1] = v[1] + yy;
            points[c][2] = v[2] + zz;
            radii[c] = v[3] * variance;
            if (c > vecs.size() - 10) {
                radii[c] *= Mth.cos((float) ((c - (vecs.size() - 12)) / 10.0f * 1.5707963267948966));
            }
            if (c == 0) {
                radii[c] = 0.0;
            } else if (c == 1) {
                radii[c] = 0.0;
            } else if (c == 2) {
                radii[c] = (particleScale * 0.5 + radii[c]) / 2.0;
            } else if (c == 3) {
                radii[c] = (particleScale + radii[c]) / 2.0;
            } else if (c == 4) {
                radii[c] = (particleScale + radii[c] * 2.0) / 3.0;
            }
            float v2 = 1.0f - Mth.sin((c + age) / 2.0f) * 0.1f;
            colours[c][0] = rCol * v2;
            colours[c][1] = gCol * v2;
            colours[c][2] = bCol * v2;
            colours[c][3] = 1.0f;
        }
        if (vecs.size() > 2 && random.nextBoolean()) {
            int q = random.nextInt(3);
            if (random.nextBoolean()) {
                q = vecs.size() - 2;
            }
            double[] v = vecs.get(q);
            FXDispatcher.essentiaDropFx(v[0] + startX, v[1] + startY, v[2] + startZ, rCol, gCol, bCol, 0.5f);
        }
    }

    public void setGravity(float value) {
        gravity = value;
    }
}
