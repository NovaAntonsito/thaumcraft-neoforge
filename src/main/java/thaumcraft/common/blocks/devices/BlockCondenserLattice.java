package thaumcraft.common.blocks.devices;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.devices.TileCondenser;

/**
 * Port of BlockCondenserLattice (clean and clogged). It must connect, through other lattice, down to a flux
 * condenser no further than about 8.6 blocks away (distance squared 74); otherwise it pops off. Clogged lattice is
 * cleaned by using a filter on it, which may give back a crystal of Vitium. The six connection properties were
 * computed in getActualState in 1.12; they are stored in the state now.
 */
public class BlockCondenserLattice extends Block {
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    private static final Map<Direction, BooleanProperty> PROPS = PipeBlock.PROPERTY_BY_DIRECTION;

    public BlockCondenserLattice(boolean dirty) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5f, 5.0f).sound(SoundType.METAL).noOcclusion()
                .lightLevel(s -> dirty ? 0 : 4));
        BlockState bs = stateDefinition.any();
        for (BooleanProperty p : PROPS.values()) bs = bs.setValue(p, false);
        registerDefaultState(bs);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    private static boolean connects(BlockState other, Direction face) {
        return other.getBlock() instanceof BlockCondenserLattice || (face == Direction.DOWN && other.is(ConfigBlocks.condenser.get()));
    }

    public static BlockState makeConnections(BlockState state, BlockGetter world, BlockPos pos) {
        for (Direction face : Direction.values()) {
            state = state.setValue(PROPS.get(face), connects(world.getBlockState(pos.relative(face)), face));
        }
        return state;
    }

    /** Keeps the connections when a lattice block is swapped for its clean or clogged twin. */
    public static BlockState copyConnections(BlockState from, BlockState to) {
        for (BooleanProperty p : PROPS.values()) to = to.setValue(p, from.getValue(p));
        return to;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return makeConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction face, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPS.get(face), connects(neighbor, face));
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, world, pos, oldState, movedByPiston);
        if (!world.isClientSide && !oldState.is(state.getBlock())) {
            triggerUpdate(world, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);
        if (!world.isClientSide && (blockIn instanceof BlockCondenserLattice || blockIn == ConfigBlocks.condenser.get())) {
            triggerUpdate(world, pos);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!state.is(ConfigBlocks.condenserLatticeDirty.get()) || !stack.is(ConfigItems.filter.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        stack.shrink(1);
        Direction facing = hit.getDirection();
        if (world.random.nextBoolean()) {
            world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5f + facing.getStepX() / 3.0f, pos.getY() + 0.5f,
                    pos.getZ() + 0.5f + facing.getStepZ() / 3.0f, ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1)));
        }
        world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                ((world.random.nextFloat() - world.random.nextFloat()) * 0.7f + 1.0f) * 1.6f);
        world.setBlock(pos, copyConnections(state, ConfigBlocks.condenserLattice.get().defaultBlockState()), 3);
        BlockState state2 = world.getBlockState(pos);
        if (state2.getBlock() instanceof BlockCondenserLattice lattice) {
            lattice.triggerUpdate(world, pos);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockState out = super.playerWillDestroy(world, pos, state, player);
        if (!world.isClientSide) {
            triggerUpdate(world, pos);
        }
        return out;
    }

    /** Finds the condenser this lattice hangs from and makes it recount; pops the lattice off when there is none. */
    public void triggerUpdate(Level world, BlockPos pos) {
        Set<Long> history = new HashSet<>();
        BlockPos p = processUpdate(world, pos, history);
        if (p == null || p.distSqr(pos) > 74.0) {
            dropResources(defaultBlockState(), world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Nullable
    private BlockPos processUpdate(Level world, BlockPos pos, Set<Long> history) {
        history.add(pos.asLong());
        for (Direction face : Direction.values()) {
            BlockPos p2 = pos.relative(face);
            if (!history.contains(p2.asLong())) {
                BlockState b = world.getBlockState(p2);
                if (b.getBlock() instanceof BlockCondenserLattice) {
                    BlockPos pp = processUpdate(world, p2, history);
                    if (pp != null) {
                        return pp;
                    }
                }
                if (face == Direction.DOWN && b.is(ConfigBlocks.condenser.get())) {
                    if (world.getBlockEntity(p2) instanceof TileCondenser te) {
                        te.latticeCount = -1.0f;
                    }
                    return p2;
                }
            }
        }
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        double minx = 5, maxx = 11, miny = 5, maxy = 11, minz = 5, maxz = 11;
        if (state.getValue(DOWN)) miny = 0;
        if (state.getValue(UP)) maxy = 16;
        if (state.getValue(NORTH)) minz = 0;
        if (state.getValue(SOUTH)) maxz = 16;
        if (state.getValue(WEST)) minx = 0;
        if (state.getValue(EAST)) maxx = 16;
        return Block.box(minx, miny, minz, maxx, maxy, maxz);
    }
}
