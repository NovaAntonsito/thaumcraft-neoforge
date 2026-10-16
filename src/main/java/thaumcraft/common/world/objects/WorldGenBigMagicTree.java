package thaumcraft.common.world.objects;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Port of WorldGenBigMagicTree: the big oak of the magical forest (vanilla's 1.12 big tree with a taller crown). */
public class WorldGenBigMagicTree extends WorldGenTreeTC {
    private RandomSource rand;
    private WorldGenLevel world;
    private BlockPos basePos = BlockPos.ZERO;
    int heightLimit;
    int height;
    double heightAttenuation = 0.6618;
    double branchSlope = 0.381;
    double scaleWidth = 1.25;
    double leafDensity = 0.9;
    int trunkSize = 1;
    int heightLimitLimit = 11;
    int leafDistanceLimit = 4;
    List<FoliageCoordinates> foliageCoords;

    public WorldGenBigMagicTree(boolean doBlockNotify) {
        super(doBlockNotify);
    }

    /** BlockPos.add(double, double, double) of 1.12: the sum is floored. */
    private static BlockPos add(BlockPos pos, double x, double y, double z) {
        return BlockPos.containing(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    void generateLeafNodeList() {
        height = (int) (heightLimit * heightAttenuation);
        if (height >= heightLimit) {
            height = heightLimit - 1;
        }
        int i = (int) (1.382 + Math.pow(leafDensity * heightLimit / 13.0, 2.0));
        if (i < 1) {
            i = 1;
        }
        int j = basePos.getY() + height;
        int k = heightLimit - leafDistanceLimit;
        (foliageCoords = new ArrayList<>()).add(new FoliageCoordinates(basePos.above(k), j));
        while (k >= 0) {
            float f = layerSize(k);
            if (f >= 0.0f) {
                for (int l = 0; l < i; ++l) {
                    double d0 = scaleWidth * f * (rand.nextFloat() + 0.328);
                    double d2 = rand.nextFloat() * 2.0f * 3.141592653589793;
                    double d3 = d0 * Math.sin(d2) + 0.5;
                    double d4 = d0 * Math.cos(d2) + 0.5;
                    BlockPos blockpos = add(basePos, d3, k - 1, d4);
                    BlockPos blockpos2 = blockpos.above(leafDistanceLimit);
                    if (checkBlockLine(blockpos, blockpos2) == -1) {
                        int i2 = basePos.getX() - blockpos.getX();
                        int j2 = basePos.getZ() - blockpos.getZ();
                        double d5 = blockpos.getY() - Math.sqrt(i2 * i2 + j2 * j2) * branchSlope;
                        int k2 = (d5 > j) ? j : ((int) d5);
                        BlockPos blockpos3 = new BlockPos(basePos.getX(), k2, basePos.getZ());
                        if (checkBlockLine(blockpos3, blockpos) == -1) {
                            foliageCoords.add(new FoliageCoordinates(blockpos, blockpos3.getY()));
                        }
                    }
                }
            }
            --k;
        }
    }

    void crosSection(BlockPos pos, float size, BlockState state) {
        for (int i = (int) (size + 0.618), j = -i; j <= i; ++j) {
            for (int k = -i; k <= i; ++k) {
                if (Math.pow(Math.abs(j) + 0.5, 2.0) + Math.pow(Math.abs(k) + 0.5, 2.0) <= size * size) {
                    BlockPos blockpos = pos.offset(j, 0, k);
                    BlockState bs = world.getBlockState(blockpos);
                    if (bs.isAir() || bs.is(BlockTags.LEAVES)) {
                        setBlockAndNotifyAdequately(world, blockpos, state);
                    }
                }
            }
        }
    }

    float layerSize(int y) {
        if (y < heightLimit * 0.3f) {
            return -1.0f;
        }
        float f = heightLimit / 2.0f;
        float f2 = f - y;
        float f3 = Mth.sqrt(f * f - f2 * f2);
        if (f2 == 0.0f) {
            f3 = f;
        } else if (Math.abs(f2) >= f) {
            return 0.0f;
        }
        return f3 * 0.5f;
    }

    float leafSize(int y) {
        return (y >= 0 && y < leafDistanceLimit) ? ((y != 0 && y != leafDistanceLimit - 1) ? 3.0f : 2.0f) : -1.0f;
    }

    void generateLeafNode(BlockPos pos) {
        for (int i = 0; i < leafDistanceLimit; ++i) {
            crosSection(pos.above(i), leafSize(i), Blocks.OAK_LEAVES.defaultBlockState());
        }
    }

    void limb(BlockPos from, BlockPos to, Block block) {
        BlockPos blockpos2 = to.offset(-from.getX(), -from.getY(), -from.getZ());
        int i = getGreatestDistance(blockpos2);
        float f = blockpos2.getX() / (float) i;
        float f2 = blockpos2.getY() / (float) i;
        float f3 = blockpos2.getZ() / (float) i;
        for (int j = 0; j <= i; ++j) {
            BlockPos blockpos3 = add(from, 0.5f + j * f, 0.5f + j * f2, 0.5f + j * f3);
            Direction.Axis axis = getLogAxis(from, blockpos3);
            setBlockAndNotifyAdequately(world, blockpos3, block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
        }
    }

    private int getGreatestDistance(BlockPos pos) {
        int i = Mth.abs(pos.getX());
        int j = Mth.abs(pos.getY());
        int k = Mth.abs(pos.getZ());
        return (k > i && k > j) ? k : ((j > i) ? j : i);
    }

    private Direction.Axis getLogAxis(BlockPos from, BlockPos to) {
        Direction.Axis axis = Direction.Axis.Y;
        int i = Math.abs(to.getX() - from.getX());
        int j = Math.abs(to.getZ() - from.getZ());
        int k = Math.max(i, j);
        if (k > 0) {
            if (i == k) {
                axis = Direction.Axis.X;
            } else if (j == k) {
                axis = Direction.Axis.Z;
            }
        }
        return axis;
    }

    void generateLeaves() {
        for (FoliageCoordinates foliagecoordinates : foliageCoords) {
            generateLeafNode(foliagecoordinates);
        }
    }

    boolean leafNodeNeedsBase(int y) {
        return y >= heightLimit * 0.2;
    }

    void generateTrunk() {
        BlockPos blockpos = basePos;
        BlockPos blockpos2 = basePos.above(height);
        Block block = Blocks.OAK_LOG;
        limb(blockpos, blockpos2, block);
        if (trunkSize == 2) {
            limb(blockpos.east(), blockpos2.east(), block);
            limb(blockpos.east().south(), blockpos2.east().south(), block);
            limb(blockpos.south(), blockpos2.south(), block);
        }
    }

    void generateLeafNodeBases() {
        for (FoliageCoordinates foliagecoordinates : foliageCoords) {
            int i = foliagecoordinates.getBranchBase();
            BlockPos blockpos = new BlockPos(basePos.getX(), i, basePos.getZ());
            if (leafNodeNeedsBase(i - basePos.getY())) {
                limb(blockpos, foliagecoordinates, Blocks.OAK_LOG);
            }
        }
    }

    int checkBlockLine(BlockPos from, BlockPos to) {
        BlockPos blockpos2 = to.offset(-from.getX(), -from.getY(), -from.getZ());
        int i = getGreatestDistance(blockpos2);
        float f = blockpos2.getX() / (float) i;
        float f2 = blockpos2.getY() / (float) i;
        float f3 = blockpos2.getZ() / (float) i;
        if (i == 0) {
            return -1;
        }
        for (int j = 0; j <= i; ++j) {
            BlockPos blockpos3 = add(from, 0.5f + j * f, 0.5f + j * f2, 0.5f + j * f3);
            if (!isReplaceable(world, blockpos3)) {
                return j;
            }
        }
        return -1;
    }

    public boolean generate(WorldGenLevel worldIn, RandomSource random, BlockPos pos) {
        world = worldIn;
        basePos = pos;
        rand = RandomSource.create(random.nextLong());
        heightLimit = 11 + rand.nextInt(heightLimitLimit);
        if (!validTreeLocation()) {
            world = null;
            return false;
        }
        generateLeafNodeList();
        generateLeaves();
        generateTrunk();
        generateLeafNodeBases();
        world = null;
        return true;
    }

    private boolean validTreeLocation() {
        if (!isSoil(world, basePos.below())) {
            return false;
        }
        int i = checkBlockLine(basePos, basePos.above(heightLimit - 1));
        if (i == -1) {
            return true;
        }
        if (i < 6) {
            return false;
        }
        heightLimit = i;
        return true;
    }

    static class FoliageCoordinates extends BlockPos {
        private final int branchBase;

        FoliageCoordinates(BlockPos pos, int branchBase) {
            super(pos.getX(), pos.getY(), pos.getZ());
            this.branchBase = branchBase;
        }

        int getBranchBase() {
            return branchBase;
        }
    }
}
