package thaumcraft.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Port of SlotGhostFull: shows a stack (of any size) that can neither be taken nor replaced. */
public class SlotGhostFull extends Slot {
    int limit = Integer.MAX_VALUE;

    public SlotGhostFull(Container inventory, int index, int x, int y, int limit) {
        super(inventory, index, x, y);
        this.limit = limit;
    }

    public SlotGhostFull(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public int getMaxStackSize() {
        return limit;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
