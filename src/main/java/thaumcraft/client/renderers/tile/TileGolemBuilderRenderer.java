package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.obj.ObjModel;
import thaumcraft.common.blocks.crafting.BlockGolemBuilder;
import thaumcraft.common.tiles.crafting.TileGolemBuilder;

/**
 * Port of TileGolemBuilderRenderer: the whole press (table, anvil, cauldron, bars) from golembuilder.obj, with the
 * press part moving down while a golem is made, and the lava in the cauldron.
 */
public class TileGolemBuilderRenderer implements BlockEntityRenderer<TileGolemBuilder> {
    private static final ResourceLocation TM = Thaumcraft.id("models/block/golembuilder.obj");
    private static final ResourceLocation TEX = Thaumcraft.id("textures/blocks/golembuilder.png");
    private ObjModel model;

    public TileGolemBuilderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileGolemBuilder tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (model == null) {
            // The original flipped V back with its texture matrix.
            model = ObjModel.load(TM, false);
        }
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        Direction facing = tile.getBlockState().getValue(BlockGolemBuilder.FACING);
        switch (facing) {
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            default -> {
            }
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
        model.renderAllExcept("press", pose, vc, light, overlay, 0xFFFFFFFF);
        pose.pushPose();
        double s = Math.sin(Math.toRadians(tile.press)) * 0.625;
        pose.translate(0.0, -s, 0.0);
        model.renderPart("press", pose, vc, light, overlay, 0xFFFFFFFF);
        pose.popPose();
        // The lava in the cauldron (renderQuadFromIcon with brightness 200).
        pose.translate(-0.3125f, 0.625f, 1.3125f);
        pose.mulPose(Axis.XN.rotationDegrees(90.0f));
        pose.scale(0.625f, 0.625f, 0.625f);
        TextureAtlasSprite icon = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.LAVA.defaultBlockState());
        VertexConsumer lava = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose p = pose.last();
        lava.addVertex(p, 0, 0, 0).setColor(-1).setUv(icon.getU1(), icon.getV1()).setOverlay(overlay).setLight(200).setNormal(p, 0, 0, 1);
        lava.addVertex(p, 1, 0, 0).setColor(-1).setUv(icon.getU0(), icon.getV1()).setOverlay(overlay).setLight(200).setNormal(p, 0, 0, 1);
        lava.addVertex(p, 1, 1, 0).setColor(-1).setUv(icon.getU0(), icon.getV0()).setOverlay(overlay).setLight(200).setNormal(p, 0, 0, 1);
        lava.addVertex(p, 0, 1, 0).setColor(-1).setUv(icon.getU1(), icon.getV0()).setOverlay(overlay).setLight(200).setNormal(p, 0, 0, 1);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(TileGolemBuilder tile) {
        return new AABB(tile.getBlockPos()).inflate(2.0);
    }
}
