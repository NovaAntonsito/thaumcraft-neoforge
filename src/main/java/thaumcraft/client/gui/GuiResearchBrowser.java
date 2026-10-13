package thaumcraft.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigResearch;
import thaumcraft.common.lib.network.playerdata.PacketSyncProgressToServer;
import thaumcraft.common.lib.network.playerdata.PacketSyncResearchFlagsToServer;
import thaumcraft.common.lib.research.ResearchManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Port of GuiResearchBrowser: the thaumonomicon's research map, with the category tabs and the search.
 * The original's GuiButtons are drawn and clicked by hand.
 */
public class GuiResearchBrowser extends Screen {
    private static int guiBoundsLeft;
    private static int guiBoundsTop;
    private static int guiBoundsRight;
    private static int guiBoundsBottom;
    protected float screenZoom = 1.0f;
    protected double curMouseX;
    protected double curMouseY;
    protected double guiMapX;
    protected double guiMapY;
    protected double tempMapX;
    protected double tempMapY;
    private boolean isMouseButtonDown;
    public static double lastX = -9999.0;
    public static double lastY = -9999.0;
    private int screenX;
    private int screenY;
    private final int startX = 16;
    private final int startY = 16;
    private final LinkedList<ResearchEntry> research = new LinkedList<>();
    static String selectedCategory = null;
    private ResearchEntry currentHighlight = null;
    private final Player player;
    long popuptime = 0L;
    String popupmessage = "";
    private EditBox searchField;
    private static boolean searching = false;
    private final ArrayList<String> categoriesTC = new ArrayList<>();
    private final ArrayList<String> categoriesOther = new ArrayList<>();
    static int catScrollPos = 0;
    static int catScrollMax = 0;
    public int addonShift = 0;
    private final ArrayList<String> invisible = new ArrayList<>();
    final ArrayList<SearchResult> searchResults = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    static final ResourceLocation tx1 = Thaumcraft.id("textures/gui/gui_research_browser.png");

    public GuiResearchBrowser() {
        this(lastX, lastY);
    }

    public GuiResearchBrowser(double x, double y) {
        super(Component.empty());
        tempMapX = guiMapX = curMouseX = x;
        tempMapY = guiMapY = curMouseY = y;
        player = Minecraft.getInstance().player;
    }

    /** initGui + setWorldAndResolution: also called on every resize. */
    @Override
    protected void init() {
        updateResearch();
        if (lastX == -9999.0 || guiMapX > guiBoundsRight || guiMapX < guiBoundsLeft) {
            tempMapX = guiMapX = (guiBoundsLeft + guiBoundsRight) / 2;
        }
        if (lastY == -9999.0 || guiMapY > guiBoundsBottom || guiMapY < guiBoundsTop) {
            tempMapY = guiMapY = (guiBoundsBottom + guiBoundsTop) / 2;
        }
    }

    public void updateResearch() {
        buttons.clear();
        buttons.add(new Button(Button.SEARCH, 1, height - 17, 16, 16, I18n.get("tc.search"), null, null, false, 0));
        String oldText = searchField != null ? searchField.getValue() : "";
        searchField = new EditBox(font, 20, 20, 89, font.lineHeight, Component.empty());
        searchField.setMaxLength(15);
        searchField.setBordered(true);
        searchField.setVisible(false);
        searchField.setTextColor(0xFFFFFF);
        if (searching) {
            searchField.setVisible(true);
            searchField.setCanLoseFocus(false);
            searchField.setFocused(true);
            searchField.setValue(oldText);
            updateSearch();
        }
        screenX = width - 32;
        screenY = height - 32;
        research.clear();
        if (selectedCategory == null) {
            selectedCategory = ResearchCategories.researchCategories.keySet().iterator().next();
        }
        int limit = (int) Math.floor((screenY - 28) / 24.0f);
        addonShift = 0;
        int count = 0;
        categoriesTC.clear();
        categoriesOther.clear();
        outer:
        for (String rcl : ResearchCategories.researchCategories.keySet()) {
            int rt = 0;
            int rco = 0;
            for (ResearchEntry res : ResearchCategories.getResearchCategory(rcl).research.values()) {
                if (res.hasMeta(ResearchEntry.EnumResearchMeta.AUTOUNLOCK)) continue;
                ++rt;
                if (ThaumcraftCapabilities.knowsResearch(player, res.getKey())) ++rco;
            }
            int v = (int) (rco / (float) rt * 100.0f);
            ResearchCategory rc = ResearchCategories.getResearchCategory(rcl);
            if (rc.researchKey != null && !ThaumcraftCapabilities.knowsResearchStrict(player, rc.researchKey)) continue;
            for (String tcc : ConfigResearch.TCCategories) {
                if (tcc.equals(rcl)) {
                    categoriesTC.add(rcl);
                    buttons.add(new Button(Button.CATEGORY, 1, 10 + categoriesTC.size() * 24, 16, 16,
                            I18n.get("tc.research_category." + rcl), rc, rcl, false, v));
                    continue outer;
                }
            }
            if (++count > limit + catScrollPos) continue;
            if (count - 1 < catScrollPos) continue;
            categoriesOther.add(rcl);
            buttons.add(new Button(Button.CATEGORY, width - 17, 10 + categoriesOther.size() * 24, 16, 16,
                    I18n.get("tc.research_category." + rcl), rc, rcl, true, v));
        }
        if (count > limit || count < catScrollPos) {
            addonShift = (screenY - 28) % 24 / 2;
            buttons.add(new Button(Button.SCROLL_UP, width - 14, 20, 10, 11, "", null, null, false, 0));
            buttons.add(new Button(Button.SCROLL_DOWN, width - 14, screenY + 1, 10, 11, "", null, null, true, 0));
        }
        catScrollMax = count - limit;
        if (selectedCategory == null || selectedCategory.isEmpty()) return;
        research.addAll(ResearchCategories.getResearchCategory(selectedCategory).research.values());
        guiBoundsLeft = 99999;
        guiBoundsTop = 99999;
        guiBoundsRight = -99999;
        guiBoundsBottom = -99999;
        for (ResearchEntry res : research) {
            if (res != null && isVisible(res)) {
                if (res.getDisplayColumn() * 24 - screenX + 48 < guiBoundsLeft) guiBoundsLeft = res.getDisplayColumn() * 24 - screenX + 48;
                if (res.getDisplayColumn() * 24 - 24 > guiBoundsRight) guiBoundsRight = res.getDisplayColumn() * 24 - 24;
                if (res.getDisplayRow() * 24 - screenY + 48 < guiBoundsTop) guiBoundsTop = res.getDisplayRow() * 24 - screenY + 48;
                if (res.getDisplayRow() * 24 - 24 > guiBoundsBottom) guiBoundsBottom = res.getDisplayRow() * 24 - 24;
            }
        }
    }

