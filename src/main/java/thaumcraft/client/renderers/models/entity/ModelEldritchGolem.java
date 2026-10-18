package thaumcraft.client.renderers.models.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;

/**
 * Port of ModelEldritchGolem (128x64 texture): the eldritch construct. Its legs and arms swing as it walks, both arms
 * slam down when it attacks, and the front cloth and cloak flap with its stride. Once headless it shows the small
 * neck piece instead of the head. The front cloth panels were mapped on a 114 wide texture in the original, which is
 * kept by scaling their texture width.
 */
public class ModelEldritchGolem extends HierarchicalModel<EntityEldritchGolem> {
    private static final float CLOTH_TEX = 114.0f / 128.0f;
    private final ModelPart root;
    private final ModelPart frontcloth1;
    private final ModelPart frontcloth2;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;
    private final ModelPart head;
    private final ModelPart head2;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart legR;
    private final ModelPart legL;

    public ModelEldritchGolem() {
        this(createLayer().bakeRoot());
    }

    private ModelEldritchGolem(ModelPart root) {
        this.root = root;
        frontcloth1 = root.getChild("frontcloth1");
        frontcloth2 = root.getChild("frontcloth2");
        cloak1 = root.getChild("cloak1");
        cloak2 = root.getChild("cloak2");
        cloak3 = root.getChild("cloak3");
        head = root.getChild("head");
        head2 = root.getChild("head2");
        armL = root.getChild("arm_l");
        armR = root.getChild("arm_r");
        legR = root.getChild("leg_r");
        legL = root.getChild("leg_l");
    }

