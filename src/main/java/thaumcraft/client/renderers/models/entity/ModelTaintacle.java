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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import thaumcraft.common.entities.monster.tainted.EntityTaintacle;

/**
 * Port of ModelTaintacle (the taintacle part; the taint seeds have their own model): a base cube with a chain of
 * cubes on top, each scaled to 0.88 of the one below it, ending in a small orb and a big bulb. ModelRendererTaintacle
 * scaled every child inside its parent; ModelPart cannot do that, so the chain is drawn by hand with the same
 * transforms.
 */
public class ModelTaintacle extends EntityModel<EntityTaintacle> {
    private static final float CHILD_SCALE = 0.88f;
    private final int length;
    private final ModelPart tentacle;
    private final ModelPart[] tents;
    private final ModelPart orb;
    private float height;
    private float grow;

    public ModelTaintacle(int length) {
        super(RenderType::entityTranslucent);
        this.length = length;
        ModelPart root = createLayer(length).bakeRoot();
        tentacle = root.getChild("tentacle");
        orb = root.getChild("orb");
        tents = new ModelPart[length];
        for (int k = 0; k < length; ++k) {
            tents[k] = root.getChild("tent" + k);
        }
    }

    public static LayerDefinition createLayer(int length) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("tentacle", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8), PartPose.ZERO);
        for (int k = 0; k < length - 1; ++k) {
            parts.addOrReplaceChild("tent" + k, CubeListBuilder.create().texOffs(0, 16).addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8), PartPose.ZERO);
        }
        parts.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0f, -2.0f, -2.0f, 4, 4, 4), PartPose.ZERO);
        parts.addOrReplaceChild("tent" + (length - 1), CubeListBuilder.create().texOffs(0, 32).addBox(-6.0f, -6.0f, -6.0f, 12, 12, 12), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(EntityTaintacle e, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float flail = e.flailIntensity;
        float ht = e.hurtTime;
        // par6 of 1.12 was the model scale, 0.0625.
        float mod = 0.0625f * 0.2f;
        float fs = (flail > 1.0f) ? 3.0f : (1.0f - mod);
        float fi = flail + ((ht > 0.0f) ? mod : (-mod));
        for (int k = 0; k < length - 1; ++k) {
            tents[k].xRot = 0.15f * fi * Mth.sin(ageInTicks * 0.1f * fs - k / 2.0f);
            tents[k].zRot = 0.1f / fi * Mth.sin(ageInTicks * 0.15f - k / 2.0f);
        }
        height = e.getBbHeight();
        float hc = height * 10.0f;
        grow = 0.0f;
        if (e.tickCount < hc) {
            grow = (hc - e.tickCount) / hc * height;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        pose.pushPose();
        pose.translate(0.0f, ((height == 3.0f) ? 0.6f : 1.2f) + grow, 0.0f);
        pose.scale(height / 3.0f, height / 3.0f, height / 3.0f);
        // The base: rotation point 12, no rotation.
        pose.pushPose();
        pose.translate(0.0f, 12.0f / 16.0f, 0.0f);
        renderCubes(tentacle, pose, vc, light, overlay, color);
        renderChild(0, pose, vc, light, overlay, color);
        pose.popPose();
        pose.popPose();
    }

    /** One link of the chain, scaled inside its parent, then its children. Every link has rotation point -8. */
    private void renderChild(int k, PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        pose.pushPose();
        pose.scale(CHILD_SCALE, CHILD_SCALE, CHILD_SCALE);
        pose.translate(0.0f, -8.0f / 16.0f, 0.0f);
        pose.mulPose(new Quaternionf().rotationZYX(tents[k].zRot, tents[k].yRot, tents[k].xRot));
        renderCubes(tents[k], pose, vc, light, overlay, color);
        if (k < length - 2) {
            renderChild(k + 1, pose, vc, light, overlay, color);
        } else if (k == length - 2) {
            renderTip(orb, pose, vc, light, overlay, color);
            renderTip(tents[length - 1], pose, vc, light, overlay, color);
        }
        pose.popPose();
    }

    /** The orb and the bulb at the end: scaled inside the last link, rotation point -8, no rotation. */
    private static void renderTip(ModelPart part, PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        pose.pushPose();
        pose.scale(CHILD_SCALE, CHILD_SCALE, CHILD_SCALE);
        pose.translate(0.0f, -8.0f / 16.0f, 0.0f);
        renderCubes(part, pose, vc, light, overlay, color);
        pose.popPose();
    }

    /** Draws a part's cubes with the current transform and none of its own. */
    private static void renderCubes(ModelPart part, PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        float xr = part.xRot, yr = part.yRot, zr = part.zRot;
        part.xRot = 0.0f;
        part.yRot = 0.0f;
        part.zRot = 0.0f;
        part.render(pose, vc, light, overlay, color);
        part.xRot = xr;
        part.yRot = yr;
        part.zRot = zr;
    }
}
