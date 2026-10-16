package thaumcraft.client.gui;

import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
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
 * The anomaly meter at the top of the screen, drawn with Thaumcraft's own hud.png: the research toast panel
 * and the flowing vis strip of the caster HUD lying on its side as the bar. Below it, the
 * active anomalies with their time left. Not in Thaumcraft.
 */
public final class AnomalyHud {
    public static final ResourceLocation ID = Thaumcraft.id("anomaly_hud");
    private static final ResourceLocation HUD = Thaumcraft.id("textures/gui/hud.png");
    private static final int WIDTH = 170;
    private static final int HEADER = 20;
    private static final int ROW = 11;
    /** Same colors as ResearchToast: violet titles, gold values. */
    static final int VIOLET = 0xA239F1;
    static final int GOLD = 0xFFAB09;
    static final int LAVENDER = 0xC8B4E0;
    static final int BORDER = 0xFF5A4466;
    private static long lastWarnedCycleEnd = -1L;

    private AnomalyHud() {
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.level == null || mc.player == null) return;
        if (!AnomalyClientState.enabled || !Config.ANOMALY_SHOW_HUD.get()) return;
        Font font = mc.font;
        long now = mc.level.getGameTime();
        float pt = delta.getGameTimeDeltaPartialTick(false);
        long remaining = AnomalyClientState.remainingNow(now);
        float progress = 1.0f - Math.min(1.0f, remaining / (float) AnomalyClientState.total);
        List<PacketAnomalySync.Entry> active = AnomalyClientState.active;
        boolean paused = AnomalyClientState.paused;
        boolean warn = !paused && remaining <= 1200L;

        int x = "top_left".equals(Config.ANOMALY_HUD_POSITION.get()) ? 4 : (g.guiWidth() - WIDTH) / 2;
        int y = 2;
        Component info = null;
        int infoColor = VIOLET;
        long choosing = AnomalyClientState.choosingNow(now);
        if (choosing > 0L) {
            info = AnomalyClientState.choiceIds.isEmpty()
                    ? Component.translatable("tc.anomaly.hud.choosing", time(choosing))
                    : Component.translatable("tc.anomaly.hud.choose", time(choosing));
        } else if (AnomalyClientState.nextPolarity >= 0 && holdsThaumometer(mc)) {
            AnomalyType.Polarity next = AnomalyType.Polarity.values()[Math.min(2, AnomalyClientState.nextPolarity)];
            info = Component.translatable("tc.anomaly.hud.next",
                    Component.translatable("tc.anomaly.polarity." + next.name().toLowerCase(java.util.Locale.ROOT)));
            infoColor = color(next);
        }
        int rows = active.size() + (info != null ? 1 : 0);
        int height = HEADER + (rows == 0 ? 0 : 3 + rows * ROW);
        panel(g, x, y, WIDTH, height);

        float pulse = warn ? 0.6f + 0.4f * (float) Math.sin((now + pt) * 0.4f) : 1.0f;

        // The bar: a sunken track with the caster's flowing vis strip inside, scrolling sideways.
        String t = paused ? "||" : time(remaining);
        int bx = x + 6;
        int bw = WIDTH - 6 - 8 - font.width("00:00");
        int by = y + 6;
        g.fill(bx - 1, by - 1, bx + bw + 1, by + 7, BORDER);
        g.fill(bx, by, bx + bw, by + 6, 0xFF1A1220);
        int fill = Math.round(bw * progress);
        if (fill > 0) {
            if (paused) g.setColor(0.55f, 0.55f, 0.55f, 1.0f);
            else if (warn) g.setColor(1.0f, 0.55f + 0.25f * pulse, 0.2f, 1.0f);
            strip(g, bx, by, bx + fill, by + 6, (now + pt) * 0.5f);
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        g.drawString(font, t, x + WIDTH - 5 - font.width(t), y + 5, paused ? 0x9A8AA8 : warn ? 0xFF7A1A : GOLD, false);

        if (rows == 0) {
            chime(mc, warn, remaining);
            return;
        }

        // A thin violet rule, the foresight or the choice, then one line per active anomaly.
        g.fill(x + 4, y + HEADER, x + WIDTH - 4, y + HEADER + 1, BORDER);
        int ry = y + HEADER + 3;
        if (info != null) {
            g.drawString(font, font.plainSubstrByWidth(info.getString(), WIDTH - 10), x + 5, ry, infoColor, false);
            ry += ROW;
        }
        for (PacketAnomalySync.Entry e : active) {
            AnomalyType type = Anomalies.get(e.id());
            int color = type != null ? color(type.polarity) : LAVENDER;
            g.drawString(font, "◆", x + 5, ry, color, false);
            Component name = Component.translatable(type != null ? type.getTranslationKey() : e.id());
            String left = time(AnomalyClientState.remainingNow(e, now));
            if (e.local()) left = pointer(mc, e.x(), e.z()) + " " + left;
            int maxName = WIDTH - 22 - font.width(left);
            g.drawString(font, font.plainSubstrByWidth(name.getString(), maxName), x + 14, ry, color, false);
            g.drawString(font, left, x + WIDTH - 5 - font.width(left), ry, LAVENDER, false);
            ry += ROW;
        }
        chime(mc, warn, remaining);
    }

