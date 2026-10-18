package thaumcraft.common.container;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.entities.monster.EntityPech;

/** Port of InventoryPech: the offer slot (0) and the four slots the pech's goods appear in. */
public class InventoryPech extends SimpleContainer {
    private final EntityPech theMerchant;

    public InventoryPech(EntityPech merchant) {
        super(5);
        theMerchant = merchant;
    }

    @Override
    public boolean stillValid(Player player) {
        return theMerchant.isTamed();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index == 0;
    }
}
