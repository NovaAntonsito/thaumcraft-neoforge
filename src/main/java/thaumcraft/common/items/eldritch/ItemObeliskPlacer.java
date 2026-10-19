package thaumcraft.common.items.eldritch;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.common.config.ConfigBlocks;

/**
 * ItemEldritchObject meta 4 of TC4, creative only: used on the top of a block it builds an altar keystone with its
 * floating obelisk, if the six blocks above are free.
 */
public class ItemObeliskPlacer extends Item {
    public ItemObeliskPlacer(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (int a = 1; a <= 6; ++a) {
            if (!world.isEmptyBlock(pos.above(a))) {
                return InteractionResult.FAIL;
            }
        }
        if (!world.isClientSide) {
            placeObelisk(world, pos.above());
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    /** The keystone at altar, the obelisk two blocks above it and four body blocks over that. */
    public static void placeObelisk(Level world, BlockPos altar) {
        world.setBlock(altar, ConfigBlocks.eldritchAltar.get().defaultBlockState(), 3);
        world.setBlock(altar.above(2), ConfigBlocks.eldritchObelisk.get().defaultBlockState(), 3);
        for (int a = 3; a <= 6; ++a) {
            world.setBlock(altar.above(a), ConfigBlocks.eldritchObeliskBody.get().defaultBlockState(), 3);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.obelisk_placer.text").withStyle(ChatFormatting.ITALIC));
    }
}
