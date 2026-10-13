package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/** Reborn's ContainerResearchTable: scribing tools, the note, and the player inventory under the parchment. */
public class ContainerResearchTable extends AbstractContainerMenu {
    public final TileResearchTable tileEntity;
    public final Player player;

    /** Client side: the table position comes from the open packet. */
    public ContainerResearchTable(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileResearchTable) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerResearchTable(int id, Inventory playerInventory, TileResearchTable table) {
        super(TCMenus.RESEARCH_TABLE.get(), id);
        player = playerInventory.player;
        tileEntity = table;
        addSlot(new LimitedSlot(table, 0, 14, 10));
        addSlot(new LimitedSlot(table, 1, 70, 10));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 48 + j * 18, 175 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, 48 + i * 18, 233));
        }
    }

    /** SlotLimitedByClass: scribing tools in slot 0, a note in slot 1, one item each. */
    private static class LimitedSlot extends Slot {
        LimitedSlot(TileResearchTable table, int index, int x, int y) {
            super(table, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getContainerSlot(), stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            stack = stackInSlot.copy();
            if (slotIndex < 2) {
                if (!moveItemStackTo(stackInSlot, 2, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stackInSlot, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity.stillValid(player);
    }
}
