package thaumcraft.client.renderers.entity.mob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.entity.ModelTaintacle;
import thaumcraft.common.entities.monster.tainted.EntityTaintacle;

/** Port of RenderTaintacle: taintacle (10 links), taint tendril (6) and giant taintacle (14). */
public class RenderTaintacle extends MobRenderer<EntityTaintacle, ModelTaintacle> {
    private static final ResourceLocation rl = Thaumcraft.id("textures/entity/taintacle.png");

    public RenderTaintacle(EntityRendererProvider.Context rm, float shadow, int length) {
        super(rm, new ModelTaintacle(length), shadow);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintacle entity) {
        return rl;
    }
}
