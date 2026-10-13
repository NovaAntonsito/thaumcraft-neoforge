package thaumcraft.common.blocks.crafting;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.container.ContainerThaumatorium;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.crafting.TileThaumatorium;
import thaumcraft.common.tiles.crafting.TileThaumatoriumTop;

/**
 * Port of BlockThaumatorium, both halves. Only made by salis mundus on two alchemical constructs over a
 * crucible; breaking either half (or the crucible) turns the rest back into alchemical constructs.
 * The top half is invisible: the lower half's model covers both. Each half drops an alchemical construct.
 */
public class BlockThaumatorium extends BaseEntityBlock {
    public static final MapCodec<BlockThaumatorium> CODEC = simpleCodec(p -> new BlockThaumatorium(false));
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final boolean top;

    public BlockThaumatorium(boolean top) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.0f, 12.0f)
                .sound(SoundType.METAL).noOcclusion());
        this.top = top;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    protected RenderShape getRenderShape(BlockState state) {
        return top ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return top ? new TileThaumatoriumTop(pos, state) : new TileThaumatorium(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return top ? null : createTickerHelper(type, TCBlockEntities.THAUMATORIUM.get(), TileThaumatorium::tick);
    }

    /** Picking the block gives what it is made of. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ConfigBlocks.metalAlchemical.get());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer sp) {
            BlockPos p = top ? pos.below() : pos;
            if (world.getBlockEntity(p) instanceof TileThaumatorium tile) {
                sp.openMenu(new SimpleMenuProvider((id, inventory, pl) -> new ContainerThaumatorium(id, inventory, tile),
                        Component.translatable("block.thaumcraft.thaumatorium")), buf -> buf.writeBlockPos(p));
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!top && world.getBlockEntity(pos) instanceof TileThaumatorium tile) {
                Containers.dropContents(world, pos, tile);
            }
            if (top && world.getBlockState(pos.below()).is(ConfigBlocks.thaumatorium.get())) {
                world.setBlockAndUpdate(pos.below(), ConfigBlocks.metalAlchemical.get().defaultBlockState());
            }
            if (!top && world.getBlockState(pos.above()).is(ConfigBlocks.thaumatoriumTop.get())) {
                world.setBlockAndUpdate(pos.above(), ConfigBlocks.metalAlchemical.get().defaultBlockState());
            }
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!top && !world.getBlockState(pos.below()).is(ConfigBlocks.crucible.get())) {
            world.setBlockAndUpdate(pos, ConfigBlocks.metalAlchemical.get().defaultBlockState());
            if (world.getBlockState(pos.above()).is(ConfigBlocks.thaumatoriumTop.get())) {
                world.setBlockAndUpdate(pos.above(), ConfigBlocks.metalAlchemical.get().defaultBlockState());
            }
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return !top;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof TileThaumatorium tile ? AbstractContainerMenu.getRedstoneSignalFromContainer(tile) : 0;
    }
}
