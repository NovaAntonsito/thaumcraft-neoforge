package thaumcraft.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.SoundsTC;

/**
 * A choice among a few cards, drawn and animated like the theorycraft cards of Thaumcraft 6's research table
 * (GuiResearchTable). The sheets are dealt one by one from the middle with a page sound, grow a little under the
 * mouse, and keep a tilt and a flip of their own. The chosen one glides to the middle while the rest fade, and is
 * written down. Foresight uses it for the next anomalies; addons may use it for their own choices.
 * <p>
 * The cards are ids: {@link #currentCards} is followed until one is chosen, and the screen closes when it empties.
 * Each card's text is drawn by {@link #drawCard} with the origin at the middle of the card.
 */
public abstract class GuiCardChoice extends Screen {
    protected static final ResourceLocation PAPER = Thaumcraft.id("textures/gui/paper.png");
    protected static final ResourceLocation PAPER_GILDED = Thaumcraft.id("textures/gui/papergilded.png");
    /** The sheet texture is square; the paper itself fills about 204 x 240 of its 256 x 256. */
    protected static final int SHEET = 168;
    protected static final int CARD_W = 134;
    protected static final int CARD_H = 158;
    protected static final int GAP = 8;
    protected static final int TEXT_W = 108;
    protected static final int INK = 0x2B1D12;
    protected static final int INK_SOFT = 0x5A3E1B;

    /** The cards on screen; kept while the chosen one is still moving, after the choice may have been dropped. */
    private final List<String> cards = new ArrayList<>();
    private float[] zoomOut = new float[0];
    private float[] zoomIn = new float[0];
    private float[] hover = new float[0];
    private int chosen = -1;
    private long lastFrame = -1L;

    protected GuiCardChoice(Component title) {
        super(title);
    }

    /** The cards to offer now. */
    protected abstract List<String> currentCards();

    /** Whether a card is on the gilded sheet. */
    protected abstract boolean isGilded(String id);

    /** The card's text, with the origin at its middle. Only drawn while the card is fully shown. */
    protected abstract void drawCard(GuiGraphics g, String id);

    /** A card was clicked. Called once. */
    protected abstract void onChosen(String id);

    /** A tint for a card's paper, as RGB. */
    protected int paperColor(String id) {
        return 0xFFFFFF;
    }

    /** A line under the title while nothing is chosen, or null. */
    @Nullable
    protected Component subtitle() {
        return null;
    }

    protected boolean hasChosen() {
        return chosen >= 0;
    }

    private void syncCards() {
        List<String> now = currentCards();
        if (chosen >= 0 || cards.equals(now)) {
            return;
        }
        cards.clear();
        cards.addAll(now);
        zoomOut = new float[cards.size()];
        zoomIn = new float[cards.size()];
        hover = new float[cards.size()];
    }

    private int slotX(int i) {
        int n = cards.size();
        int left = (width - (n * CARD_W + (n - 1) * GAP)) / 2;
        return left + i * (CARD_W + GAP) + CARD_W / 2;
    }

    private int slotY() {
        return height / 2 + 12;
    }

    @Override
    public void tick() {
        syncCards();
        if (cards.isEmpty()) onClose();
    }

