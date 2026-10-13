package thaumcraft.client.renderers.models.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Port of ModelBoreBase. Only the brass nozzle is used so far (alembics and bellows), pointing along +X at half
 * height. TODO: the base and pillars, with the arcane bore. Texture 128x64.
 */
public class ModelBoreBase {
    private final ModelPart nozzle1;
    private final ModelPart nozzle2;

    public ModelBoreBase() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("nozzle1", CubeListBuilder.create().texOffs(106, 42).addBox(2.5f, -2.0f, -2.0f, 5, 4, 4),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        root.addOrReplaceChild("nozzle2", CubeListBuilder.create().texOffs(106, 51).addBox(7.0f, -2.5f, -2.5f, 1, 5, 5),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        ModelPart model = LayerDefinition.create(mesh, 128, 64).bakeRoot();
        nozzle1 = model.getChild("nozzle1");
        nozzle2 = model.getChild("nozzle2");
    }

    public void renderNozzle(PoseStack pose, VertexConsumer vc, int light, int overlay) {
        nozzle1.render(pose, vc, light, overlay);
        nozzle2.render(pose, vc, light, overlay);
    }
}
