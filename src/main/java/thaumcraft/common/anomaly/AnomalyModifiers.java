package thaumcraft.common.anomaly;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;

/** The single entry point other systems use: 1.0 when no anomaly changes that stat. */
public final class AnomalyModifiers {
    private AnomalyModifiers() {
    }

    public static float get(@Nullable Level level, AnomalyStat stat) {
        if (level == null) return 1.0f;
        float mult = 1.0f;
        if (level.isClientSide) {
            for (String id : AnomalyClientState.activeIds()) {
                AnomalyType t = Anomalies.get(id);
                if (t != null) mult *= t.getModifier(stat);
            }
        } else if (level instanceof ServerLevel sl && AnomalyManager.isEnabledIn(sl)) {
            for (ActiveAnomaly a : AnomalyManager.get(sl).getActive()) {
                AnomalyType t = a.type();
                if (t != null) mult *= t.getModifier(stat);
            }
        }
        return mult;
    }

    /** True while an anomaly makes writing free (inspiration). */
    public static boolean freeInk(@Nullable Level level) {
        return get(level, AnomalyStat.INK_COST) <= 0.0f;
    }

    /** Amount of an aspect a crucible recipe needs after anomalies: primal convergence lowers its primal by 30 %. */
    public static int crucibleCost(@Nullable Level level, Aspect aspect, int amount) {
        if (amount <= 0 || !(level instanceof ServerLevel sl) || !AnomalyManager.isEnabledIn(sl)) return amount;
        for (ActiveAnomaly a : AnomalyManager.get(sl).getActive()) {
            if (a.type() instanceof AnomalyPrimalConvergence && aspect == AnomalyPrimalConvergence.getPrimal(a)) {
                return Math.max(1, Math.round(amount * AnomalyPrimalConvergence.COST));
            }
        }
        return amount;
    }

    /** Vis cost of an arcane recipe after anomalies. A recipe that costs vis never drops to 0. */
    public static int arcaneVis(@Nullable Level level, int vis) {
        if (vis <= 0) return vis;
        return Math.max(1, Math.round(vis * get(level, AnomalyStat.ARCANE_VIS_COST)));
    }
}
