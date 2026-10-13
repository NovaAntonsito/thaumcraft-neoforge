package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.client.renderers.models.block.ModelTubeValve;
import thaumcraft.common.tiles.essentia.TileTubeBuffer;

/** Port of TileTubeBufferRenderer: a blue (suction 1) or red (suction 0) band on each choked side. */
public class TileTubeBufferRenderer implements BlockEntityRenderer<TileTubeBuffer> {
    private static final int RED = 0xFFFF4C4C;
    private static final int BLUE = 0xFF4C4CFF;
    private final ModelTubeValve model = new ModelTubeValve();

    public TileTubeBufferRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileTubeBuffer buffer, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (buffer.getLevel() == null) {
            return;
        }
        for (Direction dir : Direction.values()) {
            int i = dir.get3DDataValue();
            if (buffer.chokedSides[i] == 0 || !buffer.openSides[i]
                    || ThaumcraftApiHelper.getConnectableTile(buffer.getLevel(), buffer.getBlockPos(), dir) == null) {
                continue;
            }
            pose.pushPose();
            pose.translate(0.5, 0.5, 0.5);
            pose.mulPose(dir.getOpposite().getRotation());
            pose.scale(2.0f, 1.0f, 2.0f);
            pose.translate(0.0, -0.5, 0.0);
            model.renderRod(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TileTubeValveRenderer.TEX_VALVE)), light, overlay,
                    buffer.chokedSides[i] == 2 ? RED : BLUE);
            pose.popPose();
        }
    }
}