    private static PartDefinition part(PartDefinition parent, String name, int u, int v, boolean mirror, float x, float y, float z, int w, int h, int d,
                                       float px, float py, float pz, float rx, float ry, float rz) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror(mirror).addBox(x, y, z, w, h, d),
                PartPose.offsetAndRotation(px, py, pz, rx, ry, rz));
    }

    private static void cloth(PartDefinition parent, String name, int u, int v, float x, float y, float z, int w, int h, int d,
                              float px, float py, float pz, float rx) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d, CubeDeformation.NONE, CLOTH_TEX, 1.0f),
                PartPose.offsetAndRotation(px, py, pz, rx, 0.0f, 0.0f));
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        part(parts, "cloak1", 0, 47, false, -5.0f, 1.5f, 4.0f, 10, 12, 1, 0.0f, 0.0f, -2.5f, 0.1396263f, 0.0f, 0.0f);
        part(parts, "cloak3", 0, 37, false, -5.0f, 17.5f, -0.8f, 10, 4, 1, 0.0f, 0.0f, -2.5f, 0.4465716f, 0.0f, 0.0f);
        part(parts, "cloak2", 0, 59, false, -5.0f, 13.5f, 1.7f, 10, 4, 1, 0.0f, 0.0f, -2.5f, 0.3069452f, 0.0f, 0.0f);
        part(parts, "cloak_cl", 0, 43, false, 3.0f, 0.5f, 2.0f, 2, 1, 3, 0.0f, 0.0f, -2.5f, 0.1396263f, 0.0f, 0.0f);
        part(parts, "cloak_cr", 0, 43, false, -5.0f, 0.5f, 2.0f, 2, 1, 3, 0.0f, 0.0f, -2.5f, 0.1396263f, 0.0f, 0.0f);
        part(parts, "head", 47, 12, false, -3.5f, -6.0f, -2.5f, 7, 7, 5, 0.0f, 4.5f, -3.8f, -0.1047198f, 0.0f, 0.0f);
        part(parts, "head2", 26, 16, false, -2.0f, -2.0f, -2.0f, 4, 4, 4, 0.0f, 0.0f, -5.0f, -0.1047198f, 0.0f, 0.0f);
        part(parts, "collar_l", 75, 50, false, 3.5f, -0.5f, -7.0f, 1, 4, 10, 0.0f, 0.0f, -2.5f, 0.837758f, 0.0f, 0.0f);
        part(parts, "collar_r", 67, 50, false, -4.5f, -0.5f, -7.0f, 1, 4, 10, 0.0f, 0.0f, -2.5f, 0.837758f, 0.0f, 0.0f);
        part(parts, "collar_b", 77, 59, false, -3.5f, -0.5f, 2.0f, 7, 4, 1, 0.0f, 0.0f, -2.5f, 0.837758f, 0.0f, 0.0f);
        part(parts, "collar_f", 77, 59, false, -3.5f, -0.5f, -7.0f, 7, 4, 1, 0.0f, 0.0f, -2.5f, 0.837758f, 0.0f, 0.0f);
        part(parts, "collar_black", 22, 0, false, -3.5f, 0.0f, -6.0f, 7, 1, 8, 0.0f, 0.0f, -2.5f, 0.837758f, 0.0f, 0.0f);
        cloth(parts, "frontcloth0", 114, 52, -3.0f, 3.2f, -3.5f, 6, 10, 1, 0.0f, 0.0f, -2.5f, 0.1745329f);
        cloth(parts, "frontcloth1", 114, 39, -1.0f, 1.5f, -3.5f, 6, 6, 1, -2.0f, 12.0f, 0.0f, -0.1047198f);
        cloth(parts, "frontcloth2", 114, 47, -1.0f, 8.5f, -1.5f, 6, 3, 1, -2.0f, 11.0f, 0.0f, -0.3316126f);
        part(parts, "torso", 34, 45, false, -5.0f, 2.5f, -3.0f, 10, 10, 6, 0.0f, 0.0f, -2.5f, 0.1745329f, 0.0f, 0.0f);
        PartDefinition armR = part(parts, "arm_r", 78, 32, false, -3.5f, 1.5f, -2.0f, 4, 13, 5, -5.0f, 3.0f, -2.0f, 0.0f, 0.0f, 0.1047198f);
        part(armR, "shoulder_r1", 0, 23, false, -3.3f, 4.0f, -2.5f, 1, 2, 6, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.186824f);
        part(armR, "shoulder_r", 0, 0, false, -4.3f, -1.0f, -3.0f, 4, 5, 7, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.186824f);
        part(armR, "shoulder_r2", 0, 12, false, -2.3f, 4.0f, -3.0f, 2, 3, 7, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.186824f);
        part(armR, "shoulder_r0", 56, 31, false, -4.5f, -1.5f, -2.5f, 5, 6, 6, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        PartDefinition armL = part(parts, "arm_l", 78, 32, true, -0.5f, 1.5f, -2.0f, 4, 13, 5, 5.0f, 3.0f, -2.0f, 0.0f, 0.0f, -0.1047198f);
        part(armL, "shoulder_l1", 0, 23, true, 2.3f, 4.0f, -2.5f, 1, 2, 6, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.186824f);
        part(armL, "shoulder_l0", 56, 31, true, -0.5f, -1.5f, -2.5f, 5, 6, 6, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        part(armL, "shoulder_l", 0, 0, true, 0.3f, -1.0f, -3.0f, 4, 5, 7, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.186824f);
        part(armL, "shoulder_l2", 0, 12, true, 0.3f, 4.0f, -3.0f, 2, 3, 7, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.186824f);
        part(parts, "backpanel_r1", 96, 7, false, 0.0f, 2.5f, -2.5f, 2, 2, 5, -2.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.1396263f);
        part(parts, "waist_r1", 96, 14, false, -3.0f, -0.5f, -2.5f, 5, 3, 5, -2.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.1396263f);
        part(parts, "waist_r2", 116, 13, false, -3.0f, 2.5f, -2.5f, 1, 4, 5, -2.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.1396263f);
        part(parts, "waist_r3", 114, 5, true, -2.0f, 2.5f, -2.5f, 2, 3, 5, -2.0f, 12.0f, 0.0f, 0.0f, 0.0f, 0.1396263f);
        part(parts, "leg_r", 79, 19, false, -2.5f, 2.5f, -2.0f, 4, 9, 4, -2.0f, 12.5f, 0.0f, 0.0f, 0.0f, 0.0f);
        part(parts, "waist_l1", 96, 14, false, -2.0f, -0.5f, -2.5f, 5, 3, 5, 2.0f, 12.0f, 0.0f, 0.0f, 0.0f, -0.1396263f);
        part(parts, "waist_l2", 116, 13, false, 2.0f, 2.5f, -2.5f, 1, 4, 5, 2.0f, 12.0f, 0.0f, 0.0f, 0.0f, -0.1396263f);
        part(parts, "waist_l3", 114, 5, false, 0.0f, 2.5f, -2.5f, 2, 3, 5, 2.0f, 12.0f, 0.0f, 0.0f, 0.0f, -0.1396263f);
        part(parts, "backpanel_l1", 96, 7, false, -2.0f, 2.5f, -2.5f, 2, 2, 5, 2.0f, 12.0f, 0.0f, 0.0f, 0.0f, -0.1396263f);
        part(parts, "leg_l", 79, 19, false, -1.5f, 2.5f, -2.0f, 4, 9, 4, 2.0f, 12.5f, 0.0f, 0.0f, 0.0f, 0.0f);
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    /** setLivingAnimations: the arms slam down while attacking, and swing as it walks otherwise. */
    @Override
    public void prepareMobModel(EntityEldritchGolem golem, float par1, float par2, float par3) {
        int i = golem.getAttackTimer();
        if (i > 0) {
            armR.xRot = -2.0f + 1.5f * doAbs(i - par3, 10.0f);
            armL.xRot = -2.0f + 1.5f * doAbs(i - par3, 10.0f);
        } else {
            armR.xRot = Mth.cos(par1 * 0.4f + 3.1415927f) * 2.0f * par2 * 0.5f;
            armL.xRot = Mth.cos(par1 * 0.4f) * 2.0f * par2 * 0.5f;
        }
    }

    private float doAbs(float p_78172_1_, float p_78172_2_) {
        return (Math.abs(p_78172_1_ % p_78172_2_ - p_78172_2_ * 0.5f) - p_78172_2_ * 0.25f) / (p_78172_2_ * 0.25f);
    }

    /** setRotationAngles, and the cloth that render() moved with the stride. */
    @Override
    public void setupAnim(EntityEldritchGolem entity, float par1, float par2, float par3, float par4, float par5) {
        if (entity.getSpawnTimer() > 0) {
            head.yRot = 0.0f;
            head.xRot = entity.getSpawnTimer() / 2 / 57.295776f;
        } else {
            head.yRot = par4 / 4.0f / 57.295776f;
            head.xRot = par5 / 2.0f / 57.295776f;
            head2.yRot = par4 / 57.295776f;
            head2.xRot = par5 / 57.295776f;
        }
        legR.xRot = Mth.cos(par1 * 0.4662f) * 1.4f * par2;
        legL.xRot = Mth.cos(par1 * 0.4662f + 3.1415927f) * 1.4f * par2;
        float a = Mth.cos(par1 * 0.44f) * 1.4f * par2;
        float b = Mth.cos(par1 * 0.44f + 3.1415927f) * 1.4f * par2;
        float c = Math.min(a, b);
        frontcloth1.xRot = c - 0.1047198f;
        frontcloth2.xRot = c - 0.3316126f;
        cloak1.xRot = -c / 3.0f + 0.1396263f;
        cloak2.xRot = -c / 3.0f + 0.3069452f;
        cloak3.xRot = -c / 3.0f + 0.4465716f;
        head.visible = !entity.isHeadless();
        head2.visible = entity.isHeadless();
    }
}
