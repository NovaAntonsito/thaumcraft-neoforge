package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumcraft.common.tiles.misc.TileJarNode;

/** The node part of the Thaumcraft 4 TileJarRenderer: the node inside the jar, always shown, at seven tenths of its size. */
public class TileJarNodeRenderer implements BlockEntityRenderer<TileJarNode> {
    public TileJarNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileJarNode tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (Minecraft.getInstance().cameraEntity == null) return;
        pose.pushPose();
        pose.translate(0.0, -0.1, 0.0);
        TileAuraNodeRenderer.renderNode(pose, buffers, Minecraft.getInstance().cameraEntity, 64.0, true, false, 0.7f,
                tile.getBlockPos().getX(), tile.getBlockPos().getY(), tile.getBlockPos().getZ(), tile.getAspects(), tile.getNodeType(),
                tile.getNodeModifier());
        pose.popPose();
    }
}
