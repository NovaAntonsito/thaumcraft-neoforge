package thaumcraft.client.gui;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.PlayerNotifications;
import thaumcraft.client.lib.UtilsFX;

/** TC4's REHNotifyHandler (kept by Reborn): draws the {@link PlayerNotifications}. */
public final class NotificationsHud {
    public static final ResourceLocation ID = Thaumcraft.id("notifications");
    private static final ResourceLocation BOOK = Thaumcraft.id("textures/items/thaumonomicon.png");

    private NotificationsHud() {
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        long time = System.nanoTime() / 1000000L;
        if (!PlayerNotifications.getListAndUpdate(time).isEmpty()) renderNotifyHUD(g, mc, time, delta.getGameTimeDeltaPartialTick(false));
        if (!PlayerNotifications.getAspectListAndUpdate(time).isEmpty()) renderAspectHUD(g, time);
    }

    private static void renderNotifyHUD(GuiGraphics g, Minecraft mc, long time, float partialTicks) {
        int k = g.guiWidth();
        int l = g.guiHeight();
        int delay = Config.NOTIFICATION_DELAY.get();
        List<PlayerNotifications.Notification> notifications = PlayerNotifications.getListAndUpdate(time);
        float shift = -8.0f;
        for (int entry = 0; entry < notifications.size() && entry < Config.NOTIFICATION_MAX.get(); ++entry) {
            PlayerNotifications.Notification li = notifications.get(entry);
            float st = 0.75f;
            int size = (int) (mc.font.width(li.text()) * st);
            int alpha = 255;
            if (entry == notifications.size() - 1 && li.created() > time) {
                alpha = 255 - (int) ((float) (li.created() - time) / (float) (delay / 4) * 240.0f);
            }
            if (li.expire() < time + delay) {
                alpha = (int) (255.0f - (float) (time + delay - li.expire()) / (float) delay * 240.0f);
                shift = -8.0f * ((float) alpha / 255.0f);
            }
            alpha = Math.max(alpha, 16);
            int color = ((alpha / 2) << 24) | 0xFFFFFF;
            RenderSystem.enableBlend();
            g.pose().pushPose();
            g.pose().translate(k - size - 10, l - entry * 8 + shift, 0);
            g.pose().scale(st, st, st);
            g.drawString(mc.font, li.text(), -4, -8, color);
            g.pose().popPose();

            if (li.image() != null) {
                int c = li.color();
                g.pose().pushPose();
                g.pose().translate(k - 9, l - entry * 8 + shift - 7.0f, 0);
                g.setColor(((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f, alpha / 511.0f);
                UtilsFX.quad(g, li.image(), 0, 0, 8, 8, 0, 1, 0, 1);
                g.setColor(1, 1, 1, 1);
                g.pose().popPose();
            }

            if (entry == notifications.size() - 1 && li.created() > time) {
                // A sparkle sweeps in from the left with the newest line.
                float scale = (float) (li.created() - time) / (float) (delay / 4);
                int a2 = 255 - (int) (scale * 240.0f);
                g.pose().pushPose();
                g.pose().translate(k - 5 - 8.0f * scale - (1.0f - scale) * (1.0f - scale) * (1.0f - scale) * size * 3.0f,
                        l - entry * 8 + shift - 2.0f - 8.0f * scale, 0);
                g.pose().scale(scale, scale, scale);
                g.setColor(1, 1, 1, 0.5f - a2 / 511.0f);
                float px = 16 * ((mc.player.tickCount + partialTicks + entry * 3) % 16);
                px = (float) Math.floor(px / 16) * 16;
                UtilsFX.quad(g, UtilsFX.PARTICLE_TEXTURE, 0, 0, 16, 16, px / 1024f, (px + 16) / 1024f, 80 / 1024f, 96 / 1024f);
                g.setColor(1, 1, 1, 1);
                g.pose().popPose();
            }
        }
        RenderSystem.disableBlend();
    }

    /** Each point gained flies along a curve from the middle of the screen to the thaumonomicon icon. */
    private static void renderAspectHUD(GuiGraphics g, long time) {
        int k = g.guiWidth();
        int l = g.guiHeight();
        float mainAlpha = 0.0f;
        for (PlayerNotifications.AspectNotification li : PlayerNotifications.getAspectListAndUpdate(time)) {
            if (li.created() > time || li.aspect().getImage() == null) continue;
            int startX = (int) (k * li.startX());
            int startY = (int) (l * li.startY());
            int endY = -8;
            int bezierX = (int) (k * (0.25f + li.startX()));
            int bezierY = (int) (l * li.startY());
            double t = (double) (time - li.created()) / (double) (li.expire() - li.created());
            double x = (1.0 - t) * (1.0 - t) * startX + 2.0 * (1.0 - t) * t * bezierX + t * t * k;
            double y = (1.0 - t) * (1.0 - t) * startY + 2.0 * (1.0 - t) * t * bezierY + t * t * endY;
            float alpha = 1.0f;
            if (t < 0.3) {
                alpha = (float) (t / 0.3);
            } else if (t > 0.66) {
                alpha = (float) (1.0 - (t - 0.66) / 0.34);
            }
            mainAlpha = Math.max(mainAlpha, alpha);
            int c = li.aspect().getColor();
            float size = 256 * 0.075f * alpha;
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.setColor(((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f, alpha * 0.66f);
            UtilsFX.quad(g, li.aspect().getImage(), 0, 0, size, size, 0, 1, 0, 1);
            g.pose().popPose();
        }
        if (mainAlpha > 0.0f) {
            g.setColor(1, 1, 1, mainAlpha);
            UtilsFX.quad(g, BOOK, k - 16, 0, k, 16, 0, 1, 0, 1);
        }
        g.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }
}
