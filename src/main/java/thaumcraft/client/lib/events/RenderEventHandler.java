package thaumcraft.client.lib.events;

import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.blocks.devices.BlockRedstoneRelay;
import thaumcraft.common.tiles.devices.TileRedstoneRelay;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import com.mojang.datafixers.util.Either;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.client.lib.AspectTooltip;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.aspects.ObjectAspects;
import thaumcraft.common.items.tools.ItemThaumometer;
import thaumcraft.common.lib.research.ScanManager;

/**
 * Partial port of RenderEventHandler with Reborn's patches: the aspects of scanned things. Item tooltips in
 * container screens and tags floating over the creature or block the thaumometer points at are shown only once
 * the thing has been scanned (isThingStillScannable is false).
 */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class RenderEventHandler {
    public static float tagscale;
    @Nullable
    public static Entity thaumTarget;

    private RenderEventHandler() {
    }

    // ---- Tooltips (tooltipEvent / renderAspectsInGui) ----

    @SubscribeEvent
    static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AspectTooltip.class, AspectTooltip.Client::new);
    }

    @SubscribeEvent
    static void tooltipEvent(RenderTooltipEvent.GatherComponents event) {
        Minecraft mc = Minecraft.getInstance();
        ItemStack stack = event.getItemStack();
        if (!(mc.screen instanceof AbstractContainerScreen) || Screen.hasShiftDown() == Config.SHOW_TAGS.getAsBoolean()
                || mc.mouseHandler.isMouseGrabbed() || stack.isEmpty() || mc.player == null
                || ScanningManager.isThingStillScannable(mc.player, stack)) {
            return;
        }
        AspectList tags = ObjectAspects.get(stack);
        if (tags != null && tags.size() > 0) {
            event.getTooltipElements().add(Either.right(new AspectTooltip(tags)));
        }
    }

    // ---- World tags (renderLast / Reborn's blockHighlight) ----

    @SubscribeEvent
    static void renderLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (tagscale > 0.0f) {
            tagscale -= 0.005f;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof Player player) || mc.level == null) return;
        drawRelayText(mc, event.getPoseStack(), event.getCamera().getPosition());
        boolean holding = player.getMainHandItem().getItem() instanceof ItemThaumometer
                || player.getOffhandItem().getItem() instanceof ItemThaumometer;
        if (!holding) {
            thaumTarget = null;
            return;
        }
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PoseStack pose = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        if (thaumTarget != null && thaumTarget.isAlive() && !ScanningManager.isThingStillScannable(player, thaumTarget)) {
            AspectList ot = ScanManager.objScanAspects(player, thaumTarget);
            if (ot != null && ot.size() > 0) {
                growTags();
                Vec3 p = thaumTarget.getPosition(partialTicks);
                drawTagsOnContainer(pose, buffers, cam, p.x, p.y + thaumTarget.getBbHeight(), p.z, ot, null);
            }
        } else if (mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                && !ScanningManager.isThingStillScannable(player, hit.getBlockPos())) {
            BlockPos pos = hit.getBlockPos();
            AspectList ot = ScanManager.objScanAspects(player, pos);
            if (ot != null && ot.size() > 0) {
                boolean spaceAbove = mc.level.isEmptyBlock(pos.above());
                Direction dir = spaceAbove ? Direction.UP : hit.getDirection();
                growTags();
                drawTagsOnContainer(pose, buffers, cam, pos.getX() + dir.getStepX() / 2.0f, pos.getY() + dir.getStepY() / 2.0f,
                        pos.getZ() + dir.getStepZ() / 2.0f, ot, dir);
            }
        }
        buffers.endBatch();
    }

    /** The redstone relay's "Out: n" or "In: n" over the torch the player looks at. */
    private static void drawRelayText(Minecraft mc, PoseStack pose, Vec3 cam) {
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockRedstoneRelay) || !(mc.level.getBlockEntity(pos) instanceof TileRedstoneRelay te)) return;
        int sub = BlockRedstoneRelay.getSubHit(state, pos, hit.getLocation());
        if (sub < 0) return;
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        drawTextInAir(pose, buffers, cam, pos.getX(), pos.getY() + 0.3, pos.getZ(), sub == 0 ? "Out: " + te.getOut() : "In: " + te.getIn());
        buffers.endBatch();
    }

    /** drawTextInAir: a line of text at the middle of a block spot, turned toward the viewer. */
    public static void drawTextInAir(PoseStack pose, MultiBufferSource buffers, Vec3 cam, double x, double y, double z, String text) {
        pose.pushPose();
        pose.translate(-cam.x + x + 0.5, -cam.y + y + 0.5, -cam.z + z + 0.5);
        float xd = (float) (cam.x - (x + 0.5));
        float zd = (float) (cam.z - (z + 0.5));
        float rotYaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
        // rotYaw + 180 and the 180 roll of the original come down to this turn and a flipped Y.
        pose.mulPose(Axis.YP.rotationDegrees(rotYaw));
        pose.scale(0.0125f, -0.0125f, 0.0125f);
        Font font = Minecraft.getInstance().font;
        int sw = font.width(text);
        font.drawInBatch(text, 1 - sw / 2.0f, 1.0f, 0xFFFFFF, true, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0,
                LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    private static void growTags() {
        if (tagscale < 0.5f) {
            tagscale += 0.031f - tagscale / 10.0f;
        }
    }

    /** drawTagsOnContainer: rows of five icons with their amounts, turned toward the viewer. */
    public static void drawTagsOnContainer(PoseStack pose, MultiBufferSource buffers, Vec3 cam, double x, double y, double z,
                                           AspectList tags, @Nullable Direction dir) {
        if (tags == null || tags.size() == 0) return;
        int fox = 0, foy = 0, foz = 0;
        if (dir != null) {
            fox = dir.getStepX();
            foy = dir.getStepY();
            foz = dir.getStepZ();
        } else {
            x -= 0.5;
            z -= 0.5;
        }
        Font font = Minecraft.getInstance().font;
        int light = LightTexture.FULL_BRIGHT;
        int rowsize = 5;
        int current = 0;
        float shifty = 0.0f;
        int left = tags.size();
        for (Aspect tag : tags.getAspects()) {
            int div = Math.min(left, rowsize);
            if (current >= rowsize) {
                current = 0;
                shifty -= tagscale * 1.05f;
                left -= rowsize;
                if (left < rowsize) {
                    div = left % rowsize;
                }
            }
            float shift = (current - div / 2.0f + 0.5f) * tagscale * 4.0f;
            shift *= tagscale;
            pose.pushPose();
            pose.translate(-cam.x + x + 0.5 + tagscale * 2.0f * fox, -cam.y + y - shifty + 0.5 + tagscale * 2.0f * foy,
                    -cam.z + z + 0.5 + tagscale * 2.0f * foz);
            float xd = (float) (cam.x - (x + 0.5));
            float zd = (float) (cam.z - (z + 0.5));
            float rotYaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
            // Our quad faces -Z, so after this turn it faces the viewer without the original's extra roll.
            pose.mulPose(Axis.YP.rotationDegrees(rotYaw + 180.0f));
            pose.translate(-shift, 0.0, 0.0);
            pose.scale(tagscale, tagscale, tagscale);
            UtilsFX.drawTag(pose, buffers, tag, 1.0f, light);
            if (tags.getAmount(tag) >= 0) {
                String am = "" + tags.getAmount(tag);
                pose.translate(0.0, 0.0, -0.01);
                pose.mulPose(Axis.YP.rotationDegrees(180.0f));
                pose.scale(0.04f, -0.04f, 0.04f);
                int sw = font.width(am);
                font.drawInBatch(am, 13 - sw, 4, 0xFFFFFF, true, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, light);
            }
            pose.popPose();
            ++current;
        }
    }

    // ---- First person (TC4's ItemThaumometerRenderer: both arms hold the thaumometer) ----

    /**
     * With the other hand empty, the thaumometer is drawn held in both hands. The arms use the vanilla two-handed
     * map pose; the TC4 renderer is not available, so its exact arm placement is approximated.
     */
    @SubscribeEvent
    static void renderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        InteractionHand hand = event.getHand();
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = event.getItemStack();
        ItemStack other = mc.player.getItemInHand(otherHand);
        if (stack.getItem() instanceof ItemThaumometer) {
            if (!other.isEmpty()) return;
            event.setCanceled(true);
            renderThaumometerFirstPerson(mc, mc.player, hand, stack, event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight(), event.getEquipProgress(), event.getSwingProgress());
        } else if (stack.isEmpty() && other.getItem() instanceof ItemThaumometer) {
            // The arm is already drawn holding the thaumometer.
            event.setCanceled(true);
        }
    }

    private static void renderThaumometerFirstPerson(Minecraft mc, AbstractClientPlayer player, InteractionHand hand, ItemStack stack,
                                                     PoseStack pose, MultiBufferSource buffers, int light, float equipProgress,
                                                     float swingProgress) {
        if (!player.isInvisible() && mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer) {
            // ItemInHandRenderer.renderTwoHandedMap, without the tilt that follows the camera pitch.
            pose.pushPose();
            pose.translate(0.0f, 0.04f + equipProgress * -1.2f, -0.72f);
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            renderHoldingArm(pose, buffers, light, player, renderer, HumanoidArm.RIGHT);
            renderHoldingArm(pose, buffers, light, player, renderer, HumanoidArm.LEFT);
            pose.popPose();
        }
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean right = arm == HumanoidArm.RIGHT;
        int i = right ? 1 : -1;
        pose.pushPose();
        // applyItemArmTransform and applyItemArmAttackTransform
        pose.translate(i * 0.56f, -0.52f + equipProgress * -0.6f, -0.72f);
        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        pose.mulPose(Axis.YP.rotationDegrees(i * (45.0f + f * -20.0f)));
        float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        pose.mulPose(Axis.ZP.rotationDegrees(i * f1 * -20.0f));
        pose.mulPose(Axis.XP.rotationDegrees(f1 * -80.0f));
        pose.mulPose(Axis.YP.rotationDegrees(i * -45.0f));
        mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player, stack,
                right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !right, pose, buffers, light);
        pose.popPose();
    }

    /** ItemInHandRenderer.renderMapHand. */
    private static void renderHoldingArm(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                         PlayerRenderer renderer, HumanoidArm side) {
        pose.pushPose();
        float f = side == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        pose.mulPose(Axis.YP.rotationDegrees(92.0f));
        pose.mulPose(Axis.XP.rotationDegrees(45.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(f * -41.0f));
        pose.translate(f * 0.3f, -1.1f, 0.45f);
        if (side == HumanoidArm.RIGHT) {
            renderer.renderRightHand(pose, buffers, light, player);
        } else {
            renderer.renderLeftHand(pose, buffers, light, player);
        }
        pose.popPose();
    }
}
