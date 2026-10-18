package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.block.ModelBanner;
import thaumcraft.common.tiles.misc.TileBanner;

/**
 * Port of TileBannerRenderer: the pole (or nothing, on a wall), the beam, the tabs and cloth in the dye's color, the
 * cloth swaying, and the aspect drawn on it. The crimson cult banner uses its own texture.
 */
public class TileBannerRenderer implements BlockEntityRenderer<TileBanner> {
    private static final ResourceLocation TEX_CULT = Thaumcraft.id("textures/models/banner_cultist.png");
    private static final ResourceLocation TEX_BLANK = Thaumcraft.id("textures/models/banner_blank.png");
    private final ModelBanner model = new ModelBanner();

    public TileBannerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileBanner banner, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation tex = banner.getAspect() == null && banner.getColor() == -1 ? TEX_CULT : TEX_BLANK;
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(tex));
        pose.pushPose();
        pose.translate(0.5f, 1.5f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (banner.getLevel() != null) {
            pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            pose.mulPose(Axis.YP.rotationDegrees(banner.getBannerFacing() * 360 / 16.0f));
        }
        if (!banner.getWall()) {
            model.pole.render(pose, vc, light, overlay);
        } else {
            pose.translate(0.0, 1.0, -0.4125);
        }
        model.beam.render(pose, vc, light, overlay);
        int color = banner.getColor() != -1 ? 0xFF000000 | banner.getColor() : -1;
        model.b1.render(pose, vc, light, overlay, color);
        model.b2.render(pose, vc, light, overlay, color);
        BlockPos bp = banner.getBlockPos();
        int ticks = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0;
        float f3 = bp.getX() * 7 + bp.getY() * 9 + bp.getZ() * 13 + (float) ticks + partialTick;
        float rx = 0.02f - Mth.sin(f3 / 11.0f) * 0.02f;
        model.banner.xRot = rx;
        model.banner.render(pose, vc, light, overlay, color);
        if (banner.getAspect() != null) {
            pose.pushPose();
            pose.translate(0.0f, 0.0f, 0.05001f);
            pose.scale(0.0375f, 0.0375f, 0.0375f);
            pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            pose.mulPose(Axis.XP.rotationDegrees(-rx * 57.295776f * 2.0f));
            // drawTag(-8, 0) at 75% alpha: a 16 wide icon from y 0 to 16, top at y 0.
            pose.translate(0.0f, 8.0f, 0.0f);
            pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
            int c = banner.getAspect().getColor();
            UtilsFX.renderQuadCentered(pose, buffers, banner.getAspect().getImage(), 16.0f,
                    ((c >> 16) & 0xFF) / 255.0f, ((c >> 8) & 0xFF) / 255.0f, (c & 0xFF) / 255.0f, 0.75f, light);
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(TileBanner banner) {
        BlockPos p = banner.getBlockPos();
        return new AABB(p.getX(), p.getY() - 1, p.getZ(), p.getX() + 1, p.getY() + 2, p.getZ() + 1);
    }
}
