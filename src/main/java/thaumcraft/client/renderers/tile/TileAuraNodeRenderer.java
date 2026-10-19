package thaumcraft.client.renderers.tile;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.client.lib.render.TCRenderTypes;
import thaumcraft.common.items.tools.ItemThaumometer;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.tiles.misc.TileAuraNode;

/**
 * Port of the Thaumcraft 4 TileNodeRenderer. Every aspect of the node is a glowing ring of its color, pulsing with its
 * amount, around a core whose look depends on the node type. The whole node shows through walls to a player wearing
 * goggles (or another revealer), and within 48 blocks to one pointing a thaumometer at it; otherwise it is a faint blob.
 */
public class TileAuraNodeRenderer implements BlockEntityRenderer<TileAuraNode> {
    public static final ResourceLocation NODE_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/nodes.png");
    private static final int FRAMES = 32;

    public TileAuraNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileAuraNode tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Entity viewer = Minecraft.getInstance().cameraEntity;
        if (viewer == null) return;
        double viewDistance = 64.0;
        boolean condition = false;
        boolean depthIgnore = false;
        if (viewer instanceof Player player) {
            if (EntityUtils.hasRevealer(player)) {
                condition = true;
                depthIgnore = true;
            } else if (player.getMainHandItem().getItem() instanceof ItemThaumometer || player.getOffhandItem().getItem() instanceof ItemThaumometer) {
                // The original also asked UtilsFX.isVisibleTo, which compared radians with degrees and so always passed.
                condition = true;
                depthIgnore = true;
                viewDistance = 48.0;
            }
        }
        renderNode(pose, buffers, viewer, viewDistance, condition, depthIgnore, 1.0f, tile.getBlockPos().getX(), tile.getBlockPos().getY(),
                tile.getBlockPos().getZ(), tile.getAspects(), tile.getNodeType(), tile.getNodeModifier());
    }

    /** renderNode, drawn around the middle of the block the pose stands at. */
    public static void renderNode(PoseStack pose, MultiBufferSource buffers, Entity viewer, double viewDistance, boolean visible,
                                  boolean depthIgnore, float size, int x, int y, int z, AspectList aspects, NodeType type, NodeModifier mod) {
        long nt = System.nanoTime();
        int i = (int) ((nt / 40000000L + x) % FRAMES);
        if (aspects.size() > 0 && visible) {
            double distance = viewer.position().distanceTo(new Vec3(x + 0.5, y + 0.5, z + 0.5));
            if (distance > viewDistance) return;
            float alpha = (float) ((viewDistance - distance) / viewDistance);
            if (mod != null) {
                switch (mod) {
                    case BRIGHT -> alpha *= 1.5f;
                    case PALE -> alpha *= 0.66f;
                    case FADING -> alpha *= Mth.sin(viewer.tickCount / 3.0f) * 0.25f + 0.33f;
                }
            }
            long time = nt / 5000000L;
            float bscale = 0.25f;
            float rad = (float) Math.PI * 2.0f;
            int count = 0;
            float scale;
            float angle = 0.0f;
            float average = 0.0f;
            for (Aspect aspect : aspects.getAspects()) {
                boolean normalBlend = aspect.getBlend() == 771;
                float a = normalBlend ? alpha * 1.5f : alpha;
                average += aspects.getAmount(aspect);
                scale = Mth.sin(viewer.tickCount / (14.0f - count)) * bscale + bscale * 2.0f;
                scale = 0.2f + scale * (aspects.getAmount(aspect) / 50.0f);
                scale *= size;
                angle = (float) (time % (5000 + 500L * count)) / (5000.0f + 500 * count) * rad;
                renderFacingStrip(pose, buffers.getBuffer(TCRenderTypes.node(NODE_TEXTURE, !normalBlend, depthIgnore)), angle, scale,
                        a / Math.max(1.0f, aspects.size() / 2.0f), 0, i, aspect.getColor());
                ++count;
            }
            average /= aspects.size();
            scale = 0.1f + average / 150.0f;
            scale *= size;
            int strip = 1;
            boolean additive = true;
            switch (type) {
                case UNSTABLE -> {
                    strip = 6;
                    angle = 0.0f;
                }
                case DARK -> {
                    additive = false;
                    strip = 2;
                }
                case TAINTED -> {
                    additive = false;
                    strip = 5;
                }
                case PURE -> strip = 4;
                case HUNGRY -> {
                    scale *= 0.75f;
                    strip = 3;
                }
                default -> {
                }
            }
            renderFacingStrip(pose, buffers.getBuffer(TCRenderTypes.node(NODE_TEXTURE, additive, depthIgnore)), angle, scale, alpha, strip, i, 0xFFFFFF);
        } else {
            renderFacingStrip(pose, buffers.getBuffer(TCRenderTypes.node(NODE_TEXTURE, true, false)), 0.0f, 0.5f, 0.1f, 1, i, 0xFFFFFF);
        }
    }

    /**
     * UtilsFX.renderFacingStrip: one frame of a strip of nodes.png, a square 2 * scale wide facing the camera and turned
     * by angle around the line of sight.
     */
    private static void renderFacingStrip(PoseStack pose, VertexConsumer vc, float angle, float scale, float alpha, int strip, int frame, int color) {
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        if (angle != 0.0f) pose.mulPose(Axis.ZP.rotation(angle));
        Matrix4f m = pose.last().pose();
        float f2 = frame / (float) FRAMES;
        float f3 = (frame + 1) / (float) FRAMES;
        float f4 = strip / (float) FRAMES;
        float f5 = (strip + 1) / (float) FRAMES;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = Mth.clamp(alpha, 0.0f, 1.0f);
        vc.addVertex(m, -scale, -scale, 0).setUv(f3, f5).setColor(r, g, b, a);
        vc.addVertex(m, -scale, scale, 0).setUv(f3, f4).setColor(r, g, b, a);
        vc.addVertex(m, scale, scale, 0).setUv(f2, f4).setColor(r, g, b, a);
        vc.addVertex(m, scale, -scale, 0).setUv(f2, f5).setColor(r, g, b, a);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TileAuraNode tile) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(TileAuraNode tile) {
        return new AABB(tile.getBlockPos()).inflate(1.0);
    }
}
