package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.entity.ModelFireBat;
import thaumcraft.common.entities.monster.EntityFireBat;

/** The fire bat: a small bat that bobs while it flies, always full-bright (getBrightnessForRender of the original). */
public class RenderFireBat extends MobRenderer<EntityFireBat, ModelFireBat<EntityFireBat>> {
    private static final ResourceLocation rl = Thaumcraft.id("textures/entity/firebat.png");

    public RenderFireBat(EntityRendererProvider.Context rm) {
        super(rm, new ModelFireBat<>(), 0.25f);
    }

    @Override
    protected void scale(EntityFireBat bat, PoseStack pose, float partialTick) {
        pose.scale(0.35f, 0.35f, 0.35f);
    }

    @Override
    protected void setupRotations(EntityFireBat bat, PoseStack pose, float bob, float yBodyRot, float partialTick, float scale) {
        if (!bat.getIsBatHanging()) {
            pose.translate(0.0f, Mth.cos(bob * 0.3f) * 0.1f, 0.0f);
        } else {
            pose.translate(0.0f, -0.1f, 0.0f);
        }
        super.setupRotations(bat, pose, bob, yBodyRot, partialTick, scale);
    }

    @Override
    protected int getBlockLightLevel(EntityFireBat bat, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(EntityFireBat bat, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFireBat entity) {
        return rl;
    }
}
