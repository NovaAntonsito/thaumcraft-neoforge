package thaumcraft.common.blocks.misc;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.misc.TileNitor;

/** Port of BlockNitor: a floating flame of energized glowstone, one block per dye color. Drawn only by its particles. */
public class BlockNitor extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = box(5.28, 5.28, 5.28, 10.56, 10.56, 10.56);

    public final DyeColor dye;
    private final MapCodec<BlockNitor> codec;

    public BlockNitor(DyeColor dye) {
        super(BlockBehaviour.Properties.of().mapColor(dye).strength(0.1f, 1.2f).sound(SoundType.WOOL)
                .lightLevel(s -> 15).noCollission().noOcclusion().pushReaction(PushReaction.DESTROY));
        this.dye = dye;
        this.codec = simpleCodec(p -> new BlockNitor(dye));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileNitor(pos, state);
    }

    /** TileNitor only ticks on the client, for its flames. */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, TCBlockEntities.NITOR.get(), TileNitor::clientTick) : null;
    }
}
