package thaumcraft.common.blocks.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import thaumcraft.client.fx.ClientFX;

/**
 * Port of BlockGrassAmbient: grass of the magical forest that, at night, sends up wispy motes from the grass around
 * it.
 */
public class BlockGrassAmbient extends GrassBlock {
    public BlockGrassAmbient() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).randomTicks().strength(0.6f).sound(SoundType.GRAVEL));
    }

    /** BlockGrass.updateTick of 1.12: dies to dirt in the dark and spreads plain grass, not itself. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Blocks.GRASS_BLOCK.defaultBlockState().randomTick(level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level worldIn, BlockPos pos, RandomSource rand) {
        int i = worldIn.getBrightness(LightLayer.SKY, pos.above()) - worldIn.getSkyDarken();
        float f = worldIn.getSunAngle(1.0f);
        float f2 = (f < 3.1415927f) ? 0.0f : 6.2831855f;
        f += (f2 - f) * 0.2f;
        i = Math.round(i * Mth.cos(f));
        i = Mth.clamp(i, 0, 15);
        if (4 + i * 2 < 1 + rand.nextInt(13)) {
            int x = Mth.nextInt(rand, -8, 8);
            int z = Mth.nextInt(rand, -8, 8);
            BlockPos pp = pos.offset(x, 5, z);
            for (int q = 0; q < 10 && pp.getY() > 50 && !worldIn.getBlockState(pp).is(Blocks.GRASS_BLOCK); pp = pp.below(), ++q) {
            }
            if (worldIn.getBlockState(pp).is(Blocks.GRASS_BLOCK)) {
                thaumcraft.client.fx.FXDispatcher.drawWispyMotesOnBlock(pp.above(), 400, -0.01f);
            }
        }
    }
}
