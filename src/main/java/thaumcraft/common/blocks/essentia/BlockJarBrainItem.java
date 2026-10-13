package thaumcraft.common.blocks.essentia;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import thaumcraft.common.registry.TCDataComponents;

/** Port of BlockJarBrainItem: shows the stored experience. The xp reaches the placed jar through the tile's components. */
public class BlockJarBrainItem extends BlockItem {
    public BlockJarBrainItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Integer xp = stack.get(TCDataComponents.XP);
        if (xp != null) {
            tooltip.add(Component.literal(xp + " xp").withStyle(ChatFormatting.GREEN));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
