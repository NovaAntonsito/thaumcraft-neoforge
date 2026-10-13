package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileArcaneEar;

/**
 * Port of BlockArcaneEar and BlockArcaneEarToggle. Sits on the face of a solid block and listens for note blocks
 * playing its note with its instrument within 64 blocks. The normal ear gives a short pulse; the toggle ear flips
 * its output. Right click raises the note it listens for, like a note block.
 */
public class BlockArcaneEar extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final VoxelShape SHAPE_DOWN = box(2, 10, 2, 14, 16, 14);
    private static final VoxelShape SHAPE_UP = box(2, 0, 2, 14, 6, 14);
    private static final VoxelShape SHAPE_NORTH = box(2, 2, 10, 14, 14, 16);
    private static final VoxelShape SHAPE_SOUTH = box(2, 2, 0, 14, 14, 6);
    private static final VoxelShape SHAPE_WEST = box(10, 2, 2, 16, 14, 14);
    private static final VoxelShape SHAPE_EAST = box(0, 2, 2, 6, 14, 14);

    public final boolean toggle;
    private final MapCodec<BlockArcaneEar> codec;

    public BlockArcaneEar(boolean toggle) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f, 12.0f).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
        this.toggle = toggle;
        this.codec = simpleCodec(p -> new BlockArcaneEar(toggle));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ENABLED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
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
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileArcaneEar(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TCBlockEntities.ARCANE_EAR.get(), TileArcaneEar::tick);
    }

    /** Placed on the clicked face; it needs a solid face behind it (canPlaceBlockOnSide). */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(ENABLED, false);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    /** neighborChanged: the ear drops when the block it sits on goes away. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
                                     BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof TileArcaneEar tile) {
            tile.updateTone();
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof TileArcaneEar tile) {
            tile.updateTone();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileArcaneEar tile) {
            tile.changePitch();
            tile.triggerNote(level, pos, true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ENABLED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ENABLED) ? 15 : 0;
    }

    /** INSTRUMENTS held the ten 1.12 note sounds, in the order of NoteBlockInstrument; the newer ones follow. */
    protected static NoteBlockInstrument getInstrument(int type) {
        NoteBlockInstrument[] all = NoteBlockInstrument.values();
        return type < 0 || type >= all.length ? NoteBlockInstrument.HARP : all[type];
    }

    /** eventReceived: plays the note with the instrument of the block behind the ear. */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        float pitch = (float) Math.pow(2.0, (param - 12) / 12.0);
        level.playSound(null, pos, getInstrument(id).getSoundEvent().value(), SoundSource.BLOCKS, 3.0f, pitch);
        level.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, param / 24.0, 0.0, 0.0);
        return true;
    }
}
