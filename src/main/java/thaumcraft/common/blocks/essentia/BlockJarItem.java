package thaumcraft.common.blocks.essentia;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.essentia.TileAlembic;
import thaumcraft.common.tiles.essentia.TileJarFillable;

/**
 * Port of BlockJarItem: a jar item keeps its essentia and label filter. Clicking an alembic with it drains the alembic.
 * The essentia goes to the placed jar through the tile's implicit components.
 */
public class BlockJarItem extends BlockItem implements IEssentiaContainerItem {
    public BlockJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** 1.0 when empty, like getDurabilityForDisplay; the "fill" model property reads it. */
    public double getDurabilityForDisplay(ItemStack stack) {
        AspectList al = getAspects(stack);
        return al == null ? 1.0 : 1.0 - al.visSize() / (double) TileJarFillable.CAPACITY;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getAspects(stack) != null;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f - (float) getDurabilityForDisplay(stack) * 13.0f);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0f, (float) (1.0 - getDurabilityForDisplay(stack))) / 3.0f, 1.0f, 1.0f);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemstack, UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !world.getBlockState(context.getClickedPos()).is(ConfigBlocks.alembic.get())
                || !(world.getBlockEntity(context.getClickedPos()) instanceof TileAlembic tile) || tile.amount <= 0) {
            return InteractionResult.PASS;
        }
        if (getFilter(itemstack) != null && getFilter(itemstack) != tile.aspect) {
            return InteractionResult.FAIL;
        }
        AspectList held = getAspects(itemstack);
        if (held != null && held.getAspects()[0] != tile.aspect) {
            return InteractionResult.FAIL;
        }
        int amt = tile.amount;
        if (held != null && held.visSize() + amt > TileJarFillable.CAPACITY) {
            amt = Math.abs(held.visSize() - TileJarFillable.CAPACITY);
        }
        if (amt <= 0) {
            return InteractionResult.FAIL;
        }
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Aspect a = tile.aspect;
        if (tile.takeFromContainer(tile.aspect, amt)) {
            int base = held == null ? 0 : held.visSize();
            if (itemstack.getCount() > 1) {
                ItemStack stack = itemstack.copyWithCount(1);
                setAspects(stack, new AspectList().add(a, base + amt));
                itemstack.shrink(1);
                if (!player.getInventory().add(stack)) {
                    world.addFreshEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), stack));
                }
            } else {
                setAspects(itemstack, new AspectList().add(a, base + amt));
            }
            player.playSound(SoundEvents.BOTTLE_FILL, 0.25f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Port addition: the jar's contents, which TC6 only shows through its aspect tooltip.
        AspectList al = getAspects(stack);
        if (al != null) {
            Aspect a = al.getAspects()[0];
            tooltip.add(a.getDisplayName().append(Component.literal(" " + al.getAmount(a) + "/" + TileJarFillable.CAPACITY)
                    .withStyle(ChatFormatting.GRAY)));
        }
        Aspect tag = getFilter(stack);
        if (tag != null) {
            tooltip.add(Component.literal(tag.getName()).withStyle(ChatFormatting.DARK_PURPLE));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Nullable
    @Override
    public AspectList getAspects(ItemStack itemstack) {
        AspectList aspects = itemstack.get(TCDataComponents.ASPECTS);
        return aspects != null && aspects.size() > 0 ? aspects : null;
    }

    @Nullable
    public Aspect getFilter(ItemStack itemstack) {
        return itemstack.get(TCDataComponents.ASPECT_FILTER);
    }

    @Override
    public void setAspects(ItemStack itemstack, AspectList aspects) {
        itemstack.set(TCDataComponents.ASPECTS, aspects.copy());
    }
}
