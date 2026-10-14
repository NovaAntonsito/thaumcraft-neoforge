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
        receivedAt = now;
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
