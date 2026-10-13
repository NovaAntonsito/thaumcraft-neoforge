package thaumcraft.client.renderers.tile;

import java.util.Random;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.crafting.TileInfusionMatrix;

/**
 * Port of TileInfusionMatrixRenderer: eight stone cubes that start spinning once the matrix is active, a purple
 * glow over them, and a halo of rays while crafting.
 * TODO: the ancient and eldritch textures (infuser_ancient, infuser_eldritch) when those pillars are ported.
 */
public class TileInfusionMatrixRenderer implements BlockEntityRenderer<TileInfusionMatrix> {
    private static final ResourceLocation TEX1 = Thaumcraft.id("textures/blocks/infuser_normal.png");
    private final ModelPart model;
    private final ModelPart modelOver;

    public TileInfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
        model = cube(0);
        modelOver = cube(32);
    }

    /** ModelCube(shift): a 16x16x16 box on a 64x64 texture. */
    private static ModelPart cube(int shift) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, shift).mirror()
                .addBox(-8.0f, -8.0f, -8.0f, 16, 16, 16), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(TileInfusionMatrix is, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float ticks = (Minecraft.getInstance().getCameraEntity() != null ? Minecraft.getInstance().getCameraEntity().tickCount : 0) + partialTick;
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f * is.startUp));
        pose.mulPose(Axis.XP.rotationDegrees(35.0f * is.startUp));
        pose.mulPose(Axis.ZP.rotationDegrees(45.0f * is.startUp));
        float inst = is.stability;
        int craftcount = is.craftCount;
        float startup = is.startUp;
        float instability = Math.min(6.0f, 1.0f + (inst < 0.0f ? -inst * 0.66f : 1.0f) * (Math.min(craftcount, 50) / 50.0f));
        VertexConsumer solid = buffers.getBuffer(RenderType.entitySolid(TEX1));
        drawCubes(pose, solid, model, ticks, startup, instability, is.active, light, overlay, false);
        if (is.active) {
            drawCubes(pose, buffers.getBuffer(RenderType.eyes(TEX1)), modelOver, ticks, startup, instability, true,
                    LightTexture.FULL_BRIGHT, overlay, true);
        }
        pose.popPose();
        if (is.crafting) {
            drawHalo(pose, buffers, craftcount);
        }
    }

    private static void drawCubes(PoseStack pose, VertexConsumer vc, ModelPart part, float ticks, float startup, float instability,
                                  boolean active, int light, int overlay, boolean glow) {
        for (int a = 0; a < 2; ++a) {
            for (int b = 0; b < 2; ++b) {
                for (int c = 0; c < 2; ++c) {
                    float b1 = 0, b2 = 0, b3 = 0;
                    if (active) {
                        b1 = Mth.sin((ticks + a * 10) / 15.0f) * 0.01f * startup * instability;
                        b2 = Mth.sin((ticks + b * 10) / 14.0f) * 0.01f * startup * instability;
                        b3 = Mth.sin((ticks + c * 10) / 13.0f) * 0.01f * startup * instability;
                    }
                    pose.pushPose();
                    pose.translate(b1 + (a == 0 ? -1 : 1) * 0.25f, b2 + (b == 0 ? -1 : 1) * 0.25f, b3 + (c == 0 ? -1 : 1) * 0.25f);
                    if (a > 0) pose.mulPose(Axis.XP.rotationDegrees(90.0f));
                    if (b > 0) pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                    if (c > 0) pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
                    pose.scale(0.45f, 0.45f, 0.45f);
                    int color = -1;
                    if (glow) {
                        // Additive blending: the original alpha is folded into the color.
                        float alpha = (Mth.sin((ticks + a * 2 + b * 3 + c * 4) / 4.0f) * 0.1f + 0.2f) * startup;
                        color = FastColor.ARGB32.colorFromFloat(1.0f, 0.8f * alpha, 0.1f * alpha, 1.0f * alpha);
                    }
                    part.render(pose, vc, light, overlay, color);
                    pose.popPose();
                }
            }
        }
    }

    /** drawHalo: rays fading from white to magenta, growing with the craft count. */
    private static void drawHalo(PoseStack pose, MultiBufferSource buffers, int count) {
        int q = Minecraft.getInstance().options.graphicsMode().get().getId() > 0 ? 20 : 10;
        float f1 = count / 500.0f;
        Random random = new Random(245L);
        VertexConsumer vc = buffers.getBuffer(RenderType.dragonRays());
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        float grow = Math.min(count, 50) / 50.0f;
        int alpha = (int) (255.0f * (1.0f - f1));
        for (int i = 0; i < q; ++i) {
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + f1 * 360.0f));
            float fa = (random.nextFloat() * 20.0f + 5.0f) / (20.0f / grow);
            float f4 = (random.nextFloat() * 2.0f + 1.0f) / (20.0f / grow);
            Matrix4f m = pose.last().pose();
            float[][] rim = {{-0.866f * f4, fa, -0.5f * f4}, {0.866f * f4, fa, -0.5f * f4}, {0.0f, fa, f4}};
            for (int t = 0; t < 3; ++t) {
                float[] p1 = rim[t];
                float[] p2 = rim[(t + 1) % 3];
                vc.addVertex(m, 0, 0, 0).setColor(255, 255, 255, alpha);
                vc.addVertex(m, p1[0], p1[1], p1[2]).setColor(255, 0, 255, 0);
                vc.addVertex(m, p2[0], p2[1], p2[2]).setColor(255, 0, 255, 0);
            }
        }
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(TileInfusionMatrix is) {
        return new AABB(is.getBlockPos()).inflate(2.0);
    }
}
