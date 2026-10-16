package thaumcraft.common.golems.client.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import thaumcraft.api.golems.seals.ISealConfigArea;
import thaumcraft.api.golems.seals.ISealConfigFilter;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.common.container.InventoryFake;
import thaumcraft.common.container.slot.SlotGhost;
import thaumcraft.common.golems.seals.SealEntity;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.registry.TCMenus;

/**
 * Port of SealBaseContainer: the settings of one seal, split in categories (0 priority/color/lock, 1 filter, 2 area,
 * 3 toggles, 4 required traits). The button ids are the ones the original sent with sendEnchantPacket.
 * <p>
 * 1.12 rebuilt the slot list on every category change. Here the filter slots always exist (first in the list)
 * and are only active in the filter category, since a menu's slot list cannot change once it is open.
 */
public class SealBaseContainer extends AbstractContainerMenu {
    private final Level world;
    ISealEntity seal;
    Player player;
    InventoryFake temp;
    int[] categories;
    int category = -1;
    int t = 0;

    /** Opens the seal's settings for the player (ProxyGUI id 18). */
    public static void open(ServerPlayer player, ISealEntity seal) {
        Object c = seal.getSeal().returnContainer(player.level(), player, seal.getSealPos().pos, seal.getSealPos().face, seal);
        if (c instanceof MenuProvider provider) {
            player.openMenu(provider, buf -> {
                buf.writeLong(seal.getSealPos().pos.asLong());
                buf.writeByte(seal.getSealPos().face.ordinal());
            });
        }
    }

    /** What ISeal.returnContainer gives for the seals that use this screen. */
    public static MenuProvider provider(ISealEntity seal) {
        return new SimpleMenuProvider((id, inventory, p) -> new SealBaseContainer(id, inventory, p.level(), seal), Component.empty());
    }

