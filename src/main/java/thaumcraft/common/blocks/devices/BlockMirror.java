package thaumcraft.common.blocks.devices;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.devices.TileMirror;

/**
 * Port of BlockMirror, for both the magic mirror and the essentia mirror. FACING is the way the mirror looks:
 * it hangs on the block behind it, and falls off when that block goes. Items that fall into a linked magic
 * mirror come out of its twin. The frame is a block model; the pane is drawn by TileMirrorRenderer.
 */
public class BlockMirror extends BaseEntityBlock {
    public static final MapCodec<BlockMirror> CODEC = simpleCodec(p -> new BlockMirror(TileMirror::new));
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape[] SHAPES = {
            Block.box(0, 14, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 0, 14, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16, 2),
            Block.box(14, 0, 0, 16, 16, 16),
            Block.box(0, 0, 0, 2, 16, 16)};

    private final BiFunction<BlockPos, BlockState, ? extends TileMirror> factory;

    public BlockMirror(BiFunction<BlockPos, BlockState, ? extends TileMirror> factory) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.1f).sound(SoundsTC.SOUND_TYPE_JAR).noOcclusion());
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState bs = defaultBlockState().setValue(FACING, context.getClickedFace());
        return bs.canSurvive(context.getLevel(), context.getClickedPos()) ? bs : null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    /** canPlaceBlockOnSide: the block behind must have a solid face towards the mirror. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction d = state.getValue(FACING);
        BlockPos behind = pos.relative(d.getOpposite());
        return world.getBlockState(behind).isFaceSturdy(world, behind, d);
    }

    /** neighborChanged: drops when the block it hangs on goes (the drop keeps the link, see the loot table). */
    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        if (facing == state.getValue(FACING).getOpposite() && !state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, world, pos, facingPos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, te) -> {
            if (te instanceof TileMirror tm) {
                TileMirror.tick(l, p, s, tm);
            }
        };
    }

    /** A mirror placed from a linked item restores the link to its twin. */
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileMirror tm && stack.has(thaumcraft.common.registry.TCDataComponents.MIRROR_LINK.get())) {
            tm.restoreLink();
        }
    }

    /** onBlockActivated returned true: clicks on a mirror never place blocks against it. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ItemInteractionResult.CONSUME;
    }

    /** Breaking a linked mirror unlinks its twin (dropMirror called invalidateLink). */
    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !world.isClientSide && world.getBlockEntity(pos) instanceof TileMirror tm && tm.linked) {
            tm.invalidateLink();
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    /** onEntityCollidedWithBlock: items that fall into a magic mirror go through. */
    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof ItemEntity item && entity.isAlive() && !entity.isOnPortalCooldown()
                && world.getBlockEntity(pos) instanceof TileMirror taf && !(taf instanceof thaumcraft.common.tiles.devices.TileMirrorEssentia)) {
            taf.transport(item);
        }
        super.entityInside(state, world, pos, entity);
    }
}
