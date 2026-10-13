package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;

/**
 * Port of ContainerFocalManipulator: the focus slot and the player inventory, laid out sideways on the left of the
 * GUI (three columns of nine, and the hotbar as a 3x3 block).
 */
public class ContainerFocalManipulator extends AbstractContainerMenu {
    private final TileFocalManipulator table;

    /** Client side: the manipulator position comes from the open packet. */
    public ContainerFocalManipulator(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileFocalManipulator) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerFocalManipulator(int id, Inventory inventoryPlayer, TileFocalManipulator tileEntity) {
        super(TCMenus.FOCAL_MANIPULATOR.get(), id);
        table = tileEntity;
        // SlotFocus
        addSlot(new Slot(tileEntity, 0, 31, 191) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemFocus;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, i * 18 - 62, 64 + j * 18));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                addSlot(new Slot(inventoryPlayer, i + j * 3, i * 18 - 62, j * 18 + 7));
            }
        }
    }

    public TileFocalManipulator getTile() {
        return table;
    }

    /** enchantItem: button 0 starts the craft. */
    @Override
    public boolean clickMenuButton(Player p, int button) {
        if (button == 0 && !table.startCraft(button, p)) {
            table.getLevel().playSound(null, table.getBlockPos(), SoundsTC.CRAFTFAIL.get(), SoundSource.BLOCKS, 0.33f, 1.0f);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return table.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int par2) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(par2);
        if (slot.hasItem()) {
            ItemStack itemstack2 = slot.getItem();
            itemstack = itemstack2.copy();
            if (par2 != 0) {
                if (itemstack2.getItem() instanceof ItemFocus) {
                    if (!moveItemStackTo(itemstack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (par2 < 28) {
                    if (!moveItemStackTo(itemstack2, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (par2 < 37 && !moveItemStackTo(itemstack2, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack2, 1, 37, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack2.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack2);
        }
        return itemstack;
    }
}
