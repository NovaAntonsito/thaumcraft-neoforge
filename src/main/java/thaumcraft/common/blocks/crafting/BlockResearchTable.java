package thaumcraft.common.blocks.crafting;

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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.container.ContainerResearchTable;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/** The TC4 research table (Reborn's BlockResearchTable). Made by using scribing tools on a wooden table. */
public class BlockResearchTable extends BaseEntityBlock {
    public static final MapCodec<BlockResearchTable> CODEC = simpleCodec(p -> new BlockResearchTable());
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /** The model's boxes: top and four legs. */
    protected static final VoxelShape SHAPE = Shapes.or(
            box(0, 10, 0, 16, 16, 16),
            box(11, 0, 1, 15, 10, 5),
            box(11, 0, 11, 15, 10, 15),
            box(1, 0, 11, 5, 10, 15),
            box(1, 0, 1, 5, 10, 5));

    public BlockResearchTable() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f, 2.0f).sound(SoundType.WOOD).noOcclusion());
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
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
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
        return new TileResearchTable(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TCBlockEntities.RESEARCH_TABLE.get(), TileResearchTable::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) return InteractionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof TileResearchTable tile) {
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ContainerResearchTable(id, inventory, tile),
                    Component.translatable("block.thaumcraft.research_table")), pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileResearchTable tile) {
            Containers.dropContents(world, pos, tile);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    /**
     * With a note on the table, motes drift toward it (FXGeneric in the original).
     * TODO: the original's particle sheet sparkles; vanilla enchant glyphs stand in for them.
     */
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(5) == 0 && world.getBlockEntity(pos) instanceof TileResearchTable te && te.hasResearchNote()) {
            double xx = rand.nextGaussian() / 2.0;
            double zz = rand.nextGaussian() / 2.0;
            double yy = 1.5 + rand.nextFloat();
            world.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, xx, yy - 0.85, zz);
        }
    }
}
