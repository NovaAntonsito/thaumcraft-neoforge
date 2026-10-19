package thaumcraft.common.blocks.eldritch;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.eldritch.TileEldritchTrap;

/**
 * BlockEldritch meta 10 of TC4: the Runed Stone, a trap that zaps players who come close. Hardness 15, resistance
 * 30, no light, drops nothing but 1-4 experience. Runes drift in the air around it.
 */
public class BlockEldritchTrap extends BaseEntityBlock {
    public static final MapCodec<BlockEldritchTrap> CODEC = simpleCodec(p -> new BlockEldritchTrap());
    private static final UniformInt XP = UniformInt.of(1, 4);

    public BlockEldritchTrap() {
        super(ConfigBlocks.eldritchSolidProps(15.0f, 30.0f, 0));
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
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (dropExperience) {
            tryDropExperience(level, pos, stack, XP);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEldritchTrap(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TCBlockEntities.ELDRITCH_TRAP.get(), TileEldritchTrap::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level w, BlockPos pos, RandomSource r) {
        int x = pos.getX() + r.nextInt(2) - r.nextInt(2);
        int y = pos.getY() + r.nextInt(2) - r.nextInt(2);
        int z = pos.getZ() + r.nextInt(2) - r.nextInt(2);
        if (w.isEmptyBlock(new BlockPos(x, y, z))) {
            FXDispatcher.blockRunes(x + r.nextFloat(), y + r.nextFloat(), z + r.nextFloat(), 0.5f + r.nextFloat() * 0.5f, r.nextFloat() * 0.3f,
                    0.9f + r.nextFloat() * 0.1f, 16 + r.nextInt(4), 0.0f);
        }
    }
}
