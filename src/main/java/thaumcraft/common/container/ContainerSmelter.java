package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.container.slot.SlotLimitedHasAspects;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.essentia.TileSmelter;

/**
 * Port of ContainerSmelter. Slots: 0 item to smelt, 1 fuel, 2-28 player inventory, 29-37 hotbar.
 * The window properties (cook time, burn time, item burn time, vis, smelt time) are the tile's {@link TileSmelter#data}.
 */
public class ContainerSmelter extends AbstractContainerMenu {
    private final TileSmelter furnace;

    /** Client side: the smelter position comes from the open packet. */
    public ContainerSmelter(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileSmelter) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerSmelter(int id, Inventory par1InventoryPlayer, TileSmelter tileEntity) {
        super(TCMenus.SMELTER.get(), id);
        furnace = tileEntity;
        addSlot(new SlotLimitedHasAspects(tileEntity, 0, 80, 8));
        addSlot(new Slot(tileEntity, 1, 80, 48));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(par1InventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(par1InventoryPlayer, i, 8 + i * 18, 142));
        }
        addDataSlots(tileEntity.data);
    }

    public TileSmelter getTile() {
        return furnace;
    }

    @Override
    public boolean stillValid(Player player) {
        return furnace.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int par2) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(par2);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack2 = slot.getItem();
            itemstack = itemstack2.copy();
            if (par2 != 1 && par2 != 0) {
                AspectList al = ThaumcraftCraftingManager.getObjectTags(itemstack2);
                if (TileSmelter.isItemFuel(itemstack2)) {
                    if (!moveItemStackTo(itemstack2, 1, 2, false) && !moveItemStackTo(itemstack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (al != null && al.size() > 0) {
                    if (!moveItemStackTo(itemstack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (par2 >= 2 && par2 < 29) {
                    if (!moveItemStackTo(itemstack2, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (par2 >= 29 && par2 < 38 && !moveItemStackTo(itemstack2, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack2, 2, 38, false)) {
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
