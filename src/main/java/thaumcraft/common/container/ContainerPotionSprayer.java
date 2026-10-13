package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.devices.TilePotionSprayer;

/** Port of ContainerPotionSprayer: the potion slot (SlotPotion) and the player inventory. */
public class ContainerPotionSprayer extends AbstractContainerMenu {
    public final TilePotionSprayer sprayer;

    public ContainerPotionSprayer(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TilePotionSprayer) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerPotionSprayer(int id, Inventory inventory, TilePotionSprayer tile) {
        super(TCMenus.POTION_SPRAYER.get(), id);
        sprayer = tile;
        addSlot(new Slot(tile, 0, 56, 64) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return TilePotionSprayer.isValidPotion(stack);
            }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventory, j + i * 9 + 9, 16 + j * 18, 151 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 16 + i * 18, 209));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return sprayer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot == 0) {
                if (!sprayer.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!sprayer.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 1, false)) {
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
