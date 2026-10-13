package thaumcraft.client.gui;

import java.text.DecimalFormat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.items.tools.ItemThaumometer;

/** HudHandler.renderThaumometerHud: the vis and flux bar of the current chunk while a thaumometer is held. */
public final class ThaumometerHud {
    public static final ResourceLocation ID = Thaumcraft.id("thaumometer_hud");
    private static final ResourceLocation HUD = Thaumcraft.id("textures/gui/hud.png");
    private static final DecimalFormat secondsFormatter = new DecimalFormat("#######.#");

    /** HudHandler.currentAura, set by PacketAuraToClient. */
    public static short auraBase;
    public static float auraVis;
    public static float auraFlux;

    private ThaumometerHud() {
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null || !(mc.getCameraEntity() instanceof Player player)) return;
        if (!(player.getMainHandItem().getItem() instanceof ItemThaumometer)
                && !(player.getOffhandItem().getItem() instanceof ItemThaumometer)) {
            return;
        }
        // Below the caster dial when a caster is held too (the original's shifty of 33).
        boolean caster = player.getMainHandItem().getItem() instanceof thaumcraft.api.casters.ICaster
                || player.getOffhandItem().getItem() instanceof thaumcraft.api.casters.ICaster;
        renderThaumometerHud(g, mc, player, delta.getGameTimeDeltaPartialTick(false), caster ? 33 : 0);
    }

    static void renderThaumometerHud(GuiGraphics g, Minecraft mc, Player player, float partialTicks, int shifty) {
        float base = Mth.clamp(auraBase / 525.0f, 0.0f, 1.0f);
        float vis = Mth.clamp(auraVis / 525.0f, 0.0f, 1.0f);
        float flux = Mth.clamp(auraFlux / 525.0f, 0.0f, 1.0f);
        float count = player.tickCount + partialTicks;
        float count2 = player.tickCount / 3.0f + partialTicks;
        if (flux + vis > 1.0f) {
            float m = 1.0f / (flux + vis);
            base *= m;
            vis *= m;
            flux *= m;
        }
        float start = 10.0f + (1.0f - vis) * 64.0f;
        g.pose().pushPose();
        g.pose().translate(2.0f, shifty, 0.0f);
        if (vis > 0.0f) {
            g.setColor(0.7f, 0.4f, 0.9f, 1.0f);
            g.pose().pushPose();
            g.pose().translate(5.0f, start, 0.0f);
            g.pose().scale(1.0f, vis, 1.0f);
            tex(g, 0, 0, 88, 56, 8, 64);
            g.pose().popPose();
            additive(g, 1.0f, 1.0f, 1.0f, 5.0f, start, 96, 56 + count % 64.0f, vis * 64.0f);
            if (player.isShiftKeyDown()) {
                text(g, mc, secondsFormatter.format(auraVis), 16.0f, start, 0xEEAAFF);
            }
        }
        if (flux > 0.0f) {
            start = 10.0f + (1.0f - flux - vis) * 64.0f;
            g.setColor(0.25f, 0.1f, 0.3f, 1.0f);
            g.pose().pushPose();
            g.pose().translate(5.0f, start, 0.0f);
            g.pose().scale(1.0f, flux, 1.0f);
            tex(g, 0, 0, 88, 56, 8, 64);
            g.pose().popPose();
            additive(g, 0.7f, 0.4f, 1.0f, 5.0f, start, 104, 120 - count2 % 64.0f, flux * 64.0f);
            if (player.isShiftKeyDown()) {
                text(g, mc, secondsFormatter.format(auraFlux), 16.0f, start - 4.0f, 0xAA11BB);
            }
        }
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        tex(g, 1, 1, 72, 48, 16, 80);
        start = 8.0f + (1.0f - base) * 64.0f;
        tex(g, 2, start, 117, 61, 14, 5);
        g.pose().popPose();
    }

    /** The scrolling overlay, drawn additively at half opacity. */
    private static void additive(GuiGraphics g, float r, float gr, float b, float x, float y, float u, float v, float h) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        g.setColor(r, gr, b, 0.5f);
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        UtilsFX.quad(g, HUD, 0, 0, 8, h, u / 256f, (u + 8) / 256f, v / 256f, (v + h) / 256f);
        g.pose().popPose();
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
    }

    /** UtilsFX.drawTexturedQuad on the 256x256 hud texture. */
    private static void tex(GuiGraphics g, float x, float y, float u, float v, float w, float h) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        UtilsFX.quad(g, HUD, x, y, x + w, y + h, u / 256f, (u + w) / 256f, v / 256f, (v + h) / 256f);
    }

    private static void text(GuiGraphics g, Minecraft mc, String msg, float x, float y, int color) {
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(0.5f, 0.5f, 0.5f);
        g.drawString(mc.font, msg, 0, 0, color, true);
        g.pose().popPose();
    }
}
