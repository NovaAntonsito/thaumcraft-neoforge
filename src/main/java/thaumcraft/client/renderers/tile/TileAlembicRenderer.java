package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.UtilsFX;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.client.renderers.models.block.ModelBoreBase;
import thaumcraft.common.tiles.essentia.TileAlembic;

/**
 * Port of TileAlembicRenderer: the label on the labelled side, and a brass nozzle towards each side
 * that takes essentia out (a tube, for example).
 */
public class TileAlembicRenderer implements BlockEntityRenderer<TileAlembic> {
    private static final ResourceLocation TEX_LABEL = Thaumcraft.id("textures/models/label.png");
    private static final ResourceLocation TEX_BORE = Thaumcraft.id("textures/models/bore.png");
    private final ModelBoreBase modelBore = new ModelBoreBase();

    public TileAlembicRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileAlembic tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (tile.getLevel() != null) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (!tile.canOutputTo(dir)) {
                    continue;
                }
                BlockEntity te = ThaumcraftApiHelper.getConnectableTile(tile.getLevel(), tile.getBlockPos(), dir);
                if (te != null && ((IEssentiaTransport) te).canInputFrom(dir.getOpposite())) {
                    pose.pushPose();
                    pose.translate(0.5f, 0.0f, 0.5f);
                    // The nozzle points along +X (east).
                    switch (dir) {
                        case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                        case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
                        case WEST -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
                        default -> {
                        }
                    }
                    modelBore.renderNozzle(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEX_BORE)), light, overlay);
                    pose.popPose();
                }
            }
        }
        if (tile.aspectFilter == null) {
            return;
        }
        Direction side = Direction.from3DDataValue(tile.facing);
        if (side.getAxis() == Direction.Axis.Y) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - side.toYRot()));
        pose.translate(0.0f, 0.0f, -0.376f);
        UtilsFX.renderQuadCentered(pose, buffers, TEX_LABEL, 0.44f, 1.0f, 1.0f, 1.0f, 1.0f, light);
        pose.translate(0.0f, 0.0f, -0.001f);
        UtilsFX.drawTag(pose, buffers, tile.aspectFilter, 0.32f, light);
        pose.popPose();
    }
}
