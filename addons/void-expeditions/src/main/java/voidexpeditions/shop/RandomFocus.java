package voidexpeditions.shop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusMediumRoot;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.casters.ItemFocus;

/**
 * An advanced focus woven by the void: a random medium with one or two random effects, each with random settings,
 * kept within the focus's complexity. Mediums and effects that need a block picked out (plan, exchange) are left out.
 */
public final class RandomFocus {
    private static final List<String> MEDIUMS = List.of("thaumcraft.BOLT", "thaumcraft.PROJECTILE", "thaumcraft.CLOUD",
            "thaumcraft.MINE", "thaumcraft.TOUCH", "thaumcraft.SPELLBAT");
    private static final List<String> EFFECTS = List.of("thaumcraft.FIRE", "thaumcraft.FROST", "thaumcraft.AIR",
            "thaumcraft.EARTH", "thaumcraft.FLUX", "thaumcraft.BREAK", "thaumcraft.RIFT", "thaumcraft.CURSE",
            "thaumcraft.HEAL");
    private static final int MAX_SETTING_STEPS = 8;

    private RandomFocus() {
    }

    public static ItemStack make(RandomSource random) {
        ItemFocus item = ConfigItems.focus3.get();
        for (int attempt = 0; attempt < 10; attempt++) {
            FocusPackage core = new FocusPackage();
            core.addNode(new FocusMediumRoot());
            if (!addRandom(core, MEDIUMS, random)) continue;
            if (!addRandom(core, EFFECTS, random)) continue;
            if (random.nextBoolean()) addRandom(core, EFFECTS, random);
            int complexity = complexity(core);
            if (complexity > item.getMaxComplexity()) continue;
            core.setComplexity(complexity);
            ItemStack stack = new ItemStack(item);
            ItemFocus.setPackage(stack, core);
            stack.set(DataComponents.CUSTOM_NAME, Component.translatable("voidexpeditions.focus.name"));
            stack.set(DataComponents.RARITY, Rarity.EPIC);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private static boolean addRandom(FocusPackage core, List<String> keys, RandomSource random) {
        IFocusElement e = FocusEngine.getElement(keys.get(random.nextInt(keys.size())));
        if (e == null) return false;
        if (e instanceof FocusNode node) {
            for (String key : node.getSettingList()) {
                NodeSetting setting = node.getSetting(key);
                int steps = random.nextInt(MAX_SETTING_STEPS + 1);
                for (int i = 0; i < steps; i++) setting.increment();
            }
        }
        core.addNode(e);
        return true;
    }

    /** As the focal manipulator counts it: each repeat of an element costs half again as much. */
    private static int complexity(FocusPackage core) {
        Map<String, Integer> count = new HashMap<>();
        int total = 0;
        for (IFocusElement e : core.nodes) {
            if (!(e instanceof FocusNode node)) continue;
            int a = count.merge(node.getKey(), 1, Integer::sum);
            total += (int) (node.getComplexity() * 0.5f * (a + 1));
        }
        return total;
    }
}
