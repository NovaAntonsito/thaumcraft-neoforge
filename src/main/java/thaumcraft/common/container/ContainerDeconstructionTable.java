package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.crafting.TileDeconstructionTable;

/** Reborn's ContainerDeconstructionTable: the item slot, the player inventory and the break timer. */
public class ContainerDeconstructionTable extends AbstractContainerMenu {
    private final TileDeconstructionTable table;

    /** Client side: the table position comes from the open packet. */
    public ContainerDeconstructionTable(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileDeconstructionTable) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerDeconstructionTable(int id, Inventory inventoryPlayer, TileDeconstructionTable tileEntity) {
        super(TCMenus.DECONSTRUCTION_TABLE.get(), id);
        table = tileEntity;
        // SlotLimitedHasAspects
        addSlot(new Slot(tileEntity, 0, 64, 16) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(0, stack);
            }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int var5 = 0; var5 < 9; ++var5) {
            addSlot(new Slot(inventoryPlayer, var5, 8 + var5 * 18, 142));
        }
        // detectAndSendChanges / updateProgressBar for the break time.
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return table.breaktime;
            }

            @Override
            public void set(int value) {
                table.breaktime = value;
            }
        });
    }

    public TileDeconstructionTable getTile() {
        return table;
    }

    /** enchantItem: button 1 takes the aspect. */
    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 1) {
            table.takeAspect(player);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return table.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index != 0) {
                AspectList al = ThaumcraftCraftingManager.getObjectTags(itemstack1);
                if (al != null && al.size() > 0) {
                    if (!moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!moveItemStackTo(itemstack1, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37 && !moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack1, 1, 37, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }
}
