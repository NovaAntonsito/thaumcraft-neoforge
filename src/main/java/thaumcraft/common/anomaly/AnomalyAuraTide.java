package thaumcraft.common.anomaly;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.common.world.aura.AuraWorld;

/**
 * The two anomalies that move flux in every loaded chunk.
 * <ul>
 * <li><b>Flux tide</b> (negative): every minute each loaded chunk gains {@link #TIDE_FLUX} flux, and rifts open
 * three times as easily.</li>
 * <li><b>Void calm</b> (positive): every second each loaded chunk loses 1 % of its flux (at least
 * {@link #CALM_MIN}), about 45 % a minute, and no rifts open.</li>
 * </ul>
 */
public class AnomalyAuraTide extends AnomalyType {
    public static final float TIDE_FLUX = 3.0f;
    public static final float CALM_MIN = 0.05f;
    private final boolean tide;

    private AnomalyAuraTide(String id, Polarity polarity, boolean tide) {
        super(id, polarity);
        this.tide = tide;
    }

    public static AnomalyAuraTide fluxTide() {
        AnomalyAuraTide t = new AnomalyAuraTide("flux_tide", Polarity.NEGATIVE, true);
        t.weight(10).durationMinutes(5, 15).modifier(AnomalyStat.RIFT_CHANCE, 3.0f);
        return t;
    }

    public static AnomalyAuraTide voidCalm() {
        AnomalyAuraTide t = new AnomalyAuraTide("void_calm", Polarity.POSITIVE, false);
        t.weight(10).durationMinutes(5, 15).modifier(AnomalyStat.RIFT_CHANCE, 0.0f);
        return t;
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        ParticleOptions particle = tide ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.END_ROD;
        for (ServerPlayer p : level.players()) {
            level.sendParticles(particle, p.getX(), p.getY() + 1.5, p.getZ(), tide ? 10 : 3, 8.0, 3.0, 8.0, 0.01);
        }
        boolean pulse = !tide || (level.getGameTime() - anomaly.start()) % 1200L < 20L;
        if (!pulse) return;
        AuraWorld world = AuraHandler.getAuraWorld(level.dimension());
        if (world == null) return;
        // Copied first: changing the aura can add chunks to the map.
        List<AuraChunk> chunks = new ArrayList<>(world.getAuraChunks().values());
        for (AuraChunk c : chunks) {
            ChunkPos loc = c.getLoc();
            BlockPos pos = new BlockPos(loc.getMinBlockX(), 0, loc.getMinBlockZ());
            if (tide) {
                AuraHelper.polluteAura(level, pos, TIDE_FLUX, false);
            } else if (c.getFlux() > 0.0f) {
                AuraHelper.drainFlux(level, pos, Math.max(CALM_MIN, c.getFlux() * 0.01f), false);
            }
        }
    }
}
