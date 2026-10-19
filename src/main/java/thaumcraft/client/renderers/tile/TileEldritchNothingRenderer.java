package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.eldritch.TileEldritchNothing;

/**
 * Port of TC4's TileEldritchNothingRenderer: the void shows the starfield (the end portal effect; from far away a flat
 * starfield texture) on every face that is not against an opaque block.
 */
public class TileEldritchNothingRenderer implements BlockEntityRenderer<TileEldritchNothing> {
    private static final ResourceLocation FIELD_FAR = Thaumcraft.id("textures/misc/particlefield32.png");

    public TileEldritchNothingRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEldritchNothing te, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Level world = te.getLevel();
        Entity view = Minecraft.getInstance().getCameraEntity();
        if (world == null || view == null) {
            return;
        }
        BlockPos pos = te.getBlockPos();
        boolean inrange = view.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 512.0;
        VertexConsumer vc = buffers.getBuffer(inrange ? RenderType.endPortal() : RenderType.entityTranslucent(FIELD_FAR));
        Matrix4f m = pose.last().pose();
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            if (!world.getBlockState(n).isSolidRender(world, n)) {
                face(vc, pose, m, d, inrange);
            }
        }
    }

    private static void face(VertexConsumer vc, PoseStack pose, Matrix4f m, Direction d, boolean portal) {
        float[][] v = switch (d) {
            case UP -> new float[][] {{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}};
            case DOWN -> new float[][] {{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}};
            case NORTH -> new float[][] {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}};
            case SOUTH -> new float[][] {{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}};
            case WEST -> new float[][] {{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}};
            case EAST -> new float[][] {{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}};
        };
        float[][] uv = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};
        for (int i = 0; i < 4; ++i) {
            if (portal) {
                vc.addVertex(m, v[i][0], v[i][1], v[i][2]);
            } else {
                // brightness 180.
                vc.addVertex(pose.last(), v[i][0], v[i][1], v[i][2]).setColor(0xFFFFFFFF).setUv(uv[i][0], uv[i][1])
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.pack(11, 0))
                        .setNormal(pose.last(), d.getStepX(), d.getStepY(), d.getStepZ());
            }
        }
    }
}
