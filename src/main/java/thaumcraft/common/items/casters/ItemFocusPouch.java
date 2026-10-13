package thaumcraft.common.items.casters;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import thaumcraft.common.container.ContainerFocusPouch;

/**
 * The focus pouch: 18 slots for foci. Changing the caster's focus looks in it first and puts the old focus back in it.
 * 1.21: the contents are the vanilla container component. The belt slot (Baubles) waits for Curios support.
 */
public class ItemFocusPouch extends Item {
    public ItemFocusPouch(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        if (!worldIn.isClientSide) {
            playerIn.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ContainerFocusPouch(id, inventory, hand),
                    Component.translatable("item.thaumcraft.focus_pouch")), buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(playerIn.getItemInHand(hand), worldIn.isClientSide);
    }

    public NonNullList<ItemStack> getInventory(ItemStack item) {
        NonNullList<ItemStack> stackList = NonNullList.withSize(18, ItemStack.EMPTY);
        ItemContainerContents c = item.get(DataComponents.CONTAINER);
        if (c != null) {
            c.copyInto(stackList);
        }
        return stackList;
    }

    public void setInventory(ItemStack item, NonNullList<ItemStack> stackList) {
        item.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stackList));
    }
}
