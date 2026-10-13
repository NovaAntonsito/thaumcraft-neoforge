package thaumcraft.common.blocks.devices;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.blocks.crafting.BlockThaumatorium;
import thaumcraft.common.config.ConfigBlocks;

/**
 * Port of BlockBrainBox, the mnemonic matrix. FACING points at the thaumatorium it hangs on (any side but its
 * front); it drops off when that thaumatorium goes. Each one lets the thaumatorium keep two more recipes.
 */
public class BlockBrainBox extends DirectionalBlock {
    public static final MapCodec<BlockBrainBox> CODEC = simpleCodec(p -> new BlockBrainBox());
    private static final VoxelShape SHAPE = Block.box(3, 3, 3, 13, 13, 13);

    public BlockBrainBox() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f, 6.0f).sound(SoundType.METAL).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState bs = defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
        return bs.canSurvive(context.getLevel(), context.getClickedPos()) ? bs : null;
    }

    /** canPlaceBlockOnSide: on a thaumatorium half, but not on its front. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockState on = world.getBlockState(pos.relative(facing));
        return (on.is(ConfigBlocks.thaumatorium.get()) || on.is(ConfigBlocks.thaumatoriumTop.get()))
                && on.getValue(BlockThaumatorium.FACING) != facing.getOpposite();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos,
                                     BlockPos neighborPos) {
        return direction == state.getValue(FACING) && !canSurvive(state, world, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
