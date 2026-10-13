package thaumcraft.client.renderers.models.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * The 1.12 bat model, kept because the Thaumcraft bat textures use its 64x64 layout (vanilla's bat changed in 1.20.3).
 * The hanging pose belongs to the fire bat and comes with it in stage 9. tint multiplies the color, so the spell bat
 * can be drawn in its spell's color.
 */
public class ModelFireBat<T extends Entity> extends EntityModel<T> {
    private final ModelPart batHead;
    private final ModelPart batBody;
    private final ModelPart batRightWing;
    private final ModelPart batLeftWing;
    private final ModelPart batOuterRightWing;
    private final ModelPart batOuterLeftWing;
    public int tint = -1;

    public ModelFireBat() {
        super(net.minecraft.client.renderer.RenderType::entityTranslucent);
        ModelPart root = createLayer().bakeRoot();
        batHead = root.getChild("head");
        batBody = root.getChild("body");
        batRightWing = batBody.getChild("right_wing");
        batLeftWing = batBody.getChild("left_wing");
        batOuterRightWing = batRightWing.getChild("outer_right_wing");
        batOuterLeftWing = batLeftWing.getChild("outer_left_wing");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        PartDefinition head = parts.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6, 6, 6), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(24, 0).addBox(-4.0f, -6.0f, -2.0f, 3, 4, 1), PartPose.ZERO);
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(1.0f, -6.0f, -2.0f, 3, 4, 1), PartPose.ZERO);
        PartDefinition body = parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0f, 4.0f, -3.0f, 6, 12, 6)
                .texOffs(0, 34).addBox(-5.0f, 16.0f, 0.0f, 10, 6, 1), PartPose.ZERO);
        PartDefinition rightWing = body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(42, 0).addBox(-12.0f, 1.0f, 1.5f, 10, 16, 1), PartPose.ZERO);
        rightWing.addOrReplaceChild("outer_right_wing", CubeListBuilder.create().texOffs(24, 16).addBox(-8.0f, 1.0f, 0.0f, 8, 12, 1),
                PartPose.offset(-12.0f, 1.0f, 1.5f));
        PartDefinition leftWing = body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(42, 0).mirror().addBox(2.0f, 1.0f, 1.5f, 10, 16, 1), PartPose.ZERO);
        leftWing.addOrReplaceChild("outer_left_wing", CubeListBuilder.create().texOffs(24, 16).mirror().addBox(0.0f, 1.0f, 0.0f, 8, 12, 1),
                PartPose.offset(12.0f, 1.0f, 1.5f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public int getBatSize() {
        return 36;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        batHead.xRot = headPitch / 57.295776f;
        batHead.yRot = netHeadYaw / 57.295776f;
        batHead.zRot = 0.0f;
        batHead.setPos(0.0f, 0.0f, 0.0f);
        batRightWing.setPos(0.0f, 0.0f, 0.0f);
        batLeftWing.setPos(0.0f, 0.0f, 0.0f);
        batBody.xRot = 0.7853982f + Mth.cos(ageInTicks * 0.1f) * 0.15f;
        batBody.yRot = 0.0f;
        batRightWing.yRot = Mth.cos(ageInTicks * 1.3f) * 3.1415927f * 0.25f;
        batLeftWing.yRot = -batRightWing.yRot;
        batOuterRightWing.yRot = batRightWing.yRot * 0.5f;
        batOuterLeftWing.yRot = -batRightWing.yRot * 0.5f;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        int c = FastColor.ARGB32.multiply(color, tint);
        batHead.render(pose, vc, light, overlay, c);
        batBody.render(pose, vc, light, overlay, c);
    }
}
