package thaumcraft.client.fx.beams;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.particles.FXBase;
import thaumcraft.client.lib.render.TCRenderTypes;

/**
 * Port of FXBolt: a wavy lightning bolt between two points, two glowing tubes (gle polycones, 5 sides) with
 * textures/misc/essentia.png. It wobbles more as it fades.
 */
public class FXBolt extends FXBase {
    private static final ResourceLocation BEAM = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/misc/essentia.png");
    float width;
    final List<Vec3> points = new ArrayList<>();
    final List<Float> pointsWidth = new ArrayList<>();
    float dr;
    long seed;
    private final double tX;
    private final double tY;
    private final double tZ;
    public float length;

    public FXBolt(ClientLevel world, double x, double y, double z, double tx, double ty, double tz, float red, float green, float blue, float width) {
        super(world, x, y, z);
        rCol = red;
        gCol = green;
        bCol = blue;
        setSize(0.02f, 0.02f);
        xd = 0.0;
        yd = 0.0;
        zd = 0.0;
        tX = tx - x;
        tY = ty - y;
        tZ = tz - z;
        this.width = width;
        lifetime = 3;
        Vec3 vs = new Vec3(0.0, 0.0, 0.0);
        Vec3 ve = new Vec3(tX, tY, tZ);
        length = (float) (ve.length() * Math.PI);
        int steps = (int) length;
        points.add(vs);
        pointsWidth.add(width);
        dr = (float) (random.nextInt(50) * Math.PI);
        float ampl = 0.1f;
        for (int a = 1; a < steps - 1; ++a) {
            float dist = a * (length / steps) + dr;
            double dx = tX / steps * a + Mth.sin(dist / 4.0f) * ampl;
            double dy = tY / steps * a + Mth.sin(dist / 3.0f) * ampl;
            double dz = tZ / steps * a + Mth.sin(dist / 2.0f) * ampl;
            dx += (random.nextFloat() - random.nextFloat()) * 0.1f;
            dy += (random.nextFloat() - random.nextFloat()) * 0.1f;
            dz += (random.nextFloat() - random.nextFloat()) * 0.1f;
            points.add(new Vec3(dx, dy, dz));
            pointsWidth.add(width);
        }
        pointsWidth.add(width);
        points.add(ve);
        seed = random.nextInt(1000);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        }
    }

    private void calcSteps(float f) {
        Random rr = new Random(seed);
        points.clear();
        pointsWidth.clear();
        Vec3 vs = new Vec3(0.0, 0.0, 0.0);
        Vec3 ve = new Vec3(tX, tY, tZ);
        int steps = (int) length;
        points.add(vs);
        pointsWidth.add(width);
        float ampl = (age + f) / 10.0f;
        for (int a = 1; a < steps - 1; ++a) {
            float dist = a * (length / steps) + dr;
            double dx = tX / steps * a + Mth.sin(dist / 4.0f) * ampl;
            double dy = tY / steps * a + Mth.sin(dist / 3.0f) * ampl;
            double dz = tZ / steps * a + Mth.sin(dist / 2.0f) * ampl;
            dx += (rr.nextFloat() - rr.nextFloat()) * 0.1f;
            dy += (rr.nextFloat() - rr.nextFloat()) * 0.1f;
            dz += (rr.nextFloat() - rr.nextFloat()) * 0.1f;
            points.add(new Vec3(dx, dy, dz));
            pointsWidth.add(((rr.nextInt(4) == 0) ? (1.0f - age * 0.25f) : 1.0f) * width);
        }
        pointsWidth.add(width);
        points.add(ve);
    }

    public void setRGB(float r, float g, float b) {
        rCol = r;
        gCol = g;
        bCol = b;
    }

    @Override
    public int getFXLayer() {
        return 1;
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
        calcSteps(f);
        float alpha = Mth.clamp(1.0f - age / (float) lifetime, 0.1f, 1.0f);
        if (points.size() > 2) {
            double[][] pp = new double[points.size()][3];
            float[][] colours = new float[points.size()][4];
            double[] radii = new double[points.size()];
            for (int a = 0; a < points.size(); ++a) {
                pp[a][0] = points.get(a).x;
                pp[a][1] = points.get(a).y;
                pp[a][2] = points.get(a).z;
                colours[a][0] = rCol;
                colours[a][1] = gCol;
                colours[a][2] = bCol;
                colours[a][3] = alpha;
                radii[a] = pointsWidth.get(a) / 10.0f;
            }
            VertexConsumer vc = buffers.getBuffer(TCRenderTypes.beam(BEAM, true));
            double ox = relX(cam, f), oy = relY(cam, f), oz = relZ(cam, f);
            PolyCone.draw(vc, ox, oy, oz, pp, colours, radii, 5, 1.0f, 0.0f);
            for (int a = 0; a < points.size(); ++a) {
                radii[a] /= 3.0;
            }
            PolyCone.draw(vc, ox, oy, oz, pp, colours, radii, 5, 1.0f, 0.0f);
        }
    }
}
