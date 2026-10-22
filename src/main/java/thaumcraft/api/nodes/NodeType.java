package thaumcraft.api.nodes;

/** The kinds of aura node, from Thaumcraft 4. Each changes how a node looks and what it does to its surroundings. */
public enum NodeType {
    NORMAL, UNSTABLE, DARK, TAINTED, HUNGRY, PURE;

    public static NodeType byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : NORMAL;
    }
}
