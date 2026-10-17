package thaumcraft.common.blocks.world.taint;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BlockUtils;

/**
 * Port of BlockTaintFeature ("Swollen Taint"): the glowing orbs taint grows where leaves touched a taint log. They
 * spread fibres, and one on top of an upright taint log can turn into a taint geyser. Breaking one releases flux.
 * Silk touch keeps it.
 * TODO: a third of the time breaking one lets out a taint crawler instead (stage 9b-2).
 */
public class BlockTaintFeature extends Block implements ITaintBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape[] SHAPES = {
            Block.box(2.0, 10.0, 2.0, 14.0, 16.0, 14.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0),
            Block.box(2.0, 2.0, 10.0, 14.0, 14.0, 16.0),
            Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 6.0),
            Block.box(10.0, 2.0, 2.0, 16.0, 14.0, 14.0),
            Block.box(0.0, 2.0, 2.0, 6.0, 14.0, 14.0)};

    public BlockTaintFeature() {
        // setLightLevel(0.625f): 10 of 15.
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.1f, 1.2f).sound(SoundsTC.SOUND_TYPE_GORE)
                .randomTicks().noOcclusion().pushReaction(PushReaction.DESTROY).lightLevel(s -> 10).emissiveRendering((s, w, p) -> true));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
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
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isClientSide && !state.is(newState.getBlock())) {
            // TODO stage 9b-2: if (world.random.nextFloat() < 0.333f) a taint crawler crawls out instead.
            AuraHelper.polluteAura(world, pos, 1.0f, true);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void die(Level world, BlockPos pos, BlockState state) {
        world.setBlockAndUpdate(pos, ConfigBlocks.fluxGoo.get().defaultBlockState());
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!TaintHelper.isNearTaintSeed(world, pos) && random.nextInt(10) == 0) {
            die(world, pos, state);
            return;
        }
        TaintHelper.spreadFibres(world, pos);
        BlockState below = world.getBlockState(pos.below());
        if (below.is(ConfigBlocks.taintLog.get()) && below.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y && world.random.nextInt(100) == 0) {
            world.setBlockAndUpdate(pos, ConfigBlocks.taintGeyser.get().defaultBlockState());
        }
    }

    /** Breaks off when the block it grows from no longer has a solid face towards it. */
    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        if (!world.isClientSide && !world.getBlockState(support).isFaceSturdy(world, support, facing)) {
            world.removeBlock(pos, false);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> ret = new ArrayList<>();
        if (BlockUtils.getSilkTouchLevel(params) > 0) {
            ret.add(new ItemStack(this));
        }
        return ret;
    }
}