    /** Ticks since the last frame. The original stepped by the partial tick each frame. */
    private float frameTicks() {
        long now = System.nanoTime();
        float dt = lastFrame < 0 ? 0.0f : (now - lastFrame) / 50_000_000.0f;
        lastFrame = now;
        return Math.min(dt, 1.0f);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        syncCards();
        float dt = frameTicks();
        int top = slotY() - CARD_H / 2;
        g.drawCenteredString(font, title, width / 2, top - 30, AnomalyHud.VIOLET);
        Component sub = chosen < 0 ? subtitle() : null;
        if (sub != null) {
            g.drawCenteredString(font, sub, width / 2, top - 18, AnomalyHud.GOLD);
        }

        int n = cards.size();
        int deckX = width / 2;
        for (int i = 0; i < n; i++) {
            int sx = slotX(i);
            int sy = slotY();
            if (zoomOut[i] >= 0.95f && chosen < 0) {
                if (Math.abs(mouseX - sx) < CARD_W / 2 && Math.abs(mouseY - sy) < CARD_H / 2) {
                    hover[i] += Math.max((0.25f - hover[i]) / 3.0f * dt, 0.0025f);
                } else {
                    hover[i] -= 0.1f * dt;
                }
            }
            // Dealt from the last card back to the first: each starts once the next is most of the way out.
            if (i == n - 1 || zoomOut[i + 1] > 0.6f) {
                float before = zoomOut[i];
                zoomOut[i] += Math.max((1.0f - zoomOut[i]) / 5.0f * dt, 0.0025f);
                if (zoomOut[i] > 0.0f && before == 0.0f) {
                    play(SoundsTC.PAGE.get(), 1.0f);
                }
            }
            float prevZoomIn = zoomIn[i];
            if (chosen >= 0) {
                zoomIn[i] += i == chosen ? Math.max((1.0f - zoomIn[i]) / 3.0f * dt, 0.0025f) : 0.3f * dt;
                hover[i] = 1.0f - zoomIn[i];
            }
            zoomIn[i] = Mth.clamp(zoomIn[i], 0.0f, 1.0f);
            hover[i] = Mth.clamp(hover[i], 0.0f, 0.25f);
            zoomOut[i] = Mth.clamp(zoomOut[i], 0.0f, 1.0f);

            float fx = deckX + (sx - deckX) * zoomOut[i];
            if (i == chosen) {
                fx += (width / 2.0f - fx) * zoomIn[i];
            }
            float scale = (6.0f + zoomOut[i] * 2.0f - zoomIn[i] * 2.0f + hover[i]) / 8.0f;
            float alpha = i == chosen ? 1.0f : 1.0f - zoomIn[i];
            float tilt = Math.max(1.0f - zoomOut[i], zoomIn[i]);
            sheet(g, cards.get(i), fx, sy, scale, alpha, tilt);

            if (i == chosen && zoomIn[i] >= 1.0f && prevZoomIn < 1.0f) {
                play(SoundsTC.WRITE.get(), 0.3f);
                onClose();
                return;
            }
        }
    }

    /** drawSheet: the paper with its own jitter, tilt and flip, and the card's text on top while fully shown. */
    private void sheet(GuiGraphics g, String id, float x, float y, float scale, float alpha, float tilt) {
        Random r = new Random(id.hashCode());
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x + r.nextGaussian(), y + r.nextGaussian(), 0.0);
        pose.scale(scale, scale, 1.0f);
        pose.mulPose(Axis.ZP.rotationDegrees((float) (r.nextGaussian() * tilt)));

        pose.pushPose();
        if (r.nextBoolean()) {
            pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        }
        if (r.nextBoolean()) {
            pose.scale(-1.0f, 1.0f, 1.0f);
        }
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        int tint = paperColor(id);
        g.setColor((tint >> 16 & 255) / 255.0f, (tint >> 8 & 255) / 255.0f, (tint & 255) / 255.0f, alpha);
        g.blit(isGilded(id) ? PAPER_GILDED : PAPER, -SHEET / 2, -SHEET / 2, 0, 0.0f, 0.0f, SHEET, SHEET, SHEET, SHEET);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        pose.popPose();

        if (alpha >= 1.0f) {
            drawCard(g, id);
        }
        pose.popPose();
    }

    protected void centered(GuiGraphics g, FormattedCharSequence line, int y, int color) {
        g.drawString(font, line, -font.width(line) / 2, y, color, false);
    }

    protected void play(SoundEvent sound, float volume) {
        if (minecraft.player != null) minecraft.player.playSound(sound, volume, 1.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (chosen < 0) {
            for (int i = 0; i < cards.size(); i++) {
                if (zoomOut[i] >= 0.95f && Math.abs(mouseX - slotX(i)) < CARD_W / 2.0 && Math.abs(mouseY - slotY()) < CARD_H / 2.0) {
                    chosen = i;
                    onChosen(cards.get(i));
                    play(SoundsTC.PAGETURN.get(), 1.0f);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
