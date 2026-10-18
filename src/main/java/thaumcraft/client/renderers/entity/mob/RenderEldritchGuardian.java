package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.TintedBufferSource;
import thaumcraft.client.lib.render.TCRenderTypes;
import thaumcraft.client.renderers.models.entity.ModelEldritchGuardian;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;

/**
 * Port of RenderEldritchGuardian: the guardian is see-through, at most 60% opaque and fading out with distance
 * (fully gone at 32 blocks, 24 on hard). The eldritch warden is drawn solid with its own skin, rises out of the
 * ground while it spawns, and has a glowing eye.
 */
public class RenderEldritchGuardian extends MobRenderer<Monster, ModelEldritchGuardian> {
    private static final ResourceLocation[] SKIN = {Thaumcraft.id("textures/entity/eldritch_guardian.png"),
            Thaumcraft.id("textures/entity/eldritch_warden.png")};

    public RenderEldritchGuardian(EntityRendererProvider.Context ctx) {
        super(ctx, new ModelEldritchGuardian(), 0.5f);
        addLayer(new WardenEyeLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Monster entity) {
        return (entity instanceof EntityEldritchWarden) ? SKIN[1] : SKIN[0];
    }

    @Override
    public void render(Monster guardian, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
        float base = 1.0f;
        pose.pushPose();
        if (guardian instanceof EntityEldritchWarden warden) {
            pose.translate(0.0, -warden.getBbHeight() * (warden.getSpawnTimer() / 150.0f), 0.0);
        } else {
            Entity e = Minecraft.getInstance().getCameraEntity();
            if (e != null) {
                float d4 = (e.level().getDifficulty() == Difficulty.HARD) ? 576.0f : 1024.0f;
                float d5 = 256.0f;
                double d6 = guardian.distanceToSqr(e.getX(), e.getY(), e.getZ());
                if (d6 < 256.0) {
                    base = 0.6f;
                } else {
                    base = (float) (1.0 - Math.min(d4 - d5, d6 - d5) / (d4 - d5)) * 0.6f;
                }
            }
        }
        super.render(guardian, yaw, partialTicks, pose, new TintedBufferSource(buffer, 1.0f, 1.0f, 1.0f, base), light);
        pose.popPose();
    }

    @Nullable
    @Override
    protected RenderType getRenderType(Monster entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (bodyVisible || translucent) {
            return RenderType.entityTranslucent(getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    /** The warden's eye, added on top and lit to 195 to 225 of block light, pulsing with its age. */
    static class WardenEyeLayer extends RenderLayer<Monster, ModelEldritchGuardian> {
        WardenEyeLayer(RenderLayerParent<Monster, ModelEldritchGuardian> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffer, int light, Monster entity, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity instanceof EntityEldritchWarden && !entity.isInvisible()) {
                int j = (int) (195.0f + Mth.sin(entity.tickCount / 3.0f) * 15.0f + 15.0f);
                getParentModel().renderEye(pose, buffer.getBuffer(TCRenderTypes.entityGlow(SKIN[1])), j, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
