package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.entity.ModelEldritchCrab;
import thaumcraft.common.entities.monster.EntityEldritchCrab;

/** Port of RenderEldritchCrab: the crab model drawn at 0.8 scale. */
public class RenderEldritchCrab extends MobRenderer<EntityEldritchCrab, ModelEldritchCrab> {
    private static final ResourceLocation SKIN = Thaumcraft.id("textures/entity/crab.png");

    public RenderEldritchCrab(EntityRendererProvider.Context ctx) {
        super(ctx, new ModelEldritchCrab(), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchCrab entity) {
        return SKIN;
    }

    @Override
    protected void scale(EntityEldritchCrab entity, PoseStack pose, float partialTick) {
        pose.scale(0.8f, 0.8f, 0.8f);
        super.scale(entity, pose, partialTick);
    }
}
