package thaumcraft.client.renderers.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.client.renderers.models.entity.ModelPech;
import thaumcraft.common.entities.monster.EntityPech;

/** Port of LayerHeldItemPech: the held item sits a little lower and further forward in the short pech arms. */
public class LayerHeldItemPech extends ItemInHandLayer<EntityPech, ModelPech> {
    private final ItemInHandRenderer itemInHandRenderer;

    public LayerHeldItemPech(RenderLayerParent<EntityPech, ModelPech> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer, itemInHandRenderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    protected void renderArmWithItem(LivingEntity entity, ItemStack stack, ItemDisplayContext context, HumanoidArm arm, PoseStack pose,
            MultiBufferSource buffer, int light) {
        if (stack.isEmpty()) {
            return;
        }
        pose.pushPose();
        getParentModel().translateToHand(arm, pose);
        pose.translate(0.0f, -0.1f, 0.0625f);
        if (stack.getItem() instanceof BowItem) {
            pose.translate(-0.07500000298023224, -0.1, 0.0);
        }
        if (entity.isCrouching()) {
            pose.translate(0.0f, 0.2f, 0.0f);
        }
        pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
        pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        boolean flag = arm == HumanoidArm.LEFT;
        pose.translate(flag ? -0.0625f : 0.0625f, 0.125f, -0.625f);
        itemInHandRenderer.renderItem(entity, stack, context, flag, pose, buffer, light);
        pose.popPose();
    }
}
