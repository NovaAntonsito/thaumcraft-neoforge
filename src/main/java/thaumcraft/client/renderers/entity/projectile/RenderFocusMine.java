package thaumcraft.client.renderers.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.client.renderers.models.entity.ModelGrappler;
import thaumcraft.common.entities.projectile.EntityFocusMine;

/** The focus mine: the grappler model, full-bright, blinking red while it arms. */
public class RenderFocusMine extends EntityRenderer<EntityFocusMine> {
    private static final ResourceLocation beam = Thaumcraft.id("textures/entity/mine.png");
    private final ModelGrappler model = new ModelGrappler();

    public RenderFocusMine(EntityRendererProvider.Context rm) {
        super(rm);
        shadowRadius = 0.0f;
    }

    @Override
    public void render(EntityFocusMine mine, float entityYaw, float pticks, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        float f = (mine.counter + pticks) % 8.0f / 8.0f;
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pticks, mine.yRotO, mine.getYRot()) - 90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pticks, mine.xRotO, mine.getXRot())));
        int color = FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f - f, 1.0f - f);
        model.render(pose, buffer.getBuffer(RenderType.entityTranslucent(beam)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
        pose.popPose();
        super.render(mine, entityYaw, pticks, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFocusMine entity) {
        return beam;
    }
}
