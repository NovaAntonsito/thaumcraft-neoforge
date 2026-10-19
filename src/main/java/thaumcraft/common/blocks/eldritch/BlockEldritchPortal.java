package thaumcraft.common.blocks.eldritch;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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
import thaumcraft.common.tiles.eldritch.TileEldritchPortal;

/**
 * Port of TC4's BlockEldritchPortal, the swirling portal between an eldritch altar (or a capstone) and the obelisk
 * above it. Unbreakable, light 15, no collision, cannot be selected, drops nothing. It vanishes when the block above
 * or below is no longer part of an eldritch structure. Its block entity moves players to the Outer Lands and back.
 */
public class BlockEldritchPortal extends BaseEntityBlock {
    public static final MapCodec<BlockEldritchPortal> CODEC = simpleCodec(p -> new BlockEldritchPortal());

    public BlockEldritchPortal() {
        super(BlockBehaviour.Properties.of().strength(-1.0f, 200000.0f).lightLevel(s -> 15).noCollission().noOcclusion()
                .noLootTable().pushReaction(PushReaction.BLOCK));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEldritchPortal(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.ELDRITCH_PORTAL.get(),
                level.isClientSide ? TileEldritchPortal::clientTick : TileEldritchPortal::serverTick);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!(world.getBlockState(pos.above()).getBlock() instanceof BlockEldritch)
                || !(world.getBlockState(pos.below()).getBlock() instanceof BlockEldritch)) {
            world.removeBlock(pos, false);
        }
        super.neighborChanged(state, world, pos, block, fromPos, moving);
    }
}
