package thaumcraft.common.blocks.essentia;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.container.ContainerSmelter;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.essentia.TileSmelter;

/**
 * Port of BlockSmelter (essentia smeltery). FACING is where the front faces, ENABLED whether it is burning.
 * {@link #type} is 0 for the basic smeltery, 1 thaumium, 2 void metal (TileSmelter.getType).
 */
public class BlockSmelter extends BaseEntityBlock {
    public static final MapCodec<BlockSmelter> CODEC = simpleCodec(p -> new BlockSmelter(0));
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public final int type;

    public BlockSmelter(int type) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 20.0f).sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(ENABLED) ? 13 : 0));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ENABLED, false));
        this.type = type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(ENABLED, false);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileSmelter(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.SMELTER.get(), TileSmelter::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof TileSmelter tile) {
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ContainerSmelter(id, inventory, tile),
                    Component.translatable(getDescriptionId())), pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (world.getBlockEntity(pos) instanceof TileSmelter te) {
            te.checkNeighbours();
        }
    }

    /** Switches the lit state; the tile survives since the block stays the same. */
    public static void setFurnaceState(Level world, BlockPos pos, boolean state) {
        BlockState bs = world.getBlockState(pos);
        if (!bs.hasProperty(ENABLED) || bs.getValue(ENABLED) == state) {
            return;
        }
        world.setBlock(pos, bs.setValue(ENABLED, state), 3);
    }

    /** The slurry left inside goes into the aura, and the items drop. */
    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileSmelter te) {
            if (!world.isClientSide && te.vis > 0) {
                AuraHelper.polluteAura(world, pos, (float) te.vis, true);
            }
            Containers.dropContents(world, pos, te);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Override
    public void animateTick(BlockState state, Level w, BlockPos pos, RandomSource r) {
        if (state.getValue(ENABLED)) {
            float f = pos.getX() + 0.5f;
            float f2 = pos.getY() + 0.2f + r.nextFloat() * 5.0f / 16.0f;
            float f3 = pos.getZ() + 0.5f;
            float f4 = 0.52f;
            float f5 = r.nextFloat() * 0.5f - 0.25f;
            switch (state.getValue(FACING)) {
                case WEST -> {
                    w.addParticle(ParticleTypes.SMOKE, f - f4, f2, f3 + f5, 0.0, 0.0, 0.0);
                    w.addParticle(ParticleTypes.FLAME, f - f4, f2, f3 + f5, 0.0, 0.0, 0.0);
                }
                case EAST -> {
                    w.addParticle(ParticleTypes.SMOKE, f + f4, f2, f3 + f5, 0.0, 0.0, 0.0);
                    w.addParticle(ParticleTypes.FLAME, f + f4, f2, f3 + f5, 0.0, 0.0, 0.0);
                }
                case NORTH -> {
                    w.addParticle(ParticleTypes.SMOKE, f + f5, f2, f3 - f4, 0.0, 0.0, 0.0);
                    w.addParticle(ParticleTypes.FLAME, f + f5, f2, f3 - f4, 0.0, 0.0, 0.0);
                }
                case SOUTH -> {
                    w.addParticle(ParticleTypes.SMOKE, f + f5, f2, f3 + f4, 0.0, 0.0, 0.0);
                    w.addParticle(ParticleTypes.FLAME, f + f5, f2, f3 + f4, 0.0, 0.0, 0.0);
                }
                default -> {
                }
            }
        }
    }
}
