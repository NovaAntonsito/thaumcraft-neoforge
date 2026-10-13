package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.block.ModelCentrifuge;
import thaumcraft.common.tiles.essentia.TileCentrifuge;

/** Port of TileCentrifugeRenderer. */
public class TileCentrifugeRenderer implements BlockEntityRenderer<TileCentrifuge> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/models/centrifuge.png");
    private final ModelCentrifuge model = new ModelCentrifuge();

    public TileCentrifugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileCentrifuge cf, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        model.renderBoxes(pose, vc, light, overlay);
        pose.mulPose(Axis.YP.rotationDegrees(cf.rotation));
        model.renderSpinnyBit(pose, vc, light, overlay);
        pose.popPose();
    }
}
