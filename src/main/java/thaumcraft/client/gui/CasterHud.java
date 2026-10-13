package thaumcraft.client.gui;

import java.awt.Color;
import java.text.DecimalFormat;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.ICaster;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.items.casters.ItemFocus;

/**
 * HudHandler.renderCastingWandHud: the dial in the top left corner while a caster is held. It shows the vis of the
 * aura around the player, the mounted focus and, while sneaking, the vis numbers and the cost per cast.
 * With an exchange focus and a picked block, the focus is replaced by the picked block and how many you carry.
 * TODO: the dialBottom config option.
 */
public final class CasterHud {
    public static final ResourceLocation ID = Thaumcraft.id("caster_hud");
    private static final ResourceLocation HUD = Thaumcraft.id("textures/gui/hud.png");
    private static final DecimalFormat secondsFormatter = new DecimalFormat("#######.#");

    private CasterHud() {
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null || !(mc.getCameraEntity() instanceof Player player)) return;
        ItemStack handStack = player.getMainHandItem();
        if (!(handStack.getItem() instanceof ICaster)) {
            handStack = player.getOffhandItem();
        }
        if (handStack.getItem() instanceof ICaster) {
            renderCastingWandHud(g, mc, player, handStack);
        }
    }

    static void renderCastingWandHud(GuiGraphics g, Minecraft mc, Player player, ItemStack wandstack) {
        ICaster wand = (ICaster) wandstack.getItem();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, -90.0f);
        g.pose().pushPose();
        g.pose().scale(0.5f, 0.5f, 0.5f);
        tex(g, 0, 0, 0, 0, 64, 64);
        g.pose().popPose();
        g.pose().translate(16.0f, 16.0f, 0.0f);
        int max = ThaumometerHud.auraBase;
        int amt = (int) ThaumometerHud.auraVis;
        ItemFocus focus = (ItemFocus) wand.getFocus(wandstack);
        ItemStack focusStack = wand.getFocusStack(wandstack);
        g.pose().pushPose();
        g.pose().translate(16.0f, -10.0f, 0.0f);
        g.pose().scale(0.5f, 0.5f, 0.5f);
        int loc = max > 0 ? Math.min(30, (int) (30.0f * amt / max)) : 0;
        Color ac = new Color(Aspect.ENERGY.getColor());
        g.setColor(ac.getRed() / 255.0f, ac.getGreen() / 255.0f, ac.getBlue() / 255.0f, 0.8f);
        tex(g, -4.0f, 35 - loc, 104.0f, 0.0f, 8.0f, loc);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        tex(g, -8.0f, -3.0f, 72.0f, 0.0f, 16.0f, 42.0f);
        if (player.isShiftKeyDown()) {
            g.pose().pushPose();
            g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90.0f));
            String msg = secondsFormatter.format(amt);
            g.drawString(mc.font, msg, -32, -4, 16777215, true);
            g.pose().popPose();
            if (focus != null && focus.getVisCost(focusStack) > 0.0f) {
                float mod = wand.getConsumptionModifier(wandstack, player, false);
                msg = secondsFormatter.format(focus.getVisCost(focusStack) * mod);
                g.drawString(mc.font, msg, -32 - mc.font.width(msg) / 2, 32, 16777215, true);
            }
        }
        g.pose().popPose();
        if (focus != null) {
            ItemStack pickedStack = wand.getPickedBlock(wandstack);
            if (!pickedStack.isEmpty()) {
                renderWandTradeHud(g, mc, player, pickedStack);
            } else {
                g.renderItem(focusStack, -8, -8);
            }
        }
        g.pose().popPose();
        RenderSystem.disableBlend();
    }

    /** renderWandTradeHud: the picked block and, in small outlined digits, how many of it are in the inventory. */
    static void renderWandTradeHud(GuiGraphics g, Minecraft mc, Player player, ItemStack picked) {
        int amount = 0;
        for (ItemStack is : player.getInventory().items) {
            if (!is.isEmpty() && ItemStack.isSameItem(is, picked)) {
                amount += is.getCount();
            }
        }
        g.renderItem(picked, -8, -8);
        g.pose().pushPose();
        String am = "" + amount;
        int sw = mc.font.width(am);
        g.pose().translate(0.0f, -mc.font.lineHeight, 500.0f);
        g.pose().scale(0.5f, 0.5f, 0.5f);
        for (int a = -1; a <= 1; ++a) {
            for (int b = -1; b <= 1; ++b) {
                if ((a == 0 || b == 0) && (a != 0 || b != 0)) {
                    g.drawString(mc.font, am, a + 16 - sw, b + 24, 0, false);
                }
            }
        }
        g.drawString(mc.font, am, 16 - sw, 24, 16777215, false);
        g.pose().popPose();
    }

    /** UtilsFX.drawTexturedQuad on the 256x256 hud texture. */
    private static void tex(GuiGraphics g, float x, float y, float u, float v, float w, float h) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        UtilsFX.quad(g, HUD, x, y, x + w, y + h, u / 256f, (u + w) / 256f, v / 256f, (v + h) / 256f);
    }
}
