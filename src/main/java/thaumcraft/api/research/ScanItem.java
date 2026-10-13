package thaumcraft.api.research;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Unlocks a research key when a matching item (or a dropped one) is scanned. */
public class ScanItem implements IScanThing {
    String research;
    ItemStack stack;

    public ScanItem(String research, ItemStack stack) {
        this.research = research;
        this.stack = stack;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj == null) return false;
        ItemStack is = null;
        if (obj instanceof ItemStack s) is = s;
        if (obj instanceof ItemEntity e) is = e.getItem();
        // areItemStacksEqualForCrafting: same item; 1.12 metadata wildcards have no 1.21 counterpart.
        return is != null && !is.isEmpty() && is.is(stack.getItem());
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}
