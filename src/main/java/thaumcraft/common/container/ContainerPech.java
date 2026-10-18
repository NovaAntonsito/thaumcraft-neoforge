package thaumcraft.common.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.common.container.slot.SlotOutput;
import thaumcraft.common.entities.ai.pech.AIPechItemEntityGoto;
import thaumcraft.common.entities.monster.EntityPech;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCMenus;

/**
 * Port of ContainerPech: put something valued in the left slot and roll the dice (button 0). The pech hands out goods
 * worth about what it is offered, from its trade list (EntityPech.tradeInventory) or now and then from its own pack,
 * and may decide it has had enough of you (a value / 2 in 100 chance to stop being tame). Whatever is left in the
 * slots when the window closes is dropped at the player's feet.
 */
public class ContainerPech extends AbstractContainerMenu {
    private final EntityPech pech;
    private final InventoryPech inventory;
    private final Player player;
    private final Level theWorld;

    /** Client side: the pech comes from the open packet. */
    public ContainerPech(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (EntityPech) playerInv.player.level().getEntity(buf.readVarInt()));
    }

    public ContainerPech(int id, Inventory playerInv, EntityPech merchant) {
        super(TCMenus.PECH.get(), id);
        pech = merchant;
        player = playerInv.player;
        theWorld = player.level();
        inventory = new InventoryPech(merchant);
        pech.trading = true;
        addSlot(new Slot(inventory, 0, 36, 29));
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                addSlot(new SlotOutput(inventory, 1 + j + i * 2, 106 + 18 * j, 20 + 18 * i));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    public EntityPech getPech() {
        return pech;
    }

    /** enchantItem: button 0 is the trade. */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            generateContents();
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    private boolean hasStuffInPack() {
        for (ItemStack stack : pech.loot) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Whether the offer can be traded: something valued, and the four output slots empty. */
    public boolean canTrade() {
        return !inventory.getItem(0).isEmpty() && inventory.getItem(1).isEmpty() && inventory.getItem(2).isEmpty()
                && inventory.getItem(3).isEmpty() && inventory.getItem(4).isEmpty() && pech.isValued(inventory.getItem(0));
    }

    private void generateContents() {
        if (!theWorld.isClientSide && canTrade()) {
            int value = pech.getValue(inventory.getItem(0));
            if (theWorld.random.nextInt(100) <= value / 2) {
                pech.setTamed(false);
                pech.playSound(SoundsTC.PECH_TRADE.get(), 0.4f, 1.0f);
            }
            if (theWorld.random.nextInt(5) == 0) {
                value += theWorld.random.nextInt(3);
            } else if (theWorld.random.nextBoolean()) {
                value -= theWorld.random.nextInt(3);
            }
            ArrayList<List<Object>> pos = EntityPech.tradeInventory.get(pech.getPechType());
            while (value > 0) {
                int am = Math.min(5, Math.max((value + 1) / 2, theWorld.random.nextInt(value) + 1));
                value -= am;
                if (am == 1 && theWorld.random.nextBoolean() && hasStuffInPack()) {
                    ArrayList<Integer> loot = new ArrayList<>();
                    for (int a = 0; a < pech.loot.size(); ++a) {
                        if (!pech.loot.get(a).isEmpty()) {
                            loot.add(a);
                        }
                    }
                    int r = loot.get(theWorld.random.nextInt(loot.size()));
                    ItemStack is = pech.loot.get(r).copy();
                    is.setCount(1);
                    addStack(is);
                    pech.loot.get(r).shrink(1);
                    if (pech.loot.get(r).isEmpty()) {
                        pech.loot.set(r, ItemStack.EMPTY);
                    }
                } else {
                    if (am >= 4 && theWorld.random.nextBoolean()) {
                        continue;
                    }
                    // The original re-rolled until it hit a trade worth exactly am. Some trades wait for items not
                    // ported yet, so a value with nothing left in it gives nothing instead of looping forever.
                    List<List<Object>> worth = new ArrayList<>();
                    if (pos != null) {
                        for (List<Object> it : pos) {
                            if ((int) it.get(0) == am) {
                                worth.add(it);
                            }
                        }
                    }
                    if (worth.isEmpty()) {
                        continue;
                    }
                    List<Object> it = worth.get(theWorld.random.nextInt(worth.size()));
                    ItemStack is2 = thaumcraft.common.config.ConfigEntities.tradeStack(it.get(1), theWorld.registryAccess());
                    is2.onCraftedBy(theWorld, player, 0);
                    addStack(is2);
                }
            }
            inventory.removeItem(0, 1);
        }
    }

    /**
     * Into the first empty output slot. As in the original, a slot already holding the same item also grows on the
     * way there.
     */
    private void addStack(ItemStack s) {
        for (int a = 1; a < 5; ++a) {
            if (inventory.getItem(a).isEmpty()) {
                inventory.setItem(a, s);
                break;
            }
            if (ItemStack.isSameItem(inventory.getItem(a), s) && inventory.getItem(a).getCount() + s.getCount() < inventory.getItem(a).getMaxStackSize()) {
                inventory.getItem(a).grow(s.getCount());
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return pech.isTamed();
    }

    /** transferStackInSlot: the trade slots go to the player, the player's inventory to the offer slot. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack2 = slot.getItem();
            itemstack = itemstack2.copy();
            if (index >= 0 && index < 5) {
                if (!moveItemStackTo(itemstack2, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 5 && index < 41 && !moveItemStackTo(itemstack2, 0, 1, true)) {
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

    /** onContainerClosed: the slots are dropped, marked so pechs do not snatch them back. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        pech.trading = false;
        if (!theWorld.isClientSide) {
            for (int a = 0; a < 5; ++a) {
                ItemStack itemstack = inventory.removeItemNoUpdate(a);
                if (!itemstack.isEmpty()) {
                    ItemEntity ei = player.drop(itemstack, false);
                    if (ei != null) {
                        ei.getPersistentData().putBoolean(AIPechItemEntityGoto.PECH_DROP, true);
                    }
                }
            }
        }
    }
}
