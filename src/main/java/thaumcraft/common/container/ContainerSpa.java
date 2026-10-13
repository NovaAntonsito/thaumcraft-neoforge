package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.items.consumables.ItemBathSalts;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.devices.TileSpa;

/** Port of ContainerSpa: the bath salts slot and the player inventory. Button 1 toggles the mix mode. */
public class ContainerSpa extends AbstractContainerMenu {
    public final TileSpa spa;

    public ContainerSpa(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileSpa) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerSpa(int id, Inventory inventory, TileSpa tileEntity) {
        super(TCMenus.SPA.get(), id);
        spa = tileEntity;
        // SlotLimitedByClass(ItemBathSalts.class)
        addSlot(new Slot(tileEntity, 0, 65, 31) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemBathSalts;
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
    }

    /** enchantItem. */
    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 1) {
            spa.toggleMix();
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return spa.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot == 0) {
                if (!spa.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!spa.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 1, false)) {
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
