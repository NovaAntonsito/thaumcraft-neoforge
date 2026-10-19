package thaumcraft.client.fx.other;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.client.fx.beams.PolyCone;
import thaumcraft.client.fx.particles.FXBase;
import thaumcraft.client.lib.render.TCRenderTypes;

/**
 * Port of FXVoidStream: the dark streak a void siphon pulls out of a flux rift, a thin wobbling tube (gle polycone,
 * 3 sides) drawn twice with the end portal stars: a wider glow added on top, then the core.
 * 1.21: the original ran its end shader on both passes; the glow uses the stars texture and the core the vanilla
 * end portal render type.
 */
public class FXVoidStream extends FXBase {
    private static final ResourceLocation STARS = ResourceLocation.withDefaultNamespace("textures/entity/end_portal.png");
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final int seed;
    public int length = 40;
    int layer = 1;
    double[][] points;
    float[][] colours;
    double[] radii;
    int growing = -1;
    final List<double[]> vecs = new ArrayList<>();

    public FXVoidStream(ClientLevel w, double x, double y, double z, double tx, double ty, double tz, int seed, float scale) {
        super(w, x, y, z);
        particleScale = (float) (scale * (1.0 + random.nextGaussian() * 0.15000000596046448));
        this.seed = seed;
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        double dx = tx - this.x;
        double dy = ty - this.y;
        double dz = tz - this.z;
        int base = (int) (Mth.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 21.0f);
        if (base < 1) {
            base = 1;
        }
        lifetime = base * 2;
        xd = Mth.sin(seed / 4.0f) * 0.025f;
        yd = Mth.sin(seed / 3.0f) * 0.025f;
        zd = Mth.sin(seed / 2.0f) * 0.025f;
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
        if (points == null || points.length <= 2) return;
        double ox = startX - cam.getPosition().x, oy = startY - cam.getPosition().y, oz = startZ - cam.getPosition().z;
        float off = growing < 0 ? 0.0f : 0.075f * (age - growing + f);
        for (int q = 0; q <= 1; ++q) {
            double[] r2 = new double[radii.length];
            float m = (1.5f - q) / 1.0f;
            for (int ri = 0; ri < radii.length; ++ri) {
                r2[ri] = radii[ri] * m;
            }
            if (q < 1) {
                PolyCone.draw(buffers.getBuffer(TCRenderTypes.beam(STARS, true)), ox, oy, oz, points, colours, r2, 3, 0.075f, off);
            } else {
                PolyCone.draw(new PositionOnly(buffers.getBuffer(RenderType.endPortal())), ox, oy, oz, points, colours, r2, 3, 0.075f, off);
            }
        }
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime || length < 1) {
            remove();
            return;
        }
        yd += 0.01 * gravity;
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.985;
        zd *= 0.985;
        xd = Mth.clamp((float) xd, -0.04f, 0.04f);
        yd = Mth.clamp((float) yd, -0.04f, 0.04f);
        zd = Mth.clamp((float) zd, -0.04f, 0.04f);
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double d13 = 0.01;
        double d14 = Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));
        dx /= d14;
        dy /= d14;
        dz /= d14;
        xd += dx * (d13 / Math.min(1.0, d14)) + random.nextGaussian() * 0.014999999664723873;
        yd += dy * (d13 / Math.min(1.0, d14)) + random.nextGaussian() * 0.014999999664723873;
        zd += dz * (d13 / Math.min(1.0, d14)) + random.nextGaussian() * 0.014999999664723873;
        float scale = particleScale * (0.75f + Mth.sin((seed + age) / 2.0f) * 0.25f);
        if (d14 < 0.5) {
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
            float xx = Mth.sin((c + age) / 6.0f) * 0.01f;
            float yy = Mth.sin((c + age) / 7.0f) * 0.01f;
            float zz = Mth.sin((c + age) / 8.0f) * 0.01f;
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
            colours[c][0] = 1.0f;
            colours[c][1] = 1.0f;
            colours[c][2] = 1.0f;
            colours[c][3] = 1.0f;
        }
    }

    public void setGravity(float value) {
        gravity = value;
    }

    /** The end portal render type only takes positions; the uv and color of the tube are dropped. */
    private record PositionOnly(VertexConsumer parent) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            parent.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }
    }
}
