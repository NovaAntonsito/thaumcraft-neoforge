package thaumcraft.api.aspects;

/** Port of IAspectSource. Tiles like jars that act as an essentia source for the infusion altar. */
public interface IAspectSource extends IAspectContainer {
    /** If true it will not act as an aspect source. */
    boolean isBlocked();
}
