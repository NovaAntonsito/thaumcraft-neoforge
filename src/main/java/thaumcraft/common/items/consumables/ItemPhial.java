package thaumcraft.common.items.consumables;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.essentia.TileAlembic;
import thaumcraft.common.tiles.essentia.TileJarFillable;

/**
 * Port of ItemPhial: carries 10 essentia of one aspect between alembics and jars.
 * 1.12 used metadata 0 for empty and 1 for filled; here a phial is filled when it has the aspects component.
 */
public class ItemPhial extends Item implements IEssentiaContainerItem {
    protected final int base = 10;

    public ItemPhial(Properties properties) {
        super(properties);
    }

    public static ItemStack makePhial(Aspect aspect, int amt) {
        ItemStack i = new ItemStack(ConfigItems.phial.get());
        ConfigItems.phial.get().setAspects(i, new AspectList().add(aspect, amt));
        return i;
    }

    public static ItemStack makeFilledPhial(Aspect aspect) {
        return makePhial(aspect, 10);
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

    public boolean isFilled(ItemStack stack) {
        return getAspects(stack) != null;
    }

    /** item.phial.empty / item.phial.filled of the original. */
    @Override
    public String getDescriptionId(ItemStack stack) {
        return isFilled(stack) ? getDescriptionId() + ".filled" : getDescriptionId();
    }

    /** "Phial of %s Essentia". */
    @Override
    public Component getName(ItemStack stack) {
        AspectList al = getAspects(stack);
        if (al != null) {
            return Component.translatable(getDescriptionId(stack), al.getAspects()[0].getName());
        }
        return super.getName(stack);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    /** Fills from an alembic or a jar, or empties into a jar. */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockState bi = world.getBlockState(pos);
        if (!isFilled(stack) && bi.is(ConfigBlocks.alembic.get()) && world.getBlockEntity(pos) instanceof TileAlembic tile && tile.amount >= base) {
            if (world.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            Aspect a = tile.aspect;
            if (tile.takeFromContainer(a, base)) {
                stack.shrink(1);
                giveOrDrop(player, makePhial(a, base), player.getX(), player.getY(), player.getZ());
                player.playSound(SoundEvents.BOTTLE_FILL, 0.25f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }
        if (!isFilled(stack) && isJar(bi) && world.getBlockEntity(pos) instanceof TileJarFillable tile2 && tile2.amount >= base) {
            if (world.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            Aspect asp = tile2.aspect;
            if (tile2.takeFromContainer(asp, base)) {
                stack.shrink(1);
                giveOrDrop(player, makePhial(asp, base), pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
                player.playSound(SoundEvents.BOTTLE_FILL, 0.25f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }
        AspectList al = getAspects(stack);
        if (al != null && al.size() == 1) {
            Aspect aspect = al.getAspects()[0];
            if (isJar(bi) && world.getBlockEntity(pos) instanceof TileJarFillable tile3
                    && tile3.amount <= TileJarFillable.CAPACITY - base && tile3.doesContainerAccept(aspect)
                    && (tile3.amount == 0 || tile3.aspect == aspect)) {
                if (world.isClientSide) {
                    return InteractionResult.SUCCESS;
                }
                if (tile3.addToContainer(aspect, base) == 0) {
                    tile3.syncTile(true);
                    tile3.setChanged();
                    stack.shrink(1);
                    giveOrDrop(player, new ItemStack(this), pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
                    player.playSound(SoundEvents.BOTTLE_FILL, 0.25f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean isJar(BlockState state) {
        return state.is(ConfigBlocks.jarNormal.get()) || state.is(ConfigBlocks.jarVoid.get());
    }

    private static void giveOrDrop(Player player, ItemStack stack, double x, double y, double z) {
        if (!player.getInventory().add(stack)) {
            player.level().addFreshEntity(new ItemEntity(player.level(), x, y, z, stack));
        }
    }
}
