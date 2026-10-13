package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileInfernalFurnace;

/**
 * Port of BlockInfernalFurnace: the lava core of the 3x3x3 furnace built with salis mundus (INFERNALFURNACE). It has
 * no item; breaking it or any of its placeholders turns everything back into nether brick, obsidian and lava.
 * FACING points from the furnace to its iron bars (the opening, where items come out).
 */
public class BlockInfernalFurnace extends BaseEntityBlock {
    public static final MapCodec<BlockInfernalFurnace> CODEC = simpleCodec(p -> new BlockInfernalFurnace());
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    public static boolean ignore = false;

    public BlockInfernalFurnace() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(2.0f, 10.0f).sound(SoundType.STONE).noOcclusion()
                .lightLevel(s -> 13).noLootTable().pushReaction(PushReaction.BLOCK));
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
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileInfernalFurnace(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TCBlockEntities.INFERNAL_FURNACE.get(), TileInfernalFurnace::serverTick);
    }

    public static void destroyFurnace(Level world, BlockPos pos, BlockState state, BlockPos startpos) {
        if (ignore || world.isClientSide) {
            return;
        }
        ignore = true;
        for (int a = -1; a <= 1; ++a) {
            for (int b = -1; b <= 1; ++b) {
                for (int c = -1; c <= 1; ++c) {
                    BlockPos p = pos.offset(a, b, c);
                    if (!p.equals(startpos)) {
                        BlockState bs = world.getBlockState(p);
                        if (bs.is(ConfigBlocks.placeholderNetherbrick.get())) {
                            world.setBlockAndUpdate(p, Blocks.NETHER_BRICKS.defaultBlockState());
                        }
                        if (bs.is(ConfigBlocks.placeholderObsidian.get())) {
                            world.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                        }
                    }
                }
            }
        }
        BlockPos back = pos.relative(state.getValue(FACING).getOpposite());
        if (world.isEmptyBlock(back)) {
            world.setBlockAndUpdate(back, Blocks.IRON_BARS.defaultBlockState());
        }
        world.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
        ignore = false;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof TileInfernalFurnace tile) {
                net.minecraft.world.Containers.dropContents(world, pos, tile);
            }
            super.onRemove(state, world, pos, newState, movedByPiston);
            destroyFurnace(world, pos, state, pos);
            return;
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    /** Items are pulled to the middle and smelted once they settle; anything alive burns. */
    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        Vec3 m = entity.getDeltaMovement();
        double mx = m.x;
        double mz = m.z;
        if (entity.getX() < pos.getX() + 0.3f) mx += 1.0E-4;
        if (entity.getX() > pos.getX() + 0.7f) mx -= 1.0E-4;
        if (entity.getZ() < pos.getZ() + 0.3f) mz += 1.0E-4;
        if (entity.getZ() > pos.getZ() + 0.7f) mz -= 1.0E-4;
        entity.setDeltaMovement(mx, m.y, mz);
        if (!world.isClientSide && entity.tickCount % 10 == 0) {
            if (entity instanceof ItemEntity item) {
                item.setDeltaMovement(item.getDeltaMovement().x, 0.025, item.getDeltaMovement().z);
                if (item.onGround() && world.getBlockEntity(pos) instanceof TileInfernalFurnace taf) {
                    item.setItem(taf.addItemsToInventory(item.getItem()));
                }
            } else if (entity instanceof LivingEntity && !entity.fireImmune()) {
                entity.hurt(world.damageSources().lava(), 3.0f);
                entity.igniteForSeconds(10);
            }
        }
        super.entityInside(state, world, pos, entity);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity te = level.getBlockEntity(pos);
        return te != null && te.triggerEvent(id, param);
    }
}
