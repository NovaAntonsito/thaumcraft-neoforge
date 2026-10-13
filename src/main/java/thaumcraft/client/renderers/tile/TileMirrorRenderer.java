package thaumcraft.client.renderers.tile;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.common.blocks.devices.BlockMirror;
import thaumcraft.common.tiles.devices.TileMirror;

/**
 * Port of TileMirrorRenderer. The frame is the block model now; this draws the pane: plain glass on an unlinked
 * mirror, and on a linked one (within 32 blocks) a see-through pane over the void. The 16 texgen layers of the
 * original end portal effect are the vanilla end portal render type.
 */
public class TileMirrorRenderer implements BlockEntityRenderer<TileMirror> {
    private static final ResourceLocation MP = Thaumcraft.id("textures/blocks/mirrorpane.png");
    private static final ResourceLocation MPT = Thaumcraft.id("textures/blocks/mirrorpanetrans.png");

    public TileMirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileMirror te, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!te.getBlockState().hasProperty(BlockMirror.FACING)) {
            return;
        }
        Direction dir = te.getBlockState().getValue(BlockMirror.FACING);
        pose.pushPose();
        // Local +Y is the way the mirror looks; local y = 0 is the wall it hangs on.
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(dir.getRotation());
        pose.translate(-0.5, -0.5, -0.5);
        Minecraft mc = Minecraft.getInstance();
        boolean linked = te.linked;
        if (linked && mc.player != null && mc.player.distanceToSqr(te.getBlockPos().getCenter()) < 1024.0) {
            float p = 0.1875f;
            Matrix4f m = pose.last().pose();
            VertexConsumer vc = buffers.getBuffer(RenderType.endPortal());
            vc.addVertex(m, p, 0.01f, p);
            vc.addVertex(m, p, 0.01f, 1.0f - p);
            vc.addVertex(m, 1.0f - p, 0.01f, 1.0f - p);
            vc.addVertex(m, 1.0f - p, 0.01f, p);
            pane(pose, buffers, MPT, light);
        } else {
            pane(pose, buffers, MP, light);
        }
        pose.popPose();
    }

    /** The pane over the whole frame, facing out. */
    private static void pane(PoseStack pose, MultiBufferSource buffers, ResourceLocation tex, int light) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(tex));
        PoseStack.Pose p = pose.last();
        float y = 0.02f;
        vc.addVertex(p, 0, y, 0).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 1, 0);
        vc.addVertex(p, 0, y, 1).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 1, 0);
        vc.addVertex(p, 1, y, 1).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 1, 0);
        vc.addVertex(p, 1, y, 0).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 1, 0);
    }
}
