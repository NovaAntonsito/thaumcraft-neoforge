package thaumcraft.client.renderers.entity.projectile;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.lib.render.TCRenderTypes;
import thaumcraft.client.renderers.models.entity.ModelGrappler;
import thaumcraft.common.entities.projectile.EntityGrapple;

/**
 * Port of RenderGrapple: the hook (the grappler model and a pulsing flare) and the rope back to the thrower, which
 * sways until the hook catches. 1.21: the rope is a four sided tube built here in place of the gle polycone.
 */
public class RenderGrapple extends EntityRenderer<EntityGrapple> {
    private static final ResourceLocation beam = Thaumcraft.id("textures/entity/grappler.png");
    private static final ResourceLocation rope = Thaumcraft.id("textures/misc/rope.png");
    private static final int SIDES = 4;
    private final ModelGrappler model = new ModelGrappler();
    public List<Vec3> points = new ArrayList<>();
    public float length = 1.0f;

    public RenderGrapple(EntityRendererProvider.Context rm) {
        super(rm);
        shadowRadius = 0.0f;
    }

    @Override
    public void render(EntityGrapple entity, float entityYaw, float pticks, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pticks, entity.yRotO, entity.getYRot()) - 90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pticks, entity.xRotO, entity.getXRot())));
        model.render(pose, buffer.getBuffer(RenderType.entityCutoutNoCull(beam)), packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        pose.popPose();
        float f2 = (1 + entity.tickCount % 6) / 32.0f;
        float f3 = f2 + 0.03125f;
        float f4 = 0.21875f;
        float f5 = f4 + 0.03125f;
        float f6 = 0.5f;
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        float bob = Mth.sin(entity.tickCount / 5.0f) * 0.2f + 0.2f;
        pose.scale(1.0f + bob, 1.0f + bob, 1.0f + bob);
        VertexConsumer vc = buffer.getBuffer(TCRenderTypes.entityGlow(UtilsFX.PARTICLE_TEXTURE));
        PoseStack.Pose last = pose.last();
        int light = LightTexture.FULL_BRIGHT;
        flare(vc, last, -f6, -f6, f2, f5, light);
        flare(vc, last, f6, -f6, f3, f5, light);
        flare(vc, last, f6, f6, f3, f4, light);
        flare(vc, last, -f6, f6, f2, f4, light);
        pose.popPose();
        calcPoints(entity.getThrower(), entity, pticks);
        if (points.size() > 2) {
            ropeTube(buffer.getBuffer(RenderType.entityCutoutNoCull(rope)), pose.last(), packedLight);
        }
        super.render(entity, entityYaw, pticks, pose, buffer, packedLight);
    }

    private static void flare(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v, int light) {
        vc.addVertex(pose, x, y, 0.0f).setColor(1.0f, 1.0f, 1.0f, 0.21f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    /** The rope, radius 0.025, through the points (the first and last point only give the direction of the ends). */
    private void ropeTube(VertexConsumer vc, PoseStack.Pose pose, int light) {
        int n = points.size();
        Vec3[][] rings = new Vec3[n][];
        float[] vs = new float[n];
        float dist = 0.0f;
        for (int i = 1; i < n - 1; ++i) {
            Vec3 t = points.get(Math.min(n - 1, i + 1)).subtract(points.get(i - 1));
            if (t.lengthSqr() < 1.0E-12) {
                t = new Vec3(0, 1, 0);
            }
            t = t.normalize();
            Vec3 ref = Math.abs(t.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
            Vec3 u = t.cross(ref).normalize();
            Vec3 w = t.cross(u);
            rings[i] = new Vec3[SIDES];
            for (int s = 0; s < SIDES; ++s) {
                double ang = s * Math.PI * 2.0 / SIDES;
                rings[i][s] = points.get(i).add(u.scale(Math.cos(ang) * 0.025)).add(w.scale(Math.sin(ang) * 0.025));
            }
            if (i > 1) {
                dist += (float) points.get(i).distanceTo(points.get(i - 1));
            }
            vs[i] = dist * 2.0f;
        }
        Matrix4f m = pose.pose();
        for (int i = 1; i < n - 2; ++i) {
            for (int s = 0; s < SIDES; ++s) {
                int s2 = (s + 1) % SIDES;
                float u1 = s / (float) SIDES;
                float u2 = (s + 1) / (float) SIDES;
                ropeVertex(vc, pose, m, rings[i][s], u1, vs[i], light);
                ropeVertex(vc, pose, m, rings[i][s2], u2, vs[i], light);
                ropeVertex(vc, pose, m, rings[i + 1][s2], u2, vs[i + 1], light);
                ropeVertex(vc, pose, m, rings[i + 1][s], u1, vs[i + 1], light);
            }
        }
    }

    private static void ropeVertex(VertexConsumer vc, PoseStack.Pose pose, Matrix4f m, Vec3 p, float u, float v, int light) {
        vc.addVertex(m, (float) p.x, (float) p.y, (float) p.z).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private void calcPoints(LivingEntity thrower, EntityGrapple grapple, float pt) {
        points.clear();
        if (thrower == null || grapple == null) {
            return;
        }
        double tx = Mth.lerp(pt, thrower.xOld, thrower.getX());
        double ty = Mth.lerp(pt, thrower.yOld, thrower.getY());
        double tz = Mth.lerp(pt, thrower.zOld, thrower.getZ());
        // As in the original, every thrower's rope starts at the hand while the view is first person.
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            double yy = Mth.lerp(pt, thrower.yRotO, thrower.getYRot());
            double px = -Mth.cos((float) ((yy - 0.5) / 180.0 * 3.1415929794311523)) * 0.1f * (grapple.hand == InteractionHand.MAIN_HAND ? 1 : -1);
            double pz = -Mth.sin((float) ((yy - 0.5) / 180.0 * 3.1415929794311523)) * 0.1f * (grapple.hand == InteractionHand.MAIN_HAND ? 1 : -1);
            Vec3 vl = thrower.getLookAngle();
            tx += px + vl.x / 5.0;
            ty += thrower.getEyeHeight() / 2.6 + vl.y / 5.0;
            tz += pz + vl.z / 5.0;
        }
        double gx = Mth.lerp(pt, grapple.xOld, grapple.getX());
        double gy = Mth.lerp(pt, grapple.yOld, grapple.getY());
        double gz = Mth.lerp(pt, grapple.zOld, grapple.getZ());
        Vec3 vs = Vec3.ZERO;
        Vec3 ve = new Vec3(tx - gx, ty - gy + thrower.getBbHeight() / 2.0f, tz - gz);
        length = (float) (ve.length() * 5.0);
        int steps = (int) length;
        points.add(vs);
        for (int a = 1; a < steps - 1; ++a) {
            float dist = a * (length / steps);
            float ss = 1.0f - a / (steps * 1.25f);
            double dx = (tx - gx) / steps * a + Mth.sin(dist / 10.0f) * grapple.ampl * ss;
            double dy = (ty - gy + thrower.getBbHeight() / 2.0f) / steps * a + Mth.sin(dist / 8.0f) * grapple.ampl * ss;
            double dz = (tz - gz) / steps * a + Mth.sin(dist / 6.0f) * grapple.ampl * ss;
            points.add(new Vec3(dx, dy, dz));
        }
        points.add(ve);
        points.add(ve);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGrapple entity) {
        return beam;
    }
}
