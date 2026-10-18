package thaumcraft.client.renderers.entity.projectile;

import java.util.Random;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import thaumcraft.client.lib.UtilsFX;

/**
 * Port of RenderEldritchOrb: twelve thin rays spinning out of the center (blended additively, white at the center and
 * clear at the tips, like the ender dragon's death rays) that grow during the first 10 ticks, and an animated
 * quad from particles.png facing the camera.
 */
public class RenderEldritchOrb extends EntityRenderer<Entity> {
    private final Random random = new Random();

    public RenderEldritchOrb(EntityRendererProvider.Context rm) {
        super(rm);
        shadowRadius = 0.0f;
    }

    @Override
    public void render(Entity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
        random.setSeed(187L);
        float f1 = entity.tickCount / 80.0f;
        float f3 = 0.0f;
        float grow = Math.min(entity.tickCount, 10) / 10.0f;
        VertexConsumer rays = buffer.getBuffer(RenderType.dragonRays());
        pose.pushPose();
        for (int i = 0; i < 12; ++i) {
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + f1 * 360.0f));
            float fa = random.nextFloat() * 20.0f + 5.0f + f3 * 10.0f;
            float f4 = random.nextFloat() * 2.0f + 1.0f + f3 * 2.0f;
            fa /= 30.0f / grow;
            f4 /= 30.0f / grow;
            Matrix4f m = pose.last().pose();
            // The original fan: the center, then three points around the tip.
            ray(rays, m, -0.866f * f4, fa, -0.5f * f4, 0.866f * f4, fa, -0.5f * f4);
            ray(rays, m, 0.866f * f4, fa, -0.5f * f4, 0.0f, fa, 1.0f * f4);
            ray(rays, m, 0.0f, fa, 1.0f * f4, -0.866f * f4, fa, -0.5f * f4);
        }
        pose.popPose();

        pose.pushPose();
        float u0 = entity.tickCount % 13 / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = 0.046875f;
        float v1 = v0 + 0.015625f;
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.scale(0.75f, 0.75f, 0.75f);
        Matrix4f m = pose.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(UtilsFX.PARTICLE_TEXTURE));
        vertex(vc, m, -0.5f, -0.5f, u0, v1, light);
        vertex(vc, m, 0.5f, -0.5f, u1, v1, light);
        vertex(vc, m, 0.5f, 0.5f, u1, v0, light);
        vertex(vc, m, -0.5f, 0.5f, u0, v0, light);
        pose.popPose();
        super.render(entity, yaw, partialTicks, pose, buffer, light);
    }

    /** One triangle of a ray. 1.12 wrapped the tip color bytes, so the tips came out almost fully transparent. */
    private static void ray(VertexConsumer vc, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2) {
        vc.addVertex(m, 0.0f, 0.0f, 0.0f).setColor(255, 255, 255, 255);
        vc.addVertex(m, x1, y1, z1).setColor(192, 192, 192, 1);
        vc.addVertex(m, x2, y2, z2).setColor(192, 192, 192, 1);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float u, float v, int light) {
        vc.addVertex(m, x, y, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(0.0f, 1.0f, 0.0f);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}
