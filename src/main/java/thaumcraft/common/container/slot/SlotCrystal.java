package thaumcraft.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.resources.ItemCrystalEssence;

/** Port of SlotCrystal: only takes vis crystals of one aspect. */
public class SlotCrystal extends Slot {
    private final Aspect aspect;

    public SlotCrystal(Aspect aspect, Container container, int index, int x, int y) {
        super(container, index, x, y);
        this.aspect = aspect;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidCrystal(stack, aspect);
    }

    public static boolean isValidCrystal(ItemStack stack, Aspect aspect) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemCrystalEssence crystal)) return false;
        AspectList aspects = crystal.getAspects(stack);
        return aspects != null && aspects.size() > 0 && aspects.getAspects()[0] == aspect;
    }
}
