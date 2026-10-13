package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.block.ModelBellows;
import thaumcraft.client.renderers.models.block.ModelBoreBase;
import thaumcraft.common.blocks.devices.BlockBellows;
import thaumcraft.common.tiles.devices.TileBellows;
import thaumcraft.common.tiles.essentia.TileTubeBuffer;

/** Port of TileBellowsRenderer: the planks and bag pumping, and a brass nozzle into an essentia buffer. */
public class TileBellowsRenderer implements BlockEntityRenderer<TileBellows> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/blocks/bellows.png");
    private static final ResourceLocation TEX_BORE = Thaumcraft.id("textures/models/bore.png");
    private final ModelBellows model = new ModelBellows();
    private final ModelBoreBase model2 = new ModelBoreBase();

    public TileBellowsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileBellows bellows, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float scale = bellows.inflation;
        Direction dir = bellows.getBlockState().getValue(BlockBellows.FACING);
        boolean extension = bellows.getLevel() != null
                && bellows.getLevel().getBlockEntity(bellows.getBlockPos().relative(dir)) instanceof TileTubeBuffer;
        float tscale = 0.125f + scale * 0.875f;
        if (extension) {
            pose.pushPose();
            pose.translate(0.5f + dir.getStepX(), dir.getStepY(), 0.5f + dir.getStepZ());
            switch (dir.getOpposite()) {
                case DOWN -> {
                    pose.translate(-0.5f, 0.5f, 0.0f);
                    pose.mulPose(Axis.ZN.rotationDegrees(90.0f));
                }
                case UP -> {
                    pose.translate(0.5f, 0.5f, 0.0f);
                    pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
                }
                case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
                case WEST -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
                case EAST -> {
                }
            }
            model2.renderNozzle(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEX_BORE)), light, overlay);
            pose.popPose();
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
        pose.pushPose();
        translateFromOrientation(pose, dir);
        pose.translate(0.0f, 1.0f, 0.0f);
        pose.pushPose();
        pose.scale(0.5f, (scale + 0.1f) / 2.0f, 0.5f);
        model.bag.setPos(0.0f, 0.5f, 0.0f);
        model.bag.render(pose, vc, light, overlay);
        pose.popPose();
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.pushPose();
        pose.translate(0.0f, -tscale / 2.0f + 0.5f, 0.0f);
        model.topPlank.render(pose, vc, light, overlay);
        pose.popPose();
        pose.pushPose();
        pose.translate(0.0f, tscale / 2.0f - 0.5f, 0.0f);
        model.bottomPlank.render(pose, vc, light, overlay);
        pose.popPose();
        model.middlePlank.render(pose, vc, light, overlay);
        model.nozzle.render(pose, vc, light, overlay);
        pose.popPose();
    }

    private static void translateFromOrientation(PoseStack pose, Direction orientation) {
        pose.translate(0.5f, -0.5f, 0.5f);
        switch (orientation) {
            case DOWN -> {
                pose.translate(0.0f, 1.0f, -1.0f);
                pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
            case UP -> {
                pose.translate(0.0f, 1.0f, 1.0f);
                pose.mulPose(Axis.XP.rotationDegrees(270.0f));
            }
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            default -> {
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(TileBellows te) {
        return new AABB(te.getBlockPos()).inflate(1.0);
    }
}
