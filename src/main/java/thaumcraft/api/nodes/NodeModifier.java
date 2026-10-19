package thaumcraft.api.nodes;

/** The brightness of an aura node, from Thaumcraft 4: how fast it regenerates and how it looks. */
public enum NodeModifier {
    BRIGHT, PALE, FADING;

    /** -1 is no modifier. */
    public static NodeModifier byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : null;
    }
}
