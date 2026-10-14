package thaumcraft.client.renderers.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.common.entities.projectile.EntityGolemDart;

/** Port of RenderDart: a vanilla arrow at two thirds of its size (0.033 against the arrow's 0.05). */
public class RenderDart extends ArrowRenderer<EntityGolemDart> {
    private static final ResourceLocation ARROW = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public RenderDart(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityGolemDart entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.scale(0.66f, 0.66f, 0.66f);
        super.render(entity, yaw, partialTicks, pose, buffers, light);
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGolemDart entity) {
        return ARROW;
    }
}
