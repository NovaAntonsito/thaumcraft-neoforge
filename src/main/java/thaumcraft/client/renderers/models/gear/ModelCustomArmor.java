package thaumcraft.client.renderers.models.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * Base of the Thaumcraft armor models. The 1.12 class copied the biped animation itself; now NeoForge
 * copies the wearer's pose into this model before rendering, so only the extras are left here:
 * the head drawn 1% bigger and the cloth parts that swing with the legs.
 */
public class ModelCustomArmor extends HumanoidModel<LivingEntity> {

    public ModelCustomArmor(ModelPart root) {
        super(root);
    }

    /** A biped with no cubes of its own (the 1.12 constructors cleared every cubeList), 128x64. */
    protected static MeshDefinition createEmptyBiped() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0f, 2.0f, 0.0f));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0f, 2.0f, 0.0f));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9f, 12.0f, 0.0f));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9f, 12.0f, 0.0f));
        return mesh;
    }

    /** The cloth swing of render(): min(cos(f*0.6662)*1.4*f1, cos(f*0.6662+PI)*1.4*f1), which are the leg angles. */
    protected float legSwing() {
        return riding ? 0.0f : Math.min(rightLeg.xRot, leftLeg.xRot);
    }

    /** Called when the model is picked for a wearer, before rendering: parts that depend on what they wear. */
    public void prepare(LivingEntity entity) {
    }

    /** Called before rendering, with the pose already copied from the wearer. */
    protected void animateCloth(float c) {
    }

    protected static void setX(ModelPart parent, String child, float rot) {
        if (parent.hasChild(child)) {
            parent.getChild(child).xRot = rot;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        animateCloth(legSwing());
        float s = young ? 1.0f : 1.01f;
        head.xScale = head.yScale = head.zScale = s;
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        head.xScale = head.yScale = head.zScale = 1.0f;
    }
}
