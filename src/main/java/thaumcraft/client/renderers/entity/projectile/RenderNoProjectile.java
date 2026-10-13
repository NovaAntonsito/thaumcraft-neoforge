package thaumcraft.client.renderers.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import thaumcraft.common.entities.projectile.EntityFocusProjectile;

/** Draws nothing; a focus projectile is only its particle trail. */
public class RenderNoProjectile<T extends Entity> extends EntityRenderer<T> {
    public RenderNoProjectile(EntityRendererProvider.Context rm) {
        super(rm);
        shadowRadius = 0.1f;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity instanceof EntityFocusProjectile gp) {
            float qq = partialTicks - gp.lastRenderTick;
            if (qq < 0.0f) {
                ++qq;
            }
            if (qq > 0.2) {
                gp.renderParticle(partialTicks);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
