package thaumcraft.client.renderers.entity.mob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.entity.ModelTaintSeed;
import thaumcraft.common.entities.monster.tainted.EntityTaintSeed;

/** Port of RenderTaintSeed, for both seeds. Drawn see-through and without back-face culling, like the original. */
public class RenderTaintSeed extends MobRenderer<EntityTaintSeed, ModelTaintSeed> {
    private static final ResourceLocation rl = Thaumcraft.id("textures/entity/taintseed.png");

    public RenderTaintSeed(EntityRendererProvider.Context rm) {
        super(rm, new ModelTaintSeed(), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintSeed entity) {
        return rl;
    }
}
