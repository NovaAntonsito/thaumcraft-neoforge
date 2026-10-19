package thaumcraft.client.fx.beams;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXBase;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.client.lib.render.TCRenderTypes;
import thaumcraft.common.lib.utils.Utils;

/**
 * Port of FXArc: an electric arc that falls like a thrown object from its start to the target, drawn as two crossed
 * ribbons with textures/misc/beamh.png, and leaves sparks along the way.
 */
public class FXArc extends FXBase {
    private static final ResourceLocation BEAM = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/misc/beamh.png");
    private final List<Vec3> points = new ArrayList<>();
    public float length = 1.0f;

    public FXArc(ClientLevel world, double x, double y, double z, double tx, double ty, double tz, float red, float green, float blue, double hg) {
        super(world, x, y, z);
        rCol = red;
        gCol = green;
        bCol = blue;
        setSize(0.02f, 0.02f);
        xd = 0.0;
        yd = 0.0;
        zd = 0.0;
        double dtX = tx - x;
        double dtY = ty - y;
        double dtZ = tz - z;
        lifetime = 3;
        double gravity = 0.115;
        double noise = 0.25;
        Vec3 vs = new Vec3(0.0, 0.0, 0.0);
        Vec3 ve = new Vec3(dtX, dtY, dtZ);
        Vec3 vc = new Vec3(0.0, 0.0, 0.0);
        length = (float) ve.length();
        Vec3 vv = Utils.calculateVelocity(vs, ve, hg, gravity);
        double l = Utils.distanceSquared3d(new Vec3(0.0, 0.0, 0.0), vv);
        points.add(vs);
        for (int c = 0; Utils.distanceSquared3d(ve, vc) > l && c < 50; ++c) {
            Vec3 vt = vc.add(vv.x, vv.y, vv.z);
            vc = new Vec3(vt.x, vt.y, vt.z);
            vt = vt.add((random.nextDouble() - random.nextDouble()) * noise, (random.nextDouble() - random.nextDouble()) * noise,
                    (random.nextDouble() - random.nextDouble()) * noise);
            points.add(vt);
            FXGeneric fb = new FXGeneric(world, x + vt.x, y + vt.y, z + vt.z, 0.0, 0.0, 0.0);
            int age = 30 + random.nextInt(20);
            fb.setMaxAge(age);
            fb.setRBGColorF(Mth.clamp(red * 3.0f, 0.0f, 1.0f), Mth.clamp(green * 3.0f, 0.0f, 1.0f), Mth.clamp(blue * 3.0f, 0.0f, 1.0f),
                    random.nextFloat(), random.nextFloat(), random.nextFloat());
            float[] alphas = new float[6 + random.nextInt(age / 3)];
            for (int a = 1; a < alphas.length - 1; ++a) {
                alphas[a] = random.nextFloat();
            }
            alphas[0] = 1.0f;
            fb.setAlphaF(alphas);
            boolean sp = random.nextFloat() < 0.2;
            fb.setParticles(sp ? 320 : 512, 16, 1);
            fb.setLoop(true);
            fb.setGravity(sp ? 0.0f : 0.125f);
            fb.setScale(0.5f, 0.125f);
            fb.setLayer(0);
            fb.setSlowDown(0.995);
            fb.setRandomMovementScale(0.0025f, 0.001f, 0.0025f);
            ParticleEngine.addEffectWithDelay(world, fb, 2 + random.nextInt(3));
            vv = vv.subtract(0.0, gravity / 1.9, 0.0);
        }
        points.add(ve);
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
        VertexConsumer vc = buffers.getBuffer(TCRenderTypes.beam(BEAM, true));
        float ox = relX(cam, f), oy = relY(cam, f), oz = relZ(cam, f);
        float size = 0.125f;
        float alpha = 1.0f - (age + f) / lifetime;
        // The two triangle strips of the original, one standing and one lying flat, as quads.
        for (int pass = 0; pass < 2; ++pass) {
            for (int c = 0; c < points.size() - 1; ++c) {
                Vec3 a = points.get(c), b = points.get(c + 1);
                float ua = c / length, ub = (c + 1) / length;
                double ax = pass == 0 ? 0.0 : size, ay = pass == 0 ? size : 0.0, az = pass == 0 ? 0.0 : size;
                vertex(vc, ox + a.x - ax, oy + a.y - ay, oz + a.z - az, ua, 1.0f, alpha);
                vertex(vc, ox + a.x + ax, oy + a.y + ay, oz + a.z + az, ua, 0.0f, alpha);
                vertex(vc, ox + b.x + ax, oy + b.y + ay, oz + b.z + az, ub, 0.0f, alpha);
                vertex(vc, ox + b.x - ax, oy + b.y - ay, oz + b.z - az, ub, 1.0f, alpha);
            }
        }
    }

    private void vertex(VertexConsumer vc, double x, double y, double z, float u, float v, float a) {
        vc.addVertex((float) x, (float) y, (float) z).setUv(u, v).setColor(rCol, gCol, bCol, a);
    }
}
