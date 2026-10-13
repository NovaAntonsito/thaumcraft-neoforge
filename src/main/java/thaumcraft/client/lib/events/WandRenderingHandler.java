package thaumcraft.client.lib.events;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import thaumcraft.Thaumcraft;
import thaumcraft.api.items.IArchitect;
import thaumcraft.client.lib.render.TCRenderTypes;

/**
 * The architect part of WandRenderingHandler (with RenderEventHandler.renderLast calling it): an item holding an
 * architect focus outlines the blocks it would hit, seen through walls, with arrows along the axes the G key grows.
 * The focus radial of this class is GuiFocusRadial.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class WandRenderingHandler {
    private static int lastArcHash = 0;
    private static ArrayList<BlockPos> architectBlocks = new ArrayList<>();
    private static final HashMap<BlockPos, boolean[]> bmCache = new HashMap<>();
    private static final ResourceLocation CFRAME = Thaumcraft.id("textures/misc/frame_corner.png");
    private static final ResourceLocation SFRAME = Thaumcraft.id("textures/misc/frame_side.png");
    private static final int[][] mos = {{4, 5, 6, 7}, {0, 1, 2, 3}, {0, 1, 4, 5}, {2, 3, 6, 7}, {0, 2, 4, 6}, {1, 3, 5, 7}};
    private static final int[][] rotmat = {{0, 90, 270, 180}, {270, 180, 0, 90}, {180, 90, 270, 0}, {0, 270, 90, 180}, {270, 180, 0, 90}, {180, 270, 90, 0}};
    private static final ResourceLocation tex = Thaumcraft.id("textures/misc/architect_arrows.png");

    private WandRenderingHandler() {
    }

    @SubscribeEvent
    static void renderLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof Player player) || mc.level == null) {
            return;
        }
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IArchitect)) {
            stack = player.getOffhandItem();
        }
        if (!(stack.getItem() instanceof IArchitect af)) {
            return;
        }
        HitResult target = af.getArchitectMOP(stack, player.level(), player);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        handleArchitectOverlay(event.getPoseStack(), buffers, event.getCamera().getPosition(), stack, player, partialTicks, player.tickCount, target);
        buffers.endBatch();
    }

    public static boolean handleArchitectOverlay(PoseStack pose, MultiBufferSource buffers, Vec3 cam, ItemStack stack, Player player, float partialTicks,
                                                 int playerticks, HitResult hit) {
        if (!(hit instanceof BlockHitResult target) || hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        Level world = player.level();
        IArchitect af = (IArchitect) stack.getItem();
        String h = target.getBlockPos().getX() + "" + target.getBlockPos().getY() + "" + target.getBlockPos().getZ() + "" + target.getDirection() + ""
                + playerticks / 5;
        int hc = h.hashCode();
        if (hc != lastArcHash) {
            lastArcHash = hc;
            bmCache.clear();
            architectBlocks = af.getArchitectBlocks(stack, world, target.getBlockPos(), target.getDirection(), player);
        }
        if (architectBlocks == null || architectBlocks.isEmpty()) {
            return false;
        }
        drawArchitectAxis(pose, buffers, cam, player, target.getBlockPos(), af.showAxis(stack, world, player, target.getDirection(), IArchitect.EnumAxis.X),
                af.showAxis(stack, world, player, target.getDirection(), IArchitect.EnumAxis.Y),
                af.showAxis(stack, world, player, target.getDirection(), IArchitect.EnumAxis.Z));
        for (BlockPos cc : architectBlocks) {
            drawOverlayBlock(pose, buffers, cam, cc);
        }
        return true;
    }

    private static boolean isConnectedBlock(BlockPos pos) {
        return architectBlocks.contains(pos);
    }

    private static boolean[] getConnectedSides(BlockPos pos) {
        if (bmCache.containsKey(pos)) {
            return bmCache.get(pos);
        }
        boolean[] bitMatrix = {
                !isConnectedBlock(pos.offset(-1, 0, 0)) && !isConnectedBlock(pos.offset(0, 0, -1)) && !isConnectedBlock(pos.offset(0, 1, 0)),
                !isConnectedBlock(pos.offset(1, 0, 0)) && !isConnectedBlock(pos.offset(0, 0, -1)) && !isConnectedBlock(pos.offset(0, 1, 0)),
                !isConnectedBlock(pos.offset(-1, 0, 0)) && !isConnectedBlock(pos.offset(0, 0, 1)) && !isConnectedBlock(pos.offset(0, 1, 0)),
                !isConnectedBlock(pos.offset(1, 0, 0)) && !isConnectedBlock(pos.offset(0, 0, 1)) && !isConnectedBlock(pos.offset(0, 1, 0)),
                !isConnectedBlock(pos.offset(-1, 0, 0)) && !isConnectedBlock(pos.offset(0, 0, -1)) && !isConnectedBlock(pos.offset(0, -1, 0)),
                !isConnectedBlock(pos.offset(1, 0, 0)) && !isConnectedBlock(pos.offset(0, 0, -1)) && !isConnectedBlock(pos.offset(0, -1, 0)),
                !isConnectedBlock(pos.offset(-1, 0, 0)) && !isConnectedBlock(pos.offset(0, 0, 1)) && !isConnectedBlock(pos.offset(0, -1, 0)),
                !isConnectedBlock(pos.offset(1, 0, 0)) && !isConnectedBlock(pos.offset(0, 0, 1)) && !isConnectedBlock(pos.offset(0, -1, 0))};
        bmCache.put(pos, bitMatrix);
        return bitMatrix;
    }

    /** The frame on every outer face of a block: the side texture faintly, and a corner wherever the outline turns. */
    private static void drawOverlayBlock(PoseStack pose, MultiBufferSource buffers, Vec3 cam, BlockPos pos) {
        boolean[] bitMatrix = getConnectedSides(pos);
        pose.pushPose();
        pose.translate(-cam.x + pos.getX() + 0.5, -cam.y + pos.getY() + 0.5, -cam.z + pos.getZ() + 0.5);
        for (Direction face : Direction.values()) {
            if (!isConnectedBlock(pos.relative(face))) {
                pose.pushPose();
                pose.mulPose(new Quaternionf().rotateAxis((float) Math.toRadians(90.0), -face.getStepY(), face.getStepX(), -face.getStepZ()));
                pose.translate(0.0, 0.0, face.getStepZ() < 0 ? -0.5 : 0.5);
                pose.mulPose(Axis.ZN.rotationDegrees(90.0f));
                quad(pose, buffers, SFRAME, 1.0f, 1.0f, 1.0f, 0.1f);
                for (int a = 0; a < 4; ++a) {
                    if (bitMatrix[mos[face.ordinal()][a]]) {
                        pose.pushPose();
                        pose.mulPose(Axis.ZP.rotationDegrees(rotmat[face.ordinal()][a]));
                        quad(pose, buffers, CFRAME, 1.0f, 1.0f, 1.0f, 0.66f);
                        pose.popPose();
                    }
                }
                pose.popPose();
            }
        }
        pose.popPose();
    }

    /** Crossed arrow pairs through the target block, one pair per axis the area grows along, slowly changing color. */
    private static void drawArchitectAxis(PoseStack pose, MultiBufferSource buffers, Vec3 cam, Player player, BlockPos pos, boolean dx, boolean dy, boolean dz) {
        if (!dx && !dy && !dz) {
            return;
        }
        float r = Mth.sin(player.tickCount / 4.0f + pos.getX()) * 0.2f + 0.3f;
        float g = Mth.sin(player.tickCount / 3.0f + pos.getY()) * 0.2f + 0.3f;
        float b = Mth.sin(player.tickCount / 2.0f + pos.getZ()) * 0.2f + 0.8f;
        pose.pushPose();
        pose.translate(-cam.x + pos.getX() + 0.5, -cam.y + pos.getY() + 0.5, -cam.z + pos.getZ() + 0.5);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        if (dz) {
            pose.pushPose();
            quad(pose, buffers, tex, r, g, b, 1.0f);
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            quad(pose, buffers, tex, r, g, b, 1.0f);
            pose.popPose();
        }
        if (dx) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            quad(pose, buffers, tex, r, g, b, 1.0f);
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            quad(pose, buffers, tex, r, g, b, 1.0f);
            pose.popPose();
        }
        if (dy) {
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            quad(pose, buffers, tex, r, g, b, 1.0f);
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            quad(pose, buffers, tex, r, g, b, 1.0f);
            pose.popPose();
        }
        pose.popPose();
    }

    /** UtilsFX.renderQuadCentered with GL_ONE blending: a unit square in the XY plane, same corners and UVs as 1.12. */
    private static void quad(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture, float r, float g, float b, float a) {
        VertexConsumer vc = buffers.getBuffer(TCRenderTypes.overlayAdditive(texture));
        Matrix4f m = pose.last().pose();
        vc.addVertex(m, -0.5f, 0.5f, 0.0f).setUv(1, 1).setColor(r, g, b, a);
        vc.addVertex(m, 0.5f, 0.5f, 0.0f).setUv(1, 0).setColor(r, g, b, a);
        vc.addVertex(m, 0.5f, -0.5f, 0.0f).setUv(0, 0).setColor(r, g, b, a);
        vc.addVertex(m, -0.5f, -0.5f, 0.0f).setUv(0, 1).setColor(r, g, b, a);
    }
}
