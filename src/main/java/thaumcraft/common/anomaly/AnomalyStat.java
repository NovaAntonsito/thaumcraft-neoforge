package thaumcraft.common.anomaly;

/** What an anomaly changes. Systems multiply their value by {@link AnomalyModifiers#get}. */
public enum AnomalyStat {
    /** How fast vis regenerates towards the chunk base ({@code AuraThread}). */
    VIS_REGEN,
    /** Vis cost of arcane workbench recipes. */
    ARCANE_VIS_COST,
    /** Vis cost of casting foci. */
    FOCUS_VIS_COST
}
