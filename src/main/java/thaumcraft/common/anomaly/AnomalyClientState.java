package thaumcraft.common.anomaly;

import java.util.ArrayList;
import java.util.List;

/**
 * The client's copy of its dimension's anomaly clock, filled by {@link PacketAnomalySync}. Plain data, so common
 * code (the arcane workbench screen's cost, the caster) can read it without touching client classes.
 */
public final class AnomalyClientState {
    public static boolean enabled;
    public static int remaining;
    public static int total = 1;
    public static boolean paused;
    public static int cycle;
    public static List<PacketAnomalySync.Entry> active = List.of();
    /** Polarity ordinal of the next anomaly, -1 if unknown. Shown with a thaumometer in hand. */
    public static int nextPolarity = -1;
    /** Ticks a seer has left to choose, at {@link #receivedAt}; 0 when nobody is choosing. */
    public static int choosing;
    /** The cards offered to this player, empty when there is nothing to choose. */
    public static List<String> choiceIds = List.of();
    public static long choiceUntil;
    /** Set when new cards arrive; the client opens the card screen and clears it. */
    public static boolean choiceToOpen;
    /** Client game time when the last packet arrived. */
    public static long receivedAt;

    private AnomalyClientState() {
    }

    static void update(PacketAnomalySync msg, long now) {
        enabled = msg.enabled();
        remaining = msg.remaining();
        total = Math.max(1, msg.total());
        paused = msg.paused();
        cycle = msg.cycle();
        active = List.copyOf(msg.active());
        nextPolarity = msg.nextPolarity();
        choosing = msg.choosing();
        receivedAt = now;
    }

    static void setChoice(List<String> ids, int ticks, long now) {
        choiceIds = List.copyOf(ids);
        choiceUntil = now + ticks;
        choiceToOpen = !ids.isEmpty();
    }

    /** Ticks left to choose, counted down on the client. */
    public static long choosingNow(long now) {
        return Math.max(0L, choosing - (now - receivedAt));
    }

    public static List<String> activeIds() {
        List<String> ids = new ArrayList<>(active.size());
        for (PacketAnomalySync.Entry e : active) ids.add(e.id());
        return ids;
    }

    /** Ticks left until the next anomaly, counted down on the client between packets. */
    public static long remainingNow(long now) {
        return paused ? remaining : Math.max(0L, remaining - (now - receivedAt));
    }

    public static long remainingNow(PacketAnomalySync.Entry e, long now) {
        return Math.max(0L, e.remaining() - (now - receivedAt));
    }
}
