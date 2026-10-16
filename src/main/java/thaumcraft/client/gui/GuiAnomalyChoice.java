package thaumcraft.client.gui;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.common.anomaly.Anomalies;
import thaumcraft.common.anomaly.AnomalyClientState;
import thaumcraft.common.anomaly.AnomalyType;
import thaumcraft.common.anomaly.PacketAnomalyChoose;

/**
 * Foresight: the next anomalies as cards, like the research ideas of Thaumcraft 6. Clicking one sends it to the
 * server. Closing the screen leaves the choice open (sneak and use the thaumometer to reopen it); when time runs
 * out the server picks one at random. Not in Thaumcraft.
 */
public class GuiAnomalyChoice extends Screen {
    private static final int CARD_W = 116;
    private static final int CARD_H = 150;
    private static final int GAP = 10;

    public GuiAnomalyChoice() {
        super(Component.translatable("tc.anomaly.choice.title"));
    }

    private List<String> ids() {
        return AnomalyClientState.choiceIds;
    }

    private int left() {
        int n = ids().size();
        return (width - (n * CARD_W + (n - 1) * GAP)) / 2;
    }

    private int top() {
        return (height - CARD_H) / 2 + 8;
    }

    @Override
    public void tick() {
        if (ids().isEmpty()) onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        long now = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        long left = Math.max(0L, AnomalyClientState.choiceUntil - now);
        g.drawCenteredString(font, title, width / 2, top() - 30, AnomalyHud.VIOLET);
        g.drawCenteredString(font, Component.translatable("tc.anomaly.choice.time", AnomalyHud.time(left)), width / 2, top() - 18, AnomalyHud.GOLD);
        List<String> ids = ids();
        for (int i = 0; i < ids.size(); i++) {
            int cx = left() + i * (CARD_W + GAP);
            int cy = top();
            boolean hover = mouseX >= cx && mouseX < cx + CARD_W && mouseY >= cy && mouseY < cy + CARD_H;
            card(g, ids.get(i), cx, hover ? cy - 3 : cy, hover);
        }
    }

    private void card(GuiGraphics g, String id, int x, int y, boolean hover) {
        AnomalyType type = Anomalies.get(id);
        if (hover) g.fill(x - 2, y - 2, x + CARD_W + 2, y + CARD_H + 2, 0xFF000000 | AnomalyHud.GOLD);
        AnomalyHud.panel(g, x, y, CARD_W, CARD_H);
        if (type == null) return;
        int color = AnomalyHud.color(type.polarity);
        int ty = y + 8;
        for (FormattedCharSequence line : font.split(Component.translatable(type.getTranslationKey()), CARD_W - 12)) {
            g.drawCenteredString(font, line, x + CARD_W / 2, ty, color);
            ty += 10;
        }
        Component kind = Component.translatable("tc.anomaly.polarity." + type.polarity.name().toLowerCase(Locale.ROOT));
        g.drawCenteredString(font, kind, x + CARD_W / 2, ty + 1, AnomalyHud.LAVENDER);
        ty += 14;
        g.fill(x + 8, ty, x + CARD_W - 8, ty + 1, AnomalyHud.BORDER);
        ty += 5;
        for (FormattedCharSequence line : font.split(Component.translatable(type.getTranslationKey() + ".desc"), CARD_W - 14)) {
            if (ty > y + CARD_H - 22) break;
            g.drawString(font, line, x + 7, ty, 0xE0D6EC, false);
            ty += 10;
        }
        Component dur = Component.translatable("tc.anomaly.choice.duration", type.getMinDuration() / 1200, type.getMaxDuration() / 1200);
        g.drawCenteredString(font, dur, x + CARD_W / 2, y + CARD_H - 13, AnomalyHud.GOLD);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<String> ids = ids();
        for (int i = 0; i < ids.size(); i++) {
            int cx = left() + i * (CARD_W + GAP);
            if (mouseX >= cx && mouseX < cx + CARD_W && mouseY >= top() && mouseY < top() + CARD_H) {
                PacketDistributor.sendToServer(new PacketAnomalyChoose(ids.get(i)));
                if (minecraft.player != null) minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
