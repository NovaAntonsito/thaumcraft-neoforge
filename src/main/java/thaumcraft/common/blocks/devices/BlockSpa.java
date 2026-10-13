package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import thaumcraft.common.container.ContainerSpa;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileSpa;

/**
 * Port of BlockSpa. A fluid container in hand fills the tank (only with the fluid already in it); an empty hand
 * opens the screen.
 */
public class BlockSpa extends BaseEntityBlock {
    public static final MapCodec<BlockSpa> CODEC = simpleCodec(p -> new BlockSpa());

    public BlockSpa() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.0f, 12.0f).sound(SoundType.STONE));
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
        return new TileSpa(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TCBlockEntities.SPA.get(), TileSpa::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        FluidStack fs = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        if (fs.isEmpty() || player.isShiftKeyDown() || !(world.getBlockEntity(pos) instanceof TileSpa tile)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClientSide && tile.tank.getFluidAmount() < tile.tank.getCapacity()
                && (tile.tank.isEmpty() || FluidStack.isSameFluidSameComponents(tile.tank.getFluid(), fs))
                && FluidUtil.interactWithFluidHandler(player, hand, tile.tank)) {
            tile.setChanged();
            world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.33f,
                    1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.3f);
        }
        return ItemInteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && !world.isClientSide && world.getBlockEntity(pos) instanceof TileSpa tile
                && player instanceof ServerPlayer sp) {
            sp.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ContainerSpa(id, inventory, tile),
                    Component.translatable("block.thaumcraft.spa")), buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileSpa tile) {
            Containers.dropContents(world, pos, tile);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
