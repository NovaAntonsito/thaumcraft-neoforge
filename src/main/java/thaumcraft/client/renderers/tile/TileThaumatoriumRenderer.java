package thaumcraft.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.common.tiles.crafting.TileThaumatorium;

/**
 * Port of TileThaumatoriumRenderer: the output of the picked recipes (one per second) shown in the window on
 * its front. The original rendered a dropped item entity at 0.75 scale; this draws it the same way, as GROUND.
 */
public class TileThaumatoriumRenderer implements BlockEntityRenderer<TileThaumatorium> {
    private final ItemRenderer itemRenderer;

    public TileThaumatoriumRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(TileThaumatorium tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (tile.getLevel() == null || tile.recipeHash.isEmpty() || Minecraft.getInstance().getCameraEntity() == null) {
            return;
        }
        int stack = Minecraft.getInstance().getCameraEntity().tickCount / 20 % tile.recipeHash.size();
        CrucibleRecipe recipe = tile.getRecipe(tile.recipeHash.get(stack));
        if (recipe == null) {
            return;
        }
        Direction facing = tile.getFacing();
        pose.pushPose();
        pose.translate(0.5f + facing.getStepX() / 1.99f, 1.125f, 0.5f + facing.getStepZ() / 1.99f);
        switch (facing) {
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            default -> {
            }
        }
        pose.scale(0.75f, 0.75f, 0.75f);
        ItemStack is = recipe.getRecipeOutput().copy();
        is.setCount(1);
        // RenderEntityItem lifts the model by its ground transform; hoverStart 0 means no bobbing or spin.
        pose.translate(0.0f, 0.125f, 0.0f);
        itemRenderer.renderStatic(is, ItemDisplayContext.GROUND, light, overlay, pose, buffers, tile.getLevel(), 0);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(TileThaumatorium tile) {
        return new AABB(tile.getBlockPos()).inflate(0.1).expandTowards(0, 1, 0);
    }
}
