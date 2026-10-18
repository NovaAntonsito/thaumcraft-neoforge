package thaumcraft.client.renderers.entity.construct;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser;

/**
 * Port of RenderCultistPortalLesser: once active, a 16 frame animated quad that faces the camera. It grows in over
 * 50 ticks, shudders when hurt, pulses when a cultist comes out and flickers more as it loses health.
 * The original scaled the vertical axis in world space; here it is the camera's vertical axis.
 */
public class RenderCultistPortalLesser extends EntityRenderer<EntityCultistPortalLesser> {
    public static final ResourceLocation portaltex = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/cultist_portal.png");

    public RenderCultistPortalLesser(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        shadowRadius = 0.0f;
        shadowStrength = 0.0f;
    }

    @Override
    public void render(EntityCultistPortalLesser portal, float yaw, float f, PoseStack pose, MultiBufferSource buffer, int light) {
        if (!portal.isActive()) {
            return;
        }
        long nt = System.nanoTime();
        long time = nt / 50000000L;
        float scaley = 1.4f;
        int e = (int) Math.min(50.0f, portal.activeCounter + f);
        if (portal.hurtTime > 0) {
            double d = Math.sin(portal.hurtTime * 72 * Math.PI / 180.0);
            scaley -= (float) (d / 4.0);
            e += (int) (6.0 * d);
        }
        if (portal.pulse > 0) {
            double d = Math.sin(portal.pulse * 36 * Math.PI / 180.0);
            scaley += (float) (d / 4.0);
            e += (int) (12.0 * d);
        }
        float scale = e / 50.0f * 1.25f;
        float m = (1.0f - portal.getHealth() / portal.getMaxHealth()) / 3.0f;
        float bob = Mth.sin(portal.activeCounter / (5.0f - 12.0f * m)) * m + m;
        float bob2 = Mth.sin(portal.activeCounter / (6.0f - 15.0f * m)) * m + m;
        float alpha = 1.0f - bob;
        scaley -= bob / 4.0f;
        scale -= bob2 / 3.0f;
        int frame = 15 - (int) time % 16;
        float f2 = frame / 16.0f;
        float f3 = f2 + 0.0625f;
        float f4 = 0.0f;
        float f5 = 1.0f;
        pose.pushPose();
        pose.translate(0.0, portal.getBbHeight() / 2.0f, 0.0);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        Matrix4f mat = pose.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(portaltex));
        // Same corners and UVs as the original (the frame is drawn turned half round).
        vertex(vc, mat, -scale, -scaley, f3, f4, alpha);
        vertex(vc, mat, -scale, scaley, f3, f5, alpha);
        vertex(vc, mat, scale, scaley, f2, f5, alpha);
        vertex(vc, mat, scale, -scaley, f2, f4, alpha);
        pose.popPose();
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float u, float v, float alpha) {
        vc.addVertex(m, x, y, 0.0f).setColor(1.0f, 1.0f, 1.0f, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(0.0f, 0.0f, -1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultistPortalLesser entity) {
        return portaltex;
    }
}
