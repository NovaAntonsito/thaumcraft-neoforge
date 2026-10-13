package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.client.renderers.models.block.ModelTubeValve;
import thaumcraft.common.tiles.essentia.TileTubeOneway;

/** Port of TileTubeOnewayRenderer: a blue band on the side suction is accepted from. */
public class TileTubeOnewayRenderer implements BlockEntityRenderer<TileTubeOneway> {
    private static final int BLUE = 0xFF000000 | (int) (0.45f * 255) << 16 | (int) (0.5f * 255) << 8 | 255;
    private final ModelTubeValve model = new ModelTubeValve();

    public TileTubeOnewayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileTubeOneway valve, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (valve.getLevel() != null && ThaumcraftApiHelper.getConnectableTile(valve.getLevel(), valve.getBlockPos(), valve.facing.getOpposite()) == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(valve.facing.getRotation());
        pose.scale(2.0f, 2.0f, 2.0f);
        pose.translate(0.0, -0.32f, 0.0);
        model.renderRod(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TileTubeValveRenderer.TEX_VALVE)), light, overlay, BLUE);
        pose.popPose();
    }
}
