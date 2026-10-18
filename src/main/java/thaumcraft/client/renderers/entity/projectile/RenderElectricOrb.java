package thaumcraft.client.renderers.entity.projectile;

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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.projectile.EntityGolemOrb;

/**
 * Port of RenderElectricOrb: a glowing animated quad from particles.png that faces the camera and pulses.
 * Red orbs use the row above. The original blended with (SRC_ALPHA, ONE), as RenderType.eyes does.
 */
public class RenderElectricOrb<T extends Entity> extends EntityRenderer<T> {
    public RenderElectricOrb(EntityRendererProvider.Context rm) {
        super(rm);
        shadowRadius = 0.0f;
    }

    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
        float f2 = (1 + entity.tickCount % 6) / 32.0f;
        float f3 = f2 + 0.03125f;
        float f4 = 0.21875f;
        if (entity instanceof EntityGolemOrb orb && orb.red) {
            f4 = 0.1875f;
        }
        float f5 = f4 + 0.03125f;
        float f6 = 1.0f;
        float f7 = 0.5f;
        float f8 = 0.5f;
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        float bob = Mth.sin(entity.tickCount / 5.0f) * 0.2f + 0.2f;
        pose.scale(1.0f + bob, 1.0f + bob, 1.0f + bob);
        Matrix4f m = pose.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.eyes(UtilsFX.PARTICLE_TEXTURE));
        vertex(vc, m, -f7, -f8, f2, f5);
        vertex(vc, m, f6 - f7, -f8, f3, f5);
        vertex(vc, m, f6 - f7, 1.0f - f8, f3, f4);
        vertex(vc, m, -f7, 1.0f - f8, f2, f4);
        pose.popPose();
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float u, float v) {
        vc.addVertex(m, x, y, 0.0f).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(0.0f, 1.0f, 0.0f);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
