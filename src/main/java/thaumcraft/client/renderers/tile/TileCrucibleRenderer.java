package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import thaumcraft.common.tiles.crafting.TileCrucible;

/**
 * Port of TileCrucibleRenderer: a water surface at {@link TileCrucible#getFluidHeight()}, tinted towards
 * purple as aspects build up. The 1.12 water texture was already blue; since 1.13 it is grey and tinted,
 * so the vanilla water color is applied first.
 */
public class TileCrucibleRenderer implements BlockEntityRenderer<TileCrucible> {
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final int WATER_COLOR = 0x3F76E4;
    private static final float MIN = 2 / 16f;
    private static final float MAX = 14 / 16f;

    public TileCrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileCrucible cr, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (cr.tank.getFluidAmount() <= 0) return;
        TextureAtlasSprite icon = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(WATER_STILL);
        float recolor = cr.aspects.visSize() / (float) cr.maxTags;
        if (recolor > 0.0f) {
            recolor = 0.5f + recolor / 2.0f;
        }
        if (recolor > 1.0f) {
            recolor = 1.0f;
        }
        float r = ((WATER_COLOR >> 16) & 0xFF) / 255f * (1.0f - recolor / 3.0f);
        float g = ((WATER_COLOR >> 8) & 0xFF) / 255f * (1.0f - recolor);
        float b = (WATER_COLOR & 0xFF) / 255f * (1.0f - recolor / 2.0f);
        float y = cr.getFluidHeight();

        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose p = pose.last();
        float u0 = icon.getU(MIN), u1 = icon.getU(MAX), v0 = icon.getV(MIN), v1 = icon.getV(MAX);
        vertex(vc, p, MIN, y, MIN, u0, v0, r, g, b, light);
        vertex(vc, p, MIN, y, MAX, u0, v1, r, g, b, light);
        vertex(vc, p, MAX, y, MAX, u1, v1, r, g, b, light);
        vertex(vc, p, MAX, y, MIN, u1, v0, r, g, b, light);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, float u, float v,
                               float r, float g, float b, int light) {
        vc.addVertex(p, x, y, z).setColor(r, g, b, 1.0f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(p, 0, 1, 0);
    }
}
