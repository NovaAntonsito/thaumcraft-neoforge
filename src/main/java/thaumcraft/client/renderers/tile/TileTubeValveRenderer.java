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
import thaumcraft.client.renderers.models.block.ModelTubeValve;
import thaumcraft.common.tiles.essentia.TileTubeValve;

/** Port of TileTubeValveRenderer: the handle on the valve's facing side, screwed in while closed. */
public class TileTubeValveRenderer implements BlockEntityRenderer<TileTubeValve> {
    static final ResourceLocation TEX_VALVE = Thaumcraft.id("textures/models/valve.png");
    private final ModelTubeValve model = new ModelTubeValve();

    public TileTubeValveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileTubeValve valve, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX_VALVE));
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        // The 1.12 rotations turn the model's +Y towards the facing.
        pose.mulPose(valve.facing.getRotation());
        pose.mulPose(Axis.YP.rotationDegrees(-valve.rotation * 1.5f));
        pose.translate(0.0, -0.03f - valve.rotation / 360.0f * 0.09f, 0.0);
        model.renderRing(pose, vc, light, overlay);
        pose.scale(0.75f, 1.0f, 0.75f);
        model.renderRod(pose, vc, light, overlay, -1);
        pose.popPose();
    }
}
