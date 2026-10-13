package thaumcraft.common.lib.crafting;

import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.crafting.IDustTrigger;

/**
 * Port of DustTriggerSimple (and DustTriggerOre, now a tag predicate): turns one block into a result, a block
 * (cauldron into crucible) or an item (bookshelf into thaumonomicon). The result counts as crafted, which
 * research such as FIRSTSTEPS waits for.
 * <p>
 * TODO: the original swaps after 50 ticks through ServerEvents.addSwapper, with its effects; here it is immediate.
 */
public class DustTriggerSimple implements IDustTrigger {
    final Predicate<BlockState> target;
    final Supplier<ItemStack> result;
    final String research;

    public DustTriggerSimple(String research, Predicate<BlockState> target, Supplier<ItemStack> result) {
        this.target = target;
        this.result = result;
        this.research = research;
    }

    @Nullable
    @Override
    public Placement getValidFace(Level world, Player player, BlockPos pos, Direction face) {
        return target.test(world.getBlockState(pos)) && (research == null || ThaumcraftCapabilities.getKnowledge(player).isResearchKnown(research))
                ? new Placement(0, 0, 0, null) : null;
    }

    @Override
    public void execute(Level world, Player player, BlockPos pos, Placement placement, Direction side) {
        if (world.isClientSide) return;
        ItemStack stack = result.get();
        EventHooks.firePlayerCraftingEvent(player, stack.copy(), new SimpleContainer(1));
        // ServerEvents swapper: a block item is placed, anything else drops where the block was.
        if (stack.getItem() instanceof BlockItem blockItem) {
            world.setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());
        } else {
            world.removeBlock(pos, false);
            ItemEntity drop = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy());
            drop.setDeltaMovement(0, 0, 0);
            world.addFreshEntity(drop);
        }
    }
}
