package thaumcraft.api.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Port of IInfusionStabiliser. Blocks that implement this interface act as infusion crafting stabilisers
 * like candles and skulls.
 * <p>
 * Deprecated in the original: it will eventually be combined with IInfusionStabiliserExt.
 */
@Deprecated
public interface IInfusionStabiliser {
    /** Returns true if the block can stabilise things. */
    boolean canStabaliseInfusion(Level world, BlockPos pos);
}
