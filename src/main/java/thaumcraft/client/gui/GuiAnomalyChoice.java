package thaumcraft.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.common.anomaly.Anomalies;
import thaumcraft.common.anomaly.AnomalyClientState;
import thaumcraft.common.anomaly.AnomalyType;
import thaumcraft.common.anomaly.PacketAnomalyChoose;
import thaumcraft.common.lib.SoundsTC;

/**
 * Foresight: the next anomalies as cards, drawn and animated like the theorycraft cards of Thaumcraft 6's research
 * table (GuiResearchTable). The sheets are dealt one by one from the middle with a page sound, grow a little under
 * the mouse, and keep a tilt and a flip of their own. The chosen one glides to the middle while the rest fade, and
 * is written down. Positive anomalies are on the gilded sheet. Clicking one sends it to the server. Closing the
 * screen leaves the choice open (sneak and use the thaumometer to reopen it); when time runs out the server picks
 * one at random. Not in Thaumcraft.
 */
public class GuiAnomalyChoice extends Screen {
    private static final ResourceLocation PAPER = Thaumcraft.id("textures/gui/paper.png");
    private static final ResourceLocation PAPER_GILDED = Thaumcraft.id("textures/gui/papergilded.png");
    /** The sheet texture is square; the paper itself fills about 204 x 240 of its 256 x 256. */
    private static final int SHEET = 168;
    private static final int CARD_W = 134;
    private static final int CARD_H = 158;
    private static final int GAP = 8;
    private static final int TEXT_W = 108;
    private static final int INK = 0x2B1D12;
    private static final int INK_SOFT = 0x5A3E1B;

    /** The cards on screen; kept while the chosen one is still moving, after the server has dropped the choice. */
    private final List<String> cards = new ArrayList<>();
    private float[] zoomOut = new float[0];
    private float[] zoomIn = new float[0];
    private float[] hover = new float[0];
    private int chosen = -1;
    private boolean sent = false;
    private long lastFrame = -1L;

    public GuiAnomalyChoice() {
        super(Component.translatable("tc.anomaly.choice.title"));
    }

    private void syncCards() {
        if (chosen >= 0 || cards.equals(AnomalyClientState.choiceIds)) {
            return;
        }
        cards.clear();
        cards.addAll(AnomalyClientState.choiceIds);
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
        long now = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        long left = Math.max(0L, AnomalyClientState.choiceUntil - now);
        int top = slotY() - CARD_H / 2;
        g.drawCenteredString(font, title, width / 2, top - 30, AnomalyHud.VIOLET);
        if (chosen < 0) {
            g.drawCenteredString(font, Component.translatable("tc.anomaly.choice.time", AnomalyHud.time(left)), width / 2, top - 18, AnomalyHud.GOLD);
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
        AnomalyType type = Anomalies.get(id);
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
        g.setColor(1.0f, 1.0f, 1.0f, alpha);
        boolean gilded = type != null && type.polarity == AnomalyType.Polarity.POSITIVE;
        g.blit(gilded ? PAPER_GILDED : PAPER, -SHEET / 2, -SHEET / 2, 0, 0.0f, 0.0f, SHEET, SHEET, SHEET, SHEET);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        pose.popPose();

        if (type != null && alpha >= 1.0f) {
            text(g, type);
        }
        pose.popPose();
    }

    private void text(GuiGraphics g, AnomalyType type) {
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

    private void centered(GuiGraphics g, FormattedCharSequence line, int y, int color) {
        g.drawString(font, line, -font.width(line) / 2, y, color, false);
    }

    /** The polarity colours, darkened to read as ink on the paper. */
    private static int ink(AnomalyType.Polarity p) {
        return switch (p) {
            case POSITIVE -> 0x1F6B12;
            case NEGATIVE -> 0x8E1B12;
            case MIXED -> 0x7A5200;
        };
    }

    private void play(SoundEvent sound, float volume) {
        if (minecraft.player != null) minecraft.player.playSound(sound, volume, 1.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (chosen < 0) {
            for (int i = 0; i < cards.size(); i++) {
                if (zoomOut[i] >= 0.95f && Math.abs(mouseX - slotX(i)) < CARD_W / 2.0 && Math.abs(mouseY - slotY()) < CARD_H / 2.0) {
                    chosen = i;
                    if (!sent) {
                        sent = true;
                        PacketDistributor.sendToServer(new PacketAnomalyChoose(cards.get(i)));
                    }
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
