package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.TintedBufferSource;
import thaumcraft.common.entities.monster.EntityGiantBrainyZombie;

/** The angry zombies. The furious one is drawn bigger and redder the angrier it is. */
public class RenderBrainyZombie extends ZombieRenderer {
    private static final ResourceLocation ZOMBIE_TEXTURES = Thaumcraft.id("textures/entity/bzombie.png");

    public RenderBrainyZombie(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return ZOMBIE_TEXTURES;
    }

    @Override
    public void render(Zombie entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
        if (entity instanceof EntityGiantBrainyZombie z) {
            float q = Math.min(1.0f, z.getAnger()) / 2.0f;
            buffer = new TintedBufferSource(buffer, 1.0f, 1.0f - q, 1.0f - q, 1.0f);
        }
        super.render(entity, yaw, partialTicks, pose, buffer, light);
    }

    @Override
    protected void scale(Zombie entity, PoseStack pose, float partialTick) {
        super.scale(entity, pose, partialTick);
        if (entity instanceof EntityGiantBrainyZombie z) {
            pose.scale(1.0f + z.getAnger(), 1.0f + z.getAnger(), 1.0f + z.getAnger());
        }
    }
}
