package thaumcraft.client.lib.events;

import java.util.concurrent.ConcurrentHashMap;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.ISealDisplayer;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.client.lib.render.TCRenderTypes;
import thaumcraft.common.golems.seals.SealEntity;
import thaumcraft.common.golems.seals.SealHandler;

/**
 * The seal part of RenderEventHandler.renderLast (drawSeals, drawSealArea, renderSeal, translateSeal), kept in its own
 * class. While the player holds a seal displayer (a seal, the bell, a golem) every seal within 16 blocks is drawn,
 * through walls while sneaking.
 * A seal stopped by redstone is drawn dimmed and now and then throws a red spark.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class SealRenderEvents {
    private SealRenderEvents() {
    }

    static final ResourceLocation MIDDLE = Thaumcraft.id("textures/misc/seal_area.png");
    static final ResourceLocation CFRAME = Thaumcraft.id("textures/misc/frame_corner.png");
    static final Direction[][] rotfaces = {
            {Direction.DOWN, Direction.NORTH, Direction.WEST}, {Direction.UP, Direction.NORTH, Direction.WEST},
            {Direction.DOWN, Direction.NORTH, Direction.EAST}, {Direction.UP, Direction.NORTH, Direction.EAST},
            {Direction.DOWN, Direction.SOUTH, Direction.EAST}, {Direction.UP, Direction.SOUTH, Direction.EAST},
            {Direction.DOWN, Direction.SOUTH, Direction.WEST}, {Direction.UP, Direction.SOUTH, Direction.WEST}};
    static final int[][] rotmat = {{0, 270, 0}, {270, 180, 270}, {90, 0, 90}, {180, 90, 180}, {180, 180, 0}, {90, 270, 270},
            {270, 90, 90}, {0, 0, 180}};

    /** The client's copy of the seals belongs to the server it came from. */
    @SubscribeEvent
    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SealHandler.sealEntities.clear();
    }

    @SubscribeEvent
    public static void renderLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof Player player) || mc.level == null) return;
        if (player.getMainHandItem().getItem() instanceof ISealDisplayer || player.getOffhandItem().getItem() instanceof ISealDisplayer) {
            drawSeals(event.getPoseStack(), event.getCamera().getPosition(), event.getPartialTick().getGameTimeDeltaPartialTick(false), player);
        }
    }

    private static void drawSeals(PoseStack pose, Vec3 cam, float partialTicks, Player player) {
        ConcurrentHashMap<SealPos, SealEntity> seals = SealHandler.sealEntities.get(player.level().dimension());
        if (seals == null || seals.isEmpty()) {
            return;
        }
        boolean xray = player.isShiftKeyDown();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        for (ISealEntity seal : seals.values()) {
            if (seal.getSeal() == null) continue;
            double dis = player.distanceToSqr(Vec3.atCenterOf(seal.getSealPos().pos));
            if (dis <= 256.0) {
                float alpha = 1.0f - (float) (dis / 256.0);
                boolean ia = false;
                if (seal.isStoppedByRedstone(player.level())) {
                    ia = true;
                    if (player.level().random.nextFloat() < partialTicks / 12.0f) {
                        Direction f = seal.getSealPos().face;
                        thaumcraft.client.fx.FXDispatcher.spark(seal.getSealPos().pos.getX() + 0.5f + f.getStepX() * 0.66f,
                                seal.getSealPos().pos.getY() + 0.5f + f.getStepY() * 0.66f, seal.getSealPos().pos.getZ() + 0.5f + f.getStepZ() * 0.66f,
                                2.0f, 0.8f - player.level().random.nextFloat() * 0.2f, 0.0f, 0.0f, 1.0f);
                        ia = false;
                    }
                }
                renderSeal(pose, buffers, xray, seal.getSealPos().pos.getX(), seal.getSealPos().pos.getY(), seal.getSealPos().pos.getZ(), alpha,
                        seal.getSealPos().face, seal.getSeal().getSealIcon(), ia);
                drawSealArea(pose, buffers, xray, player, seal, alpha, partialTicks);
            }
        }
        pose.popPose();
        buffers.endBatch();
    }

    private static void drawSealArea(PoseStack pose, MultiBufferSource buffers, boolean xray, Player player, ISealEntity seal, float alpha,
                                     float partialTicks) {
        float r;
        float g;
        float b;
        if (seal.getColor() > 0) {
            int c = DyeColor.byId(seal.getColor() - 1).getTextureDiffuseColor();
            r = ((c >> 16) & 0xFF) / 255.0f;
            g = ((c >> 8) & 0xFF) / 255.0f;
            b = (c & 0xFF) / 255.0f;
        } else {
            r = 0.7f + Mth.sin((player.tickCount + partialTicks + seal.getSealPos().pos.getX()) / 4.0f) * 0.1f;
            g = 0.7f + Mth.sin((player.tickCount + partialTicks + seal.getSealPos().pos.getY()) / 5.0f) * 0.1f;
            b = 0.7f + Mth.sin((player.tickCount + partialTicks + seal.getSealPos().pos.getZ()) / 6.0f) * 0.1f;
        }
        Direction face = seal.getSealPos().face;
        pose.pushPose();
        pose.translate(seal.getSealPos().pos.getX() + 0.5, seal.getSealPos().pos.getY() + 0.5, seal.getSealPos().pos.getZ() + 0.5);
        rotateToFace(pose, face);
        pose.translate(0.0, 0.0, face.getStepZ() < 0 ? -0.51 : 0.51);
        pose.mulPose(Axis.ZP.rotationDegrees(player.tickCount % 360 + partialTicks));
        quadCentered(pose, buffers.getBuffer(TCRenderTypes.seal(MIDDLE, xray)), 0.9f, r, g, b, alpha * 0.8f);
        pose.popPose();
        if (seal.getSeal() instanceof ISealConfigArea) {
            AABB area = GolemHelper.getBoundsForArea(seal);
            double[][] locs = {{area.minX, area.minY, area.minZ}, {area.minX, area.maxY - 1.0, area.minZ}, {area.maxX - 1.0, area.minY, area.minZ},
                    {area.maxX - 1.0, area.maxY - 1.0, area.minZ}, {area.maxX - 1.0, area.minY, area.maxZ - 1.0},
                    {area.maxX - 1.0, area.maxY - 1.0, area.maxZ - 1.0}, {area.minX, area.minY, area.maxZ - 1.0},
                    {area.minX, area.maxY - 1.0, area.maxZ - 1.0}};
            VertexConsumer vc = buffers.getBuffer(TCRenderTypes.seal(CFRAME, xray));
            int q = 0;
            for (double[] loc : locs) {
                pose.pushPose();
                pose.translate(loc[0] + 0.5, loc[1] + 0.5, loc[2] + 0.5);
                int w = 0;
                for (Direction f : rotfaces[q]) {
                    pose.pushPose();
                    rotateToFace(pose, f);
                    pose.translate(0.0, 0.0, f.getStepZ() < 0 ? -0.49 : 0.49);
                    pose.mulPose(Axis.ZN.rotationDegrees(90.0f));
                    pose.mulPose(Axis.ZP.rotationDegrees(rotmat[q][w]));
                    quadCentered(pose, vc, 1.0f, r, g, b, alpha * 0.7f);
                    pose.popPose();
                    ++w;
                }
                pose.popPose();
                ++q;
            }
        }
    }

    /** glRotatef(90, -stepY, stepX, -stepZ): turns +Z to face the given side (the Z faces keep +Z and flip the offset). */
    private static void rotateToFace(PoseStack pose, Direction face) {
        pose.mulPose(Axis.of(new Vector3f(-face.getStepY(), face.getStepX(), -face.getStepZ())).rotationDegrees(90.0f));
    }

    /** UtilsFX.renderQuadCentered: a size-wide square in the XY plane, centered on the origin. */
    private static void quadCentered(PoseStack pose, VertexConsumer vc, float size, float r, float g, float b, float a) {
        PoseStack.Pose p = pose.last();
        float h = size / 2.0f;
        vc.addVertex(p, -h, h, 0).setUv(0, 1).setColor(r, g, b, a);
        vc.addVertex(p, h, h, 0).setUv(1, 1).setColor(r, g, b, a);
        vc.addVertex(p, h, -h, 0).setUv(1, 0).setColor(r, g, b, a);
        vc.addVertex(p, -h, -h, 0).setUv(0, 0).setColor(r, g, b, a);
    }

    static void renderSeal(PoseStack pose, MultiBufferSource buffers, boolean xray, int x, int y, int z, float alpha, Direction face,
                           ResourceLocation icon, boolean ia) {
        pose.pushPose();
        float c = ia ? 0.5f : 1.0f;
        translateSeal(pose, x, y, z, face.get3DDataValue(), -0.05f);
        pose.scale(0.5f, 0.5f, 0.5f);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(icon);
        renderItemIn2D(pose, buffers.getBuffer(TCRenderTypes.seal(InventoryMenu.BLOCK_ATLAS, xray)), sprite, xray ? 0.0f : 0.1f, c, alpha);
        pose.popPose();
    }

    /** UtilsFX.renderItemIn2D without the edge strips: the icon on a 1x1 square, front at z=0 and back at z=-thickness. */
    private static void renderItemIn2D(PoseStack pose, VertexConsumer vc, TextureAtlasSprite icon, float thickness, float c, float a) {
        PoseStack.Pose p = pose.last();
        float maxu = icon.getU1();
        float minu = icon.getU0();
        float minv = icon.getV1();
        float maxv = icon.getV0();
        vc.addVertex(p, 0, 0, 0).setUv(maxu, minv).setColor(c, c, c, a);
        vc.addVertex(p, 1, 0, 0).setUv(minu, minv).setColor(c, c, c, a);
        vc.addVertex(p, 1, 1, 0).setUv(minu, maxv).setColor(c, c, c, a);
        vc.addVertex(p, 0, 1, 0).setUv(maxu, maxv).setColor(c, c, c, a);
        if (thickness > 0.0f) {
            vc.addVertex(p, 0, 1, -thickness).setUv(maxu, maxv).setColor(c, c, c, a);
            vc.addVertex(p, 1, 1, -thickness).setUv(minu, maxv).setColor(c, c, c, a);
            vc.addVertex(p, 1, 0, -thickness).setUv(minu, minv).setColor(c, c, c, a);
            vc.addVertex(p, 0, 0, -thickness).setUv(maxu, minv).setColor(c, c, c, a);
        }
    }

    private static void translateSeal(PoseStack pose, float x, float y, float z, int orientation, float off) {
        if (orientation == 1) {
            pose.translate(x + 0.25f, y + 1.0f, z + 0.75f);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
        } else if (orientation == 0) {
            pose.translate(x + 0.25f, y, z + 0.25f);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        } else if (orientation == 3) {
            pose.translate(x + 0.25f, y + 0.25f, z + 1.0f);
        } else if (orientation == 2) {
            pose.translate(x + 0.75f, y + 0.25f, z);
            pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        } else if (orientation == 5) {
            pose.translate(x + 1.0f, y + 0.25f, z + 0.75f);
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
        } else if (orientation == 4) {
            pose.translate(x, y + 0.25f, z + 0.25f);
            pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
        }
        pose.translate(0.0f, 0.0f, -off);
    }
}
