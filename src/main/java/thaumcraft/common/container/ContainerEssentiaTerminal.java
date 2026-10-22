package thaumcraft.common.container;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.consumables.ItemPhial;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCMenus;
import thaumcraft.common.tiles.essentia.TileEssentiaTerminal;
import thaumcraft.common.tiles.essentia.TileJarFillable;
import thaumcraft.common.tiles.essentia.TileJarFillableVoid;

/**
 * The essentia terminal: the total of each aspect in the jars on the network, refreshed twice a second. Buttons
 * fill the player's empty phials from it, or empty every filled phial the player carries back into the jars. No
 * slots: the phials come from and go to the player's inventory. Not in Thaumcraft.
 */
public class ContainerEssentiaTerminal extends AbstractContainerMenu {
    /** Button 0 empties the filled phials. 1000 + aspect * 64 + (count - 1) fills count phials of that aspect. */
    public static final int DEPOSIT = 0;
    public static final int FILL = 1000;
    public static final int PHIAL = 10;
    /** Every aspect, in registration order, the same on both sides. */
    public static final List<Aspect> ORDER = List.copyOf(Aspect.aspects.values());

    public final BlockPos pos;
    private final Level level;
    private final Player player;
    /** Totals per aspect in ORDER; each goes over the network as two 15-bit halves. */
    public final int[] amounts = new int[ORDER.size()];
    public final DataSlot jarCount = DataSlot.standalone();
    private int ticks;

    public ContainerEssentiaTerminal(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, buf.readBlockPos());
    }

    public ContainerEssentiaTerminal(int id, Inventory inventory, BlockPos pos) {
        super(TCMenus.ESSENTIA_TERMINAL.get(), id);
        this.pos = pos;
        this.player = inventory.player;
        this.level = inventory.player.level();
        for (int i = 0; i < amounts.length; i++) {
            final int a = i;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return amounts[a] & 0x7FFF;
                }

                @Override
                public void set(int v) {
                    amounts[a] = (amounts[a] & ~0x7FFF) | (v & 0x7FFF);
                }
            });
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return (amounts[a] >> 15) & 0x7FFF;
                }

                @Override
                public void set(int v) {
                    amounts[a] = (amounts[a] & 0x7FFF) | ((v & 0x7FFF) << 15);
                }
            });
        }
        addDataSlot(jarCount);
        if (!level.isClientSide) refresh();
    }

    private List<TileJarFillable> jars() {
        return TileEssentiaTerminal.findJars(level, pos);
    }

    private void refresh() {
        List<TileJarFillable> jars = jars();
        java.util.Arrays.fill(amounts, 0);
        for (TileJarFillable jar : jars) {
            if (jar.aspect != null && jar.amount > 0) {
                amounts[ORDER.indexOf(jar.aspect)] += jar.amount;
            }
        }
        jarCount.set(jars.size());
    }

    @Override
    public void broadcastChanges() {
        if (!level.isClientSide && ++ticks % 10 == 0) refresh();
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        boolean done;
        if (id == DEPOSIT) {
            done = deposit();
        } else if (id >= FILL && (id - FILL) / 64 < ORDER.size()) {
            done = fill(ORDER.get((id - FILL) / 64), (id - FILL) % 64 + 1);
        } else {
            return false;
        }
        if (done) {
            level.playSound(null, pos, SoundsTC.BUBBLE.get(), SoundSource.BLOCKS, 0.2f, 1.0f + level.random.nextFloat() * 0.4f);
            refresh();
        }
        return true;
    }

    private static boolean isEmptyPhial(ItemStack stack) {
        return stack.is(ConfigItems.phial.get()) && !ConfigItems.phial.get().isFilled(stack);
    }

    /** Fills up to count of the player's empty phials, ten essentia each. */
    private boolean fill(Aspect aspect, int count) {
        List<TileJarFillable> jars = jars();
        Inventory inv = player.getInventory();
        int made = 0;
        while (made < count && total(jars, aspect) >= PHIAL) {
            int slot = -1;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (isEmptyPhial(inv.getItem(i))) {
                    slot = i;
                    break;
                }
            }
            if (slot < 0) break;
            int need = PHIAL;
            for (TileJarFillable jar : jars) {
                if (need <= 0) break;
                if (jar.aspect == aspect && jar.amount > 0) {
                    int n = Math.min(jar.amount, need);
                    jar.takeFromContainer(aspect, n);
                    need -= n;
                }
            }
            inv.getItem(slot).shrink(1);
            ItemStack filled = ItemPhial.makeFilledPhial(aspect);
            if (!inv.add(filled)) player.drop(filled, false);
            made++;
        }
        return made > 0;
    }

    private static int total(List<TileJarFillable> jars, Aspect aspect) {
        int t = 0;
        for (TileJarFillable jar : jars) {
            if (jar.aspect == aspect) t += jar.amount;
        }
        return t;
    }

    /** Empties every filled phial the player carries into the jars, as long as all of a phial fits. */
    private boolean deposit() {
        List<TileJarFillable> jars = jars();
        Inventory inv = player.getInventory();
        int emptied = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(ConfigItems.phial.get())) continue;
            AspectList al = ConfigItems.phial.get().getAspects(stack);
            if (al == null || al.size() == 0) continue;
            Aspect aspect = al.getAspects()[0];
            int amount = al.getAmount(aspect);
            while (!stack.isEmpty() && room(jars, aspect) >= amount) {
                put(jars, aspect, amount);
                stack.shrink(1);
                emptied++;
            }
        }
        for (int k = 0; k < emptied; k++) {
            ItemStack empty = new ItemStack(ConfigItems.phial.get());
            if (!inv.add(empty)) player.drop(empty, false);
        }
        return emptied > 0;
    }

    /** Whether a jar takes this aspect: it already holds it, or it is empty and its label (if any) matches. */
    private static boolean takes(TileJarFillable jar, Aspect aspect) {
        return jar.doesContainerAccept(aspect) && (jar.aspect == aspect || jar.amount == 0);
    }

    private static int room(List<TileJarFillable> jars, Aspect aspect) {
        int room = 0;
        for (TileJarFillable jar : jars) {
            if (!takes(jar, aspect)) continue;
            if (jar instanceof TileJarFillableVoid) return Integer.MAX_VALUE;
            room += TileJarFillable.CAPACITY - (jar.aspect == aspect ? jar.amount : 0);
        }
        return room;
    }

    /** Jars that already hold the aspect first, then labelled empty jars, then the rest; void jars last of each. */
    private static void put(List<TileJarFillable> jars, Aspect aspect, int amount) {
        for (int pass = 0; pass < 6 && amount > 0; pass++) {
            for (TileJarFillable jar : jars) {
                if (amount <= 0) break;
                if (!takes(jar, aspect) || (jar instanceof TileJarFillableVoid) != (pass % 2 == 1)) continue;
                int stage = jar.aspect == aspect && jar.amount > 0 ? 0 : jar.aspectFilter != null ? 1 : 2;
                if (stage != pass / 2) continue;
                amount = jar.addToContainer(aspect, amount);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return level.getBlockState(pos).is(ConfigBlocks.essentiaTerminal.get())
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
