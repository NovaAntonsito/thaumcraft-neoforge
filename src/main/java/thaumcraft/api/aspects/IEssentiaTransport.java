package thaumcraft.api.aspects;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;

/**
 * Port of IEssentiaTransport. Only tiles implementing it can connect to essentia tubes or other thaumic devices.
 */
public interface IEssentiaTransport {
    /** Can this tile connect to other essentia users/sources on this side? */
    boolean isConnectable(Direction face);

    /** Is this side used to input essentia? */
    boolean canInputFrom(Direction face);

    /** Is this side used to output essentia? */
    boolean canOutputTo(Direction face);

    /** Sets the amount of suction this block will apply. */
    void setSuction(@Nullable Aspect aspect, int amount);

    /** The type of suction applied. Null means untyped: the first thing available will be drawn. */
    @Nullable
    Aspect getSuctionType(Direction face);

    /** The strength of suction applied. */
    int getSuctionAmount(Direction face);

    /** Removes essentia from this tile. Returns how much was actually taken. */
    int takeEssentia(Aspect aspect, int amount, Direction face);

    /** Adds essentia to this tile. Returns how much was actually added. */
    int addEssentia(Aspect aspect, int amount, Direction face);

    /** What type of essentia this contains. */
    @Nullable
    Aspect getEssentiaType(Direction face);

    /** How much essentia this contains. */
    int getEssentiaAmount(Direction face);

    /** Essentia will not be drawn from this container unless the suction exceeds this amount. */
    int getMinimumSuction();
}
