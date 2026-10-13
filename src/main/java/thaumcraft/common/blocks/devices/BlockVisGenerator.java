package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileVisGenerator;

/**
 * Port of BlockVisGenerator. Turns vis from the aura into Forge energy and pushes it into the block it points at.
 * When placed it turns to face a neighbour that accepts energy, if there is one. A redstone signal switches it off.
 */
public class BlockVisGenerator extends BaseEntityBlock {
    public static final MapCodec<BlockVisGenerator> CODEC = simpleCodec(p -> new BlockVisGenerator());
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public BlockVisGenerator() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 5.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ENABLED, true));
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean enabled = !world.hasNeighborSignal(pos);
        for (Direction face : Direction.values()) {
            IEnergyStorage capability = world.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(face), face.getOpposite());
            if (capability != null && capability.canReceive()) {
                return defaultBlockState().setValue(FACING, face).setValue(ENABLED, enabled);
            }
        }
        // BlockTCDevice: away from the player, or towards them when sneaking.
        Direction look = context.getNearestLookingDirection().getOpposite();
        Direction facing = context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? look.getOpposite() : look;
        return defaultBlockState().setValue(FACING, facing).setValue(ENABLED, enabled);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** The upright box (0.25, 0, 0.25)-(0.75, 0.875, 0.75) turned so its top points along FACING. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> Block.box(4, 2, 4, 12, 16, 12);
            case NORTH -> Block.box(4, 4, 2, 12, 12, 16);
            case SOUTH -> Block.box(4, 4, 0, 12, 12, 14);
            case WEST -> Block.box(2, 4, 4, 16, 12, 12);
            case EAST -> Block.box(0, 4, 4, 14, 12, 12);
            default -> Block.box(4, 0, 4, 12, 14, 12);
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileVisGenerator(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TCBlockEntities.VIS_GENERATOR.get(), TileVisGenerator::serverTick);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean flag = !world.hasNeighborSignal(pos);
        if (flag != state.getValue(ENABLED)) {
            world.setBlock(pos, state.setValue(ENABLED, flag), 3);
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    /** Sparks at the tip while it holds energy. */
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (world.getBlockEntity(pos) instanceof TileVisGenerator tile && tile.getEnergyStored() > 0) {
            Direction face = state.getValue(FACING);
            double x = face.getStepX() == 0 ? rand.nextGaussian() * 0.1 : face.getStepX() * 0.1;
            double y = face.getStepY() == 0 ? rand.nextGaussian() * 0.1 : face.getStepY() * 0.1;
            double z = face.getStepZ() == 0 ? rand.nextGaussian() * 0.1 : face.getStepZ() * 0.1;
            ClientFX.spark(pos.getX() + 0.5 + x, pos.getY() + 0.5 + y, pos.getZ() + 0.5 + z, 0.65f + rand.nextFloat() * 0.1f, 1.0f, 1.0f);
        }
    }
}
