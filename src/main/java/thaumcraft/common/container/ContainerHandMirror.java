package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.items.tools.ItemHandMirror;
import thaumcraft.common.registry.TCMenus;

/**
 * Port of ContainerHandMirror (and InventoryHandMirror). One slot: whatever goes in is sent through the linked
 * mirror. The hand mirror itself cannot be moved while the menu is open.
 */
public class ContainerHandMirror extends AbstractContainerMenu {
    public final SimpleContainer input = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    private final ItemStack mirror;
    private final Player player;
    /** Putting a stack back after a failed send must not try again (the original recursed here). */
    private boolean sending;

    /** Client side: the hand holding the mirror comes from the open packet. */
    public ContainerHandMirror(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, buf.readEnum(InteractionHand.class));
    }

    public ContainerHandMirror(int id, Inventory inventory, InteractionHand hand) {
        super(TCMenus.HAND_MIRROR.get(), id);
        player = inventory.player;
        mirror = player.getItemInHand(hand);
        addSlot(new Slot(input, 0, 80, 24));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container inv) {
        super.slotsChanged(inv);
        if (inv == input && !sending && !player.level().isClientSide && !input.getItem(0).isEmpty()) {
            sending = true;
            ItemStack is = input.getItem(0).copy();
            input.removeItemNoUpdate(0);
            if (ItemHandMirror.transport(mirror, is, player, player.level())) {
                broadcastChanges();
            } else {
                input.setItem(0, is);
            }
            sending = false;
        }
    }

    /** slotClick: the hand mirror stays put, and the hotbar keys are off (checkHotbarKeys returned false). */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP) {
            return;
        }
        if (slotId >= 0 && slotId < slots.size() && slots.get(slotId).getItem().getItem() instanceof ItemHandMirror) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject != null && slotObject.hasItem() && !(slotObject.getItem().getItem() instanceof ItemHandMirror)) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot == 0) {
                if (!moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
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

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** Anything left in the slot goes back to the player. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            clearContainer(player, input);
        }
    }
}
