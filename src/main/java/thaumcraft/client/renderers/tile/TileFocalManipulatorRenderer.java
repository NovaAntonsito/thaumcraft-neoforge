package thaumcraft.client.renderers.tile;

import java.awt.Color;
import java.util.Random;

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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;

/**
 * Port of TileFocalManipulatorRenderer: the focus spinning over the table and, while it is being crafted, one
 * crystal per aspect circling above it with a glowing spark and two rays each.
 * 1.21: the floating items are drawn the way RenderEntityItem drew a fresh EntityItem (bob, lift and spin).
 */
public class TileFocalManipulatorRenderer implements BlockEntityRenderer<TileFocalManipulator> {
    private final ItemRenderer itemRenderer;

    public TileFocalManipulatorRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(TileFocalManipulator table, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (table.getLevel() == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float ticks = (mc.player != null ? mc.player.tickCount : 0) + partialTick;
        ItemStack focus = table.getItem(0);
        if (!focus.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5f, 0.8f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f));
            float hoverStart = Mth.sin(ticks / 14.0f) * 0.2f + 0.2f;
            renderFloatingItem(table, focus.copyWithCount(1), hoverStart, pose, buffers, light, overlay);
            pose.popPose();
        }
        Aspect[] aspects = table.crystalsSync.getAspects();
        if (aspects.length > 0) {
            int q = aspects.length;
            float ang = (float) (360 / q);
            for (int a = 0; a < q; ++a) {
                float angle = ticks % 720.0f / 2.0f + ang * a;
                float bob = Mth.sin((ticks + a * 10) / 12.0f) * 0.02f + 0.02f;
                Color c = new Color(aspects[a].getColor());
                float r = c.getRed() / 255.0f;
                float g = c.getGreen() / 255.0f;
                float b = c.getBlue() / 255.0f;
                pose.pushPose();
                pose.translate(0.5f, 1.3f, 0.5f);
                pose.mulPose(Axis.YP.rotationDegrees(angle));
                pose.translate(0.0f, bob, 0.4f);
                pose.mulPose(Axis.YP.rotationDegrees(-angle));
                renderSpark(pose, buffers, r, g, b, 320 + (mc.player != null ? mc.player.tickCount : 0) % 16);
                pose.popPose();
                pose.pushPose();
                pose.translate(0.5f, 1.05f, 0.5f);
                pose.mulPose(Axis.YP.rotationDegrees(angle));
                pose.translate(0.0f, bob, 0.4f);
                pose.scale(0.5f, 0.5f, 0.5f);
                renderRay(pose, buffers, angle, a, bob, r, g, b, ticks);
                renderRay(pose, buffers, angle, (a + 1) * 5, bob, r, g, b, ticks);
                ItemStack crystal = ConfigItems.crystalEssence.get().makeCrystal(aspects[a], 1);
                renderFloatingItem(table, crystal, 0.0f, pose, buffers, light, overlay);
                pose.popPose();
            }
        }
    }

    /** A fresh EntityItem: it bobs by its hover start, sits a quarter of its ground size up and turns by the same amount. */
    private void renderFloatingItem(TileFocalManipulator table, ItemStack stack, float hoverStart, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        float bob = Mth.sin(hoverStart) * 0.1f + 0.1f;
        pose.translate(0.0f, bob + 0.125f, 0.0f);
        pose.mulPose(Axis.YP.rotation(hoverStart));
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, pose, buffers, table.getLevel(), 0);
        pose.popPose();
    }

    /** renderBillboardQuad: one frame of the 64x64 particle sheet facing the camera, added on top. */
    private static void renderSpark(PoseStack pose, MultiBufferSource buffers, float r, float g, float b, int frame) {
        pose.pushPose();
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        float s = 0.175f;
        float u1 = (frame % 64) / 64.0f;
        float v1 = (frame / 64) / 64.0f;
        float u2 = u1 + 1.0f / 64.0f;
        float v2 = v1 + 1.0f / 64.0f;
        // Additive blend: the original alpha of 0.66 goes into the color.
        int cr = (int) (r * 0.66f * 255);
        int cg = (int) (g * 0.66f * 255);
        int cb = (int) (b * 0.66f * 255);
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(UtilsFX.PARTICLE_TEXTURE));
        PoseStack.Pose p = pose.last();
        vc.addVertex(p, -s, -s, 0).setColor(cr, cg, cb, 255).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(p, 0, 0, 1);
        vc.addVertex(p, s, -s, 0).setColor(cr, cg, cb, 255).setUv(u1, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(p, 0, 0, 1);
        vc.addVertex(p, s, s, 0).setColor(cr, cg, cb, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(p, 0, 0, 1);
        vc.addVertex(p, -s, s, 0).setColor(cr, cg, cb, 255).setUv(u2, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(p, 0, 0, 1);
        pose.popPose();
    }

    /** A cone of light from the crystal, swaying and opening with the ticks (a triangle fan in the original). */
    private static void renderRay(PoseStack pose, MultiBufferSource buffers, float angle, int num, float lift, float r, float g, float b, float ticks) {
        Random random = new Random(187L + num * num);
        pose.pushPose();
        float pan = Mth.sin((ticks + num * 10) / 15.0f) * 15.0f;
        float aparture = Mth.sin((ticks + num * 10) / 14.0f) * 2.0f;
        pose.translate(0.0f, 0.475f + lift, 0.0f);
        pose.mulPose(Axis.XN.rotationDegrees(90.0f));
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
        pose.mulPose(Axis.XP.rotationDegrees(pan));
        float fa = random.nextFloat() * 20.0f + 10.0f;
        float f4 = random.nextFloat() * 4.0f + 6.0f + aparture;
        fa /= 30.0f / (Math.min(ticks, 10.0f) / 10.0f);
        f4 /= 30.0f / (Math.min(ticks, 10.0f) / 10.0f);
        int cr = (int) (r * 255);
        int cg = (int) (g * 255);
        int cb = (int) (b * 255);
        int ca = (int) (0.66f * 255);
        float[][] rim = {{-0.8f * f4, fa, -0.5f * f4}, {0.8f * f4, fa, -0.5f * f4}, {0.0f, fa, f4}};
        VertexConsumer vc = buffers.getBuffer(RenderType.dragonRays());
        Matrix4f m = pose.last().pose();
        for (int t = 0; t < 3; ++t) {
            float[] p1 = rim[t];
            float[] p2 = rim[(t + 1) % 3];
            vc.addVertex(m, 0, 0, 0).setColor(cr, cg, cb, ca);
            vc.addVertex(m, p1[0], p1[1], p1[2]).setColor(cr, cg, cb, 0);
            vc.addVertex(m, p2[0], p2[1], p2[2]).setColor(cr, cg, cb, 0);
        }
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(TileFocalManipulator table) {
        return new AABB(table.getBlockPos()).expandTowards(0, 1, 0).inflate(0.5);
    }
}
