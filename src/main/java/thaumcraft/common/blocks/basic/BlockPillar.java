package thaumcraft.common.blocks.basic;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of BlockPillar: the lower half of an infusion altar pillar, formed by salis mundus from arcane stone.
 * The model is two blocks tall and so is the collision box; the outline only covers the bottom half block.
 * Breaking it gives back its two arcane stones (loot table; the original spawned them in breakBlock).
 */
public class BlockPillar extends HorizontalDirectionalBlock {
    public static final MapCodec<BlockPillar> CODEC = simpleCodec(p -> new BlockPillar());
    protected static final VoxelShape SHAPE = box(0, 0, 0, 16, 8, 16);
    protected static final VoxelShape COLLISION = box(0, 0, 0, 16, 32, 16);

    public BlockPillar() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5f).sound(SoundType.STONE)
                .pushReaction(PushReaction.BLOCK).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }
}
