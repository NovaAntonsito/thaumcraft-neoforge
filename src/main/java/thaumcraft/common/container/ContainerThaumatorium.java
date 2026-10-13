package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.crafting.TileThaumatorium;

/** Port of ContainerThaumatorium: the catalyst slot and the player inventory. Keeps the recipe list current. */
public class ContainerThaumatorium extends AbstractContainerMenu {
    private final TileThaumatorium thaumatorium;
    private final Player player;

    public ContainerThaumatorium(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileThaumatorium) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerThaumatorium(int id, Inventory inventory, TileThaumatorium tileEntity) {
        super(TCMenus.THAUMATORIUM.get(), id);
        thaumatorium = tileEntity;
        player = inventory.player;
        addSlot(new Slot(tileEntity, 0, 55, 24));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 135 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 8 + i * 18, 193));
        }
        thaumatorium.updateRecipes(player);
    }

    public TileThaumatorium getTile() {
        return thaumatorium;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        thaumatorium.updateRecipes(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return thaumatorium.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack2 = slot.getItem();
            itemstack = itemstack2.copy();
            if (index != 0) {
                if (!moveItemStackTo(itemstack2, 0, 1, false)) {
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
