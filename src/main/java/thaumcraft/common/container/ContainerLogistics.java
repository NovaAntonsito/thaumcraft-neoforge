package thaumcraft.common.container;

import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.common.container.slot.SlotGhostFull;
import thaumcraft.common.golems.seals.SealEntity;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.golems.seals.SealProvide;
import thaumcraft.common.registry.TCMenus;

/**
 * Port of ContainerLogistics: everything the player's Provide seals within 32 blocks can hand out, 81 stacks at a
 * time. 1.12 sent the stacks above 64 with PacketItemToClientContainer; the 1.21 slot packets already carry any count.
 */
public class ContainerLogistics extends AbstractContainerMenu {
    private final Level worldObj;
    Player player;
    public InventoryLogistics input;
    TreeMap<String, ItemStack> items;
    int lastTotal;
    public int start;
    public int end;
    public String searchText;
    public boolean updated;
    /** Where the bell was clicked (the delivery target), or null to deliver to the player. Only used by the screen. */
    public final BlockPos target;
    public final Direction side;

    /** Opens the screen (ProxyGUI id 20). pos and side are the clicked block, or null. */
    public static void open(ServerPlayer player, BlockPos pos, Direction side) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ContainerLogistics(id, inv, null, null), Component.empty()), buf -> {
            buf.writeBoolean(pos != null && side != null);
            if (pos != null && side != null) {
                buf.writeLong(pos.asLong());
                buf.writeByte(side.ordinal());
            }
        });
    }

    /** The client side, with the target the server wrote in open(). */
    public static ContainerLogistics fromNetwork(int id, Inventory inventory, FriendlyByteBuf buf) {
        BlockPos pos = null;
        Direction side = null;
        if (buf.readBoolean()) {
            pos = BlockPos.of(buf.readLong());
            side = Direction.values()[buf.readByte()];
        }
        return new ContainerLogistics(id, inventory, pos, side);
    }

    public ContainerLogistics(int id, Inventory iinventory, BlockPos target, Direction side) {
        super(TCMenus.LOGISTICS.get(), id);
        this.target = target;
        this.side = side;
        input = new InventoryLogistics();
        items = new TreeMap<>();
        lastTotal = 0;
        start = 0;
        end = 0;
        searchText = "";
        updated = false;
        worldObj = iinventory.player.level();
        player = iinventory.player;
        for (int a = 0; a < input.getContainerSize(); ++a) {
            addSlot(new SlotGhostFull(input, a, 19 + a % 9 * 19, 19 + a / 9 * 19));
        }
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return start;
            }

            @Override
            public void set(int v) {
                start = v;
                updated = true;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return end;
            }

            @Override
            public void set(int v) {
                end = v;
                updated = true;
            }
        });
        if (!worldObj.isClientSide) {
            refreshItemList(true);
        }
    }

    public void refreshItemList(boolean full) {
        int newTotal = lastTotal;
        TreeMap<String, ItemStack> ti = new TreeMap<>();
        if (full) {
            newTotal = 0;
            CopyOnWriteArrayList<SealEntity> seals = SealHandler.getSealsInRange(worldObj, player.blockPosition(), 32);
            for (SealEntity seal : seals) {
                if (seal.getSeal() instanceof SealProvide provide && player.getUUID().toString().equals(seal.getOwner())) {
                    IItemHandler handler = ThaumcraftInvHelper.getItemHandlerAt(worldObj, seal.getSealPos().pos, seal.getSealPos().face);
                    if (handler == null) {
                        continue;
                    }
                    for (int slot = 0; slot < handler.getSlots(); ++slot) {
                        ItemStack stack = handler.getStackInSlot(slot).copy();
                        if (provide.matchesFilters(stack)) {
                            String name = stack.getHoverName().getString();
                            if (searchText.isEmpty() || name.toLowerCase().contains(searchText.toLowerCase())) {
                                // 1.12: display name + damage + NBT
                                String key = name + stack.getComponentsPatch();
                                if (ti.containsKey(key)) {
                                    stack.grow(ti.get(key).getCount());
                                }
                                ti.put(key, stack);
                                newTotal += stack.getCount();
                            }
                        }
                    }
                }
            }
        }
        // 1.12 only rebuilt the shown stacks when the total or the scroll position changed; rebuilding them every
        // time gives the same slots, and the menu only sends the ones that changed
        lastTotal = newTotal;
        if (full) {
            items = ti;
        }
        input.clearContent();
        int j = 0;
        int q = 0;
        for (String key2 : items.keySet()) {
            if (++j <= start * 9) {
                continue;
            }
            input.setItem(q, items.get(key2));
            if (++q >= input.getContainerSize()) {
                break;
            }
        }
        end = items.size() / 9 - 8;
    }

    /** enchantItem: 22 refresh, 0/1 scroll down/up, 100+ jump to a row. */
    @Override
    public boolean clickMenuButton(Player par1EntityPlayer, int par2) {
        if (par2 == 22) {
            refreshItemList(true);
            return true;
        }
        if (par2 == 0) {
            if (start < items.size() / 9 - 8) {
                ++start;
                refreshItemList(false);
            }
            return true;
        }
        if (par2 == 1) {
            if (start > 0) {
                --start;
                refreshItemList(false);
            }
            return true;
        }
        if (par2 >= 100) {
            int s = par2 - 100;
            if (s >= 0 && s <= items.size() / 9 - 8) {
                start = s;
                refreshItemList(false);
            }
            return true;
        }
        return super.clickMenuButton(par1EntityPlayer, par2);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player var1) {
        return true;
    }
}
