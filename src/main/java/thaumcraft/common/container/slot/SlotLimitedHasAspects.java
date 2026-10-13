package thaumcraft.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

/** Port of SlotLimitedHasAspects: only items that have aspects. */
public class SlotLimitedHasAspects extends Slot {
    public SlotLimitedHasAspects(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack1) {
        AspectList al = ThaumcraftCraftingManager.getObjectTags(stack1);
        return al != null && al.size() > 0;
    }
}
