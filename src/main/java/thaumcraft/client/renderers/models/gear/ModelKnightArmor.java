package thaumcraft.client.renderers.models.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** The crimson cult plate armor. full (f >= 1) is the chest layer, with the plates, tabard and cloak. */
public class ModelKnightArmor extends ModelCustomArmor {

    public ModelKnightArmor(ModelPart root) {
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
        if (full) body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0f, 1.0f, -3.0f, 8, 7, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("frontcloth1", CubeListBuilder.create().texOffs(120, 39).addBox(0.0f, 0.0f, 0.0f, 6, 8, 1), PartPose.offsetAndRotation(-3.0f, 11.0f, -3.5f, -0.1047198f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("frontcloth2", CubeListBuilder.create().texOffs(100, 37).addBox(0.0f, 7.5f, 1.8f, 6, 3, 1), PartPose.offsetAndRotation(-3.0f, 11.0f, -3.5f, -0.3316126f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("tabbard", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0f, 1.2f, -3.5f, 6, 10, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0f, 1.0f, 2.0f, 8, 11, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(0.0f, 0.0f, 0.0f, 9, 12, 1), PartPose.offsetAndRotation(-4.5f, 1.3f, 4.2f, 0.1396263f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(0.0f, 11.7f, -2.0f, 9, 4, 1), PartPose.offsetAndRotation(-4.5f, 1.3f, 4.2f, 0.3069452f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(0, 59).addBox(0.0f, 15.2f, -4.2f, 9, 4, 1), PartPose.offsetAndRotation(-4.5f, 1.3f, 4.2f, 0.4465716f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloakatl", CubeListBuilder.create().texOffs(0, 43).addBox(2.5f, 1.0f, 2.0f, 2, 1, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.1396263f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("cloakatr", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5f, 1.0f, 2.0f, 2, 1, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.1396263f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("shoulderr", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5f, -2.5f, -2.5f, 5, 5, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("shoulderr0", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3f, -1.5f, -3.0f, 3, 5, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        rightArm.addOrReplaceChild("shoulderr1", CubeListBuilder.create().texOffs(0, 19).addBox(-3.3f, 3.5f, -2.5f, 1, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        rightArm.addOrReplaceChild("shoulderr2", CubeListBuilder.create().texOffs(0, 11).addBox(-2.3f, 3.5f, -3.0f, 1, 2, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        rightArm.addOrReplaceChild("gauntletr", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5f, 3.5f, -2.5f, 2, 6, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("gauntletstrapr1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5f, 3.5f, -2.5f, 3, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("gauntletstrapr2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5f, 6.5f, -2.5f, 3, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("shoulderl", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5f, -2.5f, -2.5f, 5, 5, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("shoulderl0", CubeListBuilder.create().texOffs(0, 0).addBox(1.3f, -1.5f, -3.0f, 3, 5, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        leftArm.addOrReplaceChild("shoulderl1", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3f, 3.5f, -2.5f, 1, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        leftArm.addOrReplaceChild("shoulderl2", CubeListBuilder.create().texOffs(0, 11).addBox(1.3f, 3.5f, -3.0f, 1, 2, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        leftArm.addOrReplaceChild("gauntletl", CubeListBuilder.create().texOffs(114, 26).addBox(1.5f, 3.5f, -2.5f, 2, 6, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("gauntletstrapl1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5f, 3.5f, -2.5f, 3, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("gauntletstrapl2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5f, 6.5f, -2.5f, 3, 1, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("sidepanelr0", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0f, -0.5f, -2.5f, 5, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1396263f));
        rightLeg.addOrReplaceChild("sidepanelr1", CubeListBuilder.create().texOffs(96, 7).mirror().addBox(0.0f, 2.5f, -2.5f, 2, 2, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1396263f));
        rightLeg.addOrReplaceChild("sidepanelr2", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0f, 2.5f, -2.5f, 2, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1396263f));
        rightLeg.addOrReplaceChild("sidepanelr3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0f, 2.5f, -2.5f, 1, 4, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1396263f));
        leftLeg.addOrReplaceChild("sidepanell0", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0f, -0.5f, -2.5f, 5, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1396263f));
        leftLeg.addOrReplaceChild("sidepanell1", CubeListBuilder.create().texOffs(96, 7).addBox(-2.0f, 2.5f, -2.5f, 2, 2, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1396263f));
        leftLeg.addOrReplaceChild("sidepanell2", CubeListBuilder.create().texOffs(114, 5).addBox(0.0f, 2.5f, -2.5f, 2, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1396263f));
        leftLeg.addOrReplaceChild("sidepanell3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0f, 2.5f, -2.5f, 1, 4, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1396263f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    protected void animateCloth(float c) {
        setX(body, "frontcloth1", c - 0.1047198f);
        setX(body, "frontcloth2", c - 0.3316126f);
        setX(body, "cloak1", -c / 2.0f + 0.1396263f);
        setX(body, "cloak2", -c / 2.0f + 0.3069452f);
        setX(body, "cloak3", -c / 2.0f + 0.4465716f);
    }
}
