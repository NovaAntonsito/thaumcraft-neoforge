package thaumcraft.common.container;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.casters.ItemFocusPouch;
import thaumcraft.common.registry.TCMenus;

/**
 * Port of ContainerFocusPouch (and InventoryFocusPouch): 18 focus slots over the player inventory. The contents go
 * back into the pouch when the menu closes; the hotbar slot holding the pouch cannot be touched.
 */
public class ContainerFocusPouch extends AbstractContainerMenu {
    public final SimpleContainer input = new SimpleContainer(18) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.getItem() instanceof ItemFocus;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    private final int blockSlot;
    final ItemStack pouch;
    final Player player;
    public final InteractionHand hand;

    /** Client side: the hand holding the pouch comes from the open packet. */
    public ContainerFocusPouch(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, buf.readEnum(InteractionHand.class));
    }

    public ContainerFocusPouch(int id, Inventory iinventory, InteractionHand hand) {
        super(TCMenus.FOCUS_POUCH.get(), id);
        player = iinventory.player;
        this.hand = hand;
        pouch = player.getItemInHand(hand);
        blockSlot = hand == InteractionHand.MAIN_HAND ? iinventory.selected + 45 : -1;
        for (int a = 0; a < 18; ++a) {
            addSlot(new Slot(input, a, 37 + a % 6 * 18, 51 + a / 6 * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ItemFocus;
                }
            });
        }
        bindPlayerInventory(iinventory);
        if (!player.level().isClientSide && pouch.getItem() instanceof ItemFocusPouch fp) {
            NonNullList<ItemStack> list = fp.getInventory(pouch);
            for (int a2 = 0; a2 < list.size(); ++a2) {
                input.setItem(a2, list.get(a2));
            }
        }
    }

    protected void bindPlayerInventory(Inventory inventoryPlayer) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 151 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 209));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slot) {
        if (slot == blockSlot) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot < 18) {
                if (!input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 18, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 18, false)) {
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
    public boolean stillValid(Player var1) {
        return true;
    }

    /** slotClick: the pouch stays put, and the hotbar keys are off (checkHotbarKeys returned false). */
    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId == blockSlot || clickTypeIn == ClickType.SWAP) {
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public void removed(Player par1EntityPlayer) {
        super.removed(par1EntityPlayer);
        if (!player.level().isClientSide) {
            NonNullList<ItemStack> list = NonNullList.withSize(18, ItemStack.EMPTY);
            for (int a = 0; a < list.size(); ++a) {
                list.set(a, input.getItem(a));
            }
            if (pouch.getItem() instanceof ItemFocusPouch fp) {
                fp.setInventory(pouch, list);
            }
            player.getInventory().setChanged();
        }
    }
}
