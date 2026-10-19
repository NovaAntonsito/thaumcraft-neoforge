package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.api.crafting.IInfusionStabiliserExt;
import thaumcraft.common.tiles.crafting.TilePedestal;

/**
 * Port of BlockPedestal (arcane pedestal). Holds one item for infusion; right click to place or take it.
 * CHARGE is the vis inlay charge reaching the pedestal from an infusion stabilizer (BlockInlay).
 */
public class BlockPedestal extends BaseEntityBlock implements IInfusionStabiliserExt {
    public static final MapCodec<BlockPedestal> CODEC = simpleCodec(p -> new BlockPedestal());

    public BlockPedestal() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f, 12.0f).sound(SoundType.STONE).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(BlockInlay.CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockInlay.CHARGE);
    }

    /** Red runes around the base while charged. */
    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (stateIn.getValue(BlockInlay.CHARGE) > 0) {
            thaumcraft.client.fx.FXDispatcher.blockRunes2(pos.getX(), pos.getY() - 0.375, pos.getZ(), 1.0f, 0.0f, 0.0f, 10, 0.0f);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, worldIn, pos, oldState, movedByPiston);
        if (!worldIn.isClientSide && !oldState.is(state.getBlock())) {
            BlockInlay.updateSurroundingInlay(worldIn, pos, state);
            for (Direction face : Direction.Plane.HORIZONTAL) {
                BlockInlay.notifyInlayNeighborsOfStateChange(worldIn, pos.relative(face));
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide) {
            BlockInlay.updateSurroundingInlay(worldIn, pos, state);
        }
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
        return new TilePedestal(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TilePedestal ped)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (ped.getItem(0).isEmpty() && !stack.isEmpty()) {
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
        if (!(world.getBlockEntity(pos) instanceof TilePedestal ped) || ped.getItem(0).isEmpty()) {
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

    /** BlockTCTile.breakBlock: the item drops. */
    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TilePedestal ped) {
            Containers.dropContents(world, pos, ped);
        }
        super.onRemove(state, world, pos, newState, isMoving);
        if (!world.isClientSide && !state.is(newState.getBlock())) {
            for (Direction face : Direction.Plane.HORIZONTAL) {
                world.updateNeighborsAt(pos.relative(face), this);
            }
            for (Direction face : Direction.Plane.HORIZONTAL) {
                BlockInlay.notifyInlayNeighborsOfStateChange(world, pos.relative(face));
            }
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity te = level.getBlockEntity(pos);
        return te != null && te.triggerEvent(id, param);
    }

    @Override
    public boolean canStabaliseInfusion(Level world, BlockPos pos) {
        return true;
    }

    /** Only eldritch pedestals stabilise (0.1); arcane ones give 0. */
    @Override
    public float getStabilizationAmount(Level world, BlockPos pos) {
        return 0.0f;
    }

    /** A pair of pedestals is unbalanced when only one of them holds an item. */
    @Override
    public boolean hasSymmetryPenalty(Level world, BlockPos pos1, BlockPos pos2) {
        if (world.getBlockEntity(pos1) instanceof TilePedestal te1 && world.getBlockEntity(pos2) instanceof TilePedestal te2) {
            return te1.getItem(0).isEmpty() != te2.getItem(0).isEmpty();
        }
        return false;
    }

    @Override
    public float getSymmetryPenalty(Level world, BlockPos pos) {
        return 0.1f;
    }
}
