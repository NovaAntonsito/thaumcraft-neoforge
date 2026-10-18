package thaumcraft.client.renderers.models.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** The crimson praetor armor. full (f >= 1) is the chest layer, with the collar, chestplate, cloth and cloak. */
public class ModelLeaderArmor extends ModelCustomArmor {

    public ModelLeaderArmor(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer(boolean full) {
        MeshDefinition mesh = createEmptyBiped();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        PartDefinition rightArm = root.getChild("right_arm");
        PartDefinition leftArm = root.getChild("left_arm");
        PartDefinition rightLeg = root.getChild("right_leg");
        PartDefinition leftLeg = root.getChild("left_leg");
        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5f, -9.0f, -4.5f, 9, 9, 9), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0f, 8.0f, -3.0f, 8, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        body.addOrReplaceChild("mbeltl", CubeListBuilder.create().texOffs(76, 44).addBox(4.0f, 8.0f, -3.0f, 1, 3, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        body.addOrReplaceChild("mbeltr", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0f, 8.0f, -3.0f, 1, 3, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("beltl", CubeListBuilder.create().texOffs(76, 44).addBox(4.0f, 4.0f, -3.0f, 1, 3, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("beltr", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0f, 4.0f, -3.0f, 1, 3, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0f, 1.0f, -3.8f, 8, 7, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("chestornament", CubeListBuilder.create().texOffs(76, 53).addBox(-2.5f, 3.0f, -4.8f, 5, 5, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("chestclothr", CubeListBuilder.create().texOffs(20, 47).addBox(-4.5f, 1.2f, -4.5f, 3, 9, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0663225f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("chestclothl", CubeListBuilder.create().texOffs(20, 47).mirror().addBox(1.5f, 1.2f, -4.5f, 3, 9, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0663225f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("legclothr", CubeListBuilder.create().texOffs(20, 55).addBox(0.0f, 0.0f, 0.0f, 3, 8, 1), PartPose.offsetAndRotation(-4.5f, 10.4f, -3.9f, -0.0349066f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("legclothl", CubeListBuilder.create().texOffs(20, 55).mirror().addBox(0.0f, 0.0f, 0.0f, 3, 8, 1), PartPose.offsetAndRotation(1.5f, 10.4f, -3.9f, -0.0349066f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0f, 1.0f, 2.0f, 8, 11, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("collarb", CubeListBuilder.create().texOffs(17, 26).addBox(-4.5f, -1.5f, 7.0f, 9, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, -2.5f, 0.2268928f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("collarr", CubeListBuilder.create().texOffs(17, 11).addBox(-5.5f, -1.5f, -3.0f, 1, 4, 11), PartPose.offsetAndRotation(0.0f, 0.0f, -2.5f, 0.2268928f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("collarl", CubeListBuilder.create().texOffs(17, 11).addBox(4.5f, -1.5f, -3.0f, 1, 4, 11), PartPose.offsetAndRotation(0.0f, 0.0f, -2.5f, 0.2268928f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("collarf", CubeListBuilder.create().texOffs(17, 31).addBox(-4.5f, -1.5f, -3.0f, 9, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, -2.5f, 0.2268928f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-4.5f, 2.0f, 1.0f, 9, 12, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 3.0f, 0.1396263f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-4.5f, 14.0f, -1.3f, 9, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 3.0f, 0.3069452f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(0, 59).addBox(-4.5f, 17.0f, -3.7f, 9, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 3.0f, 0.4465716f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloaktl", CubeListBuilder.create().texOffs(0, 43).addBox(2.5f, 1.0f, -1.0f, 2, 1, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 3.0f, 0.1396263f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloaktr", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5f, 1.0f, -1.0f, 2, 1, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 3.0f, 0.1396263f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("shoulderr", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5f, -2.5f, -2.5f, 5, 5, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("shoulderr1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3f, -1.5f, -3.0f, 3, 5, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        rightArm.addOrReplaceChild("shoulderr2", CubeListBuilder.create().texOffs(0, 19).addBox(-3.3f, 3.5f, -2.5f, 1, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        rightArm.addOrReplaceChild("shoulderr3", CubeListBuilder.create().texOffs(0, 11).addBox(-2.3f, 3.5f, -3.0f, 1, 2, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        rightArm.addOrReplaceChild("shoulderr4", CubeListBuilder.create().texOffs(18, 4).addBox(-2.3f, -1.5f, -4.0f, 1, 6, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        rightArm.addOrReplaceChild("shoulderr5", CubeListBuilder.create().texOffs(18, 4).addBox(-2.3f, -1.5f, 3.0f, 1, 6, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        rightArm.addOrReplaceChild("gauntletr", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5f, 3.5f, -2.5f, 2, 6, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("gauntletr2", CubeListBuilder.create().texOffs(102, 37).addBox(-5.0f, 3.5f, -2.0f, 1, 5, 4), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1675516f));
        rightArm.addOrReplaceChild("gauntletstrapr1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5f, 3.5f, -2.5f, 3, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("gauntletstrapr2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5f, 6.5f, -2.5f, 3, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("shoulderl", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5f, -2.5f, -2.5f, 5, 5, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("shoulderl1", CubeListBuilder.create().texOffs(0, 0).addBox(1.3f, -1.5f, -3.0f, 3, 5, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        leftArm.addOrReplaceChild("shoulderl2", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3f, 3.5f, -2.5f, 1, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        leftArm.addOrReplaceChild("shoulderl3", CubeListBuilder.create().texOffs(0, 11).addBox(1.3f, 3.5f, -3.0f, 1, 2, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        leftArm.addOrReplaceChild("shoulderl4", CubeListBuilder.create().texOffs(18, 4).addBox(1.3f, -1.5f, -4.0f, 1, 6, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        leftArm.addOrReplaceChild("shoulderl5", CubeListBuilder.create().texOffs(18, 4).addBox(1.3f, -1.5f, 3.0f, 1, 6, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        leftArm.addOrReplaceChild("gauntletl", CubeListBuilder.create().texOffs(114, 26).addBox(1.5f, 3.5f, -2.5f, 2, 6, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("gauntletl2", CubeListBuilder.create().texOffs(102, 37).addBox(4.0f, 3.5f, -2.0f, 1, 5, 4), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1675516f));
        leftArm.addOrReplaceChild("gauntletstrapl1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5f, 3.5f, -2.5f, 3, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("gauntletstrapl2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5f, 6.5f, -2.5f, 3, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("backpanelr1", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0f, -0.5f, 2.5f, 5, 7, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0698132f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("backpanelr2", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0f, -0.5f, -2.5f, 5, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1396263f));
        rightLeg.addOrReplaceChild("backpanelr3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0f, 2.5f, -2.5f, 1, 4, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1396263f));
        rightLeg.addOrReplaceChild("backpanelr4", CubeListBuilder.create().texOffs(0, 25).mirror().addBox(-3.0f, -0.5f, -3.5f, 5, 7, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.0349066f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("backpanell1", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0f, -0.5f, 2.5f, 5, 7, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0698132f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("backpanell2", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0f, -0.5f, -2.5f, 5, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1396263f));
        leftLeg.addOrReplaceChild("backpanell3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0f, 2.5f, -2.5f, 1, 4, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1396263f));
        leftLeg.addOrReplaceChild("backpanell4", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0f, -0.5f, -3.5f, 5, 7, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.0349066f, 0.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** The leg cloths follow each leg (a and b of render()), the cloak the lower of the two. */
    @Override
    protected void animateCloth(float c) {
        setX(body, "legclothr", (riding ? 0.0f : rightLeg.xRot) - 0.1047198f);
        setX(body, "legclothl", (riding ? 0.0f : leftLeg.xRot) - 0.1047198f);
        setX(body, "cloak1", -c / 2.0f + 0.1396263f);
        setX(body, "cloak2", -c / 2.0f + 0.3069452f);
        setX(body, "cloak3", -c / 2.0f + 0.4465716f);
    }
}
