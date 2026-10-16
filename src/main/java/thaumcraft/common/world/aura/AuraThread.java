package thaumcraft.common.world.aura;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/**
 * Port of thaumcraft.common.world.aura.AuraThread: once per second, every loaded aura chunk regenerates
 * towards its base, spreads vis and flux to its neighbours, and slowly turns excess vis into flux.
 * <p>
 * The original ran this on its own thread per dimension. Here {@link #tick} runs on the server thread,
 * once every 20 ticks, so the chunk data is never touched from two threads. The maths is unchanged.
 */
public class AuraThread {
    private final Random rand = new Random(System.currentTimeMillis());
    private float phaseVis = 0.0f;
    private float phaseFlux = 0.0f;
    private float phaseMax = 0.0f;
    private long lastWorldTime = 0L;
    /** Vis regeneration multiplier from active anomalies, read once per pass. */
    private float visRegenMult = 1.0f;
    /** Rift chance multiplier from active anomalies, read once per pass. */
    private float riftMult = 1.0f;
    private final float[] phaseTable = {0.25f, 0.15f, 0.1f, 0.05f, 0.0f, 0.05f, 0.1f, 0.15f};
    private final float[] maxTable = {0.15f, 0.05f, 0.0f, -0.05f, -0.15f, -0.05f, 0.0f, 0.05f};

    public void tick(ServerLevel world) {
        AuraWorld auraWorld = AuraHandler.getAuraWorld(world.dimension());
        if (auraWorld == null || lastWorldTime == world.getDayTime()) return;
        lastWorldTime = world.getDayTime();
        int moon = world.getMoonPhase();
        phaseVis = phaseTable[moon];
        phaseMax = 1.0f + maxTable[moon];
        phaseFlux = 0.25f - phaseVis;
        visRegenMult = thaumcraft.common.anomaly.AnomalyModifiers.get(world, thaumcraft.common.anomaly.AnomalyStat.VIS_REGEN);
        riftMult = thaumcraft.common.anomaly.AnomalyModifiers.get(world, thaumcraft.common.anomaly.AnomalyStat.RIFT_CHANCE);
        for (AuraChunk auraChunk : auraWorld.auraChunks.values()) {
            processAuraChunk(auraWorld, auraChunk);
        }
    }

    private void processAuraChunk(AuraWorld auraWorld, AuraChunk auraChunk) {
        List<Integer> directions = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(directions, rand);
        int x = auraChunk.loc.x;
        int y = auraChunk.loc.z;
        float base = auraChunk.getBase() * phaseMax;
        boolean dirty = false;
        float currentVis = auraChunk.getVis();
        float currentFlux = auraChunk.getFlux();
        AuraChunk neighbourVisChunk = null;
        AuraChunk neighbourFluxChunk = null;
        float lowestVis = Float.MAX_VALUE;
        float lowestFlux = Float.MAX_VALUE;
        for (Integer a : directions) {
            Direction dir = Direction.from2DDataValue(a);
            AuraChunk n = auraWorld.getAuraChunkAt(x + dir.getStepX(), y + dir.getStepZ());
            if (n != null) {
                if ((neighbourVisChunk == null || lowestVis > n.getVis()) && n.getVis() + n.getFlux() < n.getBase() * phaseMax) {
                    neighbourVisChunk = n;
                    lowestVis = n.getVis();
                }
                if (neighbourFluxChunk != null && lowestFlux <= n.getFlux()) {
                    continue;
                }
                neighbourFluxChunk = n;
                lowestFlux = n.getFlux();
            }
        }
        if (neighbourVisChunk != null && lowestVis < currentVis && lowestVis / currentVis < 0.75) {
            float inc = Math.min(currentVis - lowestVis, 1.0f);
            currentVis -= inc;
            neighbourVisChunk.setVis(lowestVis + inc);
            dirty = true;
            neighbourVisChunk.markDirty();
        }
        if (neighbourFluxChunk != null && currentFlux > Math.max(5.0f, auraChunk.getBase() / 10.0f) && lowestFlux < currentFlux / 1.75) {
            float inc = Math.min(currentFlux - lowestFlux, 1.0f);
            currentFlux -= inc;
            neighbourFluxChunk.setFlux(lowestFlux + inc);
            dirty = true;
            neighbourFluxChunk.markDirty();
        }
        if (currentVis + currentFlux < base) {
            float inc = Math.min(base - (currentVis + currentFlux), phaseVis * visRegenMult);
            currentVis += inc;
            dirty = true;
        } else if (currentVis > base * 1.25 && rand.nextFloat() < 0.1) {
            currentFlux += phaseFlux;
            currentVis -= phaseFlux;
            dirty = true;
        } else if (currentVis <= base * 0.1 && currentVis >= currentFlux && rand.nextFloat() < 0.1) {
            currentFlux += phaseFlux;
            dirty = true;
        }
        if (dirty) {
            auraChunk.setVis(currentVis);
            auraChunk.setFlux(currentFlux);
            auraChunk.markDirty();
        }
        if (currentFlux > base * 0.75 && rand.nextFloat() < currentFlux / 500.0f / 10.0f * riftMult) {
            AuraHandler.riftTrigger.put(auraWorld.dim, new BlockPos(x * 16, 0, y * 16));
        }
    }
}
