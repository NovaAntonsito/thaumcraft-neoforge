package thaumcraft.common.blocks.eldritch;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.eldritch.TileEldritchNothing;

/**
 * Port of TC4's BlockEldritchNothing, the void that wraps the Outer Lands maze. It cannot be broken, drops nothing,
 * cannot be selected, and hurts anything that touches it (8 damage as falling out of the world, after the entity's
 * first second; creative players are spared). Meta 1, the exposed void, is its own block {@link Exposed}: only that
 * one has the block entity that draws the void on its faces. They swap when a neighbour changes, as in the original.
 */
public class BlockEldritchNothing extends Block {
    private static final VoxelShape COLLISION = Block.box(2.0, 2.0, 2.0, 14.0, 14.0, 14.0);

    public BlockEldritchNothing() {
        // setBlockUnbreakable, setResistance(6000000), cloth sound, setLightLevel(0.2F).
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3600000.0f).sound(SoundType.WOOL)
                .lightLevel(s -> 3).noOcclusion().noLootTable());
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        BlockState want = stateFor(world, pos);
        if (want.getBlock() != this) {
            world.setBlock(pos, want, Block.UPDATE_ALL);
        }
        super.neighborChanged(state, world, pos, block, fromPos, moving);
    }

    /** The void block that belongs here: the exposed one when a neighbour is not an opaque cube. */
    public static BlockState stateFor(BlockGetter world, BlockPos pos) {
        return (isExposed(world, pos) ? ConfigBlocks.eldritchNothingExposed : ConfigBlocks.eldritchNothing).get().defaultBlockState();
    }

    /** BlockUtils.isBlockExposed: any neighbour that is not an opaque cube. */
    public static boolean isExposed(BlockGetter world, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            if (!world.getBlockState(n).isSolidRender(world, n)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (entity.tickCount > 20 && !(entity instanceof Player player && player.getAbilities().instabuild)) {
            entity.hurt(world.damageSources().fellOutOfWorld(), 8.0f);
        }
    }

    /** Meta 1: the void with something to look at it from. */
    public static class Exposed extends BlockEldritchNothing implements EntityBlock {
        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new TileEldritchNothing(pos, state);
        }
    }
}
