package thaumcraft.api.aspects;

/**
 * Port of IAspectContainer. Used by blocks like the crucible and alembic to hold their aspects.
 * Tiles implementing it show their aspects when viewed with goggles of revealing.
 */
public interface IAspectContainer {
    AspectList getAspects();

    void setAspects(AspectList aspects);

    /** Can this aspect be added to the container? */
    boolean doesContainerAccept(Aspect tag);

    /** Adds an amount of an aspect. Returns the amount left over that could not be added. */
    int addToContainer(Aspect tag, int amount);

    /** Removes an amount of an aspect. Returns true if that amount was available and was removed. */
    boolean takeFromContainer(Aspect tag, int amount);

    /** Does the container hold at least this amount of the aspect? */
    boolean doesContainerContainAmount(Aspect tag, int amount);

    /** How much of the aspect the container holds. */
    int containerContains(Aspect tag);

    // The deprecated AspectList overloads of takeFromContainer and doesContainerContain were dropped:
    // the original marked them "going away in the next major patch".
}
