package thaumcraft.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

/** Port of SlotGhost: a slot that shows a stack but can never be taken from. */
public class SlotGhost extends Slot {
    int limit = Integer.MAX_VALUE;

    public SlotGhost(Container inventory, int index, int x, int y, int limit) {
        super(inventory, index, x, y);
        this.limit = limit;
    }

    public SlotGhost(Container inventory, int index, int x, int y) {
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
}
