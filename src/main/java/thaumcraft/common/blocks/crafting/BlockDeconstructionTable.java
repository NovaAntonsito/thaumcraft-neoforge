package thaumcraft.common.blocks.crafting;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
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
import thaumcraft.common.container.ContainerDeconstructionTable;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.crafting.TileDeconstructionTable;

/**
 * Reborn's deconstruction table: breaks items down into a primal aspect the player can take into their pool.
 * The original drew the arcane workbench model from its tile renderer; here the table is a block model and
 * the renderer only adds the thaumometer, the item and the aspect.
 */
public class BlockDeconstructionTable extends BaseEntityBlock {
    public static final MapCodec<BlockDeconstructionTable> CODEC = simpleCodec(p -> new BlockDeconstructionTable());

    /** ModelArcaneWorkbench: top, four legs and the base. */
    protected static final VoxelShape SHAPE = Shapes.or(
            box(0, 8, 0, 16, 16, 16),
            box(1, 4, 1, 5, 8, 5),
            box(11, 4, 1, 15, 8, 5),
            box(1, 4, 11, 5, 8, 15),
            box(11, 4, 11, 15, 8, 15),
            box(0, 0, 0, 16, 4, 16));

    public BlockDeconstructionTable() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 5.0f).sound(SoundType.WOOD).noOcclusion());
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
        return new TileDeconstructionTable(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TCBlockEntities.DECONSTRUCTION_TABLE.get(), TileDeconstructionTable::tick);
    }

    /** Sneaking takes the aspect straight into the pool; otherwise the table's screen opens. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) return InteractionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof TileDeconstructionTable tile) {
            if (player.isShiftKeyDown()) {
                tile.takeAspect(player);
            } else {
                player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ContainerDeconstructionTable(id, inventory, tile),
                        Component.translatable("block.thaumcraft.deconstruction_table")), pos);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileDeconstructionTable tile) {
            Containers.dropContents(world, pos, tile);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
