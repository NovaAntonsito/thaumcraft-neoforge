package thaumcraft.client.renderers.tile;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.misc.TileHole;

/**
 * The walls of a dimensional tear: every solid neighbour gets a face of the end sky just inside the hole.
 * 1.21: the original end shader is vanilla's end portal render type.
 */
public class TileHoleRenderer implements BlockEntityRenderer<TileHole> {
    public TileHoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileHole te, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (te.getLevel() == null) {
            return;
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.endPortal());
        Matrix4f m = pose.last().pose();
        BlockPos pos = te.getBlockPos();
        float o = 0.001f;
        for (Direction face : Direction.values()) {
            BlockPos np = pos.relative(face);
            BlockState bs = te.getLevel().getBlockState(np);
            if (bs.isSolidRender(te.getLevel(), np) && bs.getBlock() != ConfigBlocks.hole.get()) {
                switch (face) {
                    case DOWN -> quad(vc, m, 0, o, 0, 1, o, 0, 1, o, 1, 0, o, 1);
                    case UP -> quad(vc, m, 0, 1 - o, 1, 1, 1 - o, 1, 1, 1 - o, 0, 0, 1 - o, 0);
                    case NORTH -> quad(vc, m, 0, 1, o, 1, 1, o, 1, 0, o, 0, 0, o);
                    case SOUTH -> quad(vc, m, 0, 0, 1 - o, 1, 0, 1 - o, 1, 1, 1 - o, 0, 1, 1 - o);
                    case WEST -> quad(vc, m, o, 0, 0, o, 0, 1, o, 1, 1, o, 1, 0);
                    case EAST -> quad(vc, m, 1 - o, 1, 0, 1 - o, 1, 1, 1 - o, 0, 1, 1 - o, 0, 0);
                }
            }
        }
    }

    /** One face, drawn both ways so it shows whichever side the camera is on. */
    private static void quad(VertexConsumer vc, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3,
                             float x4, float y4, float z4) {
        vc.addVertex(m, x1, y1, z1);
        vc.addVertex(m, x2, y2, z2);
        vc.addVertex(m, x3, y3, z3);
        vc.addVertex(m, x4, y4, z4);
        vc.addVertex(m, x4, y4, z4);
        vc.addVertex(m, x3, y3, z3);
        vc.addVertex(m, x2, y2, z2);
        vc.addVertex(m, x1, y1, z1);
    }
}
