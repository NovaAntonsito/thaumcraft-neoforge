package thaumcraft.client.renderers.models.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** A small core with three crossed prongs: the focus mine (and later the grappling hook). */
public class ModelGrappler {
    private final ModelPart root;

    public ModelGrappler() {
        root = createLayer().bakeRoot();
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1.5f, -1.5f, 3, 3, 3), PartPose.ZERO);
        parts.addOrReplaceChild("prong1", CubeListBuilder.create().texOffs(0, 10).addBox(-0.5f, -0.5f, -2.5f, 1, 1, 5), PartPose.ZERO);
        parts.addOrReplaceChild("prong2", CubeListBuilder.create().texOffs(0, 10).addBox(-0.5f, -0.5f, -2.5f, 1, 1, 5),
                PartPose.rotation(0.0f, 1.5707964f, 0.0f));
        parts.addOrReplaceChild("prong3", CubeListBuilder.create().texOffs(0, 10).addBox(-0.5f, -0.5f, -2.5f, 1, 1, 5),
                PartPose.rotation(1.5707964f, 1.5707964f, 0.0f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void render(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        root.render(pose, vc, light, overlay, color);
    }
}
