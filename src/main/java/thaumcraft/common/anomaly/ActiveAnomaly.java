package thaumcraft.common.anomaly;

import net.minecraft.nbt.CompoundTag;

/** An anomaly running in a dimension, from game time {@code start} to {@code end}. */
public record ActiveAnomaly(String id, long start, long end) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putLong("start", start);
        tag.putLong("end", end);
        return tag;
    }

    public static ActiveAnomaly load(CompoundTag tag) {
        return new ActiveAnomaly(tag.getString("id"), tag.getLong("start"), tag.getLong("end"));
    }

    public AnomalyType type() {
        return Anomalies.get(id);
    }
}