    public SealBaseContainer(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level(), clientSeal(inventory.player.level(), buf));
    }

    /** The client's copy of the seal, or an empty one if it never arrived. */
    private static ISealEntity clientSeal(Level level, FriendlyByteBuf buf) {
        SealPos sp = new SealPos(BlockPos.of(buf.readLong()), Direction.values()[buf.readByte()]);
        ISealEntity se = SealHandler.getSealEntity(level.dimension(), sp);
        return se != null ? se : new SealEntity(level, sp, null);
    }

    public SealBaseContainer(int id, Inventory iinventory, Level par2World, ISealEntity seal) {
        super(TCMenus.SEAL.get(), id);
        world = par2World;
        player = iinventory.player;
        this.seal = seal;
        if (seal.getSeal() instanceof ISealGui gui) {
            categories = gui.getGuiCategories();
        } else {
            categories = new int[] {0};
        }
        setupCategories();
        setupFilterInventory();
        bindPlayerInventory(iinventory);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return seal.getPriority();
            }

            @Override
            public void set(int v) {
                seal.setPriority((byte) v);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return seal.getArea().getX();
            }

            @Override
            public void set(int v) {
                seal.setArea(new BlockPos(v, seal.getArea().getY(), seal.getArea().getZ()));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return seal.getArea().getY();
            }

            @Override
            public void set(int v) {
                seal.setArea(new BlockPos(seal.getArea().getX(), v, seal.getArea().getZ()));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return seal.getArea().getZ();
            }

            @Override
            public void set(int v) {
                seal.setArea(new BlockPos(seal.getArea().getX(), seal.getArea().getY(), v));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return seal.getColor();
            }

            @Override
            public void set(int v) {
                seal.setColor((byte) v);
            }
        });
    }

    public ISealEntity getSeal() {
        return seal;
    }

    void setupCategories() {
        if (category < 0) {
            category = categories[0];
        }
    }

    private void setupFilterInventory() {
        if (seal.getSeal() instanceof ISealConfigFilter cf) {
            int s = cf.getFilterSize();
            int sx = 16 + (s - 1) % 3 * 12;
            int sy = 16 + (s - 1) / 3 * 12;
            int middleX = 88;
            int middleY = 72;
            temp = new InventoryFake(cf.getInv());
            for (int a = 0; a < s; ++a) {
                int x = a % 3;
                int y = a / 3;
                addSlot(new SlotGhost(temp, a, middleX + x * 24 - sx + 8, middleY + y * 24 - sy + 8) {
                    @Override
                    public boolean isActive() {
                        return category == 1;
                    }
                });
                ++t;
            }
        }
    }

    protected void bindPlayerInventory(Inventory inventoryPlayer) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 150 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 208));
        }
    }

    @Override
    public boolean stillValid(Player var1) {
        return true;
    }

    /** enchantItem: the button presses of SealBaseGUI. */
    @Override
    public boolean clickMenuButton(Player player, int par2) {
        if (par2 >= 0 && par2 < categories.length) {
            category = categories[par2];
            setupCategories();
            return true;
        }
        if (category == 3 && seal.getSeal() instanceof ISealConfigToggles cp && par2 >= 30 && par2 < 30 + cp.getToggles().length) {
            cp.setToggle(par2 - 30, true);
            return true;
        }
        if (category == 3 && seal.getSeal() instanceof ISealConfigToggles cp && par2 >= 60 && par2 < 60 + cp.getToggles().length) {
            cp.setToggle(par2 - 60, false);
            return true;
        }
        if (category == 0 && par2 >= 25 && par2 <= 26) {
            seal.setLocked(par2 == 25);
            return true;
        }
        if (par2 >= 27 && par2 <= 28) {
            seal.setRedstoneSensitive(par2 == 27);
            return true;
        }
        if (category == 1 && seal.getSeal() instanceof ISealConfigFilter cp2 && par2 >= 20 && par2 <= 21) {
            cp2.setBlacklist(par2 == 20);
            return true;
        }
        if (par2 == 80 && seal.getPriority() > -5) {
            seal.setPriority((byte) (seal.getPriority() - 1));
            return true;
        }
        if (par2 == 81 && seal.getPriority() < 5) {
            seal.setPriority((byte) (seal.getPriority() + 1));
            return true;
        }
        if (par2 == 82 && seal.getColor() > 0) {
            seal.setColor((byte) (seal.getColor() - 1));
            return true;
        }
        if (par2 == 83 && seal.getColor() < 16) {
            seal.setColor((byte) (seal.getColor() + 1));
            return true;
        }
        if (seal.getSeal() instanceof ISealConfigArea) {
            if (par2 == 90 && seal.getArea().getY() > 1) {
                seal.setArea(seal.getArea().offset(0, -1, 0));
                return true;
            }
            if (par2 == 91 && seal.getArea().getY() < 8) {
                seal.setArea(seal.getArea().offset(0, 1, 0));
                return true;
            }
            if (par2 == 92 && seal.getArea().getX() > 1) {
                seal.setArea(seal.getArea().offset(-1, 0, 0));
                return true;
            }
            if (par2 == 93 && seal.getArea().getX() < 8) {
                seal.setArea(seal.getArea().offset(1, 0, 0));
                return true;
            }
            if (par2 == 94 && seal.getArea().getZ() > 1) {
                seal.setArea(seal.getArea().offset(0, 0, -1));
                return true;
            }
            if (par2 == 95 && seal.getArea().getZ() < 8) {
                seal.setArea(seal.getArea().offset(0, 0, 1));
                return true;
            }
        }
        return false;
    }

    /** detectAndSendChanges: the data slots send priority, area and color; the filter copy goes back into the seal. */
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (seal.getSeal() instanceof ISealConfigFilter cf && temp != null) {
            for (int a = 0; a < temp.getContainerSize(); ++a) {
                cf.setFilterSlot(a, temp.getItem(a));
            }
        }
    }

    /**
     * Not in the original: the changed settings are saved with the chunk and sent to the other players when the
     * screen closes. 1.12 wrote the seals at chunk save time and left other clients stale.
     */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!world.isClientSide && seal.getSeal() != null && seal.getSealPos() != null) {
            broadcastChanges();
            SealHandler.markChunkAsDirty(world, seal.getSealPos().pos);
            seal.syncToClient(world);
        }
    }

    @Override
    public void clicked(int slotId, int clickedButton, ClickType mode, Player playerIn) {
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = slots.get(slotId);
            ItemStack ic = ItemStack.EMPTY;
            if (!getCarried().isEmpty()) {
                ic = getCarried().copy();
            }
            if (slot instanceof SlotGhost && seal.getSeal() instanceof ISealConfigFilter cf) {
                boolean filter = cf.hasStacksizeLimiters();
                // In single player the client and the server share the seal, so only the server applies the click.
                if (playerIn.level().isClientSide && ServerLifecycleHooks.getCurrentServer() != null) {
                    return;
                }
                int idx = slot.index;
                if (clickedButton == 1) {
                    if (!filter) {
                        slot.set(ItemStack.EMPTY);
                        cf.setFilterSlotSize(idx, 0);
                    } else if (ic.isEmpty()) {
                        if (slot.hasItem()) {
                            cf.setFilterSlotSize(idx, cf.getFilterSlotSize(idx) - (mode == ClickType.QUICK_MOVE ? 10 : 1));
                            if (cf.getFilterSlotSize(idx) < 0) {
                                slot.set(ItemStack.EMPTY);
                                cf.setFilterSlotSize(idx, 0);
                            }
                        }
                    } else if (slot.hasItem() && slot.getItem().getCount() == 0) {
                        slot.set(ItemStack.EMPTY);
                        cf.setFilterSlotSize(idx, 0);
                    } else if (slot.hasItem() && ItemStack.isSameItemSameComponents(ic, slot.getItem())) {
                        cf.setFilterSlotSize(idx, cf.getFilterSlotSize(idx) - ic.getCount());
                        if (cf.getFilterSlotSize(idx) < 0) {
                            slot.set(ItemStack.EMPTY);
                            cf.setFilterSlotSize(idx, 0);
                        }
                    }
                } else if (ic.isEmpty()) {
                    if (filter && slot.hasItem()) {
                        cf.setFilterSlotSize(idx, cf.getFilterSlotSize(idx) + (mode == ClickType.QUICK_MOVE ? 10 : 1));
                    }
                } else {
                    if (!filter) {
                        ic.setCount(1);
                        cf.setFilterSlotSize(idx, 0);
                    } else {
                        int os = ic.getCount();
                        ic.setCount(1);
                        if (slot.hasItem() && ItemStack.isSameItemSameComponents(ic, slot.getItem())) {
                            cf.setFilterSlotSize(idx, cf.getFilterSlotSize(idx) + os);
                        } else {
                            cf.setFilterSlotSize(idx, 0);
                        }
                    }
                    slot.set(ic);
                }
                broadcastChanges();
                return;
            }
        }
        super.clicked(slotId, clickedButton, mode, playerIn);
    }

    /** transferStackInSlot of the original never moves anything. */
    @Override
    public ItemStack quickMoveStack(Player player, int par2) {
        return ItemStack.EMPTY;
    }
}
