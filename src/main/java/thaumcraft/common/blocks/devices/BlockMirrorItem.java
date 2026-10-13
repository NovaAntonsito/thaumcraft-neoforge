package thaumcraft.common.blocks.devices;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.devices.TileMirror;

/**
 * Port of BlockMirrorItem. Using a mirror item on a placed, unlinked mirror of the same kind takes one item and
 * gives back a mirror linked to it; placing that one links the pair.
 */
public class BlockMirrorItem extends BlockItem {
    public BlockMirrorItem(Block block, Properties properties) {
        super(block, properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !(world.getBlockState(pos).getBlock() instanceof BlockMirror)) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        if (world.getBlockState(pos).getBlock() == getBlock() && world.getBlockEntity(pos) instanceof TileMirror tm) {
            if (!tm.isLinkValid()) {
                ItemStack st = stack.copyWithCount(1);
                st.set(TCDataComponents.MIRROR_LINK.get(), GlobalPos.of(world.dimension(), pos));
                world.playSound(null, pos, SoundsTC.JAR.get(), SoundSource.BLOCKS, 1.0f, 2.0f);
                if (!player.getInventory().add(st)) {
                    player.drop(st, false);
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                player.inventoryMenu.broadcastChanges();
            } else {
                player.sendSystemMessage(Component.translatable("tc.mirror.alreadylinked").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GlobalPos link = stack.get(TCDataComponents.MIRROR_LINK.get());
        if (link != null) {
            BlockPos p = link.pos();
            tooltip.add(Component.translatable("tc.mirror.linkedto", p.getX() + "," + p.getY() + "," + p.getZ(), link.dimension().location().toString()));
        }
    }
}
