package thaumcraft.client.renderers.models.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Port of ModelCentrifuge: the two fixed boxes and the spinning part between them. Texture 64x32. */
public class ModelCentrifuge {
    private final ModelPart crossbar;
    private final ModelPart dingus1;
    private final ModelPart dingus2;
    private final ModelPart core;
    private final ModelPart top;
    private final ModelPart bottom;

    public ModelCentrifuge() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("crossbar", CubeListBuilder.create().texOffs(16, 0).addBox(-4.0f, -1.0f, -1.0f, 8, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("dingus1", CubeListBuilder.create().texOffs(0, 16).addBox(4.0f, -3.0f, -2.0f, 4, 6, 4), PartPose.ZERO);
        root.addOrReplaceChild("dingus2", CubeListBuilder.create().texOffs(0, 16).addBox(-8.0f, -3.0f, -2.0f, 4, 6, 4), PartPose.ZERO);
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -4.0f, -1.5f, 3, 8, 3), PartPose.ZERO);
        root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(20, 16).addBox(-4.0f, -8.0f, -4.0f, 8, 4, 8), PartPose.ZERO);
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(20, 16).addBox(-4.0f, 4.0f, -4.0f, 8, 4, 8), PartPose.ZERO);
        ModelPart model = LayerDefinition.create(mesh, 64, 32).bakeRoot();
        crossbar = model.getChild("crossbar");
        dingus1 = model.getChild("dingus1");
        dingus2 = model.getChild("dingus2");
        core = model.getChild("core");
        top = model.getChild("top");
        bottom = model.getChild("bottom");
    }

    public void renderBoxes(PoseStack pose, VertexConsumer vc, int light, int overlay) {
        top.render(pose, vc, light, overlay);
        bottom.render(pose, vc, light, overlay);
    }

    public void renderSpinnyBit(PoseStack pose, VertexConsumer vc, int light, int overlay) {
        crossbar.render(pose, vc, light, overlay);
        dingus1.render(pose, vc, light, overlay);
        dingus2.render(pose, vc, light, overlay);
        core.render(pose, vc, light, overlay);
    }
}
