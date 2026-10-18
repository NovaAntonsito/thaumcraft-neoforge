package thaumcraft.client.renderers.entity.mob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.entity.ModelPech;
import thaumcraft.common.entities.monster.EntityPech;

/** Port of RenderPech: one skin per kind of pech, and what it holds. */
public class RenderPech extends MobRenderer<EntityPech, ModelPech> {
    private static final ResourceLocation[] SKIN = {Thaumcraft.id("textures/entity/pech_forage.png"),
            Thaumcraft.id("textures/entity/pech_thaum.png"), Thaumcraft.id("textures/entity/pech_stalker.png")};

    public RenderPech(EntityRendererProvider.Context ctx) {
        super(ctx, new ModelPech(), 0.25f);
        addLayer(new LayerHeldItemPech(this, ctx.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPech entity) {
        return SKIN[Math.max(0, Math.min(SKIN.length - 1, entity.getPechType()))];
    }
}
