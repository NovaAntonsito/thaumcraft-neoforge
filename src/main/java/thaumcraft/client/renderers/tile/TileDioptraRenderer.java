package thaumcraft.client.renderers.tile;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.common.blocks.devices.BlockDioptra;
import thaumcraft.common.tiles.devices.TileDioptra;

/**
 * Port of TileDioptraRenderer: the 12x12 aura grid floating on top of the dioptra and the four glowing side
 * panels above it. The original blended with (SRC_ALPHA, ONE); RenderType.eyes blends (ONE, ONE), so colors are
 * pre-multiplied by alpha. Quads are emitted with both windings because eyes culls back faces.
 */
public class TileDioptraRenderer implements BlockEntityRenderer<TileDioptra> {
    private static final ResourceLocation GRID = Thaumcraft.id("textures/misc/gridblock.png");
    private static final ResourceLocation SIDE = Thaumcraft.id("textures/models/dioptra_side.png");
    private static final float A = 0.9f;

    public TileDioptraRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileDioptra tco, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var player = Minecraft.getInstance().player;
        float t = player != null ? player.tickCount + partialTick : 0.0f;
        float rc = Mth.sin(t / 12.0f) * 0.05f + 0.85f;
        float gc = tco.getBlockState().getValue(BlockDioptra.ENABLED)
                ? Mth.sin(t / 11.0f) * 0.05f + 0.9f
                : Mth.sin(t / 11.0f) * 0.05f + 0.45f;
        float bc = Mth.sin(t / 10.0f) * 0.05f + 0.95f;
        // calcColorMap: every corner gets (r * 0.8, g, b).
        float gr = rc * 0.8f;
        float gg = Mth.clamp(gc, 0.0f, 1.0f);
        byte[] g = tco.grid_amt;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);

        pose.pushPose();
        pose.translate(-0.495, 0.501, -0.495);
        pose.scale(0.99f, 1.0f, 0.99f);
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(GRID));
        Matrix4f m = pose.last().pose();
        float[] top = {A, A, A, A};
        for (int a = 0; a < 12; ++a) {
            for (int b = 0; b < 12; ++b) {
                float x0 = a / 12.0f, x1 = (a + 1) / 12.0f, z0 = b / 12.0f, z1 = (b + 1) / 12.0f;
                quad(vc, m, gr, gg, bc,
                        new float[][] {{x0, g[a + b * 13] / 96.0f, z0}, {x1, g[a + 1 + b * 13] / 96.0f, z0},
                                {x1, g[a + 1 + (b + 1) * 13] / 96.0f, z1}, {x0, g[a + (b + 1) * 13] / 96.0f, z1}},
                        new float[][] {{0, 1}, {1, 1}, {1, 0}, {0, 0}}, top);
                if (a == 0) {
                    quad(vc, m, gr, gg, bc,
                            new float[][] {{0, 0, z0}, {0, g[b * 13] / 96.0f, z0}, {0, g[(b + 1) * 13] / 96.0f, z1}, {0, 0, z1}},
                            new float[][] {{0, 1}, {1, 1}, {1, 0}, {0, 0}}, new float[] {0, A, A, 0});
                }
                if (a == 11) {
                    quad(vc, m, gr, gg, bc,
                            new float[][] {{1, 0, z0}, {1, g[a + 1 + b * 13] / 96.0f, z0},
                                    {1, g[a + 1 + (b + 1) * 13] / 96.0f, z1}, {1, 0, z1}},
                            new float[][] {{0, 1}, {1, 1}, {1, 0}, {0, 0}}, new float[] {0, A, A, 0});
                }
                if (b == 0) {
                    quad(vc, m, gr, gg, bc,
                            new float[][] {{x0, 0, 0}, {x1, 0, 0}, {x1, g[a + 1] / 96.0f, 0}, {x0, g[a] / 96.0f, 0}},
                            new float[][] {{0, 1}, {1, 1}, {1, 0}, {0, 0}}, new float[] {0, 0, A, A});
                }
                if (b == 11) {
                    quad(vc, m, gr, gg, bc,
                            new float[][] {{x0, 0, 1}, {x1, 0, 1}, {x1, g[a + 1 + (b + 1) * 13] / 96.0f, 1},
                                    {x0, g[a + (b + 1) * 13] / 96.0f, 1}},
                            new float[][] {{0, 1}, {1, 1}, {1, 0}, {0, 0}}, new float[] {0, 0, A, A});
                }
            }
        }
        pose.popPose();

        // The four side panels: UtilsFX.renderQuadCentered(sideTexture, 1, rc, gc, bc, 220, ONE, 0.8).
        VertexConsumer side = buffers.getBuffer(RenderType.eyes(SIDE));
        pose.pushPose();
        pose.translate(0.0, 1.0, 0.0);
        pose.mulPose(Axis.ZP.rotationDegrees(270.0f));
        float[] op = {0.8f, 0.8f, 0.8f, 0.8f};
        for (int q = 0; q < 4; ++q) {
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(90.0f * q));
            pose.translate(0.0, 0.0, -0.5);
            quad(side, pose.last().pose(), rc, gc, bc,
                    new float[][] {{-0.5f, 0.5f, 0}, {0.5f, 0.5f, 0}, {0.5f, -0.5f, 0}, {-0.5f, -0.5f, 0}},
                    new float[][] {{1, 1}, {1, 0}, {0, 0}, {0, 1}}, op);
            pose.popPose();
        }
        pose.popPose();
        pose.popPose();
    }

    private static void quad(VertexConsumer vc, Matrix4f m, float r, float g, float b, float[][] p, float[][] uv, float[] alpha) {
        for (int i = 0; i < 4; ++i) {
            vertex(vc, m, r, g, b, p[i], uv[i], alpha[i]);
        }
        for (int i = 3; i >= 0; --i) {
            vertex(vc, m, r, g, b, p[i], uv[i], alpha[i]);
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float r, float g, float b, float[] p, float[] uv, float a) {
        vc.addVertex(m, p[0], p[1], p[2]).setColor(r * a, g * a, b * a, 1.0f).setUv(uv[0], uv[1])
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0.0f, 1.0f, 0.0f);
    }

    @Override
    public AABB getRenderBoundingBox(TileDioptra te) {
        return new AABB(te.getBlockPos()).expandTowards(0, 2, 0);
    }
}
