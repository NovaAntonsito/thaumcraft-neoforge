package thaumcraft.common.world.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The parts of 1.12's WorldGenAbstractTree / WorldGenerator that the Thaumcraft trees use, for a {@link WorldGenLevel}
 * (the world generation region, or the server level when a sapling grows).
 */
public abstract class WorldGenTreeTC {
    protected final boolean doBlockNotify;

    protected WorldGenTreeTC(boolean doBlockNotify) {
        this.doBlockNotify = doBlockNotify;
    }

    /**
     * WorldGenerator.setBlockAndNotifyAdequately. Leaves are placed with the default (furthest) distance and get a
     * scheduled tick that works out their real distance to the logs, as 1.21 leaves need.
     */
    protected void setBlockAndNotifyAdequately(WorldGenLevel world, BlockPos pos, BlockState state) {
        world.setBlock(pos, state, doBlockNotify ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS);
        if (state.getBlock() instanceof LeavesBlock) {
            world.scheduleTick(pos, state.getBlock(), 1);
        }
    }

    /** getStateFromMeta of BlockLogsTC: the metadata was the axis (0 x, 1 y, 2 z). */
    protected static BlockState log(Block block, int meta) {
        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.values()[meta]);
    }

    /** WorldGenAbstractTree.canGrowInto: air, leaves, grass, dirt, logs, saplings and vines. */
    public static boolean canGrowInto(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.DIRT) || state.is(BlockTags.LOGS)
                || state.is(BlockTags.SAPLINGS) || state.is(Blocks.VINE);
    }

    /** WorldGenAbstractTree.isReplaceable. */
    public static boolean isReplaceable(BlockGetter world, BlockPos pos) {
        return canGrowInto(world.getBlockState(pos));
    }

    /** Block.canBeReplacedByLeaves of 1.12: air or leaves. */
    public static boolean canBeReplacedByLeaves(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES);
    }

    /** canSustainPlant(..., UP, Blocks.SAPLING): what an oak sapling could be planted on. */
    public static boolean isSoil(BlockGetter world, BlockPos pos) {
        BlockState soil = world.getBlockState(pos);
        BlockState sapling = Blocks.OAK_SAPLING.defaultBlockState();
        var result = soil.canSustainPlant(world, pos, Direction.UP, sapling);
        if (!result.isDefault()) {
            return result.isTrue();
        }
        return soil.is(BlockTags.DIRT) || soil.is(Blocks.FARMLAND);
    }

    /** Block.onPlantGrow of 1.12: grass and farmland under a growing tree turn to dirt. */
    public static void onPlantGrow(WorldGenLevel world, BlockPos pos) {
        BlockState soil = world.getBlockState(pos);
        if (soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.FARMLAND)) {
            world.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
