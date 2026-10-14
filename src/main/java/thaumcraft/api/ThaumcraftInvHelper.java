package thaumcraft.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import thaumcraft.common.lib.utils.InventoryUtils;

/**
 * Partial port of ThaumcraftInvHelper: the filter settings and the inventory lookups the golem seals use.
 * TODO: the crafting comparisons (areItemStacksEqualForCrafting, containsMatch, ...) when something needs them.
 */
public class ThaumcraftInvHelper {

    public static class InvFilter {
        public boolean igDmg;
        public boolean igNBT;
        public boolean useOre;
        public boolean useMod;
        public boolean relaxedNBT = false;

        public InvFilter(boolean ignoreDamage, boolean ignoreNBT, boolean useOre, boolean useMod) {
            igDmg = ignoreDamage;
            igNBT = ignoreNBT;
            this.useOre = useOre;
            this.useMod = useMod;
        }

        public InvFilter setRelaxedNBT() {
            relaxedNBT = true;
            return this;
        }

        public static InvFilter STRICT = new InvFilter(false, false, false, false);
        public static InvFilter BASEORE = new InvFilter(false, false, true, false);
    }

    /**
     * The item handler of the block at pos, seen from side. The capability also covers plain vanilla containers,
     * so the IInventory wrapping of the original is not needed.
     */
    public static IItemHandler getItemHandlerAt(Level world, BlockPos pos, Direction side) {
        return world.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
    }

    public static ItemStack insertStackAt(Level world, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        IItemHandler inventory = getItemHandlerAt(world, pos, side);
        if (inventory != null) {
            return ItemHandlerHelper.insertItemStacked(inventory, stack, simulate);
        }
        return stack;
    }

    public static ItemStack hasRoomFor(Level world, BlockPos pos, Direction side, ItemStack stack) {
        ItemStack testStack = insertStackAt(world, pos, side, stack.copy(), true);
        if (testStack.isEmpty()) {
            return stack.copy();
        }
        testStack.setCount(stack.getCount() - testStack.getCount());
        return testStack;
    }

    public static boolean hasRoomForSome(Level world, BlockPos pos, Direction side, ItemStack stack) {
        ItemStack testStack = insertStackAt(world, pos, side, stack.copy(), true);
        return stack.getCount() == 0 || testStack.getCount() != stack.getCount();
    }

    public static boolean hasRoomForAll(Level world, BlockPos pos, Direction side, ItemStack stack) {
        return insertStackAt(world, pos, side, stack.copy(), true).isEmpty();
    }

    public static int countTotalItemsIn(IItemHandler inventory, ItemStack stack, InvFilter filter) {
        int count = 0;
        if (inventory != null) {
            for (int a = 0; a < inventory.getSlots(); a++) {
                if (InventoryUtils.areItemStacksEqual(stack, inventory.getStackInSlot(a), filter)) {
                    count += inventory.getStackInSlot(a).getCount();
                }
            }
        }
        return count;
    }

    public static int countTotalItemsIn(Level world, BlockPos pos, Direction side, ItemStack stack, InvFilter filter) {
        return countTotalItemsIn(getItemHandlerAt(world, pos, side), stack, filter);
    }
}
