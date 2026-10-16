package thaumcraft.common.anomaly;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * An anomaly running in a dimension, from game time {@code start} to {@code end}. {@code data} is free storage for
 * the anomaly (saved with the world); local anomalies keep their place in it under "x", "y", "z".
 */
public record ActiveAnomaly(String id, long start, long end, CompoundTag data) {
    public ActiveAnomaly(String id, long start, long end) {
        this(id, start, end, new CompoundTag());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putLong("start", start);
        tag.putLong("end", end);
        tag.put("data", data);
        return tag;
    }

    public static ActiveAnomaly load(CompoundTag tag) {
        return new ActiveAnomaly(tag.getString("id"), tag.getLong("start"), tag.getLong("end"), tag.getCompound("data"));
    }

    public AnomalyType type() {
        return Anomalies.get(id);
    }

    public void setLocation(BlockPos pos) {
        data.putInt("x", pos.getX());
        data.putInt("y", pos.getY());
        data.putInt("z", pos.getZ());
    }

    /** Where a local anomaly is, or null for one that covers the whole dimension. */
    @Nullable
    public BlockPos getLocation() {
        if (!data.contains("x")) return null;
        return new BlockPos(data.getInt("x"), data.getInt("y"), data.getInt("z"));
    }
}
