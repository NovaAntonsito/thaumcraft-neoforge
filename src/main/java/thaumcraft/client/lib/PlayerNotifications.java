package thaumcraft.client.lib;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Config;
import thaumcraft.api.aspects.Aspect;

/**
 * TC4's PlayerNotifications (kept by Reborn): the text lines in the bottom-right corner ("+3 Ignis added to your
 * aspect pool") and the aspect icons that fly up to the thaumonomicon in the top-right corner.
 */
public final class PlayerNotifications {
    public static List<Notification> notificationList = new ArrayList<>();
    public static List<AspectNotification> aspectList = new ArrayList<>();

    private PlayerNotifications() {
    }

    public static void addNotification(Component text) {
        addNotification(text, null, 0xFFFFFF);
    }

    public static void addAspectNotification(Aspect aspect) {
        Minecraft mc = Minecraft.getInstance();
        long time = System.nanoTime() / 1000000L + mc.level.random.nextInt(1000);
        float x = 0.4f + mc.level.random.nextFloat() * 0.2f;
        float y = 0.4f + mc.level.random.nextFloat() * 0.2f;
        aspectList.add(new AspectNotification(aspect, x, y, time, time + 1500L));
    }

    public static void addNotification(Component text, Aspect aspect) {
        addNotification(text, aspect.getImage(), aspect.getColor());
    }

    public static void addNotification(Component text, @Nullable ResourceLocation image, int color) {
        long time = System.nanoTime() / 1000000L;
        int delay = Config.NOTIFICATION_DELAY.get();
        long timeBonus = notificationList.isEmpty() ? delay / 2 : 0L;
        notificationList.add(new Notification(text, image, time + delay + timeBonus, time + delay / 4, color));
    }

    /** Drops expired lines; every line but the oldest gets its timer reset, so they leave one at a time. */
    public static List<Notification> getListAndUpdate(long time) {
        List<Notification> temp = new ArrayList<>();
        boolean first = true;
        int delay = Config.NOTIFICATION_DELAY.get();
        for (Notification li : notificationList) {
            if (li.expire >= time) {
                temp.add(first ? li : new Notification(li.text, li.image, time + delay, li.created, li.color));
            }
            first = false;
        }
        notificationList = temp;
        return temp;
    }

    public static List<AspectNotification> getAspectListAndUpdate(long time) {
        List<AspectNotification> temp = new ArrayList<>();
        for (AspectNotification li : aspectList) {
            if (li.expire >= time) temp.add(li);
        }
        aspectList = temp;
        return temp;
    }

    public record AspectNotification(Aspect aspect, float startX, float startY, long created, long expire) {
    }

    public record Notification(Component text, @Nullable ResourceLocation image, long expire, long created, int color) {
    }
}
