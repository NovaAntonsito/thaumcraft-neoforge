package thaumcraft.common.items.casters;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusMediumRoot;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.common.registry.TCDataComponents;

/**
 * A focus crystal. The node tree built in the focal manipulator is stored in the FOCUS_PACKAGE component.
 * 1.21: the color and sorting helper are cached in their own components when the package is set, instead of
 * lazily on read (components should not be changed from the render thread).
 */
public class ItemFocus extends Item {
    private static final DecimalFormat FORMAT = new DecimalFormat("#######.##");
    private final int maxComplexity;

    public ItemFocus(int complexity, Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
        maxComplexity = complexity;
    }

    public int getFocusColor(ItemStack focusstack) {
        if (focusstack == null || focusstack.isEmpty()) {
            return 16777215;
        }
        Integer cached = focusstack.get(TCDataComponents.FOCUS_COLOR.get());
        if (cached != null) {
            return cached;
        }
        FocusPackage core = getPackage(focusstack);
        return core == null ? 16777215 : computeColor(core);
    }

    private static int computeColor(FocusPackage core) {
        FocusEffect[] fe = core.getFocusEffects();
        int r = 0;
        int g = 0;
        int b = 0;
        for (FocusEffect ef : fe) {
            Color c = new Color(FocusEngine.getElementColor(ef.getKey()));
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }
        if (fe.length > 0) {
            r /= fe.length;
            g /= fe.length;
            b /= fe.length;
        }
        return new Color(r, g, b).getRGB();
    }

    @Nullable
    public String getSortingHelper(ItemStack focusstack) {
        if (focusstack == null || focusstack.isEmpty() || !focusstack.has(TCDataComponents.FOCUS_PACKAGE.get())) {
            return null;
        }
        Integer sh = focusstack.get(TCDataComponents.FOCUS_SORT.get());
        if (sh == null || sh == 0) {
            FocusPackage p = getPackage(focusstack);
            sh = p == null ? 0 : p.getSortingHelper();
        }
        return focusstack.getHoverName().getString() + sh;
    }

    public static void setPackage(ItemStack focusstack, FocusPackage core) {
        CompoundTag tag = core.serialize();
        focusstack.set(TCDataComponents.FOCUS_PACKAGE.get(), tag);
        focusstack.set(TCDataComponents.FOCUS_COLOR.get(), computeColor(core));
        focusstack.set(TCDataComponents.FOCUS_SORT.get(), core.getSortingHelper());
    }

    @Nullable
    public static FocusPackage getPackage(ItemStack focusstack) {
        if (focusstack == null || focusstack.isEmpty()) {
            return null;
        }
        CompoundTag tag = focusstack.get(TCDataComponents.FOCUS_PACKAGE.get());
        if (tag != null) {
            FocusPackage p = new FocusPackage();
            p.deserialize(tag);
            return p;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        addFocusInformation(stack, tooltip);
    }

    public void addFocusInformation(ItemStack stack, List<Component> tooltip) {
        FocusPackage p = getPackage(stack);
        if (p != null) {
            float al = getVisCost(stack);
            tooltip.add(Component.literal(FORMAT.format(al) + " ").append(Component.translatable("item.Focus.cost1")));
            for (IFocusElement fe : p.nodes) {
                if (fe instanceof FocusNode node && !(fe instanceof FocusMediumRoot)) {
                    buildInfo(tooltip, node, 0);
                }
            }
        }
    }

    private void buildInfo(List<Component> list, FocusNode node, int depth) {
        if (!(node instanceof FocusMediumRoot)) {
            StringBuilder t0 = new StringBuilder();
            for (int a = 0; a < depth; ++a) {
                t0.append("  ");
            }
            var line = Component.literal(t0.toString())
                    .append(Component.translatable(node.getUnlocalizedName()).withStyle(ChatFormatting.DARK_PURPLE));
            if (!node.getSettingList().isEmpty()) {
                StringBuilder s = new StringBuilder(" [");
                boolean q = false;
                for (String st : node.getSettingList()) {
                    NodeSetting ns = node.getSetting(st);
                    s.append(q ? ", " : "").append(ns.getLocalizedName()).append(" ").append(ns.getValueText());
                    q = true;
                }
                s.append("]");
                line.append(Component.literal(s.toString()).withStyle(ChatFormatting.DARK_AQUA));
            }
            list.add(line);
            if (node instanceof FocusModSplit split) {
                for (FocusPackage p : split.getSplitPackages()) {
                    for (IFocusElement fe : p.nodes) {
                        if (fe instanceof FocusNode fn && !(fe instanceof FocusMediumRoot)) {
                            buildInfo(list, fn, depth + 1);
                        }
                    }
                }
            }
        }
    }

    public float getVisCost(ItemStack focusstack) {
        FocusPackage p = getPackage(focusstack);
        return (p == null) ? 0.0f : (p.getComplexity() / 5.0f);
    }

    public int getActivationTime(ItemStack focusstack) {
        FocusPackage p = getPackage(focusstack);
        return (p == null) ? 0 : Math.max(5, p.getComplexity() / 5 * (p.getComplexity() / 4));
    }

    public int getMaxComplexity() {
        return maxComplexity;
    }
}
