package thaumcraft.common.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Partial port of Utils: what the golem seals need.
 * TODO: the rest of the original helpers when something needs them.
 */
public final class Utils {
    private Utils() {
    }

    /**
     * Block.isWood, canSustainLeaves or the "logWood" ore dictionary in 1.12. The minecraft:logs tag covers all three
     * (it includes the modded logs that are tagged c:/minecraft logs).
     */
    public static boolean isWoodLog(BlockGetter world, BlockPos pos) {
        BlockState bs = world.getBlockState(pos);
        return bs.is(BlockTags.LOGS);
    }
}
