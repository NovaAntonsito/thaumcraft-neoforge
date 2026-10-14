package thaumcraft.common.golems.client;

import java.util.HashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.PartModel;

public class PartModelBreakers extends PartModel {
    private HashMap<Integer, Float[]> ani;

    public PartModelBreakers(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
        ani = new HashMap<>();
    }

    @Override
    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side, PoseStack pose) {
        if (partName.equals("grinder")) {
            float lastSpeed = 0.0f;
            float lastRot = 0.0f;
            int id = golem.getGolemEntity().getId();
            if (ani.containsKey(id)) {
                lastSpeed = ani.get(id)[0];
                lastRot = ani.get(id)[1];
            }
            float f = Math.max(lastSpeed, golem.getGolemEntity().getAttackAnim(partialTicks) * 20.0f);
            float rot = lastRot + f;
            lastSpeed = f * 0.99f;
            ani.put(id, new Float[]{lastSpeed, rot});
            pose.translate(0.0, -0.34, 0.0);
            pose.mulPose((side == EnumLimbSide.LEFT ? Axis.XN : Axis.XP)
                    .rotationDegrees((golem.getGolemEntity().tickCount + partialTicks) / 2.0f + rot + (side == EnumLimbSide.LEFT ? 22 : 0)));
        }
    }
}
