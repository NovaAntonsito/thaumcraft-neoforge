package thaumcraft.client.renderers.models.entity;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import thaumcraft.common.entities.monster.EntityPech;

/**
 * Port of ModelPech: a squat biped with big jowls and a huge backpack (128x64 texture). The jowls flap while it walks
 * and when it mumbles. It is a HumanoidModel so held items go in its hands.
 */
public class ModelPech extends HumanoidModel<EntityPech> {
    private final ModelPart jowls;
    private final ModelPart lowerPack;
    private final ModelPart upperPack;

    public ModelPech() {
        this(createLayer().bakeRoot());
    }

    private ModelPech(ModelPart root) {
        super(root);
        jowls = root.getChild("jowls");
        lowerPack = root.getChild("lower_pack");
        upperPack = root.getChild("upper_pack");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(34, 12).addBox(-3.0f, 0.0f, 0.0f, 6, 10, 6),
                PartPose.offsetAndRotation(0.0f, 9.0f, -3.0f, 0.3129957f, 0.0f, 0.0f));
        parts.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(35, 1).mirror().addBox(-2.9f, 0.0f, 0.0f, 3, 6, 3),
                PartPose.offset(0.0f, 18.0f, 0.0f));
        parts.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(35, 1).addBox(-0.1f, 0.0f, 0.0f, 3, 6, 3),
                PartPose.offset(0.0f, 18.0f, 0.0f));
        parts.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 11).addBox(-3.5f, -5.0f, -5.0f, 7, 5, 5),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        // HumanoidModel wants a hat; the pech has none.
        parts.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 8.0f, 0.0f));
        parts.addOrReplaceChild("jowls", CubeListBuilder.create().texOffs(1, 21).addBox(-4.0f, -1.0f, -6.0f, 8, 3, 5),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        parts.addOrReplaceChild("lower_pack", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, 0.0f, 10, 5, 5),
                PartPose.offsetAndRotation(0.0f, 10.0f, 3.5f, 0.3013602f, 0.0f, 0.0f));
        parts.addOrReplaceChild("upper_pack", CubeListBuilder.create().texOffs(64, 1).addBox(-7.5f, -14.0f, 0.0f, 15, 14, 11),
                PartPose.offsetAndRotation(0.0f, 10.0f, 3.0f, 0.4537856f, 0.0f, 0.0f));
        parts.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 2).mirror().addBox(-2.0f, 0.0f, -1.0f, 2, 6, 2),
                PartPose.offset(-3.0f, 10.0f, -1.0f));
        parts.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 2).addBox(0.0f, 0.0f, -1.0f, 2, 6, 2),
                PartPose.offset(3.0f, 10.0f, -1.0f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(body, rightLeg, leftLeg, jowls, lowerPack, upperPack, rightArm, leftArm);
    }

    /** setRotationAngles of the original, which did not call the biped one. */
    @Override
    public void setupAnim(EntityPech entity, float par1, float par2, float par3, float par4, float par5) {
        head.yRot = par4 / 57.295776f;
        head.xRot = par5 / 57.295776f;
        float mumble = entity.mumble;
        jowls.yRot = head.yRot;
        jowls.xRot = head.xRot + (0.2617994f + Mth.cos(par1 * 0.6662f) * par2 * 0.25f) + 0.34906587f * Math.abs(Mth.sin(mumble / 8.0f));
        rightArm.xRot = Mth.cos(par1 * 0.6662f + 3.1415927f) * 2.0f * par2 * 0.5f;
        leftArm.xRot = Mth.cos(par1 * 0.6662f) * 2.0f * par2 * 0.5f;
        rightArm.zRot = 0.0f;
        leftArm.zRot = 0.0f;
        rightLeg.xRot = Mth.cos(par1 * 0.6662f) * 1.4f * par2;
        leftLeg.xRot = Mth.cos(par1 * 0.6662f + 3.1415927f) * 1.4f * par2;
        rightLeg.yRot = 0.0f;
        leftLeg.yRot = 0.0f;
        lowerPack.yRot = Mth.cos(par1 * 0.6662f) * 2.0f * par2 * 0.125f;
        lowerPack.zRot = Mth.cos(par1 * 0.6662f) * 2.0f * par2 * 0.125f;
        if (riding) {
            rightArm.xRot -= 0.62831855f;
            leftArm.xRot -= 0.62831855f;
            rightLeg.xRot = -1.2566371f;
            leftLeg.xRot = -1.2566371f;
            rightLeg.yRot = 0.31415927f;
            leftLeg.yRot = -0.31415927f;
        }
        rightArm.yRot = 0.0f;
        leftArm.yRot = 0.0f;
        // swingProgress > -9990 was always true.
        rightArm.yRot += body.yRot;
        leftArm.yRot += body.yRot;
        leftArm.xRot += body.yRot;
        float f6 = 1.0f - attackTime;
        f6 *= f6;
        f6 *= f6;
        f6 = 1.0f - f6;
        float f7 = Mth.sin(f6 * 3.1415927f);
        float f8 = Mth.sin(attackTime * 3.1415927f) * -(head.xRot - 0.7f) * 0.75f;
        rightArm.xRot -= (float) (f7 * 1.2 + f8);
        rightArm.yRot += body.yRot * 2.0f;
        rightArm.zRot = Mth.sin(attackTime * 3.1415927f) * -0.4f;
        if (entity.isCrouching()) {
            rightArm.xRot += 0.4f;
            leftArm.xRot += 0.4f;
        }
        rightArm.zRot += Mth.cos(par3 * 0.09f) * 0.05f + 0.05f;
        leftArm.zRot -= Mth.cos(par3 * 0.09f) * 0.05f + 0.05f;
        rightArm.xRot += Mth.sin(par3 * 0.067f) * 0.05f;
        leftArm.xRot -= Mth.sin(par3 * 0.067f) * 0.05f;
    }
}
