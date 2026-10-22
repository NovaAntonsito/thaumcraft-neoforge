package thaumcraft.common.blocks.misc;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.misc.TileNodeConverter;
import thaumcraft.common.tiles.misc.TileNodeStabilizer;

/**
 * The node stabilizer, advanced node stabilizer and node transducer of Thaumcraft 4 (BlockStoneDevice 9, 10 and 11).
 * The base is a block model; the pistons are drawn by the tile renderers. The transducer hangs upside down.
 */
public class BlockNodeStabilizer extends BaseEntityBlock {
    public enum Kind { STABILIZER, ADVANCED, TRANSDUCER }

    public static final MapCodec<BlockNodeStabilizer> CODEC = simpleCodec(p -> new BlockNodeStabilizer(Kind.STABILIZER));
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 12, 16);
    private static final VoxelShape SHAPE_DOWN = box(0, 4, 0, 16, 16, 16);
    public final Kind kind;

    public BlockNodeStabilizer(Kind kind) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f, 10.0f).sound(SoundType.STONE).noOcclusion()
                .requiresCorrectToolForDrops());
        this.kind = kind;
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
        return kind == Kind.TRANSDUCER ? SHAPE_DOWN : SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return kind == Kind.TRANSDUCER ? new TileNodeConverter(pos, state) : new TileNodeStabilizer(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (kind == Kind.TRANSDUCER) {
            return createTickerHelper(type, TCBlockEntities.NODE_TRANSDUCER.get(), TileNodeConverter::tick);
        }
        return level.isClientSide ? createTickerHelper(type, TCBlockEntities.NODE_STABILIZER.get(), TileNodeStabilizer::clientTick) : null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, BlockPos from, boolean moving) {
        super.neighborChanged(state, level, pos, block, from, moving);
        if (level.getBlockEntity(pos) instanceof TileNodeConverter tc) tc.checkStatus();
    }
}
