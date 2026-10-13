package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.crafting.TileDeconstructionTable;

/**
 * Reborn's TileDeconstructionTableRenderer, without the table itself (now the block model): the thaumometer on the
 * corner, the item being broken down spinning above the table, and the aspect it left lying on top.
 */
public class TileDeconstructionTableRenderer implements BlockEntityRenderer<TileDeconstructionTable> {
    private final ItemRenderer itemRenderer;

    public TileDeconstructionTableRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(TileDeconstructionTable table, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (table.getLevel() == null) return;
        pose.pushPose();
        pose.translate(0.5f + 2.1f / 16.0f, 1.0f + 3.5f / 16.0f, 0.5f + 2.1f / 16.0f);
        pose.scale(4.2f / 16.0f, 4.2f / 16.0f, 4.2f / 16.0f);
        itemRenderer.renderStatic(new ItemStack(ConfigItems.thaumometer.get()), ItemDisplayContext.NONE, light, overlay, pose, buffers,
                table.getLevel(), 0);
        pose.popPose();

        float ticks = table.getLevel().getGameTime() + partialTicks;
        if (!table.breakItem.isEmpty()) {
            // TODO: the original drew it see-through and additive (alpha 0.75).
            float hoverStart = Mth.sin(ticks / 14.0f) * 0.2f + 0.2f;
            pose.pushPose();
            pose.translate(0.5f, 1.15f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f));
            // RenderEntityItem lifts the model by its bob and a quarter of its ground scale.
            pose.translate(0.0f, Mth.sin(hoverStart) * 0.1f + 0.1f + 0.125f, 0.0f);
            itemRenderer.renderStatic(table.breakItem.copyWithCount(1), ItemDisplayContext.GROUND, light, overlay, pose, buffers,
                    table.getLevel(), 0);
            pose.popPose();
        }

        if (table.aspect != null) {
            pose.pushPose();
            pose.translate(0.5f, 1.0f + 2.0f / 16.0f, 0.5f);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(ticks % 360.0f));
            UtilsFX.drawTag(pose, buffers, table.aspect, 16 * 0.024f, light);
            pose.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(TileDeconstructionTable table) {
        return new AABB(table.getBlockPos()).expandTowards(0, 1, 0);
    }
}
