package thaumcraft.common.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.tiles.essentia.TileTube;
import thaumcraft.common.tiles.essentia.TileTubeBuffer;
import thaumcraft.common.tiles.devices.TileCondenser;

/**
 * Port of ItemResonator (essentia resonator). Used on a tube or other essentia device it reports in chat what
 * essentia is inside and how strong a suction it applies. On a tube, the side is the arm being looked at.
 */
public class ItemResonator extends Item {
    public ItemResonator(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockEntity tile = world.getBlockEntity(pos);
        if (player == null || !(tile instanceof IEssentiaTransport et)) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        Direction side = context.getClickedFace();
        if (tile instanceof TileTube tube) {
            int hit = tube.pickPart(player);
            if (hit >= 0 && hit < 6) {
                side = Direction.from3DDataValue(hit);
            }
        }
        if (!(tile instanceof TileTubeBuffer) && et.getEssentiaType(side) != null) {
            player.sendSystemMessage(Component.translatable("tc.resonator1", "" + et.getEssentiaAmount(side), et.getEssentiaType(side).getName()));
        } else if (tile instanceof TileTubeBuffer && ((IAspectContainer) tile).getAspects().size() > 0) {
            for (Aspect aspect : ((IAspectContainer) tile).getAspects().getAspectsSortedByName()) {
                player.sendSystemMessage(Component.translatable("tc.resonator1", "" + ((IAspectContainer) tile).getAspects().getAmount(aspect), aspect.getName()));
            }
        }
        Component s = Component.translatable("tc.resonator3");
        if (et.getSuctionType(side) != null) {
            s = Component.literal(et.getSuctionType(side).getName());
        }
        player.sendSystemMessage(Component.translatable("tc.resonator2", "" + et.getSuctionAmount(side), s));
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.5f, 1.9f + world.random.nextFloat() * 0.1f);
        if (tile instanceof TileCondenser tc) {
            player.sendSystemMessage(Component.translatable("tc.condenser1", "" + tc.cost));
            int s2 = tc.interval / 20;
            player.sendSystemMessage(Component.translatable("tc.condenser2", "" + tc.interval, "" + s2));
        }
        return InteractionResult.SUCCESS;
    }
}
