package thaumcraft.common.world.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.eldritch.TileEldritchAltar;
import thaumcraft.common.tiles.misc.TileBanner;

/**
 * Port of TC4's WorldGenEldritchRing: a 7x7 floor of obsidian and obsidian tile (the corners left out) with the
 * altar keystone and its floating obelisk in the middle and eight capstones on the edges. Four in ten altars are
 * cultist shrines (ritualist clerics, then knights, with a crimson banner on each side), two in ten call eldritch
 * guardians.
 */
public class WorldGenEldritchRing {
    public int chunkX;
    public int chunkZ;
    public int width;
    public int height = 0;

    private static final Block[] VALID = {Blocks.STONE, Blocks.SAND, Blocks.PACKED_ICE, Blocks.GRASS_BLOCK, Blocks.GRAVEL, Blocks.DIRT};

    private static boolean isValid(Block b) {
        for (Block v : VALID) {
            if (b == v) {
                return true;
            }
        }
        return false;
    }

    /** The top of the ground here is at most two blocks above j and is one of the valid blocks. */
    public boolean locationIsValidSpawn(WorldGenLevel world, int i, int j, int k) {
        int distanceToAir = 0;
        while (!world.isEmptyBlock(new BlockPos(i, j + distanceToAir, k))) {
            ++distanceToAir;
            if (distanceToAir > 2) {
                return false;
            }
        }
        j += distanceToAir - 1;
        Block block = world.getBlockState(new BlockPos(i, j, k)).getBlock();
        Block above = world.getBlockState(new BlockPos(i, j + 1, k)).getBlock();
        Block below = world.getBlockState(new BlockPos(i, j - 1, k)).getBlock();
        if (above != Blocks.AIR) {
            return false;
        }
        if (isValid(block)) {
            return true;
        }
        return (block == Blocks.SNOW || block == Blocks.SHORT_GRASS) && isValid(below);
    }

    public boolean generate(WorldGenLevel world, RandomSource rand, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (thaumcraft.common.world.dim.MazeHandler.mazesInRange(chunkX, chunkZ, width, height)
                || !locationIsValidSpawn(world, i - 3, j, k - 3) || !locationIsValidSpawn(world, i, j, k) || !locationIsValidSpawn(world, i + 3, j, k)
                || !locationIsValidSpawn(world, i + 3, j, k + 3) || !locationIsValidSpawn(world, i, j, k + 3)) {
            return false;
        }
        BlockState tile = ConfigBlocks.obsidianTile.get().defaultBlockState();
        for (int x = i - 3; x <= i + 3; ++x) {
            for (int z = k - 3; z <= k + 3; ++z) {
                if ((x == i - 3 || x == i + 3) && (z == k - 3 || z == k + 3)) {
                    continue;
                }
                for (int q = -4; q < 5; ++q) {
                    BlockPos p = new BlockPos(x, j + q, z);
                    if (q <= 0) {
                        world.setBlock(p, rand.nextInt(4) == 0 ? Blocks.OBSIDIAN.defaultBlockState() : tile, 3);
                    } else {
                        world.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                if (x == i && z == k) {
                    BlockPos altarPos = new BlockPos(x, j + 1, z);
                    world.setBlock(altarPos, ConfigBlocks.eldritchAltar.get().defaultBlockState(), 3);
                    world.setBlock(new BlockPos(x, j, z), tile, 3);
                    int r = rand.nextInt(10);
                    if (world.getBlockEntity(altarPos) instanceof TileEldritchAltar altar) {
                        switch (r) {
                            case 1, 2, 3, 4 -> {
                                altar.setSpawner(true);
                                altar.setSpawnType((byte) 0);
                                // ForgeDirection 2-5: north, south, west, east; the banner goes on the opposite x.
                                int[][] dirs = {{0, -1, 8}, {0, 1, 0}, {-1, 0, 12}, {1, 0, 4}};
                                for (int[] d : dirs) {
                                    BlockPos bp = new BlockPos(x - d[0] * 3, j + 1, z + d[1] * 3);
                                    world.setBlock(bp, ConfigBlocks.bannerCrimsonCult.get().defaultBlockState(), 3);
                                    if (world.getBlockEntity(bp) instanceof TileBanner banner) {
                                        banner.setBannerFacing((byte) d[2]);
                                    }
                                }
                            }
                            case 6, 7 -> {
                                altar.setSpawner(true);
                                altar.setSpawnType((byte) 1);
                            }
                            default -> {
                            }
                        }
                    }
                    world.setBlock(new BlockPos(x, j + 3, z), ConfigBlocks.eldritchObelisk.get().defaultBlockState(), 3);
                    for (int y = 4; y <= 7; ++y) {
                        world.setBlock(new BlockPos(x, j + y, z), ConfigBlocks.eldritchObeliskBody.get().defaultBlockState(), 3);
                    }
                    // TC4 (dream branch): the dark node over the obelisk.
                    if (thaumcraft.Config.GENERATE_NODES.get()) {
                        thaumcraft.common.world.ThaumcraftWorldGenerator.createRandomNodeAt(world, new BlockPos(x, j + 9, z), rand, false, true, false);
                    }
                } else if (((x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1 || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1)
                        && Math.abs(x - i) != Math.abs(z - k)) {
                    world.setBlock(new BlockPos(x, j, z), tile, 3);
                    world.setBlock(new BlockPos(x, j + 1, z), ConfigBlocks.eldritchCap.get().defaultBlockState(), 3);
                }
            }
        }
        return true;
    }
}
