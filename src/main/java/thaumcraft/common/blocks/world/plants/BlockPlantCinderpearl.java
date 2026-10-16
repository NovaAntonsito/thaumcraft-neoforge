package thaumcraft.common.blocks.world.plants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Port of BlockPlantCinderpearl: a desert flower that smokes and burns. */
public class BlockPlantCinderpearl extends BushBlock {
    public static final MapCodec<BlockPlantCinderpearl> CODEC = simpleCodec(p -> new BlockPlantCinderpearl());
    /** BlockBush's bounding box. */
    static final VoxelShape BUSH_SHAPE = Block.box(4.8, 0.0, 4.8, 11.2, 9.6, 11.2);

    public BlockPlantCinderpearl() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS)
                .lightLevel(s -> 7).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    /** canSustainBush (sand, dirt, terracotta) plus the Desert plant type (sand, terracotta). Sand covers red sand. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL) || state.is(BlockTags.TERRACOTTA);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BUSH_SHAPE.move(state.getOffset(level, pos).x, 0.0, state.getOffset(level, pos).z);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (rand.nextBoolean()) {
            float xr = pos.getX() + 0.5f + (rand.nextFloat() - rand.nextFloat()) * 0.1f;
            float yr = pos.getY() + 0.6f + (rand.nextFloat() - rand.nextFloat()) * 0.1f;
            float zr = pos.getZ() + 0.5f + (rand.nextFloat() - rand.nextFloat()) * 0.1f;
            world.addParticle(ParticleTypes.SMOKE, xr, yr, zr, 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.FLAME, xr, yr, zr, 0.0, 0.0, 0.0);
        }
    }
}
