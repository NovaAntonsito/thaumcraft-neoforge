package thaumcraft.client.gui;

import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.common.anomaly.Anomalies;
import thaumcraft.common.anomaly.AnomalyClientState;
import thaumcraft.common.anomaly.AnomalyType;
import thaumcraft.common.anomaly.PacketAnomalyChoose;

/**
 * Foresight: the next anomalies as cards (see {@link GuiCardChoice}). Positive anomalies are on the gilded sheet.
 * Clicking one sends it to the server. Closing the screen leaves the choice open (sneak and use the thaumometer to
 * reopen it); when time runs out the server picks one at random. Not in Thaumcraft.
 */
public class GuiAnomalyChoice extends GuiCardChoice {

    public GuiAnomalyChoice() {
        super(Component.translatable("tc.anomaly.choice.title"));
    }

    @Override
    protected List<String> currentCards() {
        return AnomalyClientState.choiceIds;
    }

    @Override
    protected boolean isGilded(String id) {
        AnomalyType type = Anomalies.get(id);
        return type != null && type.polarity == AnomalyType.Polarity.POSITIVE;
    }

    @Override
    protected Component subtitle() {
        long now = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        long left = Math.max(0L, AnomalyClientState.choiceUntil - now);
        return Component.translatable("tc.anomaly.choice.time", AnomalyHud.time(left));
    }

    @Override
    protected void onChosen(String id) {
        PacketDistributor.sendToServer(new PacketAnomalyChoose(id));
    }

    @Override
    protected void drawCard(GuiGraphics g, String id) {
        AnomalyType type = Anomalies.get(id);
        if (type == null) return;
        int y = -CARD_H / 2 + 14;
        Component name = Component.translatable(type.getTranslationKey()).withStyle(ChatFormatting.BOLD);
        for (FormattedCharSequence line : font.split(name, TEXT_W)) {
            centered(g, line, y, ink(type.polarity));
            y += 10;
        }
        Component kind = Component.translatable("tc.anomaly.polarity." + type.polarity.name().toLowerCase(Locale.ROOT));
        centered(g, kind.getVisualOrderText(), y + 1, INK_SOFT);
        y += 13;
        g.fill(-TEXT_W / 2 + 6, y, TEXT_W / 2 - 6, y + 1, 0x805A3E1B);
        y += 5;
        for (FormattedCharSequence line : font.split(Component.translatable(type.getTranslationKey() + ".desc"), TEXT_W)) {
            if (y > CARD_H / 2 - 30) break;
            g.drawString(font, line, -TEXT_W / 2, y, INK, false);
            y += 10;
        }
        Component dur = Component.translatable("tc.anomaly.choice.duration", type.getMinDuration() / 1200, type.getMaxDuration() / 1200);
        centered(g, dur.getVisualOrderText(), CARD_H / 2 - 22, INK_SOFT);
    }

    /** The polarity colours, darkened to read as ink on the paper. */
    private static int ink(AnomalyType.Polarity p) {
        return switch (p) {
            case POSITIVE -> 0x1F6B12;
            case NEGATIVE -> 0x8E1B12;
            case MIXED -> 0x7A5200;
        };
    }
}
