package thaumcraft.common.blocks.basic;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.misc.TileBanner;

/**
 * Port of BlockBannerTCItem: placed like a vanilla banner. On top of a block it turns to face the player in 16
 * steps, on a side it hangs on the wall. The tooltip names the aspect it carries.
 */
public class BlockBannerTCItem extends BlockItem {
    public BlockBannerTCItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = stack.get(TCDataComponents.ASPECT_FILTER.get());
        if (aspect != null) {
            tooltip.add(Component.literal(aspect.getName()));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        Direction side = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (side == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        if (!world.getBlockState(pos).isSolid()) {
            return InteractionResult.FAIL;
        }
        pos = pos.relative(side);
        if (player != null && !player.mayUseItemAt(pos, side, stack)) {
            return InteractionResult.FAIL;
        }
        if (!world.getBlockState(pos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        world.setBlock(pos, getBlock().defaultBlockState(), 3);
        if (world.getBlockEntity(pos) instanceof TileBanner tile) {
            if (side == Direction.UP) {
                float yaw = player == null ? 0.0f : player.getYRot();
                int i = Mth.floor((yaw + 180.0f) * 16.0f / 360.0f + 0.5) & 0xF;
                tile.setBannerFacing((byte) i);
            } else {
                tile.setWall(true);
                int i = 0;
                if (side == Direction.NORTH) {
                    i = 8;
                }
                if (side == Direction.WEST) {
                    i = 4;
                }
                if (side == Direction.EAST) {
                    i = 12;
                }
                tile.setBannerFacing((byte) i);
            }
            Aspect aspect = stack.get(TCDataComponents.ASPECT_FILTER.get());
            if (aspect != null) {
                tile.setAspect(aspect);
            }
            tile.setChanged();
            tile.syncTile(false);
        }
        stack.consume(1, player);
        return InteractionResult.CONSUME;
    }
}
