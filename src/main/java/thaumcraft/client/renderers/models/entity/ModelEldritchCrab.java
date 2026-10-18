package thaumcraft.client.renderers.models.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import thaumcraft.common.entities.monster.EntityEldritchCrab;

/**
 * Port of ModelEldritchCrab (128x64 texture): a crab with two claws and four legs. Its tail is the helm while it
 * wears one, a bare shell otherwise. The legs scuttle while it walks and the claws snap all the time.
 */
public class ModelEldritchCrab extends HierarchicalModel<EntityEldritchCrab> {
    private final ModelPart root;
    private final ModelPart tailHelm;
    private final ModelPart tailBare;
    private final ModelPart rFLeg1;
    private final ModelPart rClaw1;
    private final ModelPart rClaw2;
    private final ModelPart lClaw2;
    private final ModelPart lClaw1;
    private final ModelPart rRLeg1;
    private final ModelPart lRLeg1;
    private final ModelPart lFLeg1;
    private final ModelPart rRLeg0;
    private final ModelPart rFLeg0;
    private final ModelPart lFLeg0;
    private final ModelPart lRLeg0;

    public ModelEldritchCrab() {
        this(createLayer().bakeRoot());
    }

    private ModelEldritchCrab(ModelPart root) {
        this.root = root;
        tailHelm = root.getChild("tail_helm");
        tailBare = root.getChild("tail_bare");
        rFLeg1 = root.getChild("rf_leg1");
        rClaw1 = root.getChild("r_claw1");
        rClaw2 = root.getChild("r_claw2");
        lClaw2 = root.getChild("l_claw2");
        lClaw1 = root.getChild("l_claw1");
        rRLeg1 = root.getChild("rr_leg1");
        lRLeg1 = root.getChild("lr_leg1");
        lFLeg1 = root.getChild("lf_leg1");
        rRLeg0 = root.getChild("rr_leg0");
        rFLeg0 = root.getChild("rf_leg0");
        lFLeg0 = root.getChild("lf_leg0");
        lRLeg0 = root.getChild("lr_leg0");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("tail_helm", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5f, -4.5f, -0.4f, 9, 9, 9),
                PartPose.offsetAndRotation(0.0f, 18.0f, 0.0f, 0.1047198f, 0.0f, 0.0f));
        parts.addOrReplaceChild("tail_bare", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0f, -4.0f, -0.4f, 8, 8, 8),
                PartPose.offsetAndRotation(0.0f, 18.0f, 0.0f, 0.1047198f, 0.0f, 0.0f));
        parts.addOrReplaceChild("r_claw1", CubeListBuilder.create().texOffs(0, 47).addBox(-2.0f, -1.0f, -5.066667f, 4, 3, 5),
                PartPose.offset(-6.0f, 15.5f, -10.0f));
        parts.addOrReplaceChild("head1", CubeListBuilder.create().texOffs(0, 38).addBox(-2.0f, -1.5f, -9.066667f, 4, 4, 1),
                PartPose.offset(0.0f, 18.0f, 0.0f));
        parts.addOrReplaceChild("r_claw0", CubeListBuilder.create().texOffs(0, 55).addBox(-2.0f, -2.5f, -3.066667f, 4, 5, 3),
                PartPose.offset(-6.0f, 17.0f, -7.0f));
        parts.addOrReplaceChild("r_claw2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5f, -1.0f, -4.066667f, 3, 2, 5),
                PartPose.offsetAndRotation(-6.0f, 18.5f, -10.0f, 0.3141593f, 0.0f, 0.0f));
        parts.addOrReplaceChild("r_arm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0f, -1.0f, -5.066667f, 2, 2, 6),
                PartPose.offsetAndRotation(-3.0f, 17.0f, -4.0f, 0.0f, 0.7504916f, 0.0f));
        parts.addOrReplaceChild("l_claw2", CubeListBuilder.create().texOffs(14, 54).addBox(-1.5f, -1.0f, -4.066667f, 3, 2, 5),
                PartPose.offsetAndRotation(6.0f, 18.5f, -10.0f, 0.3141593f, 0.0f, 0.0f));
        parts.addOrReplaceChild("l_claw1", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-2.0f, -1.0f, -5.066667f, 4, 3, 5),
                PartPose.offset(6.0f, 15.5f, -10.0f));
        parts.addOrReplaceChild("l_claw0", CubeListBuilder.create().texOffs(0, 55).mirror().addBox(-2.0f, -2.5f, -3.066667f, 4, 5, 3),
                PartPose.offset(6.0f, 17.0f, -7.0f));
        parts.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(44, 4).addBox(-1.0f, -1.0f, -4.066667f, 2, 2, 6),
                PartPose.offsetAndRotation(4.0f, 17.0f, -5.0f, 0.0f, -0.7504916f, 0.0f));
        parts.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -3.5f, -6.066667f, 7, 7, 6),
                PartPose.offsetAndRotation(0.0f, 18.0f, 0.0f, 0.0523599f, 0.0f, 0.0f));
        parts.addOrReplaceChild("head0", CubeListBuilder.create().texOffs(0, 31).addBox(-2.5f, -2.0f, -8.066667f, 5, 5, 2),
                PartPose.offset(0.0f, 18.0f, 0.0f));
        parts.addOrReplaceChild("rr_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(-4.5f, 1.0f, -0.9f, 2, 5, 2),
                PartPose.offset(-4.0f, 20.0f, -1.5f));
        parts.addOrReplaceChild("rf_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(-5.0f, 1.0f, -1.066667f, 2, 5, 2),
                PartPose.offset(-4.0f, 20.0f, -3.5f));
        parts.addOrReplaceChild("lr_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(2.5f, 1.0f, -0.9f, 2, 5, 2),
                PartPose.offset(4.0f, 20.0f, -1.5f));
        parts.addOrReplaceChild("lf_leg1", CubeListBuilder.create().texOffs(36, 4).addBox(3.0f, 1.0f, -1.066667f, 2, 5, 2),
                PartPose.offset(4.0f, 20.0f, -3.5f));
        parts.addOrReplaceChild("rr_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-4.5f, -1.0f, -0.9f, 6, 2, 2),
                PartPose.offset(-4.0f, 20.0f, -1.5f));
        parts.addOrReplaceChild("rf_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-5.0f, -1.0f, -1.066667f, 6, 2, 2),
                PartPose.offset(-4.0f, 20.0f, -3.5f));
        parts.addOrReplaceChild("lf_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-1.0f, -1.0f, -1.066667f, 6, 2, 2),
                PartPose.offset(4.0f, 20.0f, -3.5f));
        parts.addOrReplaceChild("lr_leg0", CubeListBuilder.create().texOffs(36, 0).addBox(-1.5f, -1.0f, -0.9f, 6, 2, 2),
                PartPose.offset(4.0f, 20.0f, -1.5f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    private static void setRotation(ModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    @Override
    public void setupAnim(EntityEldritchCrab entity, float par1, float par2, float par3, float par4, float par5) {
        tailHelm.visible = entity.hasHelm();
        tailBare.visible = !entity.hasHelm();
        setRotation(rRLeg1, 0.0f, 0.2094395f, 0.4363323f);
        setRotation(rFLeg1, 0.0f, -0.2094395f, 0.4363323f);
        setRotation(lRLeg1, 0.0f, -0.2094395f, -0.4363323f);
        setRotation(lFLeg1, 0.0f, 0.2094395f, -0.4363323f);
        setRotation(rRLeg0, 0.0f, 0.2094395f, 0.4363323f);
        setRotation(rFLeg0, 0.0f, -0.2094395f, 0.4363323f);
        setRotation(lFLeg0, 0.0f, 0.2094395f, -0.4363323f);
        setRotation(lRLeg0, 0.0f, -0.2094395f, -0.4363323f);
        float f9 = -(Mth.cos(par1 * 0.6662f * 2.0f + 0.0f) * 0.4f) * par2;
        float f10 = -(Mth.cos(par1 * 0.6662f * 2.0f + 3.1415927f) * 0.4f) * par2;
        rRLeg1.yRot += f9;
        rRLeg0.yRot += f9;
        lRLeg1.yRot += -f9;
        lRLeg0.yRot += -f9;
        rFLeg1.yRot += f10;
        rFLeg0.yRot += f10;
        lFLeg1.yRot += -f10;
        lFLeg0.yRot += -f10;
        rRLeg1.zRot += f9;
        rRLeg0.zRot += f9;
        lRLeg1.zRot += -f9;
        lRLeg0.zRot += -f9;
        rFLeg1.zRot += f10;
        rFLeg0.zRot += f10;
        lFLeg1.zRot += -f10;
        lFLeg0.zRot += -f10;
        float n = Mth.cos(par1 * 0.6662f) * 2.0f * par2 * 0.125f;
        tailHelm.yRot = n;
        tailBare.yRot = n;
        float n2 = Mth.cos(par1 * 0.6662f) * par2 * 0.125f;
        tailHelm.zRot = n2;
        tailBare.zRot = n2;
        rClaw2.xRot = 0.3141593f - Mth.sin(entity.tickCount / 4.0f) * 0.25f;
        lClaw2.xRot = 0.3141593f + Mth.sin(entity.tickCount / 4.1f) * 0.25f;
        rClaw1.xRot = Mth.sin(entity.tickCount / 4.0f) * 0.125f;
        lClaw1.xRot = -Mth.sin(entity.tickCount / 4.1f) * 0.125f;
    }
}
