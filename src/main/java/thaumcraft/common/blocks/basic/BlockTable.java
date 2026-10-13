package thaumcraft.common.blocks.basic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/** Port of BlockTable (wooden and stone table). Scribing tools on the wooden one turn it into a research table. */
public class BlockTable extends Block {
    /** The model's boxes: top, four legs and the shelf. */
    protected static final VoxelShape SHAPE = Shapes.or(
            box(0, 12, 0, 16, 16, 16),
            box(11, 0, 1, 15, 12, 5),
            box(11, 0, 11, 15, 12, 15),
            box(1, 0, 11, 5, 12, 15),
            box(1, 0, 1, 5, 12, 5),
            box(3, 3, 3, 13, 5, 13));

    public BlockTable(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (this != ConfigBlocks.tableWood.get() || !(stack.getItem() instanceof IScribeTools)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world.isClientSide) return ItemInteractionResult.SUCCESS;
        BlockState bs = ConfigBlocks.researchTable.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, player.getDirection());
        world.setBlock(pos, bs, 3);
        if (world.getBlockEntity(pos) instanceof TileResearchTable tile) {
            tile.setItem(0, stack.copy());
            player.setItemInHand(hand, ItemStack.EMPTY);
            tile.setChanged();
            tile.syncTile(false);
        }
        EventHooks.firePlayerCraftingEvent(player, new ItemStack(ConfigBlocks.researchTable.get()), new SimpleContainer(1));
        return ItemInteractionResult.CONSUME;
    }
}
