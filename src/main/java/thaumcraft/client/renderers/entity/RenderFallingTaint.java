package thaumcraft.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import thaumcraft.common.entities.EntityFallingTaint;

/** Port of RenderFallingTaint: draws the falling block's model where the entity is, like a vanilla falling block. */
public class RenderFallingTaint extends EntityRenderer<EntityFallingTaint> {
    private final BlockRenderDispatcher dispatcher;

    public RenderFallingTaint(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5f;
        dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(EntityFallingTaint entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.getBlock();
        if (state == null || state.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        Level world = entity.level();
        if (state != world.getBlockState(entity.blockPosition())) {
            pose.pushPose();
            BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
            pose.translate(-0.5, 0.0, -0.5);
            BakedModel model = dispatcher.getBlockModel(state);
            long seed = state.getSeed(pos);
            for (RenderType type : model.getRenderTypes(state, RandomSource.create(seed), ModelData.EMPTY)) {
                dispatcher.getModelRenderer().tesselateBlock(world, model, state, pos, pose, buffer.getBuffer(RenderTypeHelper.getMovingBlockRenderType(type)),
                        false, RandomSource.create(), seed, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, type);
            }
            pose.popPose();
            super.render(entity, entityYaw, partialTicks, pose, buffer, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFallingTaint entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
