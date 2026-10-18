package thaumcraft.client.renderers.entity.mob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import thaumcraft.Thaumcraft;

/** Port of RenderInhabitedZombie: a zombie with the husk skin. */
public class RenderInhabitedZombie extends ZombieRenderer {
    private static final ResourceLocation T1 = Thaumcraft.id("textures/entity/czombie.png");

    public RenderInhabitedZombie(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return T1;
    }
}