    private static boolean holdsThaumometer(Minecraft mc) {
        return mc.player.getMainHandItem().getItem() instanceof thaumcraft.common.items.tools.ItemThaumometer
                || mc.player.getOffhandItem().getItem() instanceof thaumcraft.common.items.tools.ItemThaumometer;
    }

    /** Softer than the polarity's own colors, to sit on the violet panel: herba green, ignis red, gold. */
    public static int color(AnomalyType.Polarity p) {
        return switch (p) {
            case POSITIVE -> 0x8FE06A;
            case NEGATIVE -> 0xE85A4A;
            case MIXED -> GOLD;
        };
    }

    /** One chime when the last minute starts. */
    private static void chime(Minecraft mc, boolean warn, long remaining) {
        long cycleEnd = AnomalyClientState.receivedAt + AnomalyClientState.remaining;
        if (warn && remaining > 0L && Math.abs(cycleEnd - lastWarnedCycleEnd) > 40L) {
            lastWarnedCycleEnd = cycleEnd;
            mc.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.4f, 0.5f);
        }
    }

    /** The research toast panel (hud.png 0,224 160x32) stretched to any size: 4 px corners, edges and middle. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        final int c = 4;
        final int pw = 160;
        final int ph = 32;
        final int u = 0;
        final int v = 224;
        int[] xs = {x, x + c, x + w - c, x + w};
        int[] ys = {y, y + c, y + h - c, y + h};
        int[] us = {u, u + c, u + pw - c, u + pw};
        int[] vs = {v, v + c, v + ph - c, v + ph};
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(1.0f, 1.0f, 1.0f, 0.92f);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                quad(g, xs[i], ys[j], xs[i + 1], ys[j + 1], us[i] / 256f, us[i + 1] / 256f, vs[j] / 256f, vs[j + 1] / 256f);
            }
        }
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * The caster HUD's vis strip (hud.png 96,56 8x128) turned on its side and tiled along the bar, shifted by
     * {@code scroll} texels so it flows.
     */
    private static void strip(GuiGraphics g, int x1, int y1, int x2, int y2, float scroll) {
        final float u1 = 96 / 256f;
        final float u2 = 104 / 256f;
        final int len = 128;
        g.flush(); // the track's fills are batched; draw them first so the strip lands on top
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float off = scroll % len;
        float x = x1 - off;
        while (x < x2) {
            float a = Math.max(x, x1);
            float b = Math.min(x + len, x2);
            if (b > a) {
                float v1 = (56 + (a - x)) / 256f;
                float v2 = (56 + (b - x)) / 256f;
                RenderSystem.setShaderTexture(0, HUD);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                Matrix4f m = g.pose().last().pose();
                BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                buf.addVertex(m, a, y1, 0).setUv(u2, v1);
                buf.addVertex(m, a, y2, 0).setUv(u1, v1);
                buf.addVertex(m, b, y2, 0).setUv(u1, v2);
                buf.addVertex(m, b, y1, 0).setUv(u2, v2);
                BufferUploader.drawWithShader(buf.buildOrThrow());
            }
            x += len;
        }
    }

    private static void quad(GuiGraphics g, float x1, float y1, float x2, float y2, float u1, float u2, float v1, float v2) {
        RenderSystem.setShaderTexture(0, HUD);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f m = g.pose().last().pose();
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.addVertex(m, x1, y1, 0).setUv(u1, v1);
        b.addVertex(m, x1, y2, 0).setUv(u1, v2);
        b.addVertex(m, x2, y2, 0).setUv(u2, v2);
        b.addVertex(m, x2, y1, 0).setUv(u2, v1);
        BufferUploader.drawWithShader(b.buildOrThrow());
    }

    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    /** "↗ 34m": where a local anomaly is, relative to where the player looks. */
    private static String pointer(Minecraft mc, int x, int z) {
        double dx = x + 0.5 - mc.player.getX();
        double dz = z + 0.5 - mc.player.getZ();
        int dist = (int) Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float rel = net.minecraft.util.Mth.wrapDegrees(yaw - mc.player.getYRot());
        int idx = Math.floorMod(Math.round(rel / 45.0f), 8);
        return ARROWS[idx] + " " + dist + "m";
    }

    static String time(long ticks) {
        long s = (ticks + 19L) / 20L;
        return String.format("%d:%02d", s / 60L, s % 60L);
    }
}
