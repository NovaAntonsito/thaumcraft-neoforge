package thaumcraft.common.blocks.devices;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileLevitator;

/**
 * Port of BlockLevitator. Pushes items and creatures away from its face; redstone turns it off. The small switch on
 * its back cycles the range.
 * <p>
 * 1.21: the switch is part of the outline instead of a CodeChicken ray trace; a click inside it changes the range.
 */
public class BlockLevitator extends BaseEntityBlock {
    public static final MapCodec<BlockLevitator> CODEC = simpleCodec(p -> new BlockLevitator());
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final Map<Direction, AABB> SWITCHES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.values()) {
            AABB sw = getCuboidByFacing(facing);
            float f = 0.125f;
            AABB body = new AABB(facing.getStepX() > 0 ? f : 0.0f, facing.getStepY() > 0 ? f : 0.0f, facing.getStepZ() > 0 ? f : 0.0f,
                    1.0f - (facing.getStepX() < 0 ? f : 0.0f), 1.0f - (facing.getStepY() < 0 ? f : 0.0f), 1.0f - (facing.getStepZ() < 0 ? f : 0.0f));
            SWITCHES.put(facing, sw);
            SHAPES.put(facing, Shapes.or(Shapes.create(body), Shapes.create(sw)));
        }
    }

    public BlockLevitator() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 12.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ENABLED, true));
    }

    /** TileLevitator.getCuboidByFacing. */
    private static AABB getCuboidByFacing(Direction facing) {
        return switch (facing) {
            case DOWN -> new AABB(0.375, 0.875, 0.375, 0.625, 0.9375, 0.625);
            case EAST -> new AABB(0.0625, 0.375, 0.375, 0.125, 0.625, 0.625);
            case WEST -> new AABB(0.875, 0.375, 0.375, 0.9375, 0.625, 0.625);
            case SOUTH -> new AABB(0.375, 0.375, 0.0625, 0.625, 0.625, 0.125);
            case NORTH -> new AABB(0.375, 0.375, 0.875, 0.625, 0.625, 0.9375);
            default -> new AABB(0.375, 0.0625, 0.375, 0.625, 0.125, 0.625);
        };
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileLevitator(pos, state);
    }

    /** Runs on both sides: the client moves its own player. */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.LEVITATOR.get(), TileLevitator::tick);
    }

    /** BlockTCDevice: faces the side nearest the placer, the other way when sneaking. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir = context.getNearestLookingDirection().getOpposite();
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            dir = dir.getOpposite();
        }
        return defaultBlockState().setValue(FACING, dir).setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        updateState(level, pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        updateState(level, pos, state);
    }

    /** BlockTCDevice.updateState: ENABLED while unpowered. */
    private static void updateState(Level level, BlockPos pos, BlockState state) {
        boolean flag = !level.hasNeighborSignal(pos);
        if (flag != state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, flag), 3);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        Vec3 local = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        if (!SWITCHES.get(state.getValue(FACING)).inflate(0.001).contains(local) || !(world.getBlockEntity(pos) instanceof TileLevitator tile)) {
            return InteractionResult.PASS;
        }
        tile.increaseRange(player);
        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.KEY.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
