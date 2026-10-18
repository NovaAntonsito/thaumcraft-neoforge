package thaumcraft.client.renderers.models.block;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Port of ModelBanner: the cloth, its beam and two tabs, and the pole of a standing banner. Texture 128x64. */
public class ModelBanner {
    public final ModelPart b1;
    public final ModelPart b2;
    public final ModelPart beam;
    public final ModelPart banner;
    public final ModelPart pole;

    public ModelBanner() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("b1", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-5.0f, -7.5f, -1.5f, 2, 3, 3), PartPose.ZERO);
        root.addOrReplaceChild("b2", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(3.0f, -7.5f, -1.5f, 2, 3, 3), PartPose.ZERO);
        root.addOrReplaceChild("beam", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-7.0f, -7.0f, -1.0f, 14, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("banner", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.0f, 0.0f, -0.5f, 14, 28, 1),
                PartPose.offset(0.0f, -5.0f, 0.0f));
        root.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(62, 0).mirror().addBox(0.0f, 0.0f, -1.0f, 2, 31, 2),
                PartPose.offset(-1.0f, -7.0f, -2.0f));
        ModelPart model = LayerDefinition.create(mesh, 128, 64).bakeRoot();
        b1 = model.getChild("b1");
        b2 = model.getChild("b2");
        beam = model.getChild("beam");
        banner = model.getChild("banner");
        pole = model.getChild("pole");
    }
}
