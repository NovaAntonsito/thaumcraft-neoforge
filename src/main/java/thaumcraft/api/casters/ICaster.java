package thaumcraft.api.casters;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Items that draw vis from the aura to cast, and hold a focus. */
public interface ICaster {
    float getConsumptionModifier(ItemStack is, @Nullable Player player, boolean crafting);

    boolean consumeVis(ItemStack is, Player player, float amount, boolean crafting, boolean simulate);

    @Nullable
    Item getFocus(ItemStack stack);

    /** The mounted focus, or EMPTY (null in 1.12). */
    ItemStack getFocusStack(ItemStack stack);

    void setFocus(ItemStack stack, ItemStack focus);

    ItemStack getPickedBlock(ItemStack stack);
}
