package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.obj.ObjModel;
import thaumcraft.common.tiles.eldritch.TileEldritchObelisk;

/**
 * Port of TC4's TileEldritchObeliskRenderer: a three block tall column that bobs above its base block. Its faces
 * show the starfield (the end portal effect; from far away a flat starfield texture), with the carved sides and a
 * capstone at each end over it. In the Outer Lands it uses the darker textures.
 */
public class TileEldritchObeliskRenderer implements BlockEntityRenderer<TileEldritchObelisk> {
    private static final ResourceLocation FIELD_FAR = Thaumcraft.id("textures/misc/particlefield32.png");
    private static final ResourceLocation SIDE = Thaumcraft.id("textures/models/obelisk_side.png");
    private static final ResourceLocation CAP_TEX = Thaumcraft.id("textures/models/obelisk_cap.png");
    private static final ResourceLocation SIDE_2 = Thaumcraft.id("textures/models/obelisk_side_2.png");
    private static final ResourceLocation CAP_TEX_2 = Thaumcraft.id("textures/models/obelisk_cap_2.png");

    public TileEldritchObeliskRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEldritchObelisk te, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Entity view = Minecraft.getInstance().getCameraEntity();
        if (te.getLevel() == null || view == null) {
            return;
        }
        boolean inrange = view.distanceToSqr(te.getBlockPos().getX() + 0.5, te.getBlockPos().getY() + 0.5, te.getBlockPos().getZ() + 0.5) < 512.0;
        float count = view.tickCount + partialTick;
        float bob = Mth.sin(count / 10.0f) * 0.1f + 0.1f;
        int height = 3;

        pose.pushPose();
        pose.translate(0.0, 1.0 + bob, 0.0);
        Matrix4f m = pose.last().pose();
        if (inrange) {
            VertexConsumer vc = buffers.getBuffer(RenderType.endPortal());
            portalFace(vc, m, 0, 0.01f, 1, 0.01f, height);
            portalFace(vc, m, 0, 0.99f, 1, 0.99f, height);
            portalFace(vc, m, 0.01f, 0, 0.01f, 1, height);
            portalFace(vc, m, 0.99f, 0, 0.99f, 1, height);
        } else {
            // brightness 180, colour 0.5 grey.
            VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(FIELD_FAR));
            int lt = LightTexture.pack(11, 0);
            flatFace(vc, pose, 0, 0.01f, 1, 0.01f, height, lt);
            flatFace(vc, pose, 0, 0.99f, 1, 0.99f, height, lt);
            flatFace(vc, pose, 0.01f, 0, 0.01f, 1, height, lt);
            flatFace(vc, pose, 0.99f, 0, 0.99f, 1, height, lt);
        }
        pose.popPose();

        ResourceLocation side = SIDE;
        ResourceLocation cap = CAP_TEX;
        if (te.getLevel().dimension().location().equals(TileEldritchCapRenderer.OUTER)) {
            side = SIDE_2;
            cap = CAP_TEX_2;
        }
        int lit = LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above(5));

        pose.pushPose();
        pose.translate(0.5, 1.0 + bob, 0.5);
        VertexConsumer sv = buffers.getBuffer(RenderType.entityTranslucent(side));
        for (int a = 0; a < 4; ++a) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(a * 90));
            pose.translate(0.0, 0.0, -0.5);
            PoseStack.Pose p = pose.last();
            sv.addVertex(p, -0.5f, height, 0).setColor(0xFFFFFFFF).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lit).setNormal(p, 0, 0, -1);
            sv.addVertex(p, 0.5f, height, 0).setColor(0xFFFFFFFF).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lit).setNormal(p, 0, 0, -1);
            sv.addVertex(p, 0.5f, 0, 0).setColor(0xFFFFFFFF).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lit).setNormal(p, 0, 0, -1);
            sv.addVertex(p, -0.5f, 0, 0).setColor(0xFFFFFFFF).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lit).setNormal(p, 0, 0, -1);
            pose.popPose();
        }
        pose.popPose();

        VertexConsumer cv = buffers.getBuffer(RenderType.entityTranslucent(cap));
        ObjModel model = ObjModel.load(TileEldritchCapRenderer.CAP, true);
        pose.pushPose();
        pose.translate(0.5, 1.0 + bob, 0.5);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        model.renderPart("Cap", pose, cv, lit, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        pose.popPose();
        pose.pushPose();
        pose.translate(0.5, 4.0 + bob, 0.5);
        pose.mulPose(Axis.XN.rotationDegrees(90.0f));
        model.renderPart("Cap", pose, cv, lit, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        pose.popPose();
    }

    /** A vertical face from (x1, z1) to (x2, z2), both windings so it shows from either side. */
    private static void portalFace(VertexConsumer vc, Matrix4f m, float x1, float z1, float x2, float z2, int h) {
        vc.addVertex(m, x1, h, z1);
        vc.addVertex(m, x1, 0, z1);
        vc.addVertex(m, x2, 0, z2);
        vc.addVertex(m, x2, h, z2);
        vc.addVertex(m, x2, h, z2);
        vc.addVertex(m, x2, 0, z2);
        vc.addVertex(m, x1, 0, z1);
        vc.addVertex(m, x1, h, z1);
    }

    private static void flatFace(VertexConsumer vc, PoseStack pose, float x1, float z1, float x2, float z2, int h, int light) {
        PoseStack.Pose p = pose.last();
        int c = 0xFF808080;
        vc.addVertex(p, x1, h, z1).setColor(c).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 1, 0);
        vc.addVertex(p, x1, 0, z1).setColor(c).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 1, 0);
        vc.addVertex(p, x2, 0, z2).setColor(c).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 1, 0);
        vc.addVertex(p, x2, h, z2).setColor(c).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 1, 0);
    }

    @Override
    public AABB getRenderBoundingBox(TileEldritchObelisk te) {
        return new AABB(te.getBlockPos()).expandTowards(0, 5, 0);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
