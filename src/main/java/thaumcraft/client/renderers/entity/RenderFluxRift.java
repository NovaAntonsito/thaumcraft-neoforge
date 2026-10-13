package thaumcraft.client.renderers.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.entities.EntityFluxRift;

/**
 * Port of RenderFluxRift: a wobbling tube (gle polycone, 6 sides) through the rift's points, drawn four times.
 * The core uses the vanilla end portal render type in place of the original star shader; the three wider halos
 * are additive (dragon rays render type, no depth write).
 * TODO: with goggles of revealing the first halo ignored depth.
 */
public class RenderFluxRift extends EntityRenderer<EntityFluxRift> {
    private static final int SIDES = 6;

    public RenderFluxRift(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(EntityFluxRift entity, Frustum frustum, double x, double y, double z) {
        return frustum.isVisible(entity.getBoundingBox().inflate(0.5));
    }

    @Override
    public void render(EntityFluxRift rift, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
        int n = rift.points.size();
        if (n <= 2) {
            return;
        }
        float amp = 1.0f;
        float stab = Mth.clamp(1.0f - rift.getRiftStability() / 50.0f, 0.0f, 1.5f);
        Vec3[] pp = new Vec3[n];
        double[] base = new double[n];
        for (int a = 0; a < n; ++a) {
            float var = rift.tickCount + pt;
            if (a > n / 2) {
                var -= a * 10;
            } else if (a < n / 2) {
                var += a * 10;
            }
            Vec3 p = rift.points.get(a);
            pp[a] = new Vec3(p.x + Math.sin(var / 50.0f * amp) * 0.1 * stab, p.y + Math.sin(var / 60.0f * amp) * 0.1 * stab,
                    p.z + Math.sin(var / 70.0f * amp) * 0.1 * stab);
            double w = 1.0 - Math.sin(var / 8.0f * amp) * 0.1 * stab;
            base[a] = rift.pointsWidth.get(a) * w;
        }
        Matrix4f m = pose.last().pose();
        VertexConsumer halo = buffers.getBuffer(RenderType.dragonRays());
        for (int q = 0; q < 3; ++q) {
            tube(halo, m, pp, base, 1.25f + 0.5f * q, true);
        }
        tube(buffers.getBuffer(RenderType.endPortal()), m, pp, base, 1.0f, false);
    }

    /** glePolyCone: the first and last points only give the direction of the ends. */
    private static void tube(VertexConsumer vc, Matrix4f m, Vec3[] pp, double[] radii, float scale, boolean triangles) {
        int n = pp.length;
        Vector3f[][] rings = new Vector3f[n][];
        for (int i = 1; i < n - 1; ++i) {
            Vec3 t = pp[i + 1].subtract(pp[i - 1]).normalize();
            Vec3 ref = Math.abs(t.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
            Vec3 u = t.cross(ref).normalize();
            Vec3 v = t.cross(u);
            double r = radii[i] * scale;
            rings[i] = new Vector3f[SIDES];
            for (int s = 0; s < SIDES; ++s) {
                double ang = s * Math.PI * 2.0 / SIDES;
                Vec3 o = pp[i].add(u.scale(Math.cos(ang) * r)).add(v.scale(Math.sin(ang) * r));
                rings[i][s] = new Vector3f((float) o.x, (float) o.y, (float) o.z);
            }
        }
        for (int i = 1; i < n - 2; ++i) {
            for (int s = 0; s < SIDES; ++s) {
                int s2 = (s + 1) % SIDES;
                Vector3f a = rings[i][s], b = rings[i][s2], c = rings[i + 1][s2], d = rings[i + 1][s];
                // Both windings: render types cull back faces and the ring order does not say which side is out.
                quad(vc, m, triangles, a, b, c, d);
                quad(vc, m, triangles, d, c, b, a);
            }
        }
    }

    private static void quad(VertexConsumer vc, Matrix4f m, boolean triangles, Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        if (triangles) {
            halo(vc, m, a);
            halo(vc, m, b);
            halo(vc, m, c);
            halo(vc, m, a);
            halo(vc, m, c);
            halo(vc, m, d);
        } else {
            vc.addVertex(m, a.x, a.y, a.z);
            vc.addVertex(m, b.x, b.y, b.z);
            vc.addVertex(m, c.x, c.y, c.z);
            vc.addVertex(m, d.x, d.y, d.z);
        }
    }

    private static void halo(VertexConsumer vc, Matrix4f m, Vector3f p) {
        vc.addVertex(m, p.x, p.y, p.z).setColor(0.35f, 0.2f, 0.5f, 0.35f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFluxRift entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
