package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import thaumcraft.common.tiles.devices.TileRechargePedestal;

/** Port of TileRechargePedestalRenderer: the item being charged, spinning above the pedestal. */
public class TileRechargePedestalRenderer implements BlockEntityRenderer<TileRechargePedestal> {
    private final ItemRenderer itemRenderer;

    public TileRechargePedestalRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(TileRechargePedestal ped, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack is = ped.getItem(0);
        if (is.isEmpty() || ped.getLevel() == null) {
            return;
        }
        float ticks = ped.getLevel().getGameTime() + partialTick;
        pose.pushPose();
        pose.translate(0.5f, 0.75f, 0.5f);
        pose.scale(1.5f, 1.5f, 1.5f);
        pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f));
        // RenderEntityItem lifts the model by its bob (0.1 at age 0) and a quarter of its ground scale.
        pose.translate(0.0f, 0.225f, 0.0f);
        itemRenderer.renderStatic(is.copyWithCount(1), ItemDisplayContext.GROUND, light, overlay, pose, buffers, ped.getLevel(), 0);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(TileRechargePedestal ped) {
        return new AABB(ped.getBlockPos()).expandTowards(0, 1, 0);
    }
}
