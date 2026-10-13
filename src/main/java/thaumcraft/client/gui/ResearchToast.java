package thaumcraft.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.Thaumcraft;
import thaumcraft.api.research.ResearchEntry;

/** Port of ResearchToast: "Research complete" with the research icon and name, for five seconds. */
public class ResearchToast implements Toast {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/hud.png");
    final ResearchEntry entry;
    private final String title;

    public ResearchToast(ResearchEntry entry) {
        this(entry, "research.complete");
    }

    /** Reborn's ResearchNoteToast is the same toast titled "researchnote.complete". */
    public ResearchToast(ResearchEntry entry, String title) {
        this.entry = entry;
        this.title = title;
    }

    @Override
    public Visibility render(GuiGraphics g, ToastComponent toastGui, long delta) {
        Font font = toastGui.getMinecraft().font;
        g.blit(TEX, 0, 0, 0, 224, 160, 32);
        GuiResearchBrowser.drawResearchIcon(g, entry, 6, 8, false);
        g.drawString(font, Component.translatable(title), 30, 7, 0xA239F1, false);
        Component s = entry.getLocalizedName();
        float w = font.width(s);
        if (w > 124.0f) {
            w = 124.0f / w;
            g.pose().pushPose();
            g.pose().translate(30.0f, 18.0f, 0.0f);
            g.pose().scale(w, w, w);
            g.drawString(font, s, 0, 0, 0xFFAB09, false);
            g.pose().popPose();
        } else {
            g.drawString(font, s, 30, 18, 0xFFAB09, false);
        }
        return delta < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }
}
