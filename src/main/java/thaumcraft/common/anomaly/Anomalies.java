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

    /** Vis storm: bolts of vis strike near players. Most add vis and drop crystals, one in four releases flux. */
    public static final AnomalyType VIS_STORM = register(new AnomalyVisStorm());

    /** Giant void rift: a huge, anchored flux rift appears near a player. Close it with salis mundus before time runs out. */
    public static final AnomalyType GIANT_RIFT = register(new AnomalyGiantRift());

    /** Flux tide: every minute each loaded chunk gains flux, and rifts open three times as easily. */
    public static final AnomalyType FLUX_TIDE = register(AnomalyAuraTide.fluxTide());

    /** Void calm: flux fades from every loaded chunk and no rifts open. */
    public static final AnomalyType VOID_CALM = register(AnomalyAuraTide.voidCalm());

    /** Infusion moon: infusions lose half as much stability. */
    public static final AnomalyType INFUSION_MOON = register(new AnomalyType("infusion_moon", Polarity.POSITIVE) {
        @Override
        public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
            for (ServerPlayer p : level.players()) {
                level.sendParticles(ParticleTypes.GLOW, p.getX(), p.getY() + 6.0, p.getZ(), 3, 8.0, 3.0, 8.0, 0.0);
            }
        }
    }.weight(10).durationMinutes(5, 15)
            .modifier(AnomalyStat.INFUSION_INSTABILITY, 0.5f));

    /** Inspiration: scanning gives twice the aspect points and writing research uses no ink. */
    public static final AnomalyType INSPIRATION = register(new AnomalyType("inspiration", Polarity.POSITIVE) {
        @Override
        public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
            for (ServerPlayer p : level.players()) {
                level.sendParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.5, p.getZ(), 6, 0.6, 0.8, 0.6, 0.8);
            }
        }
    }.weight(10).durationMinutes(5, 15)
            .modifier(AnomalyStat.SCAN_POINTS, 2.0f)
            .modifier(AnomalyStat.INK_COST, 0.0f));

    /** Primal convergence: one primal is drawn; its crucible recipes cost 30 % less and its crystals appear. */
    public static final AnomalyType PRIMAL_CONVERGENCE = register(new AnomalyPrimalConvergence());

    /** Crystal rain: primal vis crystals fall near players. */
    public static final AnomalyType CRYSTAL_RAIN = register(AnomalyCrystalRain.crystalRain());

    /** Vitium rain: vitium crystals fall near players and burst into flux if left on the ground. */
    public static final AnomalyType VITIUM_RAIN = register(AnomalyCrystalRain.vitiumRain());

    /** Inversion: vis and flux swap around players when it starts and when it ends. */
    public static final AnomalyType INVERSION = register(new AnomalyInversion());

    /** Amber vein: a spot near a player fills with amber and cinnabar ore and rich aura. */
    public static final AnomalyType AMBER_VEIN = register(new AnomalyAmberVein());

    /** Eldritch eclipse: the night lasts twice as long and the open night sky gives warp. */
    public static final AnomalyType ELDRITCH_ECLIPSE = register(new AnomalyEldritchEclipse());

    /** Whispers: phantom sounds, and now and then a forgotten aspect point. */
    public static final AnomalyType WHISPERS = register(new AnomalyWhispers());

    /** Runaway focus: foci cost half, but every cast has a random side effect. */
    public static final AnomalyType RUNAWAY_FOCUS = register(new AnomalyRunawayFocus());

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
