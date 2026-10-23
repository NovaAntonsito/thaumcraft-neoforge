package voidexpeditions.client;

import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.client.gui.GuiCardChoice;
import voidexpeditions.network.ChoosePerkPayload;
import voidexpeditions.perk.Perk;
import voidexpeditions.perk.Perks;

/**
 * At a portal with a sigil: three gifts of the void as Foresight's cards, one to keep for the rest of the run. The
 * gifts that do something are on the gilded sheet, the cursed ones on stained paper. Closing it leaves the choice
 * for the next time the portal is entered.
 */
public class PerkChoiceScreen extends GuiCardChoice {
    private final List<String> offered;
    private final int stage;
    private final int length;

    public PerkChoiceScreen(List<String> offered, int stage, int length) {
        super(Component.translatable("voidexpeditions.perk.choose"));
        this.offered = List.copyOf(offered);
        this.stage = stage;
        this.length = length;
    }

    @Override
    protected List<String> currentCards() {
        return offered;
    }

    @Override
    protected boolean isGilded(String id) {
        Perk p = Perks.get(id);
        return p != null && p.unique();
    }

    @Override
    protected int paperColor(String id) {
        Perk p = Perks.get(id);
        return p != null && p.category() == Perk.Category.CURSED ? 0xE8A8A0 : 0xFFFFFF;
    }

    @Override
    protected Component subtitle() {
        return Component.translatable("voidexpeditions.perk.next", stage + 1, length);
    }

    @Override
    protected void onChosen(String id) {
        PacketDistributor.sendToServer(new ChoosePerkPayload(offered.indexOf(id)));
    }

    @Override
    protected void drawCard(GuiGraphics g, String id) {
        Perk perk = Perks.get(id);
        if (perk == null) return;
        int y = -CARD_H / 2 + 14;
        Component name = Component.translatable("voidexpeditions.perk." + id).withStyle(ChatFormatting.BOLD);
        for (FormattedCharSequence line : font.split(name, TEXT_W)) {
            centered(g, line, y, ink(perk.category()));
            y += 10;
        }
        Component kind = Component.translatable("voidexpeditions.perk.category." + perk.category().name().toLowerCase(Locale.ROOT));
        centered(g, kind.getVisualOrderText(), y + 1, INK_SOFT);
        y += 13;
        g.fill(-TEXT_W / 2 + 6, y, TEXT_W / 2 - 6, y + 1, 0x805A3E1B);
        y += 5;
        for (FormattedCharSequence line : font.split(perk.description(), TEXT_W)) {
            if (y > CARD_H / 2 - 18) break;
            g.drawString(font, line, -TEXT_W / 2, y, INK, false);
            y += 10;
        }
    }

    /** The category colours, darkened to read as ink on the paper. */
    private static int ink(Perk.Category c) {
        return switch (c) {
            case PLAYER -> 0x1F6B12;
            case CASTER -> 0x12506B;
            case AURA -> 0x6B1263;
            case WARP -> 0x3D1270;
            case UTILITY -> 0x7A5200;
            case CURSED -> 0x8E1B12;
        };
    }
}
