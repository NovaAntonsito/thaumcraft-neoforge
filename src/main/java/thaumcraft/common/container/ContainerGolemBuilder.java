package thaumcraft.common.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.crafting.TileGolemBuilder;

/** Port of ContainerGolemBuilder: the output slot, the player inventory, and the cost and max cost. */
public class ContainerGolemBuilder extends AbstractContainerMenu {
    private final TileGolemBuilder builder;
    public static boolean redo = false;

    public ContainerGolemBuilder(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, (TileGolemBuilder) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ContainerGolemBuilder(int id, Inventory inventory, TileGolemBuilder tileEntity) {
        super(TCMenus.GOLEM_BUILDER.get(), id);
        builder = tileEntity;
        // SlotOutput
        addSlot(new Slot(tileEntity, 0, 160, 104) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventory, j + i * 9 + 9, 24 + j * 18, 142 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 24 + i * 18, 200));
        }
        // sendWindowProperty / updateProgressBar.
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return builder.cost;
            }

            @Override
            public void set(int value) {
                builder.cost = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return builder.maxCost;
            }

            @Override
            public void set(int value) {
                builder.maxCost = value;
            }
        });
    }

    public TileGolemBuilder getBuilder() {
        return builder;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        redo = true;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        redo = true;
        super.setItem(slotId, stateId, stack);
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 99) {
            redo = true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return builder.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot == 0) {
                if (!builder.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!builder.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 1, false)) {
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
