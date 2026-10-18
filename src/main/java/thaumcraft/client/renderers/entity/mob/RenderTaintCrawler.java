package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.common.entities.monster.tainted.EntityTaintCrawler;

/** Port of RenderTaintCrawler: the silverfish model at 0.7 size with its own texture, flipping over 180 when it dies. */
public class RenderTaintCrawler extends MobRenderer<EntityTaintCrawler, SilverfishModel<EntityTaintCrawler>> {
    private static final ResourceLocation textures = Thaumcraft.id("textures/entity/crawler.png");

    public RenderTaintCrawler(EntityRendererProvider.Context rm) {
        super(rm, new SilverfishModel<>(rm.bakeLayer(ModelLayers.SILVERFISH)), 0.2f);
    }

    @Override
    protected float getFlipDegrees(EntityTaintCrawler entity) {
        return 180.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintCrawler entity) {
        return textures;
    }

    @Override
    protected void scale(EntityTaintCrawler entity, PoseStack pose, float partialTick) {
        pose.scale(0.7f, 0.7f, 0.7f);
    }
}
