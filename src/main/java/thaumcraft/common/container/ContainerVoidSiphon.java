package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.crafting.TileVoidSiphon;

/** Port of ContainerVoidSiphon: the output slot, the player inventory and the progress. */
public class ContainerVoidSiphon extends AbstractContainerMenu {
    private final TileVoidSiphon siphon;

    public ContainerVoidSiphon(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileVoidSiphon) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerVoidSiphon(int id, Inventory inventory, TileVoidSiphon tileEntity) {
        super(TCMenus.VOID_SIPHON.get(), id);
        siphon = tileEntity;
        // SlotOutput
        addSlot(new Slot(tileEntity, 0, 80, 32) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
        // sendWindowProperty / updateProgressBar.
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return siphon.progress;
            }

            @Override
            public void set(int value) {
                siphon.progress = value;
            }
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return siphon.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot == 0) {
                if (!siphon.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!siphon.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slotObject.setByPlayer(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }
        }
        return stack;
    }
}
