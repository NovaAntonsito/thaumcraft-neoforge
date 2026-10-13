package thaumcraft.common.lib.utils;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Partial port of CropUtils: what the lamp of growth needs. The original kept crop lists keyed by block name and
 * metadata (filled by other mods through IMC); here they are keyed by block state.
 * TODO: the standard, clickable and stacked crop lists the harvesting golems use (stage 8).
 */
public final class CropUtils {
    private CropUtils() {
    }

    public static final Set<BlockState> lampBlacklist = new HashSet<>();

    /** Adds every state of the block when allStates is true, as metadata 32767 did. */
    public static void blacklistLamp(Block block, boolean allStates) {
        if (allStates) {
            lampBlacklist.addAll(block.getStateDefinition().getPossibleStates());
        } else {
            lampBlacklist.add(block.defaultBlockState());
        }
    }

    /** A plant that cannot grow any further (IGrowable.canGrow false, but not a stem) or a fully grown crop. */
    public static boolean isGrownCrop(Level world, BlockPos pos) {
        if (world.isEmptyBlock(pos)) {
            return false;
        }
        BlockState bs = world.getBlockState(pos);
        Block bi = bs.getBlock();
        return (bi instanceof BonemealableBlock g && !g.isValidBonemealTarget(world, pos, bs) && !(bi instanceof StemBlock))
                || (bi instanceof CropBlock crop && crop.isMaxAge(bs));
    }

    public static boolean doesLampGrow(Level world, BlockPos pos) {
        return !lampBlacklist.contains(world.getBlockState(pos));
    }
}
