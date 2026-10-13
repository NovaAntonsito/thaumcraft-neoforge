package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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
import thaumcraft.api.crafting.IInfusionStabiliserExt;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileStabilizer;

/**
 * Port of BlockStabilizer (infusion stabilizer). Stabilizes infusion like a candle pair (0.25), steadies flux rifts
 * nearby and feeds charge into the vis inlay. Its crystal brightens with the stored charge (block color).
 */
public class BlockStabilizer extends BaseEntityBlock implements IInfusionStabiliserExt {
    public static final MapCodec<BlockStabilizer> CODEC = simpleCodec(p -> new BlockStabilizer());

    public BlockStabilizer() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.0f, 10.0f)
                .sound(SoundType.STONE).noOcclusion().lightLevel(s -> 4));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileStabilizer(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TCBlockEntities.STABILIZER.get(), TileStabilizer::serverTick);
    }

    /** Same as the inlay: grey when empty, brighter with charge. */
    public static int colorMultiplier(int meta) {
        float f = meta / 15.0f;
        float f2 = f * 0.5f + 0.5f;
        if (meta == 0) {
            f2 = 0.3f;
        }
        int i = Mth.clamp((int) (f2 * 255.0f), 0, 255);
        return 0xFF000000 | i << 16 | i << 8 | i;
    }

    @Override
    public boolean canStabaliseInfusion(Level world, BlockPos pos) {
        return true;
    }

    @Override
    public float getStabilizationAmount(Level world, BlockPos pos) {
        return 0.25f;
    }
}
