package thaumcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.ICaster;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.items.tools.ItemSanityChecker;
import thaumcraft.common.items.tools.ItemThaumometer;

/**
 * HudHandler.renderSanityHud: while a sanity checker is held, a gauge of the player's warp. Temporary warp is pink,
 * normal warp purple and permanent warp dark purple; at 100 or more a symbol shakes on top. As in the original, it
 * goes below the caster dial and the thaumometer bar of the hand before it.
 */
public final class SanityHud {
    public static final ResourceLocation ID = Thaumcraft.id("sanity_hud");
    private static final ResourceLocation HUD = Thaumcraft.id("textures/gui/hud.png");

    private SanityHud() {
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null || !(mc.getCameraEntity() instanceof Player player)) return;
        boolean rC = false;
        boolean rT = false;
        int start = 0;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack handStack = player.getItemInHand(hand);
            if (handStack.isEmpty()) continue;
            if (!rC && handStack.getItem() instanceof ICaster) {
                rC = true;
                start += 33;
            } else if (!rT && handStack.getItem() instanceof ItemThaumometer) {
                rT = true;
                start += 80;
            } else if (handStack.getItem() instanceof ItemSanityChecker) {
                renderSanityHud(g, mc, player, start);
                return;
            }
        }
    }

    static void renderSanityHud(GuiGraphics g, Minecraft mc, Player player, int shifty) {
        g.pose().pushPose();
        g.pose().translate(0.0f, shifty, 0.0f);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        tex(g, 1, 1, 152, 0, 20, 76);
        PlayerWarp warp = ThaumcraftCapabilities.getWarp(player);
        int p = warp.get(PlayerWarp.EnumWarpType.PERMANENT);
        int s = warp.get(PlayerWarp.EnumWarpType.NORMAL);
        int t = warp.get(PlayerWarp.EnumWarpType.TEMPORARY);
        float tw = p + s + t;
        float mod = 1.0f;
        if (tw > 100.0f) {
            mod = 100.0f / tw;
            tw = 100.0f;
        }
        int gap = (int) ((100.0f - tw) / 100.0f * 48.0f);
        int wt = (int) (t / 100.0f * 48.0f * mod);
        int ws = (int) (s / 100.0f * 48.0f * mod);
        // The heights are the original's (wt + gap and so on, not wt).
        if (t > 0) {
            g.setColor(1.0f, 0.5f, 1.0f, 1.0f);
            tex(g, 7, 21 + gap, 200, gap, 8, wt + gap);
        }
        if (s > 0) {
            g.setColor(0.75f, 0.0f, 0.75f, 1.0f);
            tex(g, 7, 21 + wt + gap, 200, wt + gap, 8, wt + ws + gap);
        }
        if (p > 0) {
            g.setColor(0.5f, 0.0f, 0.5f, 1.0f);
            tex(g, 7, 21 + wt + ws + gap, 200, wt + ws + gap, 8, 48);
        }
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        tex(g, 1, 1, 176, 0, 20, 76);
        if (tw >= 100.0f) {
            g.pose().pushPose();
            g.pose().scale(0.75f, 0.75f, 1.0f);
            g.pose().translate(player.getRandom().nextInt(2), player.getRandom().nextInt(2), 0.0f);
            tex(g, 3, 3, 216, 0, 20, 16);
            g.pose().popPose();
        }
        g.pose().popPose();
    }

    /** UtilsFX.drawTexturedQuad on the 256x256 hud texture. */
    private static void tex(GuiGraphics g, float x, float y, float u, float v, float w, float h) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        UtilsFX.quad(g, HUD, x, y, x + w, y + h, u / 256f, (u + w) / 256f, v / 256f, (v + h) / 256f);
    }
}
