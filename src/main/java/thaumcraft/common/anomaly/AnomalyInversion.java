package thaumcraft.common.anomaly;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import thaumcraft.api.aura.AuraHelper;

/**
 * Inversion (mixed). When it starts and again when it ends, vis and flux swap places in the chunks around each
 * player (5 by 5 chunks). A clean area turns foul for a while, a polluted one turns rich.
 */
public class AnomalyInversion extends AnomalyType {
    public static final int RADIUS = 2;

    public AnomalyInversion() {
        super("inversion", Polarity.MIXED);
        weight(6).durationMinutes(5, 10);
    }

    @Override
    public void onStart(ServerLevel level, ActiveAnomaly anomaly) {
        invert(level);
    }

    @Override
    public void onEnd(ServerLevel level, ActiveAnomaly anomaly) {
        invert(level);
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        for (ServerPlayer p : level.players()) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1.0, p.getZ(), 4, 6.0, 2.0, 6.0, 0.0);
        }
    }

    private static void invert(ServerLevel level) {
        Set<ChunkPos> done = new HashSet<>();
        for (ServerPlayer p : level.players()) {
            ChunkPos center = p.chunkPosition();
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    ChunkPos c = new ChunkPos(center.x + x, center.z + z);
                    if (!done.add(c)) continue;
                    BlockPos pos = new BlockPos(c.getMinBlockX(), 0, c.getMinBlockZ());
                    float vis = AuraHelper.getVis(level, pos);
                    float flux = AuraHelper.getFlux(level, pos);
                    AuraHelper.drainVis(level, pos, vis, false);
                    AuraHelper.drainFlux(level, pos, flux, false);
                    if (flux > 0.0f) AuraHelper.addVis(level, pos, flux);
                    if (vis > 0.0f) AuraHelper.polluteAura(level, pos, vis, false);
                }
            }
            level.playSound(null, p.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.AMBIENT, 1.0f, 0.6f);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1.0, p.getZ(), 200, 8.0, 3.0, 8.0, 0.1);
        }
    }
}
