package thaumcraft.common.world.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.config.ConfigBlocks;

/**
 * Port of WorldGenSilverwoodTrees: a straight cross-shaped trunk with roots and a round crown. Trees made by world
 * generation get a patch of shimmerleaf around them.
 */
public class WorldGenSilverwoodTrees extends WorldGenTreeTC {
    private final int minTreeHeight;
    private final int randomTreeHeight;
    boolean worldgen;

    public WorldGenSilverwoodTrees(boolean doBlockNotify, int minTreeHeight, int randomTreeHeight) {
        super(doBlockNotify);
        worldgen = !doBlockNotify;
        this.minTreeHeight = minTreeHeight;
        this.randomTreeHeight = randomTreeHeight;
    }

    /** 1.12's 0-256 world height checks use the level's build limits. */
    public boolean generate(WorldGenLevel world, RandomSource random, BlockPos pos) {
        int height = random.nextInt(randomTreeHeight) + minTreeHeight;
        boolean flag = true;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int minY = world.getMinBuildHeight();
        int maxY = world.getMaxBuildHeight();
        if (y < minY + 1 || y + height + 1 > maxY) {
            return false;
        }
        for (int i1 = y; i1 <= y + 1 + height; ++i1) {
            byte spread = 1;
            if (i1 == y) {
                spread = 0;
            }
            if (i1 >= y + 1 + height - 2) {
                spread = 3;
            }
            for (int j1 = x - spread; j1 <= x + spread && flag; ++j1) {
                for (int k1 = z - spread; k1 <= z + spread && flag; ++k1) {
                    if (i1 >= minY && i1 < maxY) {
                        BlockState state = world.getBlockState(new BlockPos(j1, i1, k1));
                        if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.canBeReplaced() && i1 > y) {
                            flag = false;
                        }
                    } else {
                        flag = false;
                    }
                }
            }
        }
        if (!flag) {
            return false;
        }
        Block logBlock = ConfigBlocks.logSilverwood.get();
        BlockState leaves = ConfigBlocks.leafSilverwood.get().defaultBlockState();
        if (isSoil(world, new BlockPos(x, y - 1, z)) && y < maxY - height - 1) {
            onPlantGrow(world, new BlockPos(x, y - 1, z));
            int start = y + height - 5;
            for (int end = y + height + 3 + random.nextInt(3), k2 = start; k2 <= end; ++k2) {
                int cty = Mth.clamp(k2, y + height - 3, y + height);
                for (int xx = x - 5; xx <= x + 5; ++xx) {
                    for (int zz = z - 5; zz <= z + 5; ++zz) {
                        double d3 = xx - x;
                        double d4 = k2 - cty;
                        double d5 = zz - z;
                        double dist = d3 * d3 + d4 * d4 + d5 * d5;
                        BlockState s2 = world.getBlockState(new BlockPos(xx, k2, zz));
                        if (dist < 10 + random.nextInt(8) && canBeReplacedByLeaves(s2)) {
                            setBlockAndNotifyAdequately(world, new BlockPos(xx, k2, zz), leaves);
                        }
                    }
                }
            }
            int k2;
            for (k2 = 0; k2 < height; ++k2) {
                BlockState s3 = world.getBlockState(new BlockPos(x, y + k2, z));
                if (s3.isAir() || s3.is(BlockTags.LEAVES) || s3.canBeReplaced()) {
                    setBlockAndNotifyAdequately(world, new BlockPos(x, y + k2, z), log(logBlock, 1));
                    setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + k2, z), log(logBlock, 1));
                    setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + k2, z), log(logBlock, 1));
                    setBlockAndNotifyAdequately(world, new BlockPos(x, y + k2, z - 1), log(logBlock, 1));
                    setBlockAndNotifyAdequately(world, new BlockPos(x, y + k2, z + 1), log(logBlock, 1));
                }
            }
            setBlockAndNotifyAdequately(world, new BlockPos(x, y + k2, z), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y, z - 1), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y, z + 1), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y, z + 1), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y, z - 1), log(logBlock, 1));
            if (random.nextInt(3) != 0) {
                setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + 1, z - 1), log(logBlock, 1));
            }
            if (random.nextInt(3) != 0) {
                setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + 1, z + 1), log(logBlock, 1));
            }
            if (random.nextInt(3) != 0) {
                setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + 1, z + 1), log(logBlock, 1));
            }
            if (random.nextInt(3) != 0) {
                setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + 1, z - 1), log(logBlock, 1));
            }
            setBlockAndNotifyAdequately(world, new BlockPos(x - 2, y, z), log(logBlock, 0));
            setBlockAndNotifyAdequately(world, new BlockPos(x + 2, y, z), log(logBlock, 0));
            setBlockAndNotifyAdequately(world, new BlockPos(x, y, z - 2), log(logBlock, 2));
            setBlockAndNotifyAdequately(world, new BlockPos(x, y, z + 2), log(logBlock, 2));
            setBlockAndNotifyAdequately(world, new BlockPos(x - 2, y - 1, z), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x + 2, y - 1, z), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x, y - 1, z - 2), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x, y - 1, z + 2), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + (height - 4), z - 1), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + (height - 4), z + 1), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + (height - 4), z + 1), log(logBlock, 1));
            setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + (height - 4), z - 1), log(logBlock, 1));
            if (random.nextInt(3) == 0) {
                setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + (height - 5), z - 1), log(logBlock, 1));
            }
            if (random.nextInt(3) == 0) {
                setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + (height - 5), z + 1), log(logBlock, 1));
            }
            if (random.nextInt(3) == 0) {
                setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + (height - 5), z + 1), log(logBlock, 1));
            }
            if (random.nextInt(3) == 0) {
                setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + (height - 5), z - 1), log(logBlock, 1));
            }
            setBlockAndNotifyAdequately(world, new BlockPos(x - 2, y + (height - 4), z), log(logBlock, 0));
            setBlockAndNotifyAdequately(world, new BlockPos(x + 2, y + (height - 4), z), log(logBlock, 0));
            setBlockAndNotifyAdequately(world, new BlockPos(x, y + (height - 4), z - 2), log(logBlock, 2));
            setBlockAndNotifyAdequately(world, new BlockPos(x, y + (height - 4), z + 2), log(logBlock, 2));
            if (worldgen) {
                new WorldGenCustomFlowers(ConfigBlocks.shimmerleaf.get()).generate(world, random, new BlockPos(x, y, z));
            }
            return true;
        }
        return false;
    }

    /** Only replaces air, leaves and replaceable blocks, so the roots and branches never cut through the ground. */
    @Override
    protected void setBlockAndNotifyAdequately(WorldGenLevel worldIn, BlockPos pos, BlockState state) {
        BlockState bs = worldIn.getBlockState(pos);
        if (bs.isAir() || bs.is(BlockTags.LEAVES) || bs.canBeReplaced()) {
            super.setBlockAndNotifyAdequately(worldIn, pos, state);
        }
    }
}
