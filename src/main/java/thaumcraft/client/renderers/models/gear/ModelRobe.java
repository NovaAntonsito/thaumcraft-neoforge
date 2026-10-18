package thaumcraft.client.renderers.models.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** The crimson cult robe. full (f >= 1) is the chest layer; the other one has the skirt cloths and the foci pouch. */
public class ModelRobe extends ModelCustomArmor {

    public ModelRobe(ModelPart root) {
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
        head.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(16, 7).addBox(-4.5f, -9.0f, -4.6f, 9, 9, 9), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        head.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(52, 13).addBox(-4.0f, -9.7f, 2.0f, 8, 9, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.2268928f, 0.0f, 0.0f));
        head.addOrReplaceChild("hood3", CubeListBuilder.create().texOffs(52, 14).addBox(-3.5f, -10.0f, 3.5f, 7, 8, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.3490659f, 0.0f, 0.0f));
        head.addOrReplaceChild("hood4", CubeListBuilder.create().texOffs(53, 15).addBox(-3.0f, -10.7f, 3.5f, 6, 7, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.5759587f, 0.0f, 0.0f));
        body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0f, 7.0f, -3.0f, 8, 5, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        body.addOrReplaceChild("mbeltb", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0f, 7.0f, -4.0f, 8, 5, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 3.141593f, 0.0f));
        body.addOrReplaceChild("mbeltl", CubeListBuilder.create().texOffs(16, 36).addBox(4.0f, 8.0f, -3.0f, 1, 3, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        body.addOrReplaceChild("mbeltr", CubeListBuilder.create().texOffs(16, 36).addBox(-5.0f, 8.0f, -3.0f, 1, 3, 6), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (!full) leftLeg.addOrReplaceChild("focipouch", CubeListBuilder.create().texOffs(100, 20).addBox(3.5f, 0.5f, -2.5f, 3, 6, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.122173f));
        if (!full) body.addOrReplaceChild("frontclothr1", CubeListBuilder.create().texOffs(108, 38).addBox(0.0f, 0.0f, 0.0f, 3, 8, 1), PartPose.offsetAndRotation(-3.0f, 11.0f, -2.9f, -0.1047198f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("frontclothr2", CubeListBuilder.create().texOffs(108, 47).addBox(0.0f, 7.5f, 1.7f, 3, 3, 1), PartPose.offsetAndRotation(-3.0f, 11.0f, -2.9f, -0.3316126f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("frontclothl1", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(0.0f, 0.0f, 0.0f, 3, 8, 1), PartPose.offsetAndRotation(0.0f, 11.0f, -2.9f, -0.1047198f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("frontclothl2", CubeListBuilder.create().texOffs(108, 47).mirror().addBox(0.0f, 7.5f, 1.7f, 3, 3, 1), PartPose.offsetAndRotation(0.0f, 11.0f, -2.9f, -0.3316126f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("clothbackr1", CubeListBuilder.create().texOffs(118, 16).mirror().addBox(0.0f, 0.0f, 0.0f, 4, 8, 1), PartPose.offsetAndRotation(-4.0f, 11.5f, 2.9f, 0.1047198f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("clothbackr2", CubeListBuilder.create().texOffs(123, 9).addBox(0.0f, 7.8f, -0.9f, 1, 2, 1), PartPose.offsetAndRotation(-4.0f, 11.5f, 2.9f, 0.2268928f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("clothbackr3", CubeListBuilder.create().texOffs(120, 12).mirror().addBox(1.0f, 7.8f, -0.9f, 3, 3, 1), PartPose.offsetAndRotation(-4.0f, 11.5f, 2.9f, 0.2268928f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("clothbackl1", CubeListBuilder.create().texOffs(118, 16).addBox(0.0f, 0.0f, 0.0f, 4, 8, 1), PartPose.offsetAndRotation(0.0f, 11.5f, 2.9f, 0.1047198f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("clothbackl2", CubeListBuilder.create().texOffs(123, 9).mirror().addBox(3.0f, 7.8f, -0.9f, 1, 2, 1), PartPose.offsetAndRotation(0.0f, 11.5f, 2.9f, 0.2268928f, 0.0f, 0.0f));
        if (!full) body.addOrReplaceChild("clothbackl3", CubeListBuilder.create().texOffs(120, 12).addBox(0.0f, 7.8f, -0.9f, 3, 3, 1), PartPose.offsetAndRotation(0.0f, 11.5f, 2.9f, 0.2268928f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(16, 25).addBox(-4.0f, 1.0f, -3.0f, 8, 6, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("chestthing", CubeListBuilder.create().texOffs(56, 50).addBox(-2.5f, 1.0f, -4.0f, 5, 7, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(78, 25).addBox(-2.0f, 9.5f, 4.0f, 8, 3, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1919862f));
        if (full) body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0f, 1.0f, 1.9f, 8, 11, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("book", CubeListBuilder.create().texOffs(81, 16).addBox(1.0f, 0.0f, 4.0f, 5, 7, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7679449f));
        if (full) body.addOrReplaceChild("clothchestl", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(2.1f, 0.5f, -3.5f, 2, 8, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("clothchestr", CubeListBuilder.create().texOffs(108, 38).addBox(-4.1f, 0.5f, -3.5f, 2, 8, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("shoulderr", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.5f, -2.5f, -2.5f, 5, 5, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("rarm1", CubeListBuilder.create().texOffs(88, 39).addBox(-3.5f, 2.5f, -2.5f, 5, 7, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("rarm2", CubeListBuilder.create().texOffs(76, 32).addBox(-3.0f, 5.5f, 2.5f, 4, 4, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("rarm3", CubeListBuilder.create().texOffs(88, 32).addBox(-2.5f, 3.5f, 2.5f, 3, 2, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        rightArm.addOrReplaceChild("shoulderplatetopr", CubeListBuilder.create().texOffs(56, 25).addBox(-5.5f, -2.5f, -3.5f, 2, 1, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightArm.addOrReplaceChild("shoulderplater1", CubeListBuilder.create().texOffs(56, 33).addBox(-4.5f, -1.5f, -3.5f, 1, 4, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightArm.addOrReplaceChild("shoulderplater2", CubeListBuilder.create().texOffs(40, 33).addBox(-3.5f, 1.5f, -3.5f, 1, 3, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightArm.addOrReplaceChild("shoulderplater3", CubeListBuilder.create().texOffs(40, 33).addBox(-2.5f, 3.5f, -3.5f, 1, 3, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        leftArm.addOrReplaceChild("shoulderl", CubeListBuilder.create().texOffs(16, 45).addBox(-1.5f, -2.5f, -2.5f, 5, 5, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("larm1", CubeListBuilder.create().texOffs(88, 39).mirror().addBox(-1.5f, 2.5f, -2.5f, 5, 7, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("larm2", CubeListBuilder.create().texOffs(76, 32).addBox(-1.0f, 5.5f, 2.5f, 4, 4, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("larm3", CubeListBuilder.create().texOffs(88, 32).addBox(-0.5f, 3.5f, 2.5f, 3, 2, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        leftArm.addOrReplaceChild("shoulderplatetopl", CubeListBuilder.create().texOffs(56, 25).addBox(3.5f, -2.5f, -3.5f, 2, 1, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftArm.addOrReplaceChild("shoulderplatel1", CubeListBuilder.create().texOffs(56, 33).addBox(3.5f, -1.5f, -3.5f, 1, 4, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftArm.addOrReplaceChild("shoulderplatel2", CubeListBuilder.create().texOffs(40, 33).addBox(2.5f, 1.5f, -3.5f, 1, 3, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftArm.addOrReplaceChild("shoulderplatel3", CubeListBuilder.create().texOffs(40, 33).addBox(1.5f, 3.5f, -3.5f, 1, 3, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        rightLeg.addOrReplaceChild("legpanelr4", CubeListBuilder.create().texOffs(76, 38).addBox(-3.0f, 0.5f, -3.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("legpanelr5", CubeListBuilder.create().texOffs(76, 42).addBox(-3.0f, 2.5f, -2.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("legpanelr6", CubeListBuilder.create().texOffs(82, 38).addBox(-3.0f, 4.5f, -1.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("sidepanelr1", CubeListBuilder.create().texOffs(116, 25).addBox(-2.5f, 0.5f, -2.5f, 1, 4, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightLeg.addOrReplaceChild("sideclothr1", CubeListBuilder.create().texOffs(116, 42).addBox(-2.5f, 0.5f, -2.5f, 1, 5, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.122173f));
        rightLeg.addOrReplaceChild("sideclothr2", CubeListBuilder.create().texOffs(116, 34).addBox(-1.5f, 5.5f, -2.5f, 1, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.296706f));
        rightLeg.addOrReplaceChild("sideclothr3", CubeListBuilder.create().texOffs(116, 1).addBox(0.4f, 8.4f, -2.5f, 1, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5235988f));
        leftLeg.addOrReplaceChild("legpanell4", CubeListBuilder.create().texOffs(76, 38).mirror().addBox(1.0f, 0.5f, -3.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("legpanell5", CubeListBuilder.create().texOffs(76, 42).mirror().addBox(1.0f, 2.5f, -2.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("legpanell6", CubeListBuilder.create().texOffs(82, 38).mirror().addBox(1.0f, 4.5f, -1.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("sidepanell1", CubeListBuilder.create().texOffs(116, 25).addBox(1.5f, 0.5f, -2.5f, 1, 4, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftLeg.addOrReplaceChild("sideclothl1", CubeListBuilder.create().texOffs(116, 42).addBox(1.5f, 0.5f, -2.5f, 1, 5, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.122173f));
        leftLeg.addOrReplaceChild("sideclothl2", CubeListBuilder.create().texOffs(116, 34).addBox(0.5f, 5.5f, -2.5f, 1, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.296706f));
        leftLeg.addOrReplaceChild("sideclothl3", CubeListBuilder.create().texOffs(116, 1).addBox(-1.4f, 8.4f, -2.5f, 1, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.5235988f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    protected void animateCloth(float c) {
        setX(body, "frontclothr1", c - 0.1047198f);
        setX(body, "frontclothl1", c - 0.1047198f);
        setX(body, "frontclothr2", c - 0.3316126f);
        setX(body, "frontclothl2", c - 0.3316126f);
        setX(body, "clothbackr1", -c + 0.1047198f);
        setX(body, "clothbackl1", -c + 0.1047198f);
        setX(body, "clothbackr2", -c + 0.2268928f);
        setX(body, "clothbackl2", -c + 0.2268928f);
        setX(body, "clothbackr3", -c + 0.2268928f);
        setX(body, "clothbackl3", -c + 0.2268928f);
    }
}
