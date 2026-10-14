package thaumcraft.api.golems.parts;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.api.golems.IGolemAPI;

/**
 * An OBJ model attached to the golem by one of its parts. The render hooks get the pose stack the 1.12 version
 * changed through GlStateManager; postRenderObjectPart also gets the buffers and light to draw extra things.
 */
public class PartModel {
    private ResourceLocation objModel;
    private ResourceLocation texture;
    private EnumAttachPoint attachPoint;

    public PartModel(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        this.objModel = objModel;
        texture = objTexture;
        this.attachPoint = attachPoint;
    }

    public ResourceLocation getObjModel() {
        return objModel;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public EnumAttachPoint getAttachPoint() {
        return attachPoint;
    }

    public boolean useMaterialTextureForObjectPart(String partName) {
        return partName.startsWith("bm");
    }

    @OnlyIn(Dist.CLIENT)
    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side, PoseStack pose) {
    }

    @OnlyIn(Dist.CLIENT)
    public void postRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side, PoseStack pose,
                                     MultiBufferSource buffers, int light) {
    }

    public float preRenderArmRotationX(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        return inputRot;
    }

    public float preRenderArmRotationY(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        return inputRot;
    }

    public float preRenderArmRotationZ(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        return inputRot;
    }

    public enum EnumAttachPoint {
        ARMS,
        LEGS,
        BODY,
        HEAD
    }

    public enum EnumLimbSide {
        LEFT,
        RIGHT,
        MIDDLE
    }
}
