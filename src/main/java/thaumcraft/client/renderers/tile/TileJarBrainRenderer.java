package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.devices.TileJarBrain;

/** The brain in a jar part of TileJarRenderer (renderBrain): ModelBrain bobbing in the brine of ModelJar. */
public class TileJarBrainRenderer implements BlockEntityRenderer<TileJarBrain> {
    private static final ResourceLocation TEX_BRAIN = Thaumcraft.id("textures/models/brain2.png");
    private static final ResourceLocation TEX_BRINE = Thaumcraft.id("textures/models/jarbrine.png");

    private final ModelPart brain;
    private final ModelPart brine;

    public TileJarBrainRenderer(BlockEntityRendererProvider.Context context) {
        // ModelBrain
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0f, 0.0f, 0.0f, 12, 10, 16),
                PartPose.offset(-6.0f, 8.0f, -8.0f));
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(0.0f, 0.0f, 0.0f, 8, 3, 7),
                PartPose.offset(-4.0f, 18.0f, 0.0f));
        root.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(0.0f, 0.0f, 0.0f, 2, 6, 2),
                PartPose.offsetAndRotation(-1.0f, 18.0f, -2.0f, 0.4089647f, 0.0f, 0.0f));
        brain = LayerDefinition.create(mesh, 128, 64).bakeRoot();
        // ModelJar.Brine
        MeshDefinition jar = new MeshDefinition();
        jar.getRoot().addOrReplaceChild("brine", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0f, -11.0f, -4.0f, 8, 10, 8),
                PartPose.ZERO);
        brine = LayerDefinition.create(jar, 64, 32).bakeRoot().getChild("brine");
    }

    @Override
    public void render(TileJarBrain te, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5f, 0.01f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        int ticks = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0;
        float bob = Mth.sin(ticks / 14.0f) * 0.03f + 0.03f;
        pose.pushPose();
        pose.translate(0.0f, -0.8f + bob, 0.0f);
        float f2;
        for (f2 = te.rota - te.rotb; f2 >= 3.141593f; f2 -= 6.283185f) {
        }
        while (f2 < -3.141593f) {
            f2 += 6.283185f;
        }
        float f3 = te.rotb + f2 * partialTick;
        pose.mulPose(Axis.YP.rotationDegrees(f3 * 180.0f / 3.141593f));
        pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
        pose.scale(0.4f, 0.4f, 0.4f);
        brain.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEX_BRAIN)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        brine.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEX_BRINE)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
