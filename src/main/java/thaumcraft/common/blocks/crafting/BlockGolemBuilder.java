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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.container.ContainerGolemBuilder;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.crafting.TileGolemBuilder;

/**
 * Port of BlockGolemBuilder: the golem press, the piston of the multiblock built with salis mundus (MINDCLOCKWORK).
 * It drops a piston (loot table) and breaking it or any of its placeholders turns the rest back into the original
 * blocks.
 */
public class BlockGolemBuilder extends BaseEntityBlock {
    public static final MapCodec<BlockGolemBuilder> CODEC = simpleCodec(p -> new BlockGolemBuilder());
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static boolean ignore = false;

    public BlockGolemBuilder() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f, 10.0f).sound(SoundType.STONE).noOcclusion());
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
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileGolemBuilder(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.GOLEM_BUILDER.get(), TileGolemBuilder::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        openGui(world, pos, player);
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    public static void openGui(Level world, BlockPos pos, Player player) {
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof TileGolemBuilder tile && player instanceof ServerPlayer sp) {
            sp.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ContainerGolemBuilder(id, inventory, tile),
                    Component.translatable("block.thaumcraft.golem_builder")), buf -> buf.writeBlockPos(pos));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof TileGolemBuilder tile) {
                Containers.dropContents(world, pos, tile);
            }
            destroy(world, pos, state, pos);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    /** Turns the placeholders around the press back into bars, anvil, cauldron and table, and the press into a piston. */
    public static void destroy(Level worldIn, BlockPos pos, BlockState state, BlockPos startpos) {
        if (ignore || worldIn.isClientSide) {
            return;
        }
        ignore = true;
        for (int a = -1; a <= 1; ++a) {
            for (int b = 0; b <= 1; ++b) {
                for (int c = -1; c <= 1; ++c) {
                    BlockPos p = pos.offset(a, b, c);
                    if (!p.equals(startpos)) {
                        BlockState bs = worldIn.getBlockState(p);
                        if (bs.is(ConfigBlocks.placeholderBars.get())) {
                            worldIn.setBlockAndUpdate(p, Blocks.IRON_BARS.defaultBlockState());
                        }
                        if (bs.is(ConfigBlocks.placeholderAnvil.get())) {
                            worldIn.setBlockAndUpdate(p, Blocks.ANVIL.defaultBlockState());
                        }
                        if (bs.is(ConfigBlocks.placeholderCauldron.get())) {
                            worldIn.setBlockAndUpdate(p, Blocks.CAULDRON.defaultBlockState());
                        }
                        if (bs.is(ConfigBlocks.placeholderTable.get())) {
                            worldIn.setBlockAndUpdate(p, ConfigBlocks.tableStone.get().defaultBlockState());
                        }
                    }
                }
            }
        }
        if (!pos.equals(startpos)) {
            worldIn.setBlockAndUpdate(pos, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP));
        }
        ignore = false;
    }
}