    private boolean isVisible(ResearchEntry res) {
        if (ThaumcraftCapabilities.knowsResearch(player, res.getKey())) return true;
        if (invisible.contains(res.getKey()) || (res.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN) && !canUnlockResearch(res))) {
            return false;
        }
        if (res.getParents() == null && res.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN)) return false;
        if (res.getParents() != null) {
            for (String r : res.getParents()) {
                ResearchEntry ri = ResearchCategories.getResearch(r);
                if (ri != null && !isVisible(ri)) {
                    invisible.add(r);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canUnlockResearch(ResearchEntry res) {
        return ResearchManager.doesPlayerHaveRequisites(player, res.getKey());
    }

    @Override
    public void removed() {
        lastX = guiMapX;
        lastY = guiMapY;
        super.removed();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searching) {
            if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
                updateSearch();
                return true;
            }
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) return true;
        } else if (minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searching && searchField.charTyped(codePoint, modifiers)) {
            updateSearch();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void updateSearch() {
        searchResults.clear();
        invisible.clear();
        String s1 = searchField.getValue().toLowerCase();
        for (String cat : categoriesTC) {
            if (cat.toLowerCase().contains(s1)) {
                searchResults.add(new SearchResult(I18n.get("tc.research_category." + cat), cat, null, true));
            }
        }
        for (String cat : categoriesOther) {
            if (cat.toLowerCase().contains(s1)) {
                searchResults.add(new SearchResult(I18n.get("tc.research_category." + cat), cat, null, true));
            }
        }
        ArrayList<ResourceLocation> dupCheck = new ArrayList<>();
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        for (String pre : knowledge.getResearchList()) {
            ResearchEntry ri = ResearchCategories.getResearch(pre);
            if (ri == null) continue;
            String name = ri.getLocalizedName().getString();
            if (name.toLowerCase().contains(s1)) {
                searchResults.add(new SearchResult(name, pre, null, false));
            }
            int stage = knowledge.getResearchStage(pre);
            if (ri.getStages() == null) continue;
            int s2 = Math.min(ri.getStages().length - 1, stage + 1);
            ResearchStage page = ri.getStages()[s2];
            if (page == null || page.getRecipes() == null) continue;
            for (ResourceLocation rec : page.getRecipes()) {
                if (dupCheck.contains(rec)) continue;
                dupCheck.add(rec);
                ItemStack ro = GuiResearchPage.recipeOutput(GuiResearchPage.findRecipe(rec));
                if (!ro.isEmpty()) {
                    String display = ro.getHoverName().getString();
                    if (display.toLowerCase().contains(s1)) {
                        searchResults.add(new SearchResult(display, pre, rec, false));
                    }
                }
            }
        }
        searchResults.sort(Comparator.comparing((SearchResult r) -> r.name).thenComparing(SearchResult::compareTo));
    }

    @Override
    public void tick() {
        curMouseX = guiMapX;
        curMouseY = guiMapY;
        double var1 = tempMapX - guiMapX;
        double var2 = tempMapY - guiMapY;
        if (var1 * var1 + var2 * var2 < 4.0) {
            guiMapX += var1;
            guiMapY += var2;
        } else {
            guiMapX += var1 * 0.85;
            guiMapY += var2 * 0.85;
        }
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        if (!searching && button == 0 && isMouseButtonDown) {
            guiMapX -= dragX * screenZoom;
            guiMapY -= dragY * screenZoom;
            curMouseX = tempMapX = guiMapX;
            curMouseY = tempMapY = guiMapY;
            if (tempMapX < guiBoundsLeft * (double) screenZoom) tempMapX = guiBoundsLeft * (double) screenZoom;
            if (tempMapY < guiBoundsTop * (double) screenZoom) tempMapY = guiBoundsTop * (double) screenZoom;
            if (tempMapX >= guiBoundsRight * (double) screenZoom) tempMapX = guiBoundsRight * screenZoom - 1.0f;
            if (tempMapY >= guiBoundsBottom * (double) screenZoom) tempMapY = guiBoundsBottom * screenZoom - 1.0f;
            return true;
        }
        return super.mouseDragged(mx, my, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) isMouseButtonDown = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (!searching) {
            if (scrollY < 0) {
                screenZoom += 0.25f;
            } else if (scrollY > 0) {
                screenZoom -= 0.25f;
            }
            screenZoom = Mth.clamp(screenZoom, 1.0f, 2.0f);
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderTransparentBackground(g);
        int locX = Mth.floor(curMouseX + (guiMapX - curMouseX) * pt);
        int locY = Mth.floor(curMouseY + (guiMapY - curMouseY) * pt);
        if (locX < guiBoundsLeft * screenZoom) locX = (int) (guiBoundsLeft * screenZoom);
        if (locY < guiBoundsTop * screenZoom) locY = (int) (guiBoundsTop * screenZoom);
        if (locX >= guiBoundsRight * screenZoom) locX = (int) (guiBoundsRight * screenZoom - 1.0f);
        if (locY >= guiBoundsBottom * screenZoom) locY = (int) (guiBoundsBottom * screenZoom - 1.0f);
        currentHighlight = null;
        if (!searching) {
            g.pose().pushPose();
            g.pose().scale(1.0f / screenZoom, 1.0f / screenZoom, 1.0f);
            genResearchBackgroundZoomable(g, mx, my, locX, locY);
            g.pose().popPose();
        } else {
            searchField.render(g, mx, my, pt);
            int q = 0;
            for (SearchResult sr : searchResults) {
                int color = sr.cat ? 0xDDAAAA : (sr.recipe == null ? 0xDDDDDD : 0xAAAADD);
                if (sr.recipe != null) {
                    g.pose().pushPose();
                    g.pose().scale(0.5f, 0.5f, 0.5f);
                    UtilsFX.blit(g, tx1, 44, (32 + q * 10) * 2, 224, 48, 16, 16);
                    g.pose().popPose();
                }
                if (mx > 22 && mx < 18 + screenX && my >= 32 + q * 10 && my < 40 + q * 10) {
                    color = sr.recipe == null ? 0xFFFFFF : (sr.cat ? 0xFFCCCC : 0xCCCCFF);
                }
                g.drawString(font, sr.name, 32, 32 + q * 10, color, false);
                ++q;
                if (32 + (q + 1) * 10 > screenY) {
                    g.drawString(font, I18n.get("tc.search.more"), 22, 34 + q * 10, 0xAAAAAA, false);
                    break;
                }
            }
        }
        genResearchBackgroundFixedPost(g, mx, my);
        if (popuptime > System.currentTimeMillis()) {
            UtilsFX.drawCustomTooltip(g, font, List.of(popupmessage), 10, 34, -99);
        }
    }

    protected void genResearchBackgroundZoomable(GuiGraphics g, int mx, int my, int locX, int locY) {
        ResearchCategory cat = ResearchCategories.getResearchCategory(selectedCategory);
        float x0 = (startX - 2) * screenZoom;
        float y0 = (startY - 2) * screenZoom;
        float w = (screenX + 4) * screenZoom;
        float h = (screenY + 4) * screenZoom;
        // drawTexturedModalRectWithDoubles: the background repeats every 256 units, with parallax.
        UtilsFX.quad(g, cat.background, x0, y0, x0 + w, y0 + h,
                (float) (locX / 2.0 / 256.0), (float) ((locX / 2.0 + w) / 256.0), (float) (locY / 2.0 / 256.0), (float) ((locY / 2.0 + h) / 256.0));
        if (cat.background2 != null) {
            UtilsFX.quad(g, cat.background2, x0, y0, x0 + w, y0 + h,
                    (float) (locX / 1.5 / 256.0), (float) ((locX / 1.5 + w) / 256.0), (float) (locY / 1.5 / 256.0), (float) ((locY / 1.5 + h) / 256.0));
        }
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        for (ResearchEntry source : research) {
            if (source.getParents() != null && source.getParents().length > 0) {
                for (int a = 0; a < source.getParents().length; ++a) {
                    ResearchEntry parent = source.getParents()[a] == null ? null : ResearchCategories.getResearch(source.getParentsClean()[a]);
                    if (parent == null || !parent.getCategory().equals(selectedCategory)) continue;
                    if (parent.getSiblings() != null && Arrays.asList(parent.getSiblings()).contains(source.getKey())) continue;
                    boolean knowsParent = ThaumcraftCapabilities.knowsResearchStrict(player, source.getParents()[a]);
                    boolean b = isVisible(source) && !source.getParents()[a].startsWith("~");
                    if (b) {
                        if (knowsParent) {
                            drawLine(g, source.getDisplayColumn(), source.getDisplayRow(), parent.getDisplayColumn(), parent.getDisplayRow(),
                                    0.6f, 0.6f, 0.6f, locX, locY, 3.0f, true, source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
                        } else if (isVisible(parent)) {
                            drawLine(g, source.getDisplayColumn(), source.getDisplayRow(), parent.getDisplayColumn(), parent.getDisplayRow(),
                                    0.2f, 0.2f, 0.2f, locX, locY, 2.0f, true, source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
                        }
                    }
                }
            }
            if (source.getSiblings() != null && source.getSiblings().length > 0) {
                for (int a = 0; a < source.getSiblings().length; ++a) {
                    ResearchEntry sibling = source.getSiblings()[a] == null ? null : ResearchCategories.getResearch(source.getSiblings()[a]);
                    if (sibling == null || !sibling.getCategory().equals(selectedCategory)) continue;
                    boolean knowsSibling = ThaumcraftCapabilities.knowsResearchStrict(player, sibling.getKey());
                    if (isVisible(source) && !source.getSiblings()[a].startsWith("~")) {
                        if (knowsSibling) {
                            drawLine(g, sibling.getDisplayColumn(), sibling.getDisplayRow(), source.getDisplayColumn(), source.getDisplayRow(),
                                    0.3f, 0.3f, 0.4f, locX, locY, 1.0f, false, source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
                        } else if (isVisible(sibling)) {
                            drawLine(g, sibling.getDisplayColumn(), sibling.getDisplayRow(), source.getDisplayColumn(), source.getDisplayRow(),
                                    0.1875f, 0.1875f, 0.25f, locX, locY, 0.0f, false, source.hasMeta(ResearchEntry.EnumResearchMeta.REVERSE));
                        }
                    }
                }
            }
        }
        g.pose().pushPose();
        g.pose().translate(0, 0, 10.0f);
        for (ResearchEntry iconResearch : research) {
            boolean hasWarp = false;
            if (iconResearch.getStages() != null) {
                for (ResearchStage stage : iconResearch.getStages()) {
                    if (stage.getWarp() > 0) {
                        hasWarp = true;
                        break;
                    }
                }
            }
            int var25 = iconResearch.getDisplayColumn() * 24 - locX;
            int var26 = iconResearch.getDisplayRow() * 24 - locY;
            if (var25 < -24 || var26 < -24 || var25 > screenX * screenZoom || var26 > screenY * screenZoom) continue;
            int iconX = startX + var25;
            int iconY = startY + var26;
            if (!isVisible(iconResearch)) continue;
            if (hasWarp) drawForbidden(g, iconX + 8, iconY + 8);
            if (knowledge.isResearchComplete(iconResearch.getKey())) {
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else if (canUnlockResearch(iconResearch)) {
                float var27 = (float) Math.sin(System.currentTimeMillis() % 600L / 600.0 * Math.PI * 2.0) * 0.25f + 0.75f;
                g.setColor(var27, var27, var27, 1.0f);
            } else {
                g.setColor(0.3f, 0.3f, 0.3f, 1.0f);
            }
            boolean hidden = iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN);
            if (iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.ROUND)) {
                UtilsFX.blit(g, tx1, iconX - 8, iconY - 8, 144, 48 + (hidden ? 32 : 0), 32, 32);
            } else {
                int ix = 80;
                int iy = 48;
                if (hidden) iy += 32;
                if (iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.HEX)) ix += 32;
                UtilsFX.blit(g, tx1, iconX - 8, iconY - 8, ix, iy, 32, 32);
            }
            if (iconResearch.hasMeta(ResearchEntry.EnumResearchMeta.SPIKY)) {
                UtilsFX.blit(g, tx1, iconX - 8, iconY - 8, 176, 48 + (hidden ? 32 : 0), 32, 32);
            }
            boolean bw = false;
            if (!canUnlockResearch(iconResearch)) {
                g.setColor(0.1f, 0.1f, 0.1f, 1.0f);
                bw = true;
            }
            if (knowledge.hasResearchFlag(iconResearch.getKey(), PlayerKnowledge.EnumResearchFlag.RESEARCH)) {
                drawHalf(g, iconX - 9, iconY - 9, 176, 16);
            }
            if (knowledge.hasResearchFlag(iconResearch.getKey(), PlayerKnowledge.EnumResearchFlag.PAGE)) {
                drawHalf(g, iconX - 9, iconY + 9, 208, 16);
            }
            if (bw) g.setColor(0.1f, 0.1f, 0.1f, 1.0f);
            drawResearchIcon(g, iconResearch, iconX, iconY, bw);
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (mx >= startX && my >= startY && mx < startX + screenX && my < startY + screenY
                    && mx >= (iconX - 2) / screenZoom && mx <= (iconX + 18) / screenZoom
                    && my >= (iconY - 2) / screenZoom && my <= (iconY + 18) / screenZoom) {
                currentHighlight = iconResearch;
            }
        }
        g.pose().popPose();
    }

    /** The "new research" and "new page" marks, at half size. */
    private void drawHalf(GuiGraphics g, int x, int y, int u, int v) {
        g.pose().pushPose();
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        g.pose().translate(x, y, 1.0f);
        g.pose().scale(0.5f, 0.5f, 1.0f);
        UtilsFX.blit(g, tx1, 0, 0, u, v, 32, 32);
        g.pose().popPose();
    }

    /** The research icon (an item, a texture or a strip of animation frames) on a 16x16 square. */
    public static void drawResearchIcon(GuiGraphics g, ResearchEntry iconResearch, int iconX, int iconY, boolean bw) {
        if (iconResearch.getIcons() == null || iconResearch.getIcons().length == 0) return;
        int idx = (int) (System.currentTimeMillis() / 1000L % iconResearch.getIcons().length);
        Object icon = iconResearch.getIcons()[idx];
        if (icon instanceof ResourceLocation loc) {
            // Items not ported yet arrive as their id: draw the unknown symbol instead.
            if (!UtilsFX.hasTexture(loc)) loc = GuiResearchPage.dummyResearch;
            if (bw) g.setColor(0.2f, 0.2f, 0.2f, 1.0f);
            int[] size = UtilsFX.getTextureSize(loc);
            int w = size[0];
            int h = size[1];
            if (h > w && h % w == 0) {
                int m = h / w;
                float q = 16.0f / m;
                float idx2 = System.currentTimeMillis() / 150L % m * q;
                UtilsFX.drawTexturedQuadF(g, loc, iconX, iconY, 0.0f, idx2, 16.0f, q);
            } else if (w > h && w % h == 0) {
                int m = w / h;
                float q = 16.0f / m;
                float idx2 = System.currentTimeMillis() / 150L % m * q;
                UtilsFX.drawTexturedQuadF(g, loc, iconX, iconY, idx2, 0.0f, q, 16.0f);
            } else {
                UtilsFX.drawTexturedQuadFull(g, loc, iconX, iconY);
            }
        } else if (icon instanceof ItemStack stack) {
            g.renderItem(stack, iconX, iconY);
        } else if (icon instanceof String str && str.startsWith("focus")) {
            String k = str.replaceAll("focus:", "");
            if (thaumcraft.api.casters.FocusEngine.getElement(k.trim()) instanceof thaumcraft.api.casters.FocusNode fp) {
                GuiFocalManipulator.drawPart(g, fp, iconX + 8, iconY + 8, 24.0f, bw ? 50 : 220, false);
            }
        }
    }

    private void genResearchBackgroundFixedPost(GuiGraphics g, int mx, int my) {
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        for (int c = 16; c < width - 16; c += 64) {
            int p = Math.min(64, width - 16 - c);
            if (p > 0) {
                UtilsFX.blit(g, tx1, c, -2, 48, 13, p, 22);
                UtilsFX.blit(g, tx1, c, height - 20, 48, 13, p, 22);
            }
        }
        for (int c = 16; c < height - 16; c += 64) {
            int p = Math.min(64, height - 16 - c);
            if (p > 0) {
                UtilsFX.blit(g, tx1, -2, c, 13, 48, 22, p);
                UtilsFX.blit(g, tx1, width - 20, c, 13, 48, 22, p);
            }
        }
        UtilsFX.blit(g, tx1, -2, -2, 13, 13, 22, 22);
        UtilsFX.blit(g, tx1, -2, height - 20, 13, 13, 22, 22);
        UtilsFX.blit(g, tx1, width - 20, -2, 13, 13, 22, 22);
        UtilsFX.blit(g, tx1, width - 20, height - 20, 13, 13, 22, 22);
        g.pose().translate(0, 0, 10);
        for (Button b : buttons) b.render(g, mx, my);
        g.pose().popPose();
        if (currentHighlight != null) {
            PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
            ArrayList<String> text = new ArrayList<>();
            text.add("§6" + currentHighlight.getLocalizedName().getString());
            if (canUnlockResearch(currentHighlight)) {
                if (!knowledge.isResearchComplete(currentHighlight.getKey()) && currentHighlight.getStages() != null) {
                    int stage = knowledge.getResearchStage(currentHighlight.getKey());
                    if (stage > 0) {
                        text.add("@@" + ChatFormatting.AQUA + I18n.get("tc.research.stage") + " " + stage + "/" + currentHighlight.getStages().length + ChatFormatting.RESET);
                    } else {
                        text.add("@@" + ChatFormatting.GREEN + I18n.get("tc.research.begin") + ChatFormatting.RESET);
                    }
                }
            } else {
                text.add("@@§c" + I18n.get("tc.researchmissing"));
                int a = 0;
                for (String p2 : currentHighlight.getParents()) {
                    if (!ThaumcraftCapabilities.knowsResearchStrict(player, p2)) {
                        String s = "?";
                        ResearchEntry pe = ResearchCategories.getResearch(currentHighlight.getParentsClean()[a]);
                        if (pe != null) s = pe.getLocalizedName().getString();
                        text.add("@@§e - " + s);
                    }
                    ++a;
                }
            }
            if (knowledge.hasResearchFlag(currentHighlight.getKey(), PlayerKnowledge.EnumResearchFlag.RESEARCH)) {
                text.add("@@" + I18n.get("tc.research.newresearch"));
            }
            if (knowledge.hasResearchFlag(currentHighlight.getKey(), PlayerKnowledge.EnumResearchFlag.PAGE)) {
                text.add("@@" + I18n.get("tc.research.newpage"));
            }
            UtilsFX.drawCustomTooltip(g, font, text, mx + 3, my - 3, -99);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        popuptime = System.currentTimeMillis() - 1L;
        if (button == 0 && !searching && mx >= startX && mx < startX + screenX && my >= startY && my < startY + screenY) {
            isMouseButtonDown = true;
        }
        if (button == 0) {
            PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
            if (!searching && currentHighlight != null && !ThaumcraftCapabilities.knowsResearch(player, currentHighlight.getKey()) && canUnlockResearch(currentHighlight)) {
                updateResearch();
                PacketDistributor.sendToServer(new PacketSyncProgressToServer(currentHighlight.getKey(), true));
                minecraft.setScreen(new GuiResearchPage(currentHighlight, null, guiMapX, guiMapY));
                popuptime = System.currentTimeMillis() + 3000L;
                popupmessage = I18n.get("tc.research.popup", currentHighlight.getLocalizedName().getString());
                return true;
            } else if (currentHighlight != null && ThaumcraftCapabilities.knowsResearch(player, currentHighlight.getKey())) {
                knowledge.clearResearchFlag(currentHighlight.getKey(), PlayerKnowledge.EnumResearchFlag.RESEARCH);
                knowledge.clearResearchFlag(currentHighlight.getKey(), PlayerKnowledge.EnumResearchFlag.PAGE);
                PacketDistributor.sendToServer(new PacketSyncResearchFlagsToServer(player, currentHighlight.getKey()));
                int stage = knowledge.getResearchStage(currentHighlight.getKey());
                if (stage > 1 && stage >= currentHighlight.getStages().length) {
                    PacketDistributor.sendToServer(new PacketSyncProgressToServer(currentHighlight.getKey(), false, true, false));
                }
                minecraft.setScreen(new GuiResearchPage(currentHighlight, null, guiMapX, guiMapY));
                return true;
            } else if (searching) {
                int q = 0;
                for (SearchResult sr : searchResults) {
                    if (mx > 22 && mx < 18 + screenX && my >= 32 + q * 10 && my < 40 + q * 10) {
                        if (ThaumcraftCapabilities.knowsResearch(player, sr.key) && !sr.cat) {
                            minecraft.setScreen(new GuiResearchPage(ResearchCategories.getResearch(sr.key), sr.recipe, guiMapX, guiMapY));
                            return true;
                        }
                        if (categoriesTC.contains(sr.key) || categoriesOther.contains(sr.key)) {
                            selectCategory(sr.key);
                            return true;
                        }
                    }
                    ++q;
                    if (32 + (q + 1) * 10 > screenY) break;
                }
            }
            for (Button b : buttons) {
                if (b.isInside(mx, my)) {
                    actionPerformed(b);
                    return true;
                }
            }
        }
        if (searching && searchField.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectCategory(String key) {
        searching = false;
        searchField.setVisible(false);
        searchField.setCanLoseFocus(true);
        searchField.setFocused(false);
        selectedCategory = key;
        updateResearch();
        tempMapX = guiMapX = (guiBoundsLeft + guiBoundsRight) / 2;
        tempMapY = guiMapY = (guiBoundsBottom + guiBoundsTop) / 2;
    }

    private void actionPerformed(Button button) {
        if (button.type == Button.SEARCH) {
            selectedCategory = "";
            searching = true;
            searchField.setVisible(true);
            searchField.setCanLoseFocus(false);
            searchField.setFocused(true);
            searchField.setValue("");
            updateSearch();
        }
        if (button.type == Button.SCROLL_UP && catScrollPos > 0) {
            --catScrollPos;
            updateResearch();
        }
        if (button.type == Button.SCROLL_DOWN && catScrollPos < catScrollMax) {
            ++catScrollPos;
            updateResearch();
        }
        if (button.type == Button.CATEGORY && !button.key.equals(selectedCategory)) {
            selectCategory(button.key);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawLine(GuiGraphics g, int x, int y, int x2, int y2, float r, float gr, float b, int locX, int locY, float zMod, boolean arrow, boolean flipped) {
        boolean bigCorner = false;
        int xd;
        int yd;
        int xm;
        int ym;
        int xx;
        int yy;
        if (flipped) {
            xd = Math.abs(x2 - x);
            yd = Math.abs(y2 - y);
            xm = xd == 0 ? 0 : (x2 - x > 0 ? -1 : 1);
            ym = yd == 0 ? 0 : (y2 - y > 0 ? -1 : 1);
            if (xd > 1 && yd > 1) bigCorner = true;
            xx = x2 * 24 - 4 - locX + startX;
            yy = y2 * 24 - 4 - locY + startY;
        } else {
            xd = Math.abs(x - x2);
            yd = Math.abs(y - y2);
            xm = xd == 0 ? 0 : (x - x2 > 0 ? -1 : 1);
            ym = yd == 0 ? 0 : (y - y2 > 0 ? -1 : 1);
            if (xd > 1 && yd > 1) bigCorner = true;
            xx = x * 24 - 4 - locX + startX;
            yy = y * 24 - 4 - locY + startY;
        }
        g.pose().pushPose();
        // Over the background and under the icons; the better known lines over the others.
        g.pose().translate(0, 0, 1.0f + zMod);
        g.setColor(r, gr, b, 1.0f);
        if (arrow) {
            if (flipped) {
                int xx2 = x * 24 - 8 - locX + startX;
                int yy2 = y * 24 - 8 - locY + startY;
                if (xm < 0) {
                    UtilsFX.blit(g, tx1, xx2, yy2, 160, 112, 32, 32);
                } else if (xm > 0) {
                    UtilsFX.blit(g, tx1, xx2, yy2, 128, 112, 32, 32);
                } else if (ym > 0) {
                    UtilsFX.blit(g, tx1, xx2, yy2, 64, 112, 32, 32);
                } else if (ym < 0) {
                    UtilsFX.blit(g, tx1, xx2, yy2, 96, 112, 32, 32);
                }
            } else if (ym < 0) {
                UtilsFX.blit(g, tx1, xx - 4, yy - 4, 64, 112, 32, 32);
            } else if (ym > 0) {
                UtilsFX.blit(g, tx1, xx - 4, yy - 4, 96, 112, 32, 32);
            } else if (xm > 0) {
                UtilsFX.blit(g, tx1, xx - 4, yy - 4, 160, 112, 32, 32);
            } else if (xm < 0) {
                UtilsFX.blit(g, tx1, xx - 4, yy - 4, 128, 112, 32, 32);
            }
        }
        int v = 1;
        int h = 0;
        while (v < yd - (bigCorner ? 1 : 0)) {
            UtilsFX.blit(g, tx1, xx + xm * 24 * h, yy + ym * 24 * v, 0, 228, 24, 24);
            ++v;
        }
        if (bigCorner) {
            if (xm < 0 && ym > 0) UtilsFX.blit(g, tx1, xx + xm * 24 * h - 24, yy + ym * 24 * v, 0, 180, 48, 48);
            if (xm > 0 && ym > 0) UtilsFX.blit(g, tx1, xx + xm * 24 * h, yy + ym * 24 * v, 48, 180, 48, 48);
            if (xm < 0 && ym < 0) UtilsFX.blit(g, tx1, xx + xm * 24 * h - 24, yy + ym * 24 * v - 24, 96, 180, 48, 48);
            if (xm > 0 && ym < 0) UtilsFX.blit(g, tx1, xx + xm * 24 * h, yy + ym * 24 * v - 24, 144, 180, 48, 48);
        } else {
            if (xm < 0 && ym > 0) UtilsFX.blit(g, tx1, xx + xm * 24 * h, yy + ym * 24 * v, 48, 228, 24, 24);
            if (xm > 0 && ym > 0) UtilsFX.blit(g, tx1, xx + xm * 24 * h, yy + ym * 24 * v, 72, 228, 24, 24);
            if (xm < 0 && ym < 0) UtilsFX.blit(g, tx1, xx + xm * 24 * h, yy + ym * 24 * v, 96, 228, 24, 24);
            if (xm > 0 && ym < 0) UtilsFX.blit(g, tx1, xx + xm * 24 * h, yy + ym * 24 * v, 120, 228, 24, 24);
        }
        v += bigCorner ? 1 : 0;
        for (h += bigCorner ? 2 : 1; h < xd; ++h) {
            UtilsFX.blit(g, tx1, xx + xm * 24 * h, yy + ym * 24 * v, 24, 228, 24, 24);
        }
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        g.pose().popPose();
    }

    /** The purple swirl behind research that gives warp. */
    public static void drawForbidden(GuiGraphics g, double x, double y) {
        Player p = Minecraft.getInstance().player;
        int count = p == null ? 0 : p.tickCount;
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0);
        UtilsFX.renderQuadCentered(g, UtilsFX.nodeTexture, 32, 32, 160 + count % 32, 90.0f, 0.33f, 0.0f, 0.44f, true, 0.9f);
        g.pose().popPose();
    }

    static class SearchResult implements Comparable<SearchResult> {
        final String name;
        final String key;
        final ResourceLocation recipe;
        final boolean cat;

        SearchResult(String name, String key, ResourceLocation recipe, boolean cat) {
            this.name = name;
            this.key = key;
            this.recipe = recipe;
            this.cat = cat;
        }

        @Override
        public int compareTo(SearchResult arg) {
            int k = key.compareTo(arg.key);
            return k == 0 && recipe != null && arg.recipe != null ? recipe.compareTo(arg.recipe) : k;
        }
    }

    /** GuiCategoryButton, GuiScrollButton and GuiSearchButton. */
    private class Button {
        static final int SEARCH = 2;
        static final int SCROLL_UP = 3;
        static final int SCROLL_DOWN = 4;
        static final int CATEGORY = 20;
        final int type;
        final int x;
        final int y;
        final int width;
        final int height;
        final String displayString;
        final ResearchCategory rc;
        final String key;
        final boolean flip;
        final int completion;

        Button(int type, int x, int y, int width, int height, String displayString, ResearchCategory rc, String key, boolean flip, int completion) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.displayString = displayString;
            this.rc = rc;
            this.key = key;
            this.flip = flip;
            this.completion = completion;
        }

        int shift() {
            return type == CATEGORY ? addonShift : 0;
        }

        boolean isInside(int mx, int my) {
            return mx >= x && my >= y + shift() && mx < x + width && my < y + height + shift();
        }

        void render(GuiGraphics g, int mx, int my) {
            boolean hovered = isInside(mx, my);
            RenderSystem.enableBlend();
            switch (type) {
                case SEARCH -> {
                    float c = hovered ? 1.0f : 0.8f;
                    g.setColor(c, c, c, 1.0f);
                    UtilsFX.blit(g, tx1, x, y, 160, 16, 16, 16);
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    if (hovered) g.drawString(font, displayString, x + 19, y + 4, 0xFFFFFF);
                }
                case SCROLL_UP, SCROLL_DOWN -> {
                    float c = hovered ? 1.0f : 0.7f;
                    g.setColor(c, c, c, 1.0f);
                    UtilsFX.blit(g, tx1, x, y, 51, flip ? 71 : 55, 10, 11);
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
                default -> renderCategory(g, hovered);
            }
        }

        private void renderCategory(GuiGraphics g, boolean hovered) {
            int sy = y + addonShift;
            if (!key.equals(selectedCategory)) {
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                g.setColor(0.6f, 1.0f, 1.0f, 1.0f);
            }
            UtilsFX.blit(g, tx1, x - 3, sy - 3, 13, 13, 22, 22);
            if (key.equals(selectedCategory) || hovered) {
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                g.setColor(0.66f, 0.66f, 0.66f, 0.8f);
            }
            UtilsFX.drawTexturedQuadFull(g, rc.icon, x, sy);
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
            boolean nr = false;
            boolean np = false;
            for (String rk : rc.research.keySet()) {
                if (ThaumcraftCapabilities.knowsResearch(player, rk)) {
                    if (!nr && knowledge.hasResearchFlag(rk, PlayerKnowledge.EnumResearchFlag.RESEARCH)) nr = true;
                    if (!np && knowledge.hasResearchFlag(rk, PlayerKnowledge.EnumResearchFlag.PAGE)) np = true;
                    if (nr && np) break;
                }
            }
            if (nr) drawQuarter(g, x - 2, sy - 2, 176);
            if (np) drawQuarter(g, x - 2, sy + 9, 208);
            if (hovered) {
                String dp = displayString + " (" + completion + "%)";
                g.drawString(font, dp, flip ? screenX + 9 - font.width(dp) : x + 22, sy + 4, 0xFFFFFF);
                int t = 9;
                if (nr) {
                    String s = I18n.get("tc.research.newresearch");
                    g.drawString(font, s, flip ? screenX + 9 - font.width(s) : x + 22, sy + 4 + t, 0xFFFFFF);
                    t += 9;
                }
                if (np) {
                    String s = I18n.get("tc.research.newpage");
                    g.drawString(font, s, flip ? screenX + 9 - font.width(s) : x + 22, sy + 4 + t, 0xFFFFFF);
                }
            }
        }

        private void drawQuarter(GuiGraphics g, int qx, int qy, int u) {
            g.pose().pushPose();
            g.setColor(1.0f, 1.0f, 1.0f, 0.7f);
            g.pose().translate(qx, qy, 1.0f);
            g.pose().scale(0.25f, 0.25f, 1.0f);
            UtilsFX.blit(g, tx1, 0, 0, u, 16, 32, 32);
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            g.pose().popPose();
        }
    }
}
