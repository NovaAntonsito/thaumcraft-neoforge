package thaumcraft.client.renderers.entity.mob;

import java.util.Random;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.entities.monster.cult.EntityCultistCleric;

/**
 * Port of RenderCultist: a plain biped (64x32 skin) with held items and armor. The armor layer uses uninflated
 * bipeds, like the original's LayerBipedArmor. A ritualist cleric bobs in the air with a purple wisp to its altar.
 */
public class RenderCultist extends HumanoidMobRenderer<EntityCultist, HumanoidModel<EntityCultist>> {
    private static final ResourceLocation skin = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/entity/cultist.png");
    private static final ResourceLocation fl = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/wispy.png");

    public RenderCultist(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(createBiped().bakeRoot()), 0.5f);
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(createBiped().bakeRoot()), new HumanoidModel<>(createBiped().bakeRoot()),
                context.getModelManager()));
    }

    /** new ModelBiped(): no inflation, 64x32. */
    private static LayerDefinition createBiped() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultist entity) {
        return skin;
    }

    @Override
    public void render(EntityCultist entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();
        float bob = 0.0f;
        boolean rit = entity instanceof EntityCultistCleric cleric && cleric.getIsRitualist();
        if (rit) {
            int val = new Random(entity.getId()).nextInt(1000);
            float c = entity.tickCount + partialTicks + val;
            bob = Mth.sin(c / 9.0f) * 0.1f + 0.21f;
            pose.translate(0.0, bob, 0.0);
        }
        super.render(entity, yaw, partialTicks, pose, buffer, light);
        if (rit) {
            Vec3 e = entity.getPosition(partialTicks);
            BlockPos home = entity.getRestrictCenter();
            pose.pushPose();
            pose.translate(home.getX() + 0.5 - e.x, home.getY() + 1.5 - bob - e.y, home.getZ() + 0.5 - e.z);
            drawFloatyLine(pose, buffer.getBuffer(RenderType.entityTranslucent(fl)), e.x, e.y + entity.getEyeHeight() * 1.2f, e.z,
                    home.getX() + 0.5, home.getY() + 1.5 - bob, home.getZ() + 0.5, 1114129, -0.03f, Math.min(entity.tickCount, 10) / 10.0f, 0.25f);
            pose.popPose();
        }
        pose.popPose();
    }

    /** Two crossed wavy ribbons from (x2, y2, z2), where the pose is, toward (x, y, z). The strips are drawn as quads. */
    private static void drawFloatyLine(PoseStack pose, VertexConsumer vc, double x, double y, double z, double x2, double y2, double z2,
            int color, float speed, float distance, float width) {
        float time = (float) (System.nanoTime() / 30000000L);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        double dc1x = (float) (x - x2);
        double dc1y = (float) (y - y2);
        double dc1z = (float) (z - z2);
        double d3 = x - x2;
        double d4 = y - y2;
        double d5 = z - z2;
        float dist = Mth.sqrt((float) (d3 * d3 + d4 * d4 + d5 * d5));
        float blocks = (float) Math.round(dist);
        float length = blocks * 6.0f;
        float f9 = 0.0f;
        float f10 = 1.0f;
        Matrix4f m = pose.last().pose();
        for (int pass = 0; pass < 2; ++pass) {
            float[] prev = null;
            for (int i = 0; i <= length * distance; ++i) {
                float f11 = i / length;
                float f12 = 1.0f - Math.abs(i - length / 2.0f) / (length / 2.0f);
                double dx3 = dc1x + Mth.sin((float) ((z % 16.0 + dist * (1.0f - f11) * 6.0f - time % 32767.0f / 5.0f) / 4.0)) * 0.5f * f12;
                double dy3 = dc1y + Mth.sin((float) ((x % 16.0 + dist * (1.0f - f11) * 6.0f - time % 32767.0f / 5.0f) / 3.0)) * 0.5f * f12;
                double dz3 = dc1z + Mth.sin((float) ((y % 16.0 + dist * (1.0f - f11) * 6.0f - time % 32767.0f / 5.0f) / 2.0)) * 0.5f * f12;
                float f13 = (1.0f - f11) * dist - time * speed;
                float px = (float) (dx3 * f11);
                float py = (float) (dy3 * f11);
                float pz = (float) (dz3 * f11);
                float[] cur = pass == 0
                        ? new float[] {px, py - width, pz, px, py + width, pz, f13}
                        : new float[] {px - width, py, pz, px + width, py, pz, f13};
                if (prev != null) {
                    vertex(vc, m, prev[0], prev[1], prev[2], prev[6], f10, r, g, b);
                    vertex(vc, m, prev[3], prev[4], prev[5], prev[6], f9, r, g, b);
                    vertex(vc, m, cur[3], cur[4], cur[5], cur[6], f9, r, g, b);
                    vertex(vc, m, cur[0], cur[1], cur[2], cur[6], f10, r, g, b);
                }
                prev = cur;
            }
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float z, float u, float v, float r, float g, float b) {
        vc.addVertex(m, x, y, z).setColor(r, g, b, 0.8f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(0.0f, 1.0f, 0.0f);
    }
}
