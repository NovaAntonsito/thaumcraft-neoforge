package thaumcraft.common.blocks.crafting;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
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
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.crafting.TileInfusionMatrix;

/** Port of BlockInfusionMatrix (the runic matrix). Drawn by TileInfusionMatrixRenderer. */
public class BlockInfusionMatrix extends BaseEntityBlock {
    public static final MapCodec<BlockInfusionMatrix> CODEC = simpleCodec(p -> new BlockInfusionMatrix());

    public BlockInfusionMatrix() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f, 12.0f).sound(SoundType.STONE).noOcclusion());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileInfusionMatrix(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.INFUSION_MATRIX.get(), TileInfusionMatrix::tick);
    }
}
