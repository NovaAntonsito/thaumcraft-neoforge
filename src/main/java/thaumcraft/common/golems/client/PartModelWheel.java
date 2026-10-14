package thaumcraft.common.golems.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.PartModel;
import thaumcraft.common.golems.parts.GolemLegWheels;

public class PartModelWheel extends PartModel {
    public PartModelWheel(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }

    @Override
    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side, PoseStack pose) {
        if (partName.equals("wheel")) {
            float lastRot = GolemLegWheels.ani.getOrDefault(golem.getGolemEntity().getId(), 0.0f);
            pose.translate(0.0, -0.375, 0.0);
            pose.mulPose(Axis.XN.rotationDegrees(lastRot));
        }
    }
}
