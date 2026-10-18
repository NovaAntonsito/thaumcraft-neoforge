package thaumcraft.client.renderers.models.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;

/**
 * Port of ModelEldritchGuardian (128x64 texture): a hooded, armored figure with no legs. Its loincloth panels and
 * cloak sway, its arms bob and lift when it casts. The hood_eye cube is the glowing eye of the eldritch warden
 * (stage 9d-4) and stays hidden on the guardian.
 */
public class ModelEldritchGuardian extends HierarchicalModel<Monster> {
    private final ModelPart root;
    private final ModelPart hoodEye;
    private final ModelPart hood1;
    private final ModelPart armL1;
    private final ModelPart armR1;
    private final ModelPart legpanelC1;
    private final ModelPart legpanelC2;
    private final ModelPart legpanelC3;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;
    private final ModelPart sidepanelL2;
    private final ModelPart sidepanelL3;
    private final ModelPart sidepanelL4;
    private final ModelPart sidepanelR2;
    private final ModelPart sidepanelR3;
    private final ModelPart sidepanelR4;

    public ModelEldritchGuardian() {
        this(createLayer().bakeRoot());
    }

    private ModelEldritchGuardian(ModelPart root) {
        this.root = root;
        hoodEye = root.getChild("hood_eye");
        hoodEye.visible = false;
        hood1 = root.getChild("hood1");
        armL1 = root.getChild("arm_l1");
        armR1 = root.getChild("arm_r1");
        legpanelC1 = root.getChild("legpanel_c1");
        legpanelC2 = legpanelC1.getChild("legpanel_c2");
        legpanelC3 = legpanelC2.getChild("legpanel_c3");
        cloak1 = root.getChild("cloak1");
        cloak2 = cloak1.getChild("cloak2");
        cloak3 = cloak2.getChild("cloak3");
        sidepanelL2 = root.getChild("sidepanel_l2");
        sidepanelL3 = sidepanelL2.getChild("sidepanel_l3");
        sidepanelL4 = sidepanelL3.getChild("sidepanel_l4");
        sidepanelR2 = root.getChild("sidepanel_r2");
        sidepanelR3 = sidepanelR2.getChild("sidepanel_r3");
        sidepanelR4 = sidepanelR3.getChild("sidepanel_r4");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0f, 4.0f, -3.0f, 1, 3, 6),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        parts.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0f, 8.0f, -3.0f, 8, 4, 1),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        parts.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0f, 8.0f, -3.0f, 1, 3, 6),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        parts.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0f, 8.0f, -3.0f, 1, 3, 6),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        parts.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0f, 4.0f, -3.0f, 1, 3, 6),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        parts.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0f, 1.0f, -4.0f, 8, 7, 2),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        parts.addOrReplaceChild("hood_eye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        PartDefinition hood1 = parts.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(40, 12).addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        hood1.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(36, 28).addBox(-3.5f, -8.7f, 2.0f, 7, 7, 3),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.2268928f, 0.0f, 0.0f));
        hood1.addOrReplaceChild("hood3", CubeListBuilder.create().texOffs(22, 19).addBox(-3.0f, -9.0f, 2.5f, 6, 6, 3),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.3490659f, 0.0f, 0.0f));
        hood1.addOrReplaceChild("hood4", CubeListBuilder.create().texOffs(40, 4).addBox(-2.5f, -9.7f, 3.5f, 5, 5, 3),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.5759587f, 0.0f, 0.0f));
        parts.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0f, 1.0f, 2.0f, 8, 11, 2),
                PartPose.offset(0.0f, -6.0f, 0.0f));
        parts.addOrReplaceChild("shoulderplate_top_r", CubeListBuilder.create().texOffs(110, 37).addBox(-5.5f, -2.5f, -3.5f, 2, 1, 7),
                PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f));
        parts.addOrReplaceChild("shoulderplate_r1", CubeListBuilder.create().texOffs(110, 45).addBox(3.5f, -1.5f, -3.5f, 1, 4, 7),
                PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f));
        parts.addOrReplaceChild("shoulderplate_r2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5f, 1.5f, -3.5f, 1, 3, 7),
                PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f));
        parts.addOrReplaceChild("shoulderplate_r3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5f, 3.5f, -3.5f, 1, 3, 7),
                PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f));
        parts.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5f, -2.5f, -2.5f, 5, 5, 5),
                PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.122173f, 0.0349066f));
        PartDefinition armL1 = parts.addOrReplaceChild("arm_l1", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0f, 2.5f, -1.5f, 4, 10, 5),
                PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.9599311f, -0.1047198f, -0.1919862f));
        armL1.addOrReplaceChild("arm_l2", CubeListBuilder.create().texOffs(76, 28).addBox(-1.0f, 9.5f, 3.5f, 4, 3, 3),
                PartPose.ZERO);
        armL1.addOrReplaceChild("arm_l3", CubeListBuilder.create().texOffs(76, 23).addBox(-1.0f, 6.5f, 3.5f, 4, 3, 2),
                PartPose.ZERO);
        PartDefinition armR1 = parts.addOrReplaceChild("arm_r1", CubeListBuilder.create().texOffs(72, 8).addBox(-3.0f, 2.5f, -1.5f, 4, 10, 5),
                PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.9599311f, 0.1047198f, 0.1919862f));
        armR1.addOrReplaceChild("arm_r2", CubeListBuilder.create().texOffs(76, 28).addBox(-3.0f, 9.5f, 3.5f, 4, 3, 3),
                PartPose.ZERO);
        armR1.addOrReplaceChild("arm_r3", CubeListBuilder.create().texOffs(76, 23).addBox(-3.0f, 6.5f, 3.5f, 4, 3, 2),
                PartPose.ZERO);
        parts.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5f, -2.5f, -2.5f, 5, 5, 5),
                PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.122173f, -0.0349066f));
        parts.addOrReplaceChild("shoulderplate_ltop", CubeListBuilder.create().texOffs(110, 37).addBox(3.5f, -2.5f, -3.5f, 2, 1, 7),
                PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f));
        parts.addOrReplaceChild("shoulderplate_l1", CubeListBuilder.create().texOffs(110, 45).addBox(-4.5f, -1.5f, -3.5f, 1, 4, 7),
                PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f));
        parts.addOrReplaceChild("shoulderplate_l2", CubeListBuilder.create().texOffs(94, 45).addBox(2.5f, 1.5f, -3.5f, 1, 3, 7),
                PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f));
        parts.addOrReplaceChild("shoulderplate_l3", CubeListBuilder.create().texOffs(94, 45).addBox(1.5f, 3.5f, -3.5f, 1, 3, 7),
                PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f));
        parts.addOrReplaceChild("legpanel_r4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0f, 0.5f, -3.5f, 2, 3, 1),
                PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("legpanel_r5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0f, 2.5f, -2.5f, 2, 3, 1),
                PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("legpanel_r6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0f, 4.5f, -1.5f, 2, 3, 1),
                PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0f, 0.5f, 2.5f, 5, 3, 1),
                PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("backpanel_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0f, 2.5f, 1.5f, 5, 3, 1),
                PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("backpanel_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0f, 4.5f, 0.5f, 5, 3, 1),
                PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("backpanel_l3", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0f, 4.5f, 0.5f, 5, 3, 1),
                PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("legpanel_l4", CubeListBuilder.create().texOffs(0, 43).addBox(1.0f, 0.5f, -3.5f, 2, 3, 1),
                PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("legpanel_l5", CubeListBuilder.create().texOffs(0, 47).addBox(1.0f, 2.5f, -2.5f, 2, 3, 1),
                PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("legpanel_l6", CubeListBuilder.create().texOffs(6, 43).addBox(1.0f, 4.5f, -1.5f, 2, 3, 1),
                PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0f, 0.5f, 2.5f, 5, 3, 1),
                PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("backpanel_l2", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0f, 2.5f, 1.5f, 5, 3, 1),
                PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        parts.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(0, 22).addBox(1.5f, 0.5f, -2.5f, 1, 4, 5),
                PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        parts.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5f, 0.5f, -2.5f, 1, 4, 5),
                PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        PartDefinition sidepanelR2 = parts.addOrReplaceChild("sidepanel_r2", CubeListBuilder.create().texOffs(0, 54).addBox(0.0f, 0.0f, -0.5f, 1, 5, 5),
                PartPose.offsetAndRotation(-4.5f, 9.5f, -2.0f, 0.0f, 0.0f, 0.122173f));
        PartDefinition sidepanelR3 = sidepanelR2.addOrReplaceChild("sidepanel_r3", CubeListBuilder.create().texOffs(0, 35).addBox(0.0f, 0.0f, -0.5f, 1, 3, 5),
                PartPose.offsetAndRotation(0.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.296706f));
        sidepanelR3.addOrReplaceChild("sidepanel_r4", CubeListBuilder.create().texOffs(24, 35).addBox(0.0f, 0.0f, -0.5f, 1, 3, 5),
                PartPose.offsetAndRotation(0.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.5235988f));
        PartDefinition sidepanelL2 = parts.addOrReplaceChild("sidepanel_l2", CubeListBuilder.create().texOffs(0, 54).addBox(0.0f, 0.0f, -0.5f, 1, 5, 5),
                PartPose.offsetAndRotation(4.5f, 9.5f, -2.0f, 0.0f, 0.0f, -0.122173f));
        PartDefinition sidepanelL3 = sidepanelL2.addOrReplaceChild("sidepanel_l3", CubeListBuilder.create().texOffs(0, 35).addBox(0.0f, 0.0f, -0.5f, 1, 3, 5),
                PartPose.offsetAndRotation(0.0f, 5.0f, 0.0f, 0.0f, 0.0f, -0.296706f));
        sidepanelL3.addOrReplaceChild("sidepanel_l4", CubeListBuilder.create().texOffs(24, 35).addBox(0.0f, 0.0f, -0.5f, 1, 3, 5),
                PartPose.offsetAndRotation(0.0f, 3.0f, 0.0f, 0.0f, 0.0f, -0.5235988f));
        PartDefinition legpanelC1 = parts.addOrReplaceChild("legpanel_c1", CubeListBuilder.create().texOffs(16, 45).addBox(-3.0f, 0.0f, -0.5f, 6, 8, 1),
                PartPose.offset(0.0f, 5.5f, -3.0f));
        PartDefinition legpanelC2 = legpanelC1.addOrReplaceChild("legpanel_c2", CubeListBuilder.create().texOffs(16, 54).addBox(-3.0f, 0.0f, -0.5f, 6, 4, 1),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        legpanelC2.addOrReplaceChild("legpanel_c3", CubeListBuilder.create().texOffs(32, 59).addBox(-3.0f, 0.0f, -0.5f, 6, 4, 1),
                PartPose.offset(0.0f, 4.0f, 0.0f));
        PartDefinition cloak1 = parts.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(106, 0).addBox(0.0f, 0.0f, -0.5f, 10, 18, 1),
                PartPose.offset(-5.0f, -6.0f, 4.0f));
        PartDefinition cloak2 = cloak1.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(106, 19).addBox(0.0f, 0.0f, -0.5f, 10, 4, 1),
                PartPose.offset(0.0f, 18.0f, 0.0f));
        cloak2.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(106, 24).addBox(0.0f, 0.0f, -0.5f, 10, 4, 1),
                PartPose.offset(0.0f, 4.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    /** setRotationAngles; par3 is ticksExisted plus the partial tick. */
    @Override
    public void setupAnim(Monster entity, float par1, float par2, float par3, float par4, float par5) {
        hood1.yRot = par4 / 57.295776f;
        hood1.xRot = par5 / 57.295776f;
        hoodEye.xRot = hood1.xRot;
        hoodEye.yRot = hood1.yRot;
        float alr = 0.0f;
        float all = 0.0f;
        if (entity instanceof EntityEldritchGuardian g) {
            alr = g.armLiftR;
            all = g.armLiftL;
        }
        armL1.xRot = -1.0f - all + Mth.sin((par3 + 20) / 10.0f) * 0.08f;
        armR1.xRot = -1.0f - alr + Mth.sin(par3 / 10.0f) * 0.08f;
        legpanelC1.xRot = -0.15f + Mth.sin(par3 / 8.0f) * 0.12f;
        legpanelC2.xRot = Mth.sin((par3 - 5.0f) / 8.0f) * 0.13f;
        legpanelC3.xRot = Mth.sin((par3 - 10.0f) / 8.0f) * 0.14f;
        cloak1.xRot = 0.2f + Mth.sin(par3 / 7.0f) * 0.08f;
        cloak2.xRot = Mth.sin((par3 - 5.0f) / 7.0f) * 0.1f;
        cloak3.xRot = Mth.sin((par3 - 10.0f) / 7.0f) * 0.12f;
        sidepanelL2.zRot = -0.2f + Mth.sin((par3 + 10) / 8.0f) * 0.12f;
        sidepanelL3.zRot = Mth.sin((par3 + 5.0f) / 8.0f) * 0.13f;
        sidepanelL4.zRot = Mth.sin(par3 / 8.0f) * 0.14f;
        sidepanelR2.zRot = 0.2f + Mth.sin((par3 - 5) / 8.0f) * 0.12f;
        sidepanelR3.zRot = Mth.sin((par3 - 10.0f) / 8.0f) * 0.13f;
        sidepanelR4.zRot = Mth.sin((par3 - 15.0f) / 8.0f) * 0.14f;
    }
}
