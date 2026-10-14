package thaumcraft.common.golems.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thaumcraft.Thaumcraft;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemProperties;
import thaumcraft.api.golems.parts.PartModel;
import thaumcraft.client.lib.obj.ObjModel;
import thaumcraft.common.golems.EntityThaumcraftGolem;

/**
 * Port of RenderThaumcraftGolem: the golem is built from OBJ parts (golem_base plus the head, arms, legs and addon
 * models of its parts) with the original's procedural limb animation, drawn in the model's own (unflipped) space.
 * TODO (stage 8b): the faint outline drawn through walls while sneaking with a seal displayer in hand.
 */
@OnlyIn(Dist.CLIENT)
public class RenderThaumcraftGolem extends EntityRenderer<EntityThaumcraftGolem> {
    private final HashMap<Integer, HashMap<PartModel.EnumAttachPoint, ArrayList<PartModel>>> partsCache = new HashMap<>();
    private final ObjModel baseModel;
    private float swingProgress = 0.0f;

    public RenderThaumcraftGolem(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.3f;
        baseModel = ObjModel.load(Thaumcraft.id("models/obj/golem_base.obj"), true);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityThaumcraftGolem entity) {
        return entity.getProperties().getMaterial().texture;
    }

    @Override
    public void render(EntityThaumcraftGolem entity, float entityYaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        swingProgress = entity.getAttackAnim(partialTicks);
        float f2 = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f3 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float f4 = Mth.wrapDegrees(f3 - f2);
        float f6 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float f5 = entity.tickCount + partialTicks;
        // applyRotations
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - f2));
        if (entity.deathTime > 0) {
            float f = Mth.sqrt((entity.deathTime + partialTicks - 1.0f) / 20.0f * 1.6f);
            pose.mulPose(Axis.ZP.rotationDegrees(Math.min(f, 1.0f) * 90.0f));
        }
        float f7 = Math.min(entity.walkAnimation.speed(partialTicks), 1.0f);
        float f8 = entity.walkAnimation.position(partialTicks);
        boolean visible = !entity.isInvisible();
        boolean ghost = !visible && !entity.isInvisibleTo(Minecraft.getInstance().player);
        if (visible || ghost) {
            int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0f);
            renderParts(entity, f8, f7, f5, f4, f6, partialTicks, pose, buffers, light, overlay, ghost);
        }
        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, light);
    }

    private RenderType type(ResourceLocation texture, boolean ghost) {
        return ghost ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
    }

    private void renderParts(EntityThaumcraftGolem entity, float limbSwing, float prevLimbSwing, float rotFloat, float headPitch, float headYaw,
                             float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay, boolean ghost) {
        int white = ghost ? 0x26FFFFFF : 0xFFFFFFFF;
        ResourceLocation matTexture = entity.getProperties().getMaterial().texture;
        boolean holding = !entity.getMainHandItem().isEmpty();
        boolean aflag = entity.getProperties().hasTrait(EnumGolemTrait.WHEELED) || entity.getProperties().hasTrait(EnumGolemTrait.FLYER);
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        double speed = dx * dx + dz * dz;
        if (entity.redrawParts || !partsCache.containsKey(entity.getId())) {
            entity.redrawParts = false;
            createPartsCache(entity);
        }
        HashMap<PartModel.EnumAttachPoint, ArrayList<PartModel>> parts = partsCache.get(entity.getId());
        float f1;
        float bry = 0.0f;
        float rx = (float) Math.toDegrees(Mth.sin(rotFloat * 0.067f) * 0.03f);
        float rz = (float) Math.toDegrees(Mth.cos(rotFloat * 0.09f) * 0.05f + 0.05f);
        float rrx = 0.0f;
        float rry = 0.0f;
        float rrz = 0.0f;
        float rlx = 0.0f;
        float rly = 0.0f;
        float rlz = 0.0f;
        if (holding) {
            rrx = 90.0f - rz / 2.0f;
            rrz = -2.0f;
            rlx = 90.0f - rz / 2.0f;
            rlz = 2.0f;
        } else {
            if (aflag) {
                rrx = rx * 2.0f;
                rlx = -rx * 2.0f;
            } else {
                f1 = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * 2.0f * prevLimbSwing * 0.5f;
                rrx = (float) (Math.toDegrees(f1) + rx);
                f1 = Mth.cos(limbSwing * 0.6662f) * 2.0f * prevLimbSwing * 0.5f;
                rlx = (float) (Math.toDegrees(f1) - rx);
            }
            rrz += rz + 2.0f;
            rlz -= rz + 2.0f;
        }
        if (swingProgress > 0.0f) {
            float wiggle = -Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI * 2.0f) * 0.2f;
            bry = (float) Math.toDegrees(wiggle);
            rrz = -(float) Math.toDegrees(Mth.sin(wiggle) * 3.0f);
            rrx = (float) Math.toDegrees(-Mth.cos(wiggle) * 5.0f);
            rry += bry;
        }
        pose.mulPose(Axis.YP.rotationDegrees(bry));
        float lean = aflag ? 75.0f : 25.0f;
        pose.mulPose(Axis.XN.rotationDegrees((float) (speed * lean)));
        pose.mulPose(Axis.ZN.rotationDegrees((float) (speed * lean * 0.06 * (entity.getYRot() - entity.yRotO))));

        pose.pushPose();
        pose.translate(0.0, 0.5, 0.0);
        baseModel.renderPart("chest", pose, buffers.getBuffer(type(matTexture, ghost)), light, overlay, white);
        baseModel.renderPart("waist", pose, buffers.getBuffer(type(matTexture, ghost)), light, overlay, white);
        if (entity.getGolemColor() > 0) {
            int c = DyeColor.byId(entity.getGolemColor() - 1).getTextureDiffuseColor();
            baseModel.renderPart("flag", pose, buffers.getBuffer(type(matTexture, ghost)), light, overlay, (white & 0xFF000000) | (c & 0xFFFFFF));
        }
        for (PartModel part : parts.get(PartModel.EnumAttachPoint.BODY)) {
            renderPart(entity, part, matTexture, partialTicks, PartModel.EnumLimbSide.MIDDLE, pose, buffers, light, overlay, ghost, white);
        }
        pose.popPose();

        pose.pushPose();
        pose.translate(0.0, 0.75, -0.03125);
        pose.mulPose(Axis.YN.rotationDegrees(headPitch));
        pose.mulPose(Axis.XN.rotationDegrees(headYaw));
        for (PartModel part : parts.get(PartModel.EnumAttachPoint.HEAD)) {
            renderPart(entity, part, matTexture, partialTicks, PartModel.EnumLimbSide.MIDDLE, pose, buffers, light, overlay, ghost, white);
        }
        baseModel.renderPart("head", pose, buffers.getBuffer(type(matTexture, ghost)), light, overlay, white);
        pose.popPose();

        List<PartModel> arms = parts.get(PartModel.EnumAttachPoint.ARMS);
        pose.pushPose();
        pose.translate(0.20625, 0.6875, 0.0);
        if (!arms.isEmpty()) {
            PartModel part = arms.get(0);
            rrx = part.preRenderArmRotationX(entity, partialTicks, PartModel.EnumLimbSide.RIGHT, rrx);
            rry = part.preRenderArmRotationY(entity, partialTicks, PartModel.EnumLimbSide.RIGHT, rry);
            rrz = part.preRenderArmRotationZ(entity, partialTicks, PartModel.EnumLimbSide.RIGHT, rrz);
        }
        pose.mulPose(Axis.XP.rotationDegrees(rrx));
        pose.mulPose(Axis.YP.rotationDegrees(rry));
        pose.mulPose(Axis.ZP.rotationDegrees(rrz));
        baseModel.renderPart("arm", pose, buffers.getBuffer(type(matTexture, ghost)), light, overlay, white);
        for (PartModel part : arms) {
            renderPart(entity, part, matTexture, partialTicks, PartModel.EnumLimbSide.RIGHT, pose, buffers, light, overlay, ghost, white);
        }
        pose.popPose();

        pose.pushPose();
        pose.translate(-0.20625, 0.6875, 0.0);
        if (!arms.isEmpty()) {
            PartModel part = arms.get(0);
            rlx = part.preRenderArmRotationX(entity, partialTicks, PartModel.EnumLimbSide.LEFT, rlx);
            rly = part.preRenderArmRotationY(entity, partialTicks, PartModel.EnumLimbSide.LEFT, rly);
            rlz = part.preRenderArmRotationZ(entity, partialTicks, PartModel.EnumLimbSide.LEFT, rlz);
        }
        pose.mulPose(Axis.XP.rotationDegrees(rlx));
        pose.mulPose(Axis.YP.rotationDegrees(rly + 180.0f));
        pose.mulPose(Axis.ZN.rotationDegrees(rlz));
        baseModel.renderPart("arm", pose, buffers.getBuffer(type(matTexture, ghost)), light, overlay, white);
        for (PartModel part : arms) {
            renderPart(entity, part, matTexture, partialTicks, PartModel.EnumLimbSide.LEFT, pose, buffers, light, overlay, ghost, white);
        }
        pose.popPose();

        pose.pushPose();
        pose.translate(0.09375, 0.375, 0.0);
        f1 = Mth.cos(limbSwing * 0.6662f) * prevLimbSwing;
        pose.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(f1)));
        for (PartModel part : parts.get(PartModel.EnumAttachPoint.LEGS)) {
            renderPart(entity, part, matTexture, partialTicks, PartModel.EnumLimbSide.RIGHT, pose, buffers, light, overlay, ghost, white);
        }
        pose.popPose();

        pose.pushPose();
        pose.translate(-0.09375, 0.375, 0.0);
        f1 = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * prevLimbSwing;
        pose.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(f1)));
        for (PartModel part : parts.get(PartModel.EnumAttachPoint.LEGS)) {
            renderPart(entity, part, matTexture, partialTicks, PartModel.EnumLimbSide.LEFT, pose, buffers, light, overlay, ghost, white);
        }
        pose.popPose();

        pose.pushPose();
        pose.translate(0.0, 0.625, 0.0);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f - rz * 0.5f));
        drawHeldItem(entity, pose, buffers, light);
        pose.popPose();
    }

    private void drawHeldItem(EntityThaumcraftGolem entity, PoseStack pose, MultiBufferSource buffers, int light) {
        ItemStack itemstack = entity.getMainHandItem();
        if (!itemstack.isEmpty()) {
            pose.pushPose();
            pose.mulPose(Axis.XN.rotationDegrees(90.0f));
            pose.scale(0.375f, 0.375f, 0.375f);
            pose.translate(0.0f, 0.25f, -1.5f);
            if (!(itemstack.getItem() instanceof BlockItem)) {
                pose.translate(0.0f, -0.6f, 0.0f);
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(entity, itemstack, ItemDisplayContext.HEAD, false, pose, buffers,
                    entity.level(), light, OverlayTexture.NO_OVERLAY, entity.getId());
            pose.popPose();
        }
    }

    private void renderPart(EntityThaumcraftGolem golem, PartModel part, ResourceLocation matTexture, float partialTicks,
                            PartModel.EnumLimbSide side, PoseStack pose, MultiBufferSource buffers, int light, int overlay, boolean ghost, int color) {
        ObjModel model = ObjModel.load(part.getObjModel(), true);
        for (String op : model.getPartNames()) {
            pose.pushPose();
            ResourceLocation tex = part.useMaterialTextureForObjectPart(op) || part.getTexture() == null ? matTexture : part.getTexture();
            part.preRenderObjectPart(op, golem, partialTicks, side, pose);
            model.renderPart(op, pose, buffers.getBuffer(type(tex, ghost)), light, overlay, color);
            part.postRenderObjectPart(op, golem, partialTicks, side, pose, buffers, light);
            pose.popPose();
        }
    }

    private void createPartsCache(EntityThaumcraftGolem golem) {
        HashMap<PartModel.EnumAttachPoint, ArrayList<PartModel>> pl = new HashMap<>();
        for (PartModel.EnumAttachPoint p : PartModel.EnumAttachPoint.values()) {
            pl.put(p, new ArrayList<>());
        }
        IGolemProperties props = golem.getProperties();
        if (props.getHead().model != null) pl.get(props.getHead().model.getAttachPoint()).add(props.getHead().model);
        if (props.getArms().model != null) pl.get(props.getArms().model.getAttachPoint()).add(props.getArms().model);
        if (props.getLegs().model != null) pl.get(props.getLegs().model.getAttachPoint()).add(props.getLegs().model);
        if (props.getAddon().model != null) pl.get(props.getAddon().model.getAttachPoint()).add(props.getAddon().model);
        partsCache.put(golem.getId(), pl);
    }
}
