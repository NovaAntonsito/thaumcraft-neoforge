package thaumcraft.common.blocks.eldritch;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.eldritch.TileEldritchLock;

/**
 * BlockEldritch meta 8 of TC4: the Ancient Locking Mechanism in the middle of a boss room doorway. Unbreakable, light
 * 5. The facing side (TC4 kept it in the tile entity) shows the lock, the rest the doorway stone. A Runed Tablet
 * used on it is taken and starts the lock; while it turns, sparks fly around it.
 */
public class BlockEldritchLock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final MapCodec<BlockEldritchLock> CODEC = simpleCodec(p -> new BlockEldritchLock());

    public BlockEldritchLock() {
        super(ConfigBlocks.eldritchSolidProps(-1.0f, 3600000.0f, 5));
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEldritchLock(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.ELDRITCH_LOCK.get(), TileEldritchLock::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ConfigItems.runedTablet.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world.getBlockEntity(pos) instanceof TileEldritchLock te && te.count < 0) {
            if (!world.isClientSide) {
                te.count = 0;
                te.setChanged();
                te.syncTile(false);
                stack.shrink(1);
                world.playSound(null, pos, SoundsTC.RUNICSHIELDCHARGE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void animateTick(BlockState state, Level w, BlockPos pos, RandomSource r) {
        if (!(w.getBlockEntity(pos) instanceof TileEldritchLock te) || te.count < 0) {
            return;
        }
        // FXSpark with a scale of 0.5, coloured (0.65-0.75, 1, 1), alpha 0.8.
        FXDispatcher.spark(pos.getX() + r.nextFloat(), pos.getY() + r.nextFloat(), pos.getZ() + r.nextFloat(), 0.5f,
                0.65f + r.nextFloat() * 0.1f, 1.0f, 1.0f, 0.8f);
    }
}
