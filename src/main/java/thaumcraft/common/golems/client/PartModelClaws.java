package thaumcraft.common.golems.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.PartModel;

public class PartModelClaws extends PartModel {
    float f;

    public PartModelClaws(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
        f = 0.0f;
    }

    @Override
    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side, PoseStack pose) {
        if (partName.startsWith("claw")) {
            f = golem.getGolemEntity().getAttackAnim(partialTicks) * 4.1f;
            f *= f;
            pose.translate(0.0, -0.2, 0.0);
            pose.mulPose((partName.endsWith("1") ? Axis.XP : Axis.XN).rotationDegrees(f));
        }
    }
}
