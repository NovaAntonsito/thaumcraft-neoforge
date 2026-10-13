package thaumcraft.client.renderers.entity.mob;

import java.awt.Color;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.entity.ModelFireBat;
import thaumcraft.common.entities.monster.EntitySpellBat;

/** The spell bat: a small, see-through bat in the average color of its spell's effects, always full-bright. */
public class RenderSpellBat extends MobRenderer<EntitySpellBat, ModelFireBat<EntitySpellBat>> {
    private static final ResourceLocation rl = Thaumcraft.id("textures/entity/spellbat.png");

    public RenderSpellBat(EntityRendererProvider.Context rm) {
        super(rm, new ModelFireBat<>(), 0.25f);
    }

    @Override
    public void render(EntitySpellBat bat, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        Color c = new Color(bat.color);
        model.tint = FastColor.ARGB32.color(127, c.getRed(), c.getGreen(), c.getBlue());
        super.render(bat, entityYaw, partialTicks, pose, buffer, packedLight);
        model.tint = -1;
    }

    @Override
    protected void scale(EntitySpellBat bat, PoseStack pose, float partialTick) {
        pose.scale(0.35f, 0.35f, 0.35f);
    }

    @Override
    protected void setupRotations(EntitySpellBat bat, PoseStack pose, float bob, float yBodyRot, float partialTick, float scale) {
        pose.translate(0.0f, -0.1f, 0.0f);
        super.setupRotations(bat, pose, bob, yBodyRot, partialTick, scale);
    }

    @Override
    protected int getBlockLightLevel(EntitySpellBat bat, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(EntitySpellBat bat, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpellBat entity) {
        return rl;
    }
}
