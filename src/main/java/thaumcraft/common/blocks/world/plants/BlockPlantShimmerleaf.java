package thaumcraft.common.blocks.world.plants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.client.fx.ClientFX;

/** Port of BlockPlantShimmerleaf: the glowing flower that grows under silverwood trees. */
public class BlockPlantShimmerleaf extends BushBlock {
    public static final MapCodec<BlockPlantShimmerleaf> CODEC = simpleCodec(p -> new BlockPlantShimmerleaf());

    public BlockPlantShimmerleaf() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS)
                .lightLevel(s -> 6).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    /** canSustainBush (grass, dirt) plus the Plains plant type (grass, dirt, farmland). */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
                || state.is(Blocks.FARMLAND);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BlockPlantCinderpearl.BUSH_SHAPE.move(state.getOffset(level, pos).x, 0.0, state.getOffset(level, pos).z);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(3) == 0) {
            float xr = (float) (pos.getX() + 0.5f + rand.nextGaussian() * 0.1);
            float yr = (float) (pos.getY() + 0.4f + rand.nextGaussian() * 0.1);
            float zr = (float) (pos.getZ() + 0.5f + rand.nextGaussian() * 0.1);
            thaumcraft.client.fx.FXDispatcher.drawWispyMotes(xr, yr, zr, rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01, rand.nextGaussian() * 0.01,
                    10, 0.3f + world.random.nextFloat() * 0.3f, 0.7f + world.random.nextFloat() * 0.3f, 0.7f + world.random.nextFloat() * 0.3f, 0.0f);
        }
    }
}
