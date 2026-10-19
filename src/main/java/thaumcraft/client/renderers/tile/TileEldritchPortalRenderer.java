package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.eldritch.TileEldritchPortal;

/**
 * Port of TC4's TileEldritchPortalRenderer: the 16 frame portal texture on a quad facing the camera, two blocks
 * across, that grows over the portal's first 5 ticks (sideways) and 30 ticks (upwards).
 */
public class TileEldritchPortalRenderer implements BlockEntityRenderer<TileEldritchPortal> {
    public static final ResourceLocation portaltex = Thaumcraft.id("textures/misc/eldritch_portal.png");

    public TileEldritchPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEldritchPortal te, float f, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (te.getLevel() == null) {
            return;
        }
        long time = System.nanoTime() / 50000000L;
        float c = Math.min(30.0f, te.opencount + f);
        float e = Math.min(5.0f, te.opencount + f);
        float scale = e / 5.0f;
        float scaley = c / 30.0f;
        int frame = (int) (time % 16);
        float f2 = frame / 16.0f;
        float f3 = f2 + 0.0625f;
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        PoseStack.Pose p = pose.last();
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(portaltex));
        // brightness 220.
        int lt = LightTexture.pack(13, 13);
        vc.addVertex(p, -scale, -scaley, 0).setColor(0xFFFFFFFF).setUv(f2, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lt).setNormal(p, 0, 0, -1);
        vc.addVertex(p, -scale, scaley, 0).setColor(0xFFFFFFFF).setUv(f2, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lt).setNormal(p, 0, 0, -1);
        vc.addVertex(p, scale, scaley, 0).setColor(0xFFFFFFFF).setUv(f3, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lt).setNormal(p, 0, 0, -1);
        vc.addVertex(p, scale, -scaley, 0).setColor(0xFFFFFFFF).setUv(f3, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lt).setNormal(p, 0, 0, -1);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(TileEldritchPortal te) {
        return new AABB(te.getBlockPos()).inflate(1.0);
    }

    /** getMaxRenderDistanceSquared 9216. */
    @Override
    public int getViewDistance() {
        return 96;
    }
}
