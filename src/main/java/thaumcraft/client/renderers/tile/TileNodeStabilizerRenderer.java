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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.obj.ObjModel;
import thaumcraft.client.lib.render.TCRenderTypes;
import thaumcraft.common.tiles.misc.TileNodeConverter;
import thaumcraft.common.tiles.misc.TileNodeStabilizer;

/**
 * Port of the Thaumcraft 4 TileNodeStabilizerRenderer and TileNodeConverterRenderer. The base ("lock") is the block
 * model; this draws the four pistons, which slide out and glow as the device works, and above a stabilizer the
 * bubble around its node. The transducer's base also glows: green idle, orange converting, red energized.
 */
public class TileNodeStabilizerRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public static final ResourceLocation MODEL = Thaumcraft.id("models/obj/node_stabilizer.obj");
    private static final ResourceLocation STAB = Thaumcraft.id("textures/models/node_stabilizer.png");
    private static final ResourceLocation STAB_OVER = Thaumcraft.id("textures/models/node_stabilizer_over.png");
    private static final ResourceLocation CONV = Thaumcraft.id("textures/models/node_converter.png");
    private static final ResourceLocation CONV_OVER = Thaumcraft.id("textures/models/node_converter_over.png");
    private static final ResourceLocation BUBBLE = Thaumcraft.id("textures/misc/node_bubble.png");

    public TileNodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ObjModel model = ObjModel.load(MODEL, true);
        int ticks = Minecraft.getInstance().cameraEntity != null ? Minecraft.getInstance().cameraEntity.tickCount : 0;
        if (tile instanceof TileNodeStabilizer st) {
            int lock = st.getLock();
            pose.pushPose();
            pose.translate(0.5, 0.0, 0.5);
            pose.mulPose(Axis.XN.rotationDegrees(90.0f));
            for (int a = 0; a < 4; ++a) {
                pose.pushPose();
                pose.mulPose(Axis.ZP.rotationDegrees(90.0f * a));
                pose.mulPose(Axis.YP.rotationDegrees(45.0f));
                pose.translate(0.0, 0.0, st.count / 100.0f);
                model.renderPart("piston", pose, buffers.getBuffer(RenderType.entityCutout(STAB)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                float scale = Mth.sin((ticks + a * 5) / 3.0f) * 0.1f + 0.9f;
                int j = 50 + (int) (170.0f * st.count / 37.0f * scale);
                int color = lock == 2 ? 0xFFFF3333 : 0xFFFFFFFF;
                model.renderPart("piston", pose, buffers.getBuffer(RenderType.entityTranslucent(STAB_OVER)), glow(light, j), OverlayTexture.NO_OVERLAY, color);
                pose.popPose();
            }
            pose.popPose();
            if (st.count > 0) {
                float alpha = Mth.sin(ticks / 8.0f) * 0.1f + 0.5f;
                bubble(pose, buffers.getBuffer(TCRenderTypes.node(BUBBLE, true, false)), st.count / 37.0f * alpha, lock == 1 ? 0xFFFFFF : 0xFF4444);
            }
        } else if (tile instanceof TileNodeConverter tc) {
            pose.pushPose();
            pose.translate(0.5, 1.0, 0.5);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            float v = Math.min(50, tc.count) / 137.0f;
            int color = tc.status == 2 ? 0xFFFF004C : tc.status == 1 ? 0xFFFF9919 : 0xFF7FFF7F;
            float scale0 = Mth.sin(ticks / 3.0f) * 0.1f + 0.9f;
            model.renderPart("lock", pose, buffers.getBuffer(RenderType.entityTranslucent(CONV_OVER)),
                    glow(light, 50 + (int) (170.0f * v * 2.5f * scale0)), OverlayTexture.NO_OVERLAY, color);
            for (int a = 0; a < 4; ++a) {
                pose.pushPose();
                pose.mulPose(Axis.ZP.rotationDegrees(90.0f * a));
                pose.mulPose(Axis.YP.rotationDegrees(45.0f));
                pose.translate(0.0, 0.0, v);
                model.renderPart("piston", pose, buffers.getBuffer(RenderType.entityCutout(CONV)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
                float scale = Mth.sin((ticks + a * 5) / 3.0f) * 0.1f + 0.9f;
                model.renderPart("piston", pose, buffers.getBuffer(RenderType.entityTranslucent(CONV_OVER)),
                        glow(light, 50 + (int) (170.0f * v * 2.5f * scale)), OverlayTexture.NO_OVERLAY, color);
                pose.popPose();
            }
            pose.popPose();
        }
    }

    /** OpenGlHelper.setLightmapTextureCoords(j % 65536, j / 65536): j is the block light coordinate, 0 to 240. */
    private static int glow(int light, int j) {
        int block = Mth.clamp(j / 16, 0, 15);
        return LightTexture.pack(Math.max(block, LightTexture.block(light)), LightTexture.sky(light));
    }

    /** The node_bubble.png quad around the node above, facing the camera, 1.8 blocks wide. */
    private static void bubble(PoseStack pose, VertexConsumer vc, float alpha, int color) {
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        Matrix4f m = pose.last().pose();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = Mth.clamp(alpha, 0.0f, 1.0f);
        float s = 0.9f;
        vc.addVertex(m, -s, -s, 0).setUv(1, 1).setColor(r, g, b, a);
        vc.addVertex(m, -s, s, 0).setUv(1, 0).setColor(r, g, b, a);
        vc.addVertex(m, s, s, 0).setUv(0, 0).setColor(r, g, b, a);
        vc.addVertex(m, s, -s, 0).setUv(0, 1).setColor(r, g, b, a);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(T tile) {
        return new AABB(tile.getBlockPos()).expandTowards(0, 1, 0);
    }
}
