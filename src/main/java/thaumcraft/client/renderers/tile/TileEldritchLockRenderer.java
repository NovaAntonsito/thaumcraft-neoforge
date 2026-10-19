package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import org.joml.Matrix4f;
import org.joml.Vector3f;

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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.eldritch.TileEldritchLock;

/**
 * Port of TC4's TileEldritchLockRenderer. Four arms of pulsing eldritch cubes stretch across the doorway from the
 * lock and pull back one cube at a time as it turns; the Runed Tablet sits in the lock once used; and the 5x5
 * doorway shows the starfield (the end portal effect, or the flat starfield texture from far away).
 */
public class TileEldritchLockRenderer implements BlockEntityRenderer<TileEldritchLock> {
    private static final ResourceLocation CUBE = Thaumcraft.id("textures/models/eldritch_cube.png");
    private static final ResourceLocation FIELD_FAR = Thaumcraft.id("textures/misc/particlefield32.png");
    private final ModelPart cube;
    private ItemStack is = null;

    public TileEldritchLockRenderer(BlockEntityRendererProvider.Context context) {
        // ModelCube(0): a mirrored 16x16x16 box centred on the origin, texture 64x64.
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16),
                PartPose.ZERO);
        this.cube = LayerDefinition.create(mesh, 64, 64).bakeRoot().getChild("cube");
    }

    @Override
    public void render(TileEldritchLock te, float f, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Entity view = Minecraft.getInstance().getCameraEntity();
        if (view == null) {
            return;
        }
        BlockPos pos = te.getBlockPos();
        boolean inrange = view.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 512.0;
        if (this.is == null) {
            this.is = new ItemStack(ConfigItems.runedTablet.get());
        }

        float count = view.tickCount + f;
        Direction dir = te.getFacing();
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutout(CUBE));
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);

        for (int u = 0; u < 4; ++u) {
            pose.pushPose();
            pose.mulPose(Axis.of(new Vector3f(dir.getStepX(), dir.getStepY(), dir.getStepZ())).rotationDegrees(90 * u));

            for (int a = 1; a < 5 - (te.count + u * 5) / 20; ++a) {
                pose.pushPose();
                pose.translate(0.0F, 0.25F + 0.5F * a, 0.0F);
                float w = Mth.sin((count + a * 10 + u * 20) / 20.0F) * 0.1F;
                if (a == 1 || a == 4) {
                    w = w / 2.0F + 0.2F;
                }

                pose.scale(0.5F + w, 0.5F, 0.5F + w);
                this.cube.render(pose, vc, light, OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }

            pose.popPose();
        }

        pose.popPose();
        if (te.count >= 0 && dir.getAxis().isHorizontal()) {
            pose.pushPose();
            pose.translate(0.5F + dir.getStepX() * 0.525F, 0.5F, 0.5F + dir.getStepZ() * 0.525F);
            // The frame item's front faces north; turn it to face out of the lock.
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - dir.toYRot()));
            pose.scale(0.5F, 0.5F, 0.5F);
            Minecraft.getInstance().getItemRenderer().renderStatic(this.is, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose,
                    buffers, te.getLevel(), 0);
            pose.popPose();
        }

        if (dir.getAxis().isHorizontal()) {
            drawPlane(pose, buffers, dir.getAxis() == Direction.Axis.X, inrange);
        }
    }

    /** drawPlaneZPos/ZNeg/XPos/XNeg: the 5x5 square through the middle of the lock, seen from either side. */
    private static void drawPlane(PoseStack pose, MultiBufferSource buffers, boolean alongX, boolean inrange) {
        float offset = 0.5F;
        float[][] v = alongX
                ? new float[][]{{offset, 3, -2}, {offset, 3, 3}, {offset, -2, 3}, {offset, -2, -2}}
                : new float[][]{{-2, 3, offset}, {-2, -2, offset}, {3, -2, offset}, {3, 3, offset}};
        float[][] uv = {{1, 1}, {1, 0}, {0, 0}, {0, 1}};
        Matrix4f m = pose.last().pose();
        VertexConsumer vc = buffers.getBuffer(inrange ? RenderType.endPortal() : RenderType.entityTranslucent(FIELD_FAR));
        for (int side = 0; side < 2; ++side) {
            for (int k = 0; k < 4; ++k) {
                int i = side == 0 ? k : 3 - k;
                if (inrange) {
                    vc.addVertex(m, v[i][0], v[i][1], v[i][2]);
                } else {
                    // Brightness 180, colour 0.5 grey.
                    vc.addVertex(pose.last(), v[i][0], v[i][1], v[i][2]).setColor(0.5F, 0.5F, 0.5F, 1.0F).setUv(uv[i][0], uv[i][1])
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.pack(11, 0))
                            .setNormal(pose.last(), alongX ? 1 : 0, 0, alongX ? 0 : 1);
                }
            }
        }
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public AABB getRenderBoundingBox(TileEldritchLock te) {
        BlockPos p = te.getBlockPos();
        return new AABB(p.getX() - 2.25, p.getY() - 2.25, p.getZ() - 2.25, p.getX() + 3.25, p.getY() + 3.25, p.getZ() + 3.25);
    }
}
