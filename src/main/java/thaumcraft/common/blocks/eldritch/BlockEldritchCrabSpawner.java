package thaumcraft.common.blocks.eldritch;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
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
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.eldritch.TileEldritchCrabSpawner;

/**
 * BlockEldritch meta 9 of TC4: the Crusted Opening. Crusted stone with a vent on its facing side (TC4 kept the
 * facing in the tile entity and drew the vent model with a renderer; here the block model has it). Hardness 15,
 * resistance 30, light 4, drops nothing but 6-10 experience.
 */
public class BlockEldritchCrabSpawner extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final MapCodec<BlockEldritchCrabSpawner> CODEC = simpleCodec(p -> new BlockEldritchCrabSpawner());
    private static final UniformInt XP = UniformInt.of(6, 10);

    public BlockEldritchCrabSpawner() {
        super(ConfigBlocks.eldritchSolidProps(15.0f, 30.0f, 4));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
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
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (dropExperience) {
            tryDropExperience(level, pos, stack, XP);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEldritchCrabSpawner(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.ELDRITCH_CRAB_SPAWNER.get(), TileEldritchCrabSpawner::tick);
    }
}
