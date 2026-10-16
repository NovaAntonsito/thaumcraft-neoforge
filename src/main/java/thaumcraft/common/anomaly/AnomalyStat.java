package thaumcraft.common.anomaly;

/** What an anomaly changes. Systems multiply their value by {@link AnomalyModifiers#get}. */
public enum AnomalyStat {
    /** How fast vis regenerates towards the chunk base ({@code AuraThread}). */
    VIS_REGEN,
    /** Vis cost of arcane workbench recipes. */
    ARCANE_VIS_COST,
    /** Vis cost of casting foci. */
    FOCUS_VIS_COST,
    /** Chance that a chunk with too much flux opens a rift ({@code AuraThread}). 0 means no rifts. */
    RIFT_CHANCE,
    /** Stability lost per infusion cycle ({@code TileInfusionMatrix}). */
    INFUSION_INSTABILITY,
    /** Aspect points gained by scanning ({@code ScanGeneric}). */
    SCAN_POINTS,
    /** Ink used by the research table and research notes. 0 means writing is free. */
    INK_COST
}
