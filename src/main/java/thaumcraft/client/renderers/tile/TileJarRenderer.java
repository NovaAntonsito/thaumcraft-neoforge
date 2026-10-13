package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.essentia.TileJarFillable;

/**
 * Port of TileJarRenderer: the essentia inside, the label and the brass brace or lid extension.
 * The brain in a jar has its own renderer, TileJarBrainRenderer.
 */
public class TileJarRenderer implements BlockEntityRenderer<TileJarFillable> {
    private static final ResourceLocation TEX_LABEL = Thaumcraft.id("textures/models/label.png");
    private static final ResourceLocation TEX_BRINE = Thaumcraft.id("textures/models/jarbrine.png");
    private static final ResourceLocation GLOW = Thaumcraft.id("blocks/animatedglow");
    /** RenderCubes brightness 200 of the original: block light 12. */
    private static final int LIQUID_LIGHT = LightTexture.pack(12, 12);

    private final ModelPart lid;
    private final ModelPart lidExtension;

    public TileJarRenderer(BlockEntityRendererProvider.Context context) {
        // ModelJar: only the parts drawn here (Lid and LidExtension).
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(32, 24).mirror().addBox(-3.0f, 0.0f, -3.0f, 6, 2, 6),
                PartPose.offset(0.0f, -14.0f, 0.0f));
        root.addOrReplaceChild("lid_extension", CubeListBuilder.create().texOffs(0, 23).mirror().addBox(-2.0f, -16.0f, -2.0f, 4, 2, 4),
                PartPose.ZERO);
        ModelPart model = LayerDefinition.create(mesh, 64, 32).bakeRoot();
        lid = model.getChild("lid");
        lidExtension = model.getChild("lid_extension");
    }

    @Override
    public void render(TileJarFillable tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5f, 0.01f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (tile.blocked) {
            pose.pushPose();
            pose.scale(1.001f, 1.001f, 1.001f);
            lid.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEX_BRINE)), light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        if (tile.getLevel() != null && ThaumcraftApiHelper.getConnectableTile(tile.getLevel(), tile.getBlockPos(), Direction.UP) != null) {
            pose.pushPose();
            pose.scale(0.9f, 1.0f, 0.9f);
            lidExtension.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEX_BRINE)), light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        pose.popPose();

        if (tile.aspectFilter != null) {
            pose.pushPose();
            Direction side = Direction.from3DDataValue(tile.facing);
            pose.translate(0.5f, 0.41f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(180.0f - side.toYRot()));
            // ModConfig.CONFIG_GRAPHICS.crooked: labels sit slightly askew.
            float rot = (float) ((tile.aspectFilter.getTag().hashCode() + tile.getBlockPos().getX() + tile.facing) % 4 - 2);
            pose.translate(0.0f, 0.0f, -0.315f);
            pose.mulPose(Axis.ZP.rotationDegrees(rot));
            UtilsFX.renderQuadCentered(pose, buffers, TEX_LABEL, 0.5f, 1.0f, 1.0f, 1.0f, 1.0f, light);
            pose.translate(0.0f, 0.0f, -0.001f);
            UtilsFX.drawTag(pose, buffers, tile.aspectFilter, 0.336f, light);
            pose.popPose();
        }
        if (tile.amount > 0) {
            renderLiquid(tile, pose, buffers);
        }
    }

    /** A box of the aspect's color, 0.625 high when full. */
    private void renderLiquid(TileJarFillable te, PoseStack pose, MultiBufferSource buffers) {
        float level = te.amount / (float) TileJarFillable.CAPACITY * 0.625f;
        int color = te.aspect != null ? te.aspect.getColor() : 0;
        float r = ((color >> 16) & 0xFF) / 255f, g = ((color >> 8) & 0xFF) / 255f, b = (color & 0xFF) / 255f;
        TextureAtlasSprite icon = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(GLOW);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose p = pose.last();
        float x0 = 0.25f, x1 = 0.75f, y0 = 0.0625f, y1 = 0.0625f + level, z0 = 0.25f, z1 = 0.75f;
        float u0 = icon.getU0(), u1 = icon.getU1(), v0 = icon.getV0(), v1 = icon.getV1();
        float vh = icon.getV(level);
        // up, down
        quad(vc, p, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, u0, v0, u1, v1, r, g, b, 0, 1, 0);
        quad(vc, p, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, u0, v0, u1, v1, r, g, b, 0, -1, 0);
        // north, south
        quad(vc, p, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, u0, v0, u1, vh, r, g, b, 0, 0, -1);
        quad(vc, p, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, u0, v0, u1, vh, r, g, b, 0, 0, 1);
        // west, east
        quad(vc, p, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, u0, v0, u1, vh, r, g, b, -1, 0, 0);
        quad(vc, p, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, u0, v0, u1, vh, r, g, b, 1, 0, 0);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose p,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float u0, float v0, float u1, float v1, float r, float g, float b,
                             float nx, float ny, float nz) {
        vertex(vc, p, ax, ay, az, u0, v0, r, g, b, nx, ny, nz);
        vertex(vc, p, bx, by, bz, u0, v1, r, g, b, nx, ny, nz);
        vertex(vc, p, cx, cy, cz, u1, v1, r, g, b, nx, ny, nz);
        vertex(vc, p, dx, dy, dz, u1, v0, r, g, b, nx, ny, nz);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, float u, float v,
                               float r, float g, float b, float nx, float ny, float nz) {
        vc.addVertex(p, x, y, z).setColor(r, g, b, 1.0f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LIQUID_LIGHT).setNormal(p, nx, ny, nz);
    }
}
