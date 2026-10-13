package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import thaumcraft.Thaumcraft;
import thaumcraft.common.blocks.crafting.BlockResearchTable;
import thaumcraft.common.items.consumables.ItemResearchNote;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/**
 * Reborn's TileResearchTableRenderer with TC6's ModelResearchTable: the inkwell and quill when scribing tools are
 * on the table, the scroll (its ribbon in the note's color) when a note is.
 */
public class TileResearchTableRenderer implements BlockEntityRenderer<TileResearchTable> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/blocks/research_table_model.png");
    private static final ResourceLocation QUILL = Thaumcraft.id("textures/research/quill.png");

    private final ModelPart inkwell;
    private final ModelPart scrollTube;
    private final ModelPart scrollRibbon;

    public TileResearchTableRenderer(BlockEntityRendererProvider.Context context) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("inkwell", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(0.0f, 0.0f, 0.0f, 3, 2, 3),
                PartPose.offset(-6.0f, -2.0f, 3.0f));
        root.addOrReplaceChild("scroll_tube", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0f, -0.5f, 0.0f, 8, 2, 2),
                PartPose.offsetAndRotation(-2.0f, -2.0f, 2.0f, 0.0f, 10.0f, 0.0f));
        root.addOrReplaceChild("scroll_ribbon", CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-4.25f, -0.275f, 0.0f, 1, 2, 2),
                PartPose.offsetAndRotation(-2.0f, -2.0f, 2.0f, 0.0f, 10.0f, 0.0f));
        ModelPart model = LayerDefinition.create(mesh, 64, 32).bakeRoot();
        inkwell = model.getChild("inkwell");
        scrollTube = model.getChild("scroll_tube");
        scrollRibbon = model.getChild("scroll_ribbon");
    }

    @Override
    public void render(TileResearchTable table, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5f, 1.0f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        Direction facing = table.getBlockState().hasProperty(BlockResearchTable.FACING)
                ? table.getBlockState().getValue(BlockResearchTable.FACING) : Direction.NORTH;
        switch (facing) {
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            default -> {
            }
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
        if (table.hasResearchNote()) {
            scrollTube.render(pose, vc, light, OverlayTexture.NO_OVERLAY);
            pose.pushPose();
            pose.scale(1.2f, 1.2f, 1.2f);
            int color = FastColor.ARGB32.opaque(ItemResearchNote.getColor(table.getItem(1)));
            scrollRibbon.render(pose, vc, light, OverlayTexture.NO_OVERLAY, color);
            pose.popPose();
        }
        if (table.hasScribingTools()) {
            inkwell.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEX)), light, OverlayTexture.NO_OVERLAY);
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            pose.translate(-0.5, 0.1, 0.125);
            pose.mulPose(Axis.YP.rotationDegrees(60.0f));
            pose.scale(0.5f, 0.5f, 0.5f);
            renderQuill(pose, buffers, light);
            pose.popPose();
        }
        pose.popPose();
    }

    /** renderItemIn2D of the quill sprite: a flat one-block card, mirrored as the original's renderTextureIn3D. */
    private static void renderQuill(PoseStack pose, MultiBufferSource buffers, int light) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(QUILL));
        PoseStack.Pose p = pose.last();
        vertex(vc, p, 0, 0, 1, 1, light);
        vertex(vc, p, 1, 0, 0, 1, light);
        vertex(vc, p, 1, 1, 0, 0, light);
        vertex(vc, p, 0, 1, 1, 0, light);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose p, float x, float y, float u, float v, int light) {
        vc.addVertex(p, x, y, 0).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(p, 0, 0, 1);
    }
}
