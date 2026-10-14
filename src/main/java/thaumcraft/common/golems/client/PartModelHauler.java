package thaumcraft.common.golems.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.PartModel;

public class PartModelHauler extends PartModel {
    public PartModelHauler(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }

    /** Draws the second carried stack in the carry frame. */
    @Override
    public void postRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side, PoseStack pose,
                                     MultiBufferSource buffers, int light) {
        if (golem.getCarrying().size() > 1) {
            ItemStack itemstack = golem.getCarrying().get(1);
            if (!itemstack.isEmpty()) {
                pose.pushPose();
                pose.scale(0.375f, 0.375f, 0.375f);
                pose.translate(0.0f, 0.33f, 0.825f);
                if (!(itemstack.getItem() instanceof BlockItem)) {
                    pose.translate(0.0f, 0.0f, -0.25f);
                }
                Minecraft.getInstance().getItemRenderer().renderStatic(golem.getGolemEntity(), itemstack, ItemDisplayContext.HEAD, false, pose,
                        buffers, golem.getGolemWorld(), light, OverlayTexture.NO_OVERLAY, golem.getGolemEntity().getId());
                pose.popPose();
            }
        }
    }
}
