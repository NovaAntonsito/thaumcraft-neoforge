package thaumcraft.common.world.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Port of WorldGenCustomFlowers: scatters a plant on grass and sand around a spot, 18 tries. */
public class WorldGenCustomFlowers {
    private final Block plantBlock;

    public WorldGenCustomFlowers(Block block) {
        plantBlock = block;
    }

    public boolean generate(WorldGenLevel world, RandomSource par2Random, BlockPos pos) {
        for (int var6 = 0; var6 < 18; ++var6) {
            int var7 = pos.getX() + par2Random.nextInt(8) - par2Random.nextInt(8);
            int var8 = pos.getY() + par2Random.nextInt(4) - par2Random.nextInt(4);
            int var9 = pos.getZ() + par2Random.nextInt(8) - par2Random.nextInt(8);
            BlockPos bp = new BlockPos(var7, var8, var9);
            if (world.isEmptyBlock(bp) && (world.getBlockState(bp.below()).is(Blocks.GRASS_BLOCK) || world.getBlockState(bp.below()).is(Blocks.SAND))) {
                world.setBlock(bp, plantBlock.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        return true;
    }
}
