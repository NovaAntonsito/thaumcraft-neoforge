package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileWaterJug;

/**
 * Port of BlockWaterJug (the everfull urn). Buckets and other fluid containers drain it; a glass bottle takes 333 mB
 * as a water bottle. It splashes when full.
 */
public class BlockWaterJug extends BaseEntityBlock {
    public static final MapCodec<BlockWaterJug> CODEC = simpleCodec(p -> new BlockWaterJug());
    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 16, 13);

    public BlockWaterJug() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.0f, 12.0f).sound(SoundType.STONE)
                .noOcclusion());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileWaterJug(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.EVERFULL_URN.get(), TileWaterJug::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TileWaterJug tile)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, tile.handler)) {
            tile.setChanged();
            tile.syncTile(false);
            world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.33f, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.3f);
        } else if (stack.is(Items.GLASS_BOTTLE) && tile.tank.getFluidAmount() >= 333) {
            ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, water));
            tile.drainWater(333);
            world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.33f, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.3f);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (world.getBlockEntity(pos) instanceof TileWaterJug tile && tile.tank.getFluidAmount() >= tile.tank.getCapacity()) {
            thaumcraft.client.fx.FXDispatcher.jarSplashFx(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        }
    }
}
