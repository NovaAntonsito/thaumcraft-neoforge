package thaumcraft.common.blocks.crafting;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import thaumcraft.api.casters.ICaster;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.crafting.TileCrucible;

/** Port of BlockCrucible. */
public class BlockCrucible extends BaseEntityBlock {
    public static final MapCodec<BlockCrucible> CODEC = simpleCodec(p -> new BlockCrucible());

    /** Legs plus the four walls (the original addCollisionBoxToList boxes). The inside is open. */
    protected static final VoxelShape COLLISION = Shapes.or(
            box(0, 0, 0, 16, 5, 16),
            box(0, 0, 0, 16, 16, 2),
            box(0, 0, 14, 16, 16, 16),
            box(14, 0, 0, 16, 16, 16),
            box(0, 0, 0, 2, 16, 16));

    private int delay;

    public BlockCrucible() {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 20.0f).sound(SoundType.METAL).noOcclusion());
    }

    public BlockCrucible(Properties properties) {
        super(properties);
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileCrucible(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.CRUCIBLE.get(), TileCrucible::tick);
    }

    /** Items thrown in are melted; living things standing in boiling water get burned. */
    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileCrucible tile) {
            if (entity instanceof ItemEntity item && !item.getTags().contains(TileCrucible.SPECIAL_ITEM_TAG)
                    && tile.heat > 150 && tile.tank.getFluidAmount() > 0) {
                tile.attemptSmelt(item);
            } else {
                ++delay;
                if (delay < 10) return;
                delay = 0;
                if (entity instanceof LivingEntity && tile.heat > 150 && tile.tank.getFluidAmount() > 0) {
                    entity.hurt(world.damageSources().inFire(), 1.0f);
                    world.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 2.0f + world.random.nextFloat() * 0.4f);
                }
            }
        }
        super.entityInside(state, world, pos, entity);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileCrucible tile) {
            tile.spillRemnants();
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    /** Water containers fill the tank; any other item is dropped in (one at a time) from the top. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TileCrucible tile)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (FluidUtil.getFluidContained(stack).map(fs -> tile.tank.isFluidValid(fs)).orElse(false)) {
            if (tile.tank.getFluidAmount() < tile.tank.getCapacity()) {
                if (!world.isClientSide && FluidUtil.interactWithFluidHandler(player, hand, tile.tank)) {
                    world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.33f,
                            1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.3f);
                }
                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!player.isShiftKeyDown() && !(stack.getItem() instanceof ICaster) && hit.getDirection() == Direction.UP && !stack.isEmpty()) {
            if (tile.heat > 150 && tile.tank.getFluidAmount() > 0) {
                if (!world.isClientSide && tile.attemptSmelt(stack.copyWithCount(1), player).isEmpty()) {
                    stack.consume(1, player);
                }
                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Sneak with an empty hand to empty the crucible. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof TileCrucible tile) {
            if (!world.isClientSide) tile.spillRemnants();
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity te = level.getBlockEntity(pos);
        return te != null && te.triggerEvent(id, param);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TileCrucible tile) {
            float r = tile.aspects.visSize() / (float) tile.maxTags;
            return Mth.floor(r * 14.0f) + (tile.aspects.visSize() > 0 ? 1 : 0);
        }
        return 0;
    }

    @Override
    public void animateTick(BlockState state, Level w, BlockPos pos, RandomSource r) {
        if (r.nextInt(10) == 0 && w.getBlockEntity(pos) instanceof TileCrucible tile
                && tile.tank.getFluidAmount() > 0 && tile.heat > 150) {
            w.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1f + r.nextFloat() * 0.1f, 1.2f + r.nextFloat() * 0.2f, false);
        }
    }
}
