package voidexpeditions.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thaumcraft.client.gui.AnomalyHud;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.perk.Perk;
import voidexpeditions.perk.Perks;
import voidexpeditions.registry.VEAttachments;
import voidexpeditions.registry.VEItems;
import voidexpeditions.run.RunState;

/**
 * The expedition at the top left, on the anomaly meter's panel: the encounter and difficulty, what opens the portal
 * (the sigil, or the boss in the last labyrinth), the extra life and the gifts held. Below the anomaly meter when the
 * two would overlap; hidden behind screens other than the chat.
 */
public final class RunHud {
    public static final ResourceLocation ID = VoidExpeditions.id("run_hud");
    private static final int WIDTH = 150;
    private static final int VIOLET = 0xA239F1;
    private static final int GOLD = 0xFFAB09;
    private static final int TEXT = 0xE0D0F0;
    private static final int DIM = 0x9A8AA8;

    private RunHud() {
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.level == null) return;
        if (mc.screen != null && !(mc.screen instanceof ChatScreen)) return;
        RunState run = mc.player.getData(VEAttachments.RUN);
        if (!run.active) return;
        Font font = mc.font;

        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("voidexpeditions.hud.encounter", run.stage, run.length, run.difficulty.displayName()));
        boolean last = run.stage >= run.length;
        boolean ready = last ? run.bossSlain : mc.player.getInventory().contains(new ItemStack(VEItems.VOID_SIGIL.get()));
        lines.add(Component.translatable(last ? "voidexpeditions.hud.goal.boss" : "voidexpeditions.hud.goal.sigil")
                .withColor(ready ? GOLD : DIM));
        if (run.extraLife) lines.add(Component.translatable("voidexpeditions.hud.extra_life").withColor(GOLD));
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String id : run.perks) counts.merge(id, 1, Integer::sum);
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            Perk p = Perks.get(e.getKey());
            if (p == null) continue;
            MutableComponent line = p.displayName().copy();
            if (e.getValue() > 1) line.append(Component.literal(" ×" + e.getValue()).withColor(DIM));
            lines.add(line);
        }

        int x = 4;
        int y = 2 + AnomalyHud.bottomOver(x, x + WIDTH);
        int h = 8 + lines.size() * 10;
        AnomalyHud.panel(g, x, y, WIDTH, h);
        int ty = y + 5;
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(font, lines.get(i), x + 6, ty, i == 0 ? VIOLET : TEXT, false);
            ty += 10;
        }
    }
}
