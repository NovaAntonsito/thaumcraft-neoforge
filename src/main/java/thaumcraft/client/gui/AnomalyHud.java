package thaumcraft.client.gui;

import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.common.anomaly.Anomalies;
import thaumcraft.common.anomaly.AnomalyClientState;
import thaumcraft.common.anomaly.AnomalyType;
import thaumcraft.common.anomaly.PacketAnomalySync;

/**
 * The anomaly meter at the top of the screen: a bar that fills up until the next anomaly, with the time left,
 * and below it the active anomalies with their time left. Not in Thaumcraft.
 */
public final class AnomalyHud {
    public static final ResourceLocation ID = Thaumcraft.id("anomaly_hud");
    private static final int WIDTH = 140;
    private static final int BAR = 92;
    private static long lastWarnedCycleEnd = -1L;

    private AnomalyHud() {
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.level == null || mc.player == null) return;
        if (!AnomalyClientState.enabled || !Config.ANOMALY_SHOW_HUD.get()) return;
        Font font = mc.font;
        long now = mc.level.getGameTime();
        long remaining = AnomalyClientState.remainingNow(now);
        float progress = 1.0f - Math.min(1.0f, remaining / (float) AnomalyClientState.total);
        List<PacketAnomalySync.Entry> active = AnomalyClientState.active;

        int x = "top_left".equals(Config.ANOMALY_HUD_POSITION.get()) ? 4 : (g.guiWidth() - WIDTH) / 2;
        int y = 2;
        int height = 14 + active.size() * 10;
        g.fill(x, y, x + WIDTH, y + height, 0x88000000);

        // "?" until it comes, then the bar and the time left.
        boolean warn = !AnomalyClientState.paused && remaining <= 1200L;
        g.drawString(font, "?", x + 4, y + 3, warn && (now / 10L) % 2L == 0L ? 0xFFAA00 : 0xC080FF, true);
        int bx = x + 12;
        int by = y + 5;
        g.fill(bx, by, bx + BAR, by + 4, 0xFF2A1A3A);
        int barColor = AnomalyClientState.paused ? 0xFF777777 : warn ? 0xFFFF8800 : 0xFF9B4DFF;
        g.fill(bx, by, bx + Math.round(BAR * progress), by + 4, barColor);
        String t = AnomalyClientState.paused ? "||" : time(remaining);
        g.drawString(font, t, x + WIDTH - 4 - font.width(t), y + 3, 0xFFFFFF, true);

        int ry = y + 14;
        for (PacketAnomalySync.Entry e : active) {
            AnomalyType type = Anomalies.get(e.id());
            int color = type != null ? type.polarity.color : 0xFFFFFF;
            g.fill(x + 4, ry + 2, x + 8, ry + 6, 0xFF000000 | color);
            Component name = Component.translatable(type != null ? type.getTranslationKey() : e.id());
            String left = time(AnomalyClientState.remainingNow(e, now));
            int maxName = WIDTH - 18 - font.width(left);
            g.drawString(font, font.plainSubstrByWidth(name.getString(), maxName), x + 11, ry, color, true);
            g.drawString(font, left, x + WIDTH - 4 - font.width(left), ry, 0xDDDDDD, true);
            ry += 10;
        }

        // One chime when the last minute starts.
        long cycleEnd = AnomalyClientState.receivedAt + AnomalyClientState.remaining;
        if (warn && remaining > 0L && Math.abs(cycleEnd - lastWarnedCycleEnd) > 40L) {
            lastWarnedCycleEnd = cycleEnd;
            mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.4f, 0.5f);
        }
    }

    private static String time(long ticks) {
        long s = (ticks + 19L) / 20L;
        return String.format("%d:%02d", s / 60L, s % 60L);
    }
}
