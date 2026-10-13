package thaumcraft.client.renderers.models.block;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Port of ModelBellows. Texture 128x128 (the bag's later setTextureSize(64, 32) had no effect in 1.12: the box
 * UVs were already built).
 */
public class ModelBellows {
    public final ModelPart bottomPlank;
    public final ModelPart middlePlank;
    public final ModelPart topPlank;
    public final ModelPart bag;
    public final ModelPart nozzle;

    public ModelBellows() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom_plank", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, 0.0f, -6.0f, 12, 2, 12),
                PartPose.offset(0.0f, 22.0f, 0.0f));
        root.addOrReplaceChild("middle_plank", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -1.0f, -6.0f, 12, 2, 12),
                PartPose.offset(0.0f, 16.0f, 0.0f));
        root.addOrReplaceChild("top_plank", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, 0.0f, -6.0f, 12, 2, 12),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        root.addOrReplaceChild("bag", CubeListBuilder.create().texOffs(48, 0).addBox(-10.0f, -12.03333f, -10.0f, 20, 24, 20),
                PartPose.offset(0.0f, 16.0f, 0.0f));
        root.addOrReplaceChild("nozzle", CubeListBuilder.create().texOffs(0, 36).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 2),
                PartPose.offset(0.0f, 16.0f, 6.0f));
        ModelPart model = LayerDefinition.create(mesh, 128, 128).bakeRoot();
        bottomPlank = model.getChild("bottom_plank");
        middlePlank = model.getChild("middle_plank");
        topPlank = model.getChild("top_plank");
        bag = model.getChild("bag");
        nozzle = model.getChild("nozzle");
    }
}
