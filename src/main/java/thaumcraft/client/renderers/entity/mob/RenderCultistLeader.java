package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.common.entities.monster.boss.EntityCultistLeader;

/** Port of RenderCultistLeader: the cultist biped (64x32 skin) at 1.15 size, with held items and armor. */
public class RenderCultistLeader extends HumanoidMobRenderer<EntityCultistLeader, HumanoidModel<EntityCultistLeader>> {
    private static final ResourceLocation skin = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/entity/cultist.png");

    public RenderCultistLeader(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(createBiped().bakeRoot()), 0.5f);
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(createBiped().bakeRoot()), new HumanoidModel<>(createBiped().bakeRoot()),
                context.getModelManager()));
    }

    /** new ModelBiped(): no inflation, 64x32. */
    private static LayerDefinition createBiped() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultistLeader entity) {
        return skin;
    }

    @Override
    protected void scale(EntityCultistLeader entity, PoseStack pose, float partialTickTime) {
        pose.scale(1.15f, 1.15f, 1.15f);
    }
}
