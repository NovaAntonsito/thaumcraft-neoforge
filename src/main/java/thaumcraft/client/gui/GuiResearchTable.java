package thaumcraft.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.container.ContainerResearchTable;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.misc.PacketAspectCombinationToServer;
import thaumcraft.common.lib.network.misc.PacketAspectPlaceToServer;
import thaumcraft.common.lib.network.misc.PacketCopyPlayerNoteToServer;
import thaumcraft.common.lib.research.PointExchange;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.research.ResearchNoteManager;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/**
 * The TC4 research table screen, from TC4 Research Port: Reborn. On the left, the player's aspects (five per column,
 * paged) and the two slots to combine them; on the right, the note's hex grid on a parchment.
 * Drag an aspect onto an empty cell to write it, click a written one to erase it. The note is solved when every
 * aspect on its rim is linked through a chain of related aspects.
 */
public class GuiResearchTable extends AbstractContainerScreen<ContainerResearchTable> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/guiresearchtable2.png");
    private static final ResourceLocation PARCHMENT = Thaumcraft.id("textures/misc/parchment3.png");
    private static final ResourceLocation HEX1 = Thaumcraft.id("textures/gui/hex1.png");
    private static final ResourceLocation HEX2 = Thaumcraft.id("textures/gui/hex2.png");
    private static final ResourceLocation SCRIPT = Thaumcraft.id("textures/misc/script.png");
    private static final ResourceLocation BACK = Thaumcraft.id("textures/aspects/_back.png");
    private static final ResourceLocation UNKNOWN = Thaumcraft.id("textures/aspects/_unknown.png");
    private static final int HEX_SIZE = 9;

    private final boolean researchMastery;
    private final boolean researchDupe;
    private final TileResearchTable tileEntity;
    private final Player player;
    private long butcount2;
    private int page;
    private int lastPage;
    private boolean dragging;
    @Nullable
    private Aspect draggedAspect;
    @Nullable
    public Aspect select1;
    @Nullable
    public Aspect select2;
    private final Map<String, Rune> runes = new HashMap<>();
    private long lastRuneCheck;
    private final Map<String, HexUtils.Hex[]> lines = new HashMap<>();
    private final List<String> checked = new ArrayList<>();
    private final List<String> highlight = new ArrayList<>();
    /** Dream branch: the point exchange panel is open over the sheet. */
    private boolean exchangeOpen;
    private final boolean pointExchange;
    private static final ResourceLocation PAPER = Thaumcraft.id("textures/gui/paper.png");
    private static final ResourceLocation WARP = Thaumcraft.id("textures/research/r_warp.png");

    public GuiResearchTable(ContainerResearchTable menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        tileEntity = menu.tileEntity;
        player = inventory.player;
        imageWidth = 256;
        imageHeight = 256;
        researchMastery = ThaumcraftCapabilities.getKnowledge(player).isResearchComplete("RESEARCHMASTERY");
        researchDupe = ThaumcraftCapabilities.getKnowledge(player).isResearchComplete("RESEARCHDUPE");
        pointExchange = ThaumcraftCapabilities.getKnowledge(player).isResearchComplete(PointExchange.RESEARCH);
    }

    private PlayerAspects aspects() {
        return ThaumcraftCapabilities.getAspects(player);
    }

    @Nullable
    private ResearchNoteData note() {
        return tileEntity.note;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTicks) {
        super.render(g, mx, my, partialTicks);
        int gx = leftPos;
        int gy = topPos;
        ResearchNoteData note = note();
        if (note != null && researchDupe && note.isComplete() && inside(mx, my, gx + 37, gy + 5, 24, 24)) {
            // The copy cost: one more of each aspect per copy already made.
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            UtilsFX.blit(g, TEX, gx + 100, gy + 21, 184, 224, 48, 16);
            AspectList al = note.aspects.copy();
            for (Aspect aspect : al.getAspects()) al.add(aspect, note.copies);
            int count = 0;
            for (Aspect aspect : al.getAspects()) {
                UtilsFX.drawTag(g, gx + 100 + 48 + count * 16, gy + 21, aspect, al.getAmount(aspect), 0);
                ++count;
            }
            g.drawString(font, I18n.get("tc.research.copy"), gx + 100, gy + 12, 0xFFFFFF, true);
            g.pose().popPose();
        }
        if (dragging && draggedAspect != null) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            drawOrb(g, mx - 8, my - 8, draggedAspect.getColor());
            g.pose().popPose();
        }
        if (pointExchange) drawExchange(g, gx, gy, mx, my);
        drawAspectText(g, gx, gy, mx, my);
        ItemStack tools = tileEntity.getItem(0);
        if (note != null && (tools.isEmpty() || tools.getDamageValue() >= tools.getMaxDamage())) {
            String l0 = I18n.get("tile.researchtable.noink.0");
            String l1 = I18n.get("tile.researchtable.noink.1");
            int sx = Math.max(font.width(l0), font.width(l1)) / 2;
            UtilsFX.drawCustomTooltip(g, font, List.of(l0, l1), gx + imageWidth / 2 - sx - 10, gy + 84, 11);
        }
        renderTooltip(g, mx, my);
    }

    /** The original drew no titles. */
    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mx, int my) {
        RenderSystem.enableBlend();
        UtilsFX.blit(g, TEX, leftPos, topPos, 0, 0, 255, 167);
        UtilsFX.blit(g, TEX, leftPos + 40, topPos + 167, 0, 166, 184, 90);
        if (page < lastPage) UtilsFX.blit(g, TEX, leftPos + 51, topPos + 121, 208, 208, 24, 8);
        if (page > 0) UtilsFX.blit(g, TEX, leftPos + 27, topPos + 121, 184, 208, 24, 8);
        if (select1 != null && select2 != null) {
            UtilsFX.blit(g, TEX, leftPos + 35, topPos + 139, 184, 184, 32, 16);
            if (butcount2 < System.nanoTime()) {
                drawOrb(g, leftPos + 43, topPos + 139);
            } else {
                UtilsFX.blit(g, TEX, leftPos + 35, topPos + 139, 184, 168, 32, 16);
            }
        }
        ResearchNoteData note = note();
        if (researchDupe && note != null && note.isComplete()) {
            UtilsFX.blit(g, TEX, leftPos + 37, topPos + 5, 232, 200, 24, 24);
        }
        drawAspects(g, leftPos + 10, topPos + 40);
        drawSheet(g, leftPos, topPos, mx, my);
        RenderSystem.defaultBlendFunc();
        g.setColor(1, 1, 1, 1);
    }

    /**
     * Dream branch: the point exchange. A button right of the inventory opens a panel over the sheet with the four
     * offers, their cost and the spare points; clicking one buys it.
     */
    private void drawExchange(GuiGraphics g, int gx, int gy, int mx, int my) {
        int bx = gx + 228;
        int by = gy + 172;
        boolean overButton = inside(mx, my, bx, by, 22, 22);
        g.fill(bx, by, bx + 22, by + 22, exchangeOpen || overButton ? 0xFF6A4A2A : 0xFF3A2A1A);
        g.fill(bx + 1, by + 1, bx + 21, by + 21, 0xFF20160E);
        g.renderItem(new ItemStack(thaumcraft.common.config.ConfigItems.cartographicNote.get()), bx + 3, by + 3);
        List<Component> tooltip = null;
        if (overButton) tooltip = List.of(Component.translatable("tc.exchange.title"));
        if (exchangeOpen) {
            int px = gx + 94;
            int py = gy + 8;
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            RenderSystem.enableBlend();
            g.blit(PAPER, px, py, 150, 150, 0, 0, 256, 256, 256, 256);
            g.drawCenteredString(font, Component.translatable("tc.exchange.title"), px + 75, py + 10, 0xFF40206A);
            int spare = PointExchange.surplus(aspects());
            Component sp = Component.translatable("tc.exchange.spare", spare);
            g.drawString(font, sp, px + 75 - font.width(sp) / 2, py + 22, 0xFF6A4A80, false);
            PointExchange.Offer[] offers = PointExchange.Offer.values();
            for (int i = 0; i < offers.length; ++i) {
                PointExchange.Offer o = offers[i];
                int ry = py + 36 + i * 27;
                boolean over = inside(mx, my, px + 10, ry, 130, 24);
                if (over) g.fill(px + 10, ry, px + 140, ry + 24, 0x30603080);
                switch (o) {
                    case INK -> g.renderItem(new ItemStack(thaumcraft.common.config.ConfigItems.scribingTools.get()), px + 14, ry + 4);
                    case CALM -> UtilsFX.quad(g, WARP, px + 14, ry + 4, px + 30, ry + 20, 0, 1, 0, 1);
                    case RARE -> UtilsFX.quad(g, UNKNOWN, px + 14, ry + 4, px + 30, ry + 20, 0, 1, 0, 1);
                    case CARTOGRAPHY -> g.renderItem(new ItemStack(thaumcraft.common.config.ConfigItems.cartographicNote.get()), px + 14, ry + 4);
                }
                g.drawString(font, Component.translatable(o.key()), px + 36, ry + 4, 0xFF302010, false);
                g.drawString(font, Component.translatable("tc.exchange.cost", o.cost), px + 36, ry + 14,
                        spare >= o.cost ? 0xFF3A6A2A : 0xFF8A2A2A, false);
                if (over) {
                    tooltip = new ArrayList<>();
                    tooltip.add(Component.translatable(o.key()));
                    tooltip.add(Component.translatable(o.key() + ".desc").withStyle(net.minecraft.ChatFormatting.GRAY));
                    if (o.daily) tooltip.add(Component.translatable("tc.exchange.daily").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
                }
            }
            g.pose().popPose();
        }
        if (tooltip != null) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            g.renderComponentTooltip(font, tooltip, mx, my);
            g.pose().popPose();
        }
    }

    private void drawAspects(GuiGraphics g, int x, int y) {
        AspectList pool = aspects().aspectsPool();
        int count = pool.size();
        lastPage = Math.max(0, (count - 20) / 5);
        count = 0;
        int drawn = 0;
        for (Aspect aspect : pool.getAspects()) {
            ++count;
            if (count - 1 >= page * 5 && drawn < 25) {
                boolean faded = pool.getAmount(aspect) <= 0 && tileEntity.bonusAspects.getAmount(aspect) <= 0;
                int xx = drawn / 5 * 16;
                int yy = drawn % 5 * 16;
                UtilsFX.drawTag(g, x + xx, y + yy, aspect, pool.getAmount(aspect), tileEntity.bonusAspects.getAmount(aspect), faded ? 0.33f : 1.0f, false);
                ++drawn;
            }
        }
        if (select1 != null && aspects().aspectCount(select1) <= 0 && tileEntity.bonusAspects.getAmount(select1) <= 0) select1 = null;
        if (select2 != null && aspects().aspectCount(select2) <= 0 && tileEntity.bonusAspects.getAmount(select2) <= 0) select2 = null;
        if (select1 != null) UtilsFX.drawTag(g, x + 3, y + 99, select1, 0, 0);
        if (select2 != null) UtilsFX.drawTag(g, x + 61, y + 99, select2, 0, 0);
    }

    /** Aspect tooltips over the list, the combination slots and the note's cells. */
    private void drawAspectText(GuiGraphics g, int gx, int gy, int mx, int my) {
        if (dragging) return;
        int x = gx + 10;
        int y = gy + 40;
        AspectList pool = aspects().aspectsPool();
        int count = 0;
        int drawn = 0;
        for (Aspect aspect : pool.getAspects()) {
            ++count;
            if (count - 1 >= page * 5 && drawn < 25) {
                int xx = drawn / 5 * 16;
                int yy = drawn % 5 * 16;
                if (inside(mx, my, x + xx, y + yy, 16, 16)) {
                    drawAspectTooltip(g, aspect, mx, my);
                    return;
                }
                ++drawn;
            }
        }
        if (select1 != null && inside(mx, my, x + 3, y + 99, 16, 16)) {
            drawAspectTooltip(g, select1, mx, my);
            return;
        }
        if (select2 != null && inside(mx, my, x + 61, y + 99, 16, 16)) {
            drawAspectTooltip(g, select2, mx, my);
            return;
        }
        ResearchNoteData note = note();
        if (note != null && !exchangeOpen && !note.key.isEmpty()) {
            HexUtils.Hex hp = new HexUtils.Pixel(mx - (gx + 169), my - (gy + 83)).toHex(HEX_SIZE);
            ResearchNoteData.HexEntry e = note.hexEntries.get(hp.toString());
            if (e != null && e.aspect() != null && aspects().isKnowAspect(e.aspect())) drawAspectTooltip(g, e.aspect(), mx, my);
        }
    }

    /**
     * Name and description, and for a compound its two components under the cursor. The original showed the
     * components only with Research Expertise; here they always show.
     */
    private void drawAspectTooltip(GuiGraphics g, Aspect aspect, int mx, int my) {
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        UtilsFX.drawCustomTooltip(g, font, List.of(aspect.getDisplayName().getString(), aspect.getLocalizedDescription().getString()), mx, my - 8, 11);
        if (!aspect.isPrimal()) {
            g.pose().translate(0, 0, 100);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            UtilsFX.quad(g, BACK, mx + 6, my + 6, mx + 6 + 20, my + 6 + 20, 0, 1, 0, 1);
            UtilsFX.quad(g, BACK, mx + 24, my + 6, mx + 24 + 20, my + 6 + 20, 0, 1, 0, 1);
            UtilsFX.drawTag(g, mx + 26, my + 8, aspect.getComponents()[1], 0, 0);
            UtilsFX.drawTag(g, mx + 8, my + 8, aspect.getComponents()[0], 0, 0);
        }
        g.pose().popPose();
    }

    /** The parchment: drifting runes, the links found so far, the grid, and the aspects on it. */
    private void drawSheet(GuiGraphics g, int x, int y, int mx, int my) {
        tileEntity.gatherResults();
        ResearchNoteData note = note();
        if (note == null || note.key.isEmpty()) {
            runes.clear();
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        UtilsFX.blit(g, PARCHMENT, x + 94, y + 8, 0, 0, 150, 150);
        long time = System.currentTimeMillis();
        if (lastRuneCheck < time) {
            lastRuneCheck = time + 250L;
            int k = player.getRandom().nextInt(120) - 60;
            int l = player.getRandom().nextInt(120) - 60;
            HexUtils.Hex hp = new HexUtils.Pixel(k, l).toHex(HEX_SIZE);
            if (!runes.containsKey(hp.toString()) && !note.hexes.containsKey(hp.toString())) {
                runes.put(hp.toString(), new Rune(hp.q, hp.r, time, lastRuneCheck + 15000L + player.getRandom().nextInt(10000), player.getRandom().nextInt(16)));
            }
        }
        for (Rune rune : runes.values().toArray(new Rune[0])) {
            if (rune.decay < time) {
                runes.remove(rune.q + ":" + rune.r);
            } else {
                HexUtils.Pixel pix = new HexUtils.Hex(rune.q, rune.r).toPixel(HEX_SIZE);
                float progress = (float) (time - rune.start) / (float) (rune.decay - rune.start);
                float alpha = 0.5f;
                if (progress < 0.25f) {
                    alpha = progress * 2.0f;
                } else if (progress > 0.5f) {
                    alpha = 1.0f - progress;
                }
                drawRune(g, x + 169 + pix.x, y + 83 + pix.y, rune.rune, alpha * 0.66f);
            }
        }

        HexUtils.Hex hp = new HexUtils.Pixel(mx - (x + 169), my - (y + 83)).toHex(HEX_SIZE);
        lines.clear();
        checked.clear();
        highlight.clear();
        for (HexUtils.Hex hex : note.hexes.values()) {
            ResearchNoteData.HexEntry e = note.hexEntries.get(hex.toString());
            if (e.type() == 1 && aspects().isKnowAspect(e.aspect())) checkConnections(note, hex);
        }
        for (HexUtils.Hex[] con : lines.values()) {
            HexUtils.Pixel p1 = con[0].toPixel(HEX_SIZE);
            HexUtils.Pixel p2 = con[1].toPixel(HEX_SIZE);
            drawLine(g, x + 169 + p1.x, y + 83 + p1.y, x + 169 + p2.x, y + 83 + p2.y);
        }
        if (!note.isComplete()) {
            for (HexUtils.Hex hex : note.hexes.values()) {
                HexUtils.Pixel pix = hex.toPixel(HEX_SIZE);
                if (note.hexEntries.get(hex.toString()).type() != 1) {
                    if (hex.equals(hp)) drawHexHighlight(g, x + 169 + pix.x, y + 83 + pix.y);
                    drawHex(g, x + 169 + pix.x, y + 83 + pix.y);
                } else {
                    drawOrb(g, x + 161 + pix.x, y + 75 + pix.y);
                }
            }
        }
        for (HexUtils.Hex hex : note.hexes.values()) {
            ResearchNoteData.HexEntry e = note.hexEntries.get(hex.toString());
            HexUtils.Pixel pix = hex.toPixel(HEX_SIZE);
            g.pose().pushPose();
            g.pose().translate(x + 161 + pix.x, y + 75 + pix.y, 0);
            if (e.aspect() != null && !aspects().isKnowAspect(e.aspect())) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                g.setColor(0, 0, 0, 0.5f);
                UtilsFX.drawTexturedQuadFull(g, UNKNOWN, 0, 0);
                g.setColor(1, 1, 1, 1);
            } else if (e.type() != 1 && !highlight.contains(hex.toString())) {
                if (e.type() == 2) UtilsFX.drawTag(g, 0, 0, e.aspect(), 0, 0, 0.66f, true);
            } else {
                UtilsFX.drawTag(g, 0, 0, e.aspect(), 0, 0, 1.0f, false);
            }
            g.pose().popPose();
        }
    }

    private void checkConnections(ResearchNoteData note, HexUtils.Hex hex) {
        checked.add(hex.toString());
        for (int a = 0; a < 6; ++a) {
            HexUtils.Hex target = hex.getNeighbour(a);
            ResearchNoteData.HexEntry te = note.hexEntries.get(target.toString());
            if (!checked.contains(target.toString()) && te != null && te.type() >= 1
                    && ResearchNoteManager.canConnect(aspects(), note.hexEntries.get(hex.toString()).aspect(), te.aspect())) {
                String k1 = hex + ":" + target;
                String k2 = target + ":" + hex;
                if (!lines.containsKey(k1) && !lines.containsKey(k2)) {
                    lines.put(k1, new HexUtils.Hex[]{hex, target});
                    highlight.add(target.toString());
                }
                checkConnections(note, target);
            }
        }
    }

    private void drawHex(GuiGraphics g, double x, double y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(1, 1, 1, 0.25f);
        UtilsFX.quad(g, HEX1, (float) x - 8, (float) y - 8, (float) x + 8, (float) y + 8, 0, 1, 0, 1);
        g.setColor(1, 1, 1, 1);
    }

    private void drawHexHighlight(GuiGraphics g, double x, double y) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        UtilsFX.quad(g, HEX2, (float) x - 8, (float) y - 8, (float) x + 8, (float) y + 8, 0, 1, 0, 1);
        RenderSystem.defaultBlendFunc();
    }

    /** A glowing link between two cells (a 3px GL line in the original), pulsing with the player's ticks. */
    private void drawLine(GuiGraphics g, double x, double y, double x2, double y2) {
        float alpha = 0.3f + Mth.sin((float) (player.tickCount + x)) * 0.3f + 0.3f;
        double dx = x2 - x;
        double dy = y2 - y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001) return;
        float nx = (float) (-dy / len * 0.75);
        float ny = (float) (dx / len * 0.75);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f m = g.pose().last().pose();
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        b.addVertex(m, (float) x + nx, (float) y + ny, 0).setColor(0.0f, 0.6f, 0.8f, alpha);
        b.addVertex(m, (float) x2 + nx, (float) y2 + ny, 0).setColor(0.0f, 0.6f, 0.8f, alpha);
        b.addVertex(m, (float) x2 - nx, (float) y2 - ny, 0).setColor(0.0f, 0.6f, 0.8f, alpha);
        b.addVertex(m, (float) x - nx, (float) y - ny, 0).setColor(0.0f, 0.6f, 0.8f, alpha);
        BufferUploader.drawWithShader(b.buildOrThrow());
        RenderSystem.defaultBlendFunc();
    }

    /** One of the 16 glyphs of script.png, turned a quarter as the original, 10px wide. */
    private void drawRune(GuiGraphics g, double x, double y, int rune, float alpha) {
        float u8 = 0.0625f * rune;
        float u9 = u8 + 0.0625f;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().mulPose(com.mojang.math.Axis.ZN.rotationDegrees(90.0f));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, SCRIPT);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Matrix4f m = g.pose().last().pose();
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        b.addVertex(m, -5, 5, 0).setUv(u9, 1).setColor(0, 0, 0, alpha);
        b.addVertex(m, 5, 5, 0).setUv(u9, 0).setColor(0, 0, 0, alpha);
        b.addVertex(m, 5, -5, 0).setUv(u8, 0).setColor(0, 0, 0, alpha);
        b.addVertex(m, -5, -5, 0).setUv(u8, 1).setColor(0, 0, 0, alpha);
        BufferUploader.drawWithShader(b.buildOrThrow());
        g.pose().popPose();
    }

    /** The source-aspect orb, slowly shifting color. */
    private void drawOrb(GuiGraphics g, double x, double y) {
        int count = player.tickCount;
        float red = 0.7f + Mth.sin((float) ((count + x) / 10.0)) * 0.15f + 0.15f;
        float green = 0.7f + Mth.sin((float) ((count + x + y) / 11.0)) * 0.15f + 0.15f;
        float blue = 0.7f + Mth.sin((float) ((count + y) / 12.0)) * 0.15f + 0.15f;
        drawOrb(g, x, y, ((int) (Mth.clamp(red, 0, 1) * 255) << 16) | ((int) (Mth.clamp(green, 0, 1) * 255) << 8) | (int) (Mth.clamp(blue, 0, 1) * 255));
    }

    /** A 16px orb from the particle sheet (eight animation frames). */
    private void drawOrb(GuiGraphics g, double x, double y, int color) {
        int part = player.tickCount % 8;
        float u1 = (0.5f + part / 8.0f) / 4.0f;
        float u2 = u1 + 0.0624375f / 4.0f;
        float v1 = 0.5f / 4.0f;
        float v2 = v1 + 0.0624375f / 4.0f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1.0f);
        UtilsFX.quad(g, UtilsFX.PARTICLE_TEXTURE, (float) x, (float) y, (float) x + 16, (float) y + 16, u1, u2, v1, v2);
        g.setColor(1, 1, 1, 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (button != 0) return handled;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int gx = leftPos;
        int gy = topPos;
        if (butcount2 > System.nanoTime()) return handled;
        if (pointExchange && inside(mx, my, gx + 228, gy + 172, 22, 22)) {
            exchangeOpen = !exchangeOpen;
            playButtonClick();
            return true;
        }
        if (exchangeOpen && inside(mx, my, gx + 94, gy + 8, 150, 150)) {
            for (int i = 0; i < PointExchange.Offer.values().length; ++i) {
                if (inside(mx, my, gx + 104, gy + 44 + i * 27, 130, 24)) {
                    playButtonClick();
                    PacketDistributor.sendToServer(new thaumcraft.common.lib.network.misc.PacketPointExchangeToServer(tileEntity.getBlockPos(), i));
                    return true;
                }
            }
            return true;
        }
        if (select1 != null && select2 != null && inside(mx, my, gx + 35, gy + 139, 32, 16)) {
            butcount2 = System.nanoTime() + 200000000L;
            playButtonClick();
            playButtonCombine();
            PacketDistributor.sendToServer(new PacketAspectCombinationToServer(tileEntity.getBlockPos(), select1.getTag(), select2.getTag()));
            return true;
        }
        if (page > 0 && inside(mx, my, gx + 27, gy + 121, 24, 8)) {
            --page;
            playButtonScroll();
            return true;
        }
        if (page < lastPage && inside(mx, my, gx + 51, gy + 121, 24, 8)) {
            ++page;
            playButtonScroll();
            return true;
        }
        if (select1 != null && inside(mx, my, gx + 11, gy + 137, 16, 16)) {
            select1 = null;
            playButtonAspect();
            return true;
        }
        if (select2 != null && inside(mx, my, gx + 71, gy + 137, 16, 16)) {
            select2 = null;
            playButtonAspect();
            return true;
        }
        ResearchNoteData note = note();
        if (note != null) {
            checkClickedHex(note, mx, my, gx, gy);
            if (researchDupe && note.isComplete() && inside(mx, my, gx + 37, gy + 5, 24, 24)) {
                PacketDistributor.sendToServer(new PacketCopyPlayerNoteToServer(tileEntity.getBlockPos()));
                playButtonClick();
                return true;
            }
        }
        if (Screen.hasShiftDown() && researchMastery) {
            // Shift-click a compound: combine its two components straight away.
            Aspect aspect = getClickedAspect(mx, my, gx, gy, true);
            if (aspect != null && !aspect.isPrimal()) {
                Aspect c0 = aspect.getComponents()[0];
                Aspect c1 = aspect.getComponents()[1];
                if ((aspects().aspectCount(c0) > 0 || tileEntity.bonusAspects.getAmount(c0) > 0)
                        && (aspects().aspectCount(c1) > 0 || tileEntity.bonusAspects.getAmount(c1) > 0)) {
                    playButtonCombine();
                    PacketDistributor.sendToServer(new PacketAspectCombinationToServer(tileEntity.getBlockPos(), c0.getTag(), c1.getTag()));
                }
                return true;
            }
        }
        // Start dragging an aspect out of the list.
        if (inside(mx, my, gx + 10, gy + 40, 80, 80)) {
            Aspect aspect = getClickedAspect(mx, my, gx, gy, false);
            if (aspect != null) {
                playButtonAspect();
                dragging = true;
                draggedAspect = aspect;
                return true;
            }
        }
        return handled;
    }

    /** Dropping a dragged aspect: on an empty cell it is written; on a combine slot, or back on itself, it is selected. */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging && draggedAspect != null) {
            int mx = (int) mouseX;
            int my = (int) mouseY;
            int gx = leftPos;
            int gy = topPos;
            ResearchNoteData note = exchangeOpen ? null : note();
            boolean done = false;
            if (note != null) {
                HexUtils.Hex hp = new HexUtils.Pixel(mx - (gx + 169), my - (gy + 83)).toHex(HEX_SIZE);
                ResearchNoteData.HexEntry e = note.hexEntries.get(hp.toString());
                if (e != null && e.type() == 0) {
                    playButtonCombine();
                    playButtonWrite();
                    PacketDistributor.sendToServer(new PacketAspectPlaceToServer(tileEntity.getBlockPos(), hp.q, hp.r, draggedAspect.getTag()));
                    done = true;
                }
            }
            if (!done) {
                int dx = mx - (gx + 20);
                int dy = my - (gy + 146);
                if (dx >= -16 && dy >= -16 && dx < 16 && dy < 16) {
                    playButtonAspect();
                    select1 = draggedAspect;
                } else {
                    dx = mx - (gx + 79);
                    if (dx >= -16 && dy >= -16 && dx < 16 && dy < 16) {
                        playButtonAspect();
                        select2 = draggedAspect;
                    } else if (getClickedAspect(mx, my, gx, gy, false) == draggedAspect) {
                        if (select1 == null) {
                            select1 = draggedAspect;
                        } else if (select2 == null) {
                            select2 = draggedAspect;
                        }
                    }
                }
            }
            dragging = false;
            draggedAspect = null;
            return true;
        }
        dragging = false;
        draggedAspect = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void checkClickedHex(ResearchNoteData note, int mx, int my, int gx, int gy) {
        HexUtils.Hex hp = new HexUtils.Pixel(mx - (gx + 169), my - (gy + 83)).toHex(HEX_SIZE);
        ResearchNoteData.HexEntry e = note.hexEntries.get(hp.toString());
        if (note.hexes.containsKey(hp.toString()) && e != null && e.type() == 2) {
            playButtonCombine();
            playButtonErase();
            PacketDistributor.sendToServer(new PacketAspectPlaceToServer(tileEntity.getBlockPos(), hp.q, hp.r, ""));
        }
    }

    @Nullable
    private Aspect getClickedAspect(int mx, int my, int gx, int gy, boolean ignoreZero) {
        AspectList pool = aspects().aspectsPool();
        int count = 0;
        int drawn = 0;
        for (Aspect aspect : pool.getAspects()) {
            ++count;
            if (count - 1 >= page * 5 && drawn < 25) {
                int xx = drawn / 5 * 16;
                int yy = drawn % 5 * 16;
                if ((ignoreZero || pool.getAmount(aspect) > 0 || tileEntity.bonusAspects.getAmount(aspect) > 0)
                        && inside(mx, my, gx + xx + 10, gy + yy + 40, 16, 16)) {
                    return aspect;
                }
                ++drawn;
            }
        }
        return null;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private void playSound(SoundEvent sound, float volume, float pitch) {
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), sound, net.minecraft.sounds.SoundSource.MASTER, volume, pitch, false);
    }

    private void playButtonClick() {
        playSound(SoundsTC.CLACK.get(), 0.4f, 1.0f);
    }

    private void playButtonAspect() {
        playSound(SoundsTC.HHOFF.get(), 0.2f, 1.0f + player.getRandom().nextFloat() * 0.1f);
    }

    private void playButtonCombine() {
        playSound(SoundsTC.HHON.get(), 0.3f, 1.0f);
    }

    private void playButtonWrite() {
        playSound(SoundsTC.WRITE.get(), 0.2f, 1.0f);
    }

    private void playButtonErase() {
        playSound(SoundsTC.ERASE.get(), 0.2f, 1.0f + player.getRandom().nextFloat() * 0.1f);
    }

    private void playButtonScroll() {
        playSound(SoundsTC.KEY.get(), 0.3f, 1.0f);
    }

    private record Rune(int q, int r, long start, long decay, int rune) {
    }
}
