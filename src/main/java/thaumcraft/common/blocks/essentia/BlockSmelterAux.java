package thaumcraft.common.blocks.essentia;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of BlockSmelterAux (auxiliary slurry pump). Attached to the side of an essentia smeltery, FACING pointing
 * at it, it feeds the alembics stacked on top of the pump. Also the base of {@link BlockSmelterVent}.
 */
public class BlockSmelterAux extends HorizontalDirectionalBlock {
    public static final MapCodec<BlockSmelterAux> CODEC = simpleCodec(p -> new BlockSmelterAux());

    public BlockSmelterAux() {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f, 10.0f).sound(SoundType.METAL).noOcclusion());
    }

    protected BlockSmelterAux(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** canPlaceBlockOnSide: only on a side of a smeltery other than its front. */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (!side.getAxis().isHorizontal()) {
            return null;
        }
        BlockState smelter = context.getLevel().getBlockState(context.getClickedPos().relative(side.getOpposite()));
        if (!(smelter.getBlock() instanceof BlockSmelter) || smelter.getValue(BlockSmelter.FACING) == side) {
            return null;
        }
        return defaultBlockState().setValue(FACING, side.getOpposite());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }
}
