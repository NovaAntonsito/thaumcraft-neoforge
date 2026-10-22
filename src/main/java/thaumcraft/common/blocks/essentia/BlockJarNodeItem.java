package thaumcraft.common.blocks.essentia;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeData;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.registry.TCDataComponents;

/** Port of the Thaumcraft 4 ItemJarNode: a node jar as an item, with its node's kind and aspects in the tooltip. */
public class BlockJarNodeItem extends BlockItem implements thaumcraft.common.items.ISubItems {
    public BlockJarNodeItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NodeData data = stack.get(TCDataComponents.NODE_DATA.get());
        if (data != null) {
            MutableComponent desc = Component.translatable("nodetype." + data.nodeType().name() + ".name");
            if (data.nodeModifier() != null) {
                desc = desc.append(", ").append(Component.translatable("nodemod." + data.nodeModifier().name() + ".name"));
            }
            tooltip.add(desc.withStyle(ChatFormatting.BLUE));
            for (Aspect aspect : data.aspects().getAspectsSortedByAmount()) {
                tooltip.add(Component.literal(data.aspects().getAmount(aspect) + " ").append(aspect.getDisplayName())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /** A plain node of the six primals for the creative tab. */
    @Override
    public void fillSubItems(Consumer<ItemStack> output) {
        ItemStack stack = new ItemStack(this);
        AspectList al = new AspectList();
        for (Aspect a : Aspect.getPrimalAspects()) al.add(a, 20);
        stack.set(TCDataComponents.NODE_DATA.get(), new NodeData(NodeType.NORMAL.ordinal(), -1, al, al.copy(), ""));
        output.accept(stack);
    }
}
