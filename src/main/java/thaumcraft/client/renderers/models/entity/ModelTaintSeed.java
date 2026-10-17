package thaumcraft.client.renderers.models.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
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
import thaumcraft.common.entities.monster.tainted.EntityTaintSeed;

/**
 * Port of ModelTaintSeed: an orb with a chain of seven pieces growing out of it, each a bit smaller and swaying.
 * ModelRendererTaintSeed scaled every link of the chain inside its parent, and the scale pulses; ModelPart cannot do
 * that, so the chain is drawn by hand here, link by link, with the same transforms.
 */
public class ModelTaintSeed extends EntityModel<EntityTaintSeed> {
    private static final int LENGTH = 8;
    private final ModelPart tentacle;
    private final ModelPart[] tents = new ModelPart[LENGTH - 1];
    private final float[] pivotY = new float[LENGTH - 1];
    private float height;
    private float grow;
    private float ticks;

    public ModelTaintSeed() {
        super(RenderType::entityTranslucent);
        ModelPart root = createLayer().bakeRoot();
        tentacle = root.getChild("tentacle");
        for (int k = 0; k < LENGTH - 1; ++k) {
            tents[k] = root.getChild("tent" + k);
            pivotY[k] = (k < LENGTH - 4 || k == LENGTH - 4) ? -8.0f : -4.0f;
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("tentacle", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8), PartPose.ZERO);
        for (int k = 0; k < LENGTH - 1; ++k) {
            int v = (k < LENGTH - 4) ? 16 : ((k == LENGTH - 4) ? 48 : 56);
            CubeListBuilder cube = CubeListBuilder.create().texOffs(0, v);
            if (k < LENGTH - 4) {
                cube.addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8);
            } else {
                cube.addBox(-2.0f, -2.0f, -2.0f, 4, 4, 4);
            }
            parts.addOrReplaceChild("tent" + k, cube, PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(EntityTaintSeed seed, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float ht = seed.hurtTime / 200.0f;
        float flail = 0.1f;
        // par6 of 1.12 was the model scale, 0.0625.
        float mod = 0.0625f * 0.2f;
        float fi = flail + ((ht > 0.0f) ? mod : (-mod));
        fi *= 3.0f;
        for (int k = 0; k < LENGTH - 1; ++k) {
            tents[k].xRot = 0.1f / fi * Mth.sin(ageInTicks * 0.06f - k / 2.0f) / 5.0f + ht + seed.attackAnim;
            tents[k].zRot = 0.1f / fi * Mth.sin(ageInTicks * 0.05f - k / 2.0f) / 5.0f;
        }
        height = seed.getBbHeight();
        float hc = height * 10.0f;
        grow = 0.0f;
        if (seed.tickCount < hc) {
            grow = (hc - seed.tickCount) / hc * height;
        }
        ticks = seed.tickCount + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        pose.pushPose();
        pose.translate(0.0f, ((height == 3.0f) ? 0.6f : 1.2f) + grow, 0.0f);
        pose.scale(height / 2.0f, height / 2.0f, height / 2.0f);
        // The orb: rotation point 12, no rotation.
        pose.translate(0.0f, 12.0f / 16.0f, 0.0f);
        renderCubes(tentacle, pose, vc, light, overlay, color);
        int pushes = 0;
        for (int k = 0; k < LENGTH - 1; ++k) {
            // Each link is scaled inside its parent with the scale of the parent's place in the chain.
            float qq = (float) (Math.PI * (k / 7.0f));
            float scale = 1.6f - (float) Math.sin(qq);
            float pulse = (float) Math.sin(ticks / 12.0f - qq) * 0.33f;
            pulse *= pulse;
            pose.pushPose();
            ++pushes;
            pose.scale(scale + pulse, scale * 0.9f, scale + pulse);
            pose.translate(0.0f, pivotY[k] / 16.0f, 0.0f);
            pose.mulPose(new Quaternionf().rotationZYX(tents[k].zRot, tents[k].yRot, tents[k].xRot));
            renderCubes(tents[k], pose, vc, light, overlay, color);
        }
        for (int i = 0; i < pushes; ++i) {
            pose.popPose();
        }
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
