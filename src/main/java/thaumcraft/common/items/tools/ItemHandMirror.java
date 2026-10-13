package thaumcraft.common.items.tools;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.container.ContainerHandMirror;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.devices.TileMirror;

/**
 * Port of ItemHandMirror. Used on a magic mirror it links to it; used in the air it opens a slot where anything
 * put in is sent through that mirror.
 */
public class ItemHandMirror extends Item {
    public ItemHandMirror(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(TCDataComponents.MIRROR_LINK.get());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !world.getBlockState(pos).is(ConfigBlocks.mirror.get())) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        if (world.getBlockEntity(pos) instanceof TileMirror) {
            stack.set(TCDataComponents.MIRROR_LINK.get(), GlobalPos.of(world.dimension(), pos));
            world.playSound(null, pos, SoundsTC.JAR.get(), SoundSource.BLOCKS, 1.0f, 2.0f);
            player.sendSystemMessage(Component.translatable("tc.handmirrorlinked"));
            player.inventoryMenu.broadcastChanges();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        GlobalPos link = stack.get(TCDataComponents.MIRROR_LINK.get());
        if (!world.isClientSide && link != null && world instanceof ServerLevel sl) {
            ServerLevel targetWorld = sl.getServer().getLevel(link.dimension());
            if (targetWorld == null) {
                return InteractionResultHolder.pass(stack);
            }
            if (!(targetWorld.getBlockEntity(link.pos()) instanceof TileMirror tm) || tm.getClass() != TileMirror.class) {
                stack.remove(TCDataComponents.MIRROR_LINK.get());
                world.playSound(null, player.blockPosition(), SoundsTC.ZAP.get(), SoundSource.PLAYERS, 1.0f, 0.8f);
                player.sendSystemMessage(Component.translatable("tc.handmirrorerror"));
                return InteractionResultHolder.pass(stack);
            }
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ContainerHandMirror(id, inventory, hand),
                    Component.translatable("container.handmirror")), buf -> buf.writeEnum(hand));
        }
        return link != null ? InteractionResultHolder.sidedSuccess(stack, world.isClientSide) : InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GlobalPos link = stack.get(TCDataComponents.MIRROR_LINK.get());
        if (link != null) {
            BlockPos p = link.pos();
            tooltip.add(Component.translatable("tc.handmirrorlinkedto").append(" " + p.getX() + "," + p.getY() + "," + p.getZ()
                    + " in " + link.dimension().location()));
        }
    }

    /** Sends the items through the linked mirror. False (and the link is dropped) when that mirror is gone. */
    public static boolean transport(ItemStack mirror, ItemStack items, Player player, Level worldObj) {
        GlobalPos link = mirror.get(TCDataComponents.MIRROR_LINK.get());
        if (link == null || !(worldObj instanceof ServerLevel sl)) {
            return false;
        }
        ServerLevel targetWorld = sl.getServer().getLevel(link.dimension());
        if (targetWorld == null) {
            return false;
        }
        if (!(targetWorld.getBlockEntity(link.pos()) instanceof TileMirror tm) || tm.getClass() != TileMirror.class) {
            mirror.remove(TCDataComponents.MIRROR_LINK.get());
            worldObj.playSound(null, player.blockPosition(), SoundsTC.ZAP.get(), SoundSource.PLAYERS, 1.0f, 0.8f);
            player.sendSystemMessage(Component.translatable("tc.handmirrorerror"));
            return false;
        }
        if (tm.transportDirect(items)) {
            worldObj.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.1f, 1.0f);
        }
        return true;
    }
}
