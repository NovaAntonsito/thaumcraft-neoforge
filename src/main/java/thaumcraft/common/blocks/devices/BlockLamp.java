package thaumcraft.common.blocks.devices;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.tiles.devices.TileLampArcane;
import thaumcraft.common.tiles.devices.TileLampFertility;
import thaumcraft.common.tiles.devices.TileLampGrowth;

/**
 * Port of BlockLamp (arcane lamp, lamp of growth, lamp of fertility). FACING points at the block the lamp hangs
 * from; the lamp pops off when that block is gone. ENABLED lights it (light 15) and a redstone signal switches it
 * off (BlockTCDevice.updateState).
 */
public class BlockLamp extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    private static final VoxelShape SHAPE = Block.box(4, 2, 4, 12, 14, 12);

    private final BiFunction<BlockPos, BlockState, ? extends TileThaumcraft> factory;
    private final MapCodec<BlockLamp> codec;

    public BlockLamp(BiFunction<BlockPos, BlockState, ? extends TileThaumcraft> factory) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f, 5.0f).sound(SoundType.METAL).noOcclusion()
                .lightLevel(s -> s.getValue(ENABLED) ? 15 : 0));
        this.factory = factory;
        this.codec = simpleCodec(p -> new BlockLamp(factory));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, true));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    /** Hangs from the clicked block; starts unlit until it ticks. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite()).setValue(ENABLED, false);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (l, p, s, te) -> {
            if (te instanceof TileLampArcane t) t.serverTick();
            else if (te instanceof TileLampGrowth t) t.serverTick();
            else if (te instanceof TileLampFertility t) t.serverTick();
        };
    }

    /** BlockTCDevice.onBlockAdded: lit unless powered. The lamps of growth and fertility go dark again without essentia. */
    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, world, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            boolean flag = !world.hasNeighborSignal(pos);
            if (flag != state.getValue(ENABLED)) {
                world.setBlock(pos, state.setValue(ENABLED, flag), 3);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (world.isEmptyBlock(pos.relative(state.getValue(FACING)))) {
            dropResources(state, world, pos);
            world.removeBlock(pos, false);
            return;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileLampArcane lamp && state.getValue(ENABLED) && world.hasNeighborSignal(pos)) {
            lamp.removeLights();
        }
        boolean checkUpdate = true;
        if (te instanceof TileLampGrowth lamp && lamp.charges <= 0) {
            checkUpdate = false;
        }
        if (te instanceof TileLampFertility lamp && lamp.charges <= 0) {
            checkUpdate = false;
        }
        if (checkUpdate) {
            boolean flag = !world.hasNeighborSignal(pos);
            if (flag != state.getValue(ENABLED)) {
                world.setBlock(pos, state.setValue(ENABLED, flag), 3);
            }
        }
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileLampArcane lamp) {
            lamp.removeLights();
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
