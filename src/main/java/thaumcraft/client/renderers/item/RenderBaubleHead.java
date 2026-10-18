package thaumcraft.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * onPlayerBaubleRender of the goggles and the headband of curiosity: a 16x16 texture over the face, given some depth
 * like a held item, and pushed out a bit more over a helmet.
 * 1.21: placed on the model's head instead of the Baubles head helpers; the size (0.55 of a block) is the original's.
 */
public class RenderBaubleHead implements ICurioRenderer {
    private static final float SIZE = 0.55f;
    private static final float DEPTH = 0.1f * SIZE;

    private final ResourceLocation texture;

    public RenderBaubleHead(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack pose,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers, int light, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> model)) {
            return;
        }
        boolean armor = !slotContext.entity().getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        pose.pushPose();
        model.head.translateAndRotate(pose);
        // Head space: y down, the face at z = -4/16. The texture is centered on the head.
        pose.translate(-SIZE / 2.0f, -0.25f - SIZE / 2.0f, -0.25f - 0.01f - (armor ? 0.12f * SIZE : 0.0f));
        renderTextureIn3D(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light);
        pose.popPose();
    }

    /** UtilsFX.renderTextureIn3D(0, 0, 1, 1, 16, 16, 0.1): the sprite, its back, and a side for each pixel row and column. */
    private static void renderTextureIn3D(PoseStack pose, VertexConsumer vc, int light) {
        PoseStack.Pose p = pose.last();
        float s = SIZE;
        // Front (toward -z, the way the face looks) and back.
        quad(vc, p, light, 0, 0, 0, s, 0, 0, s, s, 0, 0, s, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, -1);
        quad(vc, p, light, 0, s, DEPTH, s, s, DEPTH, s, 0, DEPTH, 0, 0, DEPTH, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1);
        int n = 16;
        float px = s / n;
        float half = 0.5f / n;
        for (int i = 0; i < n; ++i) {
            float f = i * px;
            float u = (float) i / n + half;
            // Columns: x sides.
            quad(vc, p, light, f, 0, 0, f, s, 0, f, s, DEPTH, f, 0, DEPTH, u, 0, u, 1, u, 1, u, 0, -1, 0, 0);
            quad(vc, p, light, f + px, 0, DEPTH, f + px, s, DEPTH, f + px, s, 0, f + px, 0, 0, u, 0, u, 1, u, 1, u, 0, 1, 0, 0);
            // Rows: y sides.
            quad(vc, p, light, 0, f, DEPTH, s, f, DEPTH, s, f, 0, 0, f, 0, 0, u, 1, u, 1, u, 0, u, 0, -1, 0);
            quad(vc, p, light, 0, f + px, 0, s, f + px, 0, s, f + px, DEPTH, 0, f + px, DEPTH, 0, u, 1, u, 1, u, 0, u, 0, 1, 0);
        }
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose p, int light,
            float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
            float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4, float nx, float ny, float nz) {
        vertex(vc, p, light, x1, y1, z1, u1, v1, nx, ny, nz);
        vertex(vc, p, light, x2, y2, z2, u2, v2, nx, ny, nz);
        vertex(vc, p, light, x3, y3, z3, u3, v3, nx, ny, nz);
        vertex(vc, p, light, x4, y4, z4, u4, v4, nx, ny, nz);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose p, int light, float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        vc.addVertex(p, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }
}
