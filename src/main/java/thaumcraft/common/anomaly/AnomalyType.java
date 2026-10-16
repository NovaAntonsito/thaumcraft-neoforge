package thaumcraft.common.anomaly;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.server.level.ServerLevel;

/**
 * One kind of world anomaly: a rogue-lite modifier that lasts a while and changes the rules of the aura,
 * the casters or the arcane workbench. Simple anomalies only declare {@link #modifier}s; others override
 * the hooks.
 */
public class AnomalyType {
    public enum Polarity {
        POSITIVE(0x55FF55), NEGATIVE(0xFF5555), MIXED(0xFFCC33);

        public final int color;

        Polarity(int color) {
            this.color = color;
        }
    }

    public final String id;
    public final Polarity polarity;
    private int weight = 10;
    private int minDuration = 5 * 1200;
    private int maxDuration = 15 * 1200;
    private int minTier = 1;
    private final EnumMap<AnomalyStat, Float> modifiers = new EnumMap<>(AnomalyStat.class);

    public AnomalyType(String id, Polarity polarity) {
        this.id = id;
        this.polarity = polarity;
    }

    public AnomalyType weight(int weight) {
        this.weight = weight;
        return this;
    }

    public AnomalyType durationMinutes(int min, int max) {
        this.minDuration = min * 1200;
        this.maxDuration = max * 1200;
        return this;
    }

    /** First tier (1 + cycle / 5) in which it can appear. */
    public AnomalyType minTier(int tier) {
        this.minTier = tier;
        return this;
    }

    public AnomalyType modifier(AnomalyStat stat, float multiplier) {
        modifiers.put(stat, multiplier);
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public int getMinDuration() {
        return minDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public int getMinTier() {
        return minTier;
    }

    public float getModifier(AnomalyStat stat) {
        return modifiers.getOrDefault(stat, 1.0f);
    }

    public Map<AnomalyStat, Float> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    public String getTranslationKey() {
        return "tc.anomaly." + id;
    }

    /** Called once when the anomaly starts. */
    public void onStart(ServerLevel level, ActiveAnomaly anomaly) {
    }

    /** Called once a second while the anomaly is active. */
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
    }

    /** True when the anomaly is over before its time (for example, its rift was closed). Checked once a second. */
    public boolean isFinished(ServerLevel level, ActiveAnomaly anomaly) {
        return false;
    }

    /** Called when the anomaly runs out or is stopped. */
    public void onEnd(ServerLevel level, ActiveAnomaly anomaly) {
    }
}
