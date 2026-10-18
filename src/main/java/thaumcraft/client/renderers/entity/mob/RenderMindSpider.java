package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.world.entity.Entity;
import thaumcraft.client.lib.TintedBufferSource;
import thaumcraft.common.entities.monster.EntityMindSpider;

/**
 * The mind spider: a vanilla spider at 60% size, nearly invisible (at most 10% alpha, fading in over its first five
 * seconds) with its glowing eyes. Only its viewer sees it, when it has one.
 */
public class RenderMindSpider extends SpiderRenderer<EntityMindSpider> {
    public RenderMindSpider(EntityRendererProvider.Context ctx) {
        super(ctx);
        shadowRadius = 0.5f;
    }

    @Override
    protected void scale(EntityMindSpider entity, PoseStack pose, float partialTick) {
        pose.scale(0.6f, 0.6f, 0.6f);
    }

    @Override
    public void render(EntityMindSpider entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        if (entity.getViewer().isEmpty() || (viewer != null && entity.getViewer().equals(viewer.getName().getString()))) {
            float alpha = Math.min(0.1f, entity.tickCount / 100.0f);
            super.render(entity, yaw, partialTicks, pose, new TintedBufferSource(buffer, 1.0f, 1.0f, 1.0f, alpha), light);
        }
    }

    @Override
    protected RenderType getRenderType(EntityMindSpider entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
}
