package thaumcraft.common.items.consumables;

import javax.annotation.Nullable;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.blocks.ILabelable;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Port of ItemLabel (jar label). Blank, or marked with an aspect by crafting it with a phial.
 * 1.12 metadata 0/1 (blank/filled) is now whether the aspects component is set.
 */
public class ItemLabel extends Item implements IEssentiaContainerItem {
    public ItemLabel(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public AspectList getAspects(ItemStack stack) {
        AspectList aspects = stack.get(TCDataComponents.ASPECTS);
        return aspects != null && aspects.size() > 0 ? aspects : null;
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        stack.set(TCDataComponents.ASPECTS, aspects.copy());
    }

    /** item.label.blank / item.label.filled of the original. */
    @Override
    public String getDescriptionId(ItemStack stack) {
        return getAspects(stack) != null ? getDescriptionId() + ".filled" : getDescriptionId();
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ILabelable labelable = null;
        if (world.getBlockState(context.getClickedPos()).getBlock() instanceof ILabelable l) {
            labelable = l;
        } else if (world.getBlockEntity(context.getClickedPos()) instanceof ILabelable l) {
            labelable = l;
        }
        if (labelable == null) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide && labelable.applyLabel(player, context.getClickedPos(), context.getClickedFace(), stack)) {
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    /** Labels report their own aspects, not the one they are marked with. */
    @Override
    public boolean ignoreContainedAspects() {
        return true;
    }
}
