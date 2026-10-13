package thaumcraft.client.renderers.models.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Port of ModelTubeValve: the valve handle (ring) and its rod, sticking out along +Y. Texture 64x32. */
public class ModelTubeValve {
    private final ModelPart valveRod;
    private final ModelPart valveRing;

    public ModelTubeValve() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("rod", CubeListBuilder.create().texOffs(0, 10).addBox(-1.0f, 2.0f, -1.0f, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("ring", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, 4.0f, -2.0f, 4, 1, 4), PartPose.ZERO);
        ModelPart model = LayerDefinition.create(mesh, 64, 32).bakeRoot();
        valveRod = model.getChild("rod");
        valveRing = model.getChild("ring");
    }

    public void renderRod(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        valveRod.render(pose, vc, light, overlay, color);
    }

    public void renderRing(PoseStack pose, VertexConsumer vc, int light, int overlay) {
        valveRing.render(pose, vc, light, overlay);
    }
}
