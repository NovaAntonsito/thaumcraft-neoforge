package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.api.items.IRechargable;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileRechargePedestal;

/** Port of BlockRechargePedestal: holds one rechargeable item and fills it with vis from the aura. */
public class BlockRechargePedestal extends BaseEntityBlock {
    public static final MapCodec<BlockRechargePedestal> CODEC = simpleCodec(p -> new BlockRechargePedestal());

    public BlockRechargePedestal() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.0f, 10.0f)
                .sound(SoundType.STONE).noOcclusion());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileRechargePedestal(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.RECHARGE_PEDESTAL.get(), TileRechargePedestal::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof TileRechargePedestal ped && ped.getItem(0).isEmpty() && stack.getItem() instanceof IRechargable) {
            if (!world.isClientSide) {
                ped.setItem(0, stack.copyWithCount(1));
                stack.shrink(1);
                world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                        ((world.random.nextFloat() - world.random.nextFloat()) * 0.7f + 1.0f) * 1.6f);
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** InventoryUtils.dropItemsAtEntity: the item pops out at the player. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TileRechargePedestal ped) || ped.getItem(0).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide) {
            ItemEntity item = new ItemEntity(world, player.getX(), player.getY() + player.getEyeHeight() / 2.0f, player.getZ(),
                    ped.removeItemNoUpdate(0));
            item.setNoPickUpDelay();
            world.addFreshEntity(item);
            world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                    ((world.random.nextFloat() - world.random.nextFloat()) * 0.7f + 1.0f) * 1.5f);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileRechargePedestal ped) {
            Containers.dropContents(world, pos, ped);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity te = level.getBlockEntity(pos);
        return te != null && te.triggerEvent(id, param);
    }
}
