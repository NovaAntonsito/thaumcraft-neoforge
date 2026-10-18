package thaumcraft.client.renderers.entity.mob;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.monster.EntityWisp;

/**
 * The wisp has no model: three glowing animated quads facing the camera (UtilsFX.renderFacingQuad), a white core and a
 * faint white halo from particles.png and an aura node from auranodes.png in the color of its aspect.
 * The original blended with (SRC_ALPHA, ONE); RenderType.eyes blends (ONE, ONE), so colors are pre-multiplied by alpha.
 */
public class RenderWisp extends EntityRenderer<EntityWisp> {
    public RenderWisp(EntityRendererProvider.Context rm) {
        super(rm);
        shadowRadius = 0.0f;
    }

    @Override
    public void render(EntityWisp entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
        if (entity.getHealth() <= 0.0f) {
            return;
        }
        int color = 0;
        Aspect aspect = Aspect.getAspect(entity.getWispType());
        if (aspect != null) {
            color = aspect.getColor();
        }
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        Matrix4f m = pose.last().pose();
        facingQuad(buffer.getBuffer(RenderType.eyes(UtilsFX.PARTICLE_TEXTURE)), m, 64, 512 + entity.tickCount % 16, 0.4f, 0xFFFFFF, 1.0f);
        facingQuad(buffer.getBuffer(RenderType.eyes(UtilsFX.PARTICLE_TEXTURE)), m, 64, 320 + entity.tickCount % 16, 0.75f, 0xFFFFFF, 0.25f);
        facingQuad(buffer.getBuffer(RenderType.eyes(UtilsFX.nodeTexture)), m, 32, 800 + entity.tickCount % 16, 0.75f, color, 0.5f);
        pose.popPose();
    }

    /** One frame of a square grid texture, 2 * scale wide, drawn from both sides. */
    private static void facingQuad(VertexConsumer vc, Matrix4f m, int grid, int frame, float scale, int color, float alpha) {
        float f1 = (frame % grid) / (float) grid;
        float f2 = f1 + 1.0f / grid;
        float f3 = (frame / grid) / (float) grid;
        float f4 = f3 + 1.0f / grid;
        float r = ((color >> 16) & 0xFF) / 255.0f * alpha;
        float g = ((color >> 8) & 0xFF) / 255.0f * alpha;
        float b = (color & 0xFF) / 255.0f * alpha;
        float[][] v = { { -scale, -scale, f2, f4 }, { -scale, scale, f2, f3 }, { scale, scale, f1, f3 }, { scale, -scale, f1, f4 } };
        for (int i = 0; i < 4; ++i) {
            vertex(vc, m, v[i], r, g, b);
        }
        for (int i = 3; i >= 0; --i) {
            vertex(vc, m, v[i], r, g, b);
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float[] v, float r, float g, float b) {
        vc.addVertex(m, v[0], v[1], 0.0f).setColor(r, g, b, 1.0f).setUv(v[2], v[3]).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(0.0f, 1.0f, 0.0f);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureLocation(EntityWisp entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
