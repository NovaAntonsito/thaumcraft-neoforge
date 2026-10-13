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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.devices.TileHungryChest;

/**
 * Port of TileHungryChestRenderer. Draws the 1.12 ModelChest (lid, knob and base) so the original texture, laid out
 * for that model, still fits.
 */
public class TileHungryChestRenderer implements BlockEntityRenderer<TileHungryChest> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/models/chesthungry.png");

    private final ModelPart lid;
    private final ModelPart knob;
    private final ModelPart base;

    public TileHungryChestRenderer(BlockEntityRendererProvider.Context context) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -5.0f, -14.0f, 14, 5, 14),
                PartPose.offset(1.0f, 7.0f, 15.0f));
        root.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -2.0f, -15.0f, 2, 4, 1),
                PartPose.offset(8.0f, 7.0f, 15.0f));
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 19).addBox(0.0f, 0.0f, 0.0f, 14, 10, 14),
                PartPose.offset(1.0f, 6.0f, 1.0f));
        ModelPart model = LayerDefinition.create(mesh, 64, 64).bakeRoot();
        lid = model.getChild("lid");
        knob = model.getChild("knob");
        base = model.getChild("base");
    }

    @Override
    public void render(TileHungryChest chest, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = chest.getBlockState();
        int rot = 0;
        if (state.hasProperty(ChestBlock.FACING)) {
            rot = switch (state.getValue(ChestBlock.FACING)) {
                case NORTH -> 180;
                case WEST -> 90;
                case EAST -> -90;
                default -> 0;
            };
        }
        pose.pushPose();
        pose.translate(0.0f, 1.0f, 1.0f);
        pose.scale(1.0f, -1.0f, -1.0f);
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(rot));
        pose.translate(-0.5f, -0.5f, -0.5f);
        float open = chest.getOpenNess(partialTick);
        open = 1.0f - open;
        open = 1.0f - open * open * open;
        lid.xRot = -(open * (float) Math.PI / 2.0f);
        knob.xRot = lid.xRot;
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        lid.render(pose, vc, light, overlay);
        knob.render(pose, vc, light, overlay);
        base.render(pose, vc, light, overlay);
        pose.popPose();
    }
}
