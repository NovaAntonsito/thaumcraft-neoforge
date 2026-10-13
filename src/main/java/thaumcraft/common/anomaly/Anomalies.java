package thaumcraft.common.anomaly;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.common.anomaly.AnomalyType.Polarity;

/** The anomaly registry and the anomalies themselves. Ids are what the commands, the save and the packets use. */
public final class Anomalies {
    private static final Map<String, AnomalyType> REGISTRY = new LinkedHashMap<>();

    /** Aurora arcana: vis regenerates twice as fast and arcane recipes cost 25 % less vis. */
    public static final AnomalyType AURORA_ARCANA = register(new AnomalyType("aurora_arcana", Polarity.POSITIVE) {
        @Override
        public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
            for (ServerPlayer p : level.players()) {
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 10.0, p.getZ(), 4, 10.0, 2.0, 10.0, 0.01);
            }
        }
    }.weight(10).durationMinutes(5, 15)
            .modifier(AnomalyStat.VIS_REGEN, 2.0f)
            .modifier(AnomalyStat.ARCANE_VIS_COST, 0.75f));

    /** Aura drought: vis regenerates at half speed and arcane recipes and foci cost 50 % more vis. */
    public static final AnomalyType AURA_DROUGHT = register(new AnomalyType("aura_drought", Polarity.NEGATIVE) {
        @Override
        public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
            for (ServerPlayer p : level.players()) {
                level.sendParticles(ParticleTypes.WHITE_ASH, p.getX(), p.getY() + 2.0, p.getZ(), 12, 8.0, 3.0, 8.0, 0.0);
            }
        }
    }.weight(10).durationMinutes(5, 15)
            .modifier(AnomalyStat.VIS_REGEN, 0.5f)
            .modifier(AnomalyStat.ARCANE_VIS_COST, 1.5f)
            .modifier(AnomalyStat.FOCUS_VIS_COST, 1.5f));

    private Anomalies() {
    }

    public static AnomalyType register(AnomalyType type) {
        if (REGISTRY.containsKey(type.id)) throw new IllegalStateException("Duplicate anomaly " + type.id);
        REGISTRY.put(type.id, type);
        return type;
    }

    @Nullable
    public static AnomalyType get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<AnomalyType> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }
}
