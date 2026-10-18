package thaumcraft.client.renderers.entity.mob;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.entity.ModelEldritchGolem;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;

/** Port of RenderEldritchGolem: the eldritch construct model at 1.7 times its size, blended. */
public class RenderEldritchGolem extends MobRenderer<EntityEldritchGolem, ModelEldritchGolem> {
    private static final ResourceLocation SKIN = Thaumcraft.id("textures/entity/eldritch_golem.png");

    public RenderEldritchGolem(EntityRendererProvider.Context ctx) {
        super(ctx, new ModelEldritchGolem(), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEldritchGolem entity) {
        return SKIN;
    }

    @Override
    protected void scale(EntityEldritchGolem entity, PoseStack pose, float partialTicks) {
        pose.scale(1.7f, 1.7f, 1.7f);
    }

    @Nullable
    @Override
    protected RenderType getRenderType(EntityEldritchGolem entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (bodyVisible || translucent) {
            return RenderType.entityTranslucent(getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }
}
