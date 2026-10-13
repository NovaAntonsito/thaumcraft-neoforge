package thaumcraft.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.common.lib.network.playerdata.PacketGivePlayerNoteToServer;
import thaumcraft.common.lib.research.ResearchNoteManager;
import org.lwjgl.glfw.GLFW;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.Part;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.internal.CommonInternals;
import thaumcraft.api.research.ResearchAddendum;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.crafting.InfusionEnchantmentRecipe;
import thaumcraft.common.lib.crafting.Matrix;
import thaumcraft.common.lib.network.playerdata.PacketSyncProgressToServer;
import thaumcraft.common.lib.utils.InventoryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of GuiResearchPage: one research open in the thaumonomicon. Two pages of text at a time, the stage
 * requirements and the "complete" button, recipe bookmarks on the right, and the aspect and knowledge inserts.
 */
public class GuiResearchPage extends Screen {
    public static LinkedList<ResourceLocation> history = new LinkedList<>();
    protected int paneWidth = 256;
    protected int paneHeight = 181;
    protected double guiMapX;
    protected double guiMapY;
    private final ResearchEntry research;
    private int currentStage = 0;
    int lastStage = 0;
    boolean hold = false;
    private int page = 0;
    private int maxPages = 0;
    private int maxAspectPages;
    AspectList knownPlayerAspects;
    PlayerKnowledge playerKnowledge;
    int rhash = 0;
    float transX;
    float transY;
    float rotX;
    float rotY;
    long lastCheck = 0L;
    static final ResourceLocation tex1 = Thaumcraft.id("textures/gui/gui_researchbook.png");
    static final ResourceLocation tex2 = Thaumcraft.id("textures/gui/gui_researchbook_overlay.png");
    static final ResourceLocation tex3 = Thaumcraft.id("textures/aspects/_back.png");
    static final ResourceLocation tex4 = Thaumcraft.id("textures/gui/paper.png");
    public static final ResourceLocation dummyResearch = Thaumcraft.id("textures/aspects/_unknown.png");
    static final ResourceLocation dummyMap = Thaumcraft.id("textures/research/rd_map.png");
    static final ResourceLocation dummyFlask = Thaumcraft.id("textures/research/rd_flask.png");
    static final ResourceLocation dummyChest = Thaumcraft.id("textures/research/rd_chest.png");
    /** HudHandler.KNOW_TYPE, by EnumKnowledgeType ordinal. */
    static final ResourceLocation[] KNOW_TYPE = {Thaumcraft.id("textures/research/knowledge_theory.png"),
            Thaumcraft.id("textures/research/knowledge_observation.png")};
    int hrx = 0;
    int hry = 0;
    static ResourceLocation shownRecipe;
    int recipePage = 0;
    int recipePageMax = 0;
    private long lastCycle = 0L;
    private boolean showingAspects = false;
    private boolean showingKnowledge = false;
    private static int aspectsPage = 0;
    final LinkedHashMap<ResourceLocation, List<Object>> recipeLists = new LinkedHashMap<>();
    final LinkedHashMap<ResourceLocation, List<Object>> recipeOutputs = new LinkedHashMap<>();
    final LinkedHashMap<ResourceLocation, List<Object>> drilldownLists = new LinkedHashMap<>();
    boolean hasRecipePages;
    boolean renderingCompound = false;
    static boolean cycleMultiblockLines = false;
    BlueprintBlockAccess blockAccess = null;
    final HashMap<ResourceLocation, BlueprintBlockAccess> blockAccessIcons = new HashMap<>();
    /** Clickable stacks of the last frame: x, y, the recipe they lead to and its page. */
    final ArrayList<Reference> reference = new ArrayList<>();
    private int cycle = -1;
    boolean allowWithPagePopup = false;
    List<String> tipText = null;
    private static final PageImage PILINE = PageImage.parse("thaumcraft:textures/gui/gui_researchbook.png:24:184:95:6:1");
    private static final PageImage PIDIV = PageImage.parse("thaumcraft:textures/gui/gui_researchbook.png:28:192:140:6:1");
    private final ArrayList<Page> pages = new ArrayList<>();
    boolean isComplete = false;
    boolean hasAllRequisites = false;
    boolean[] hasItem = null;
    boolean[] hasCraft = null;
    boolean[] hasResearch = null;
    boolean[] hasKnow = null;
    /** Where each research note was drawn this frame, for clicks. */
    private final Map<int[], String> notePoints = new LinkedHashMap<>();
    public final HashMap<Integer, String> keyCache = new HashMap<>();

    record Reference(int x, int y, ResourceLocation recipe, int page) {
    }

    public GuiResearchPage(ResearchEntry research, ResourceLocation recipe, double x, double y) {
        super(Component.empty());
        this.research = research;
        guiMapX = x;
        guiMapY = y;
        minecraft = Minecraft.getInstance();
        font = minecraft.font;
        playerKnowledge = ThaumcraftCapabilities.getKnowledge(minecraft.player);
        parsePages();
        // Reborn: the aspects insert lists the player's aspect pool (amount = complexity), not the "!aspect" research.
        knownPlayerAspects = new AspectList();
        for (Aspect a : ThaumcraftCapabilities.getAspects(minecraft.player).aspectsPool().getAspects()) {
            knownPlayerAspects.add(a, ResearchNoteManager.getAspectComplexity(a));
        }
        maxAspectPages = Mth.ceil(knownPlayerAspects.size() / 5.0f);
        page = 0;
        if (recipe != null) shownRecipe = recipe;
    }

    @Override
    protected void init() {
        rotX = 25.0f;
        rotY = -45.0f;
    }

    private Player player() {
        return minecraft.player;
    }

    private void playSound(SoundEvent sound, float volume, float pitch) {
        if (minecraft.player != null) minecraft.player.playSound(sound, volume, pitch);
    }

    @Override
    public void render(GuiGraphics g, int par1, int par2, float par3) {
        // The knowledge object is replaced on every sync from the server.
        playerKnowledge = ThaumcraftCapabilities.getKnowledge(player());
        hasRecipePages = false;
        long nano = System.nanoTime();
        if (nano > lastCheck) {
            parsePages();
            lastCheck = nano + (hold ? 250000000L : 2000000000L);
            if (currentStage > lastStage) hold = false;
        }
        renderTransparentBackground(g);
        genResearchBackground(g, par1, par2);
        int sw = (width - paneWidth) / 2;
        int sh = (height - paneHeight) / 2;
        if (!history.isEmpty()) {
            int mx = par1 - (sw + 118);
            int my = par2 - (sh + 190);
            if (mx >= 0 && my >= 0 && mx < 20 && my < 12) {
                g.pose().pushPose();
                g.pose().translate(0, 0, 450);
                g.drawString(font, I18n.get("recipe.return"), par1, par2, 0xFFFFFF, true);
                g.pose().popPose();
            }
        }
    }

    protected void genResearchBackground(GuiGraphics g, int par1, int par2) {
        int sw = (width - paneWidth) / 2;
        int sh = (height - paneHeight) / 2;
        float var10 = (width - paneWidth * 1.3f) / 2.0f;
        float var11 = (height - paneHeight * 1.3f) / 2.0f;
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        g.pose().pushPose();
        g.pose().translate(var10, var11, 0.0f);
        g.pose().scale(1.3f, 1.3f, 1.0f);
        UtilsFX.blit(g, tex1, 0, 0, 0, 0, paneWidth, paneHeight);
        g.pose().popPose();
        reference.clear();
        int current = 0;
        for (Page p : pages) {
            if ((current == page || current == page + 1) && current < maxPages) {
                drawPage(g, p, current % 2, sw, sh - 10, par1, par2);
            }
            if (++current > page + 1) break;
        }
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        float bob = Mth.sin(player().tickCount / 3.0f) * 0.2f + 0.1f;
        if (!history.isEmpty()) drawTexturedModalRectScaled(g, tex1, sw + 118, sh + 190, 38, 202, 20, 12, bob);
        if (page > 0 && shownRecipe == null) drawTexturedModalRectScaled(g, tex1, sw - 16, sh + 190, 0, 184, 12, 8, bob);
        if (page < maxPages - 2 && shownRecipe == null) drawTexturedModalRectScaled(g, tex1, sw + 262, sh + 190, 12, 184, 12, 8, bob);
        if (tipText != null) {
            UtilsFX.drawCustomTooltip(g, font, tipText, par1, par2 + 12, 11);
            tipText = null;
        }
    }

    private void drawPage(GuiGraphics g, Page pageParm, int side, int x, int y, int mx, int my) {
        if (lastCycle < System.currentTimeMillis()) {
            ++cycle;
            lastCycle = System.currentTimeMillis() + 1000L;
            if (cycleMultiblockLines && blockAccess != null) ++blockAccess.sliceLine;
        }
        if (page == 0 && side == 0) {
            UtilsFX.blit(g, tex1, x + 4, y - 7, 24, 184, 96, 4);
            UtilsFX.blit(g, tex1, x + 4, y + 10, 24, 184, 96, 4);
            String name = research.getLocalizedName().getString();
            int offset = font.width(name);
            if (offset <= 140) {
                g.drawString(font, name, x - 15 + 140 / 2 - offset / 2, y, 0x202020, false);
            } else {
                float vv = 140.0f / offset;
                g.pose().pushPose();
                g.pose().translate(x - 15 + 140 / 2 - offset / 2 * vv, y + 1.0f * vv, 0.0f);
                g.pose().scale(vv, vv, vv);
                g.drawString(font, name, 0, 0, 0x202020, false);
                g.pose().popPose();
            }
            y += 28;
        }
        for (Object content : pageParm.contents) {
            if (content instanceof String s) {
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                g.drawString(font, s.replace("~B", ""), x - 15 + side * 152, y - 6, 0, false);
                y += font.lineHeight;
                if (s.endsWith("~B")) y += (int) (font.lineHeight * 0.66);
            } else if (content instanceof PageImage pi) {
                g.pose().pushPose();
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                int pad = (140 - pi.aw) / 2;
                g.pose().translate(x - 15 + side * 152 + pad, y - 5, 0.0f);
                g.pose().scale(pi.scale, pi.scale, pi.scale);
                UtilsFX.blit(g, pi.loc, 0, 0, pi.x, pi.y, pi.w, pi.h);
                g.pose().popPose();
                y += pi.ah + 2;
            }
        }
        if (playerKnowledge.isResearchComplete("FIRSTSTEPS")) {
            y = (height - paneHeight) / 2 + 9;
            int le = mouseInside(x - 48, y, 25, 16, mx, my) ? 0 : 3;
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            drawPopupAt(x - 48, y, 25, 16, mx, my, "tc.aspect.name");
            UtilsFX.blit(g, tex1, x - 48 + le, y, 76, 232, 24 - le, 16);
            UtilsFX.blit(g, tex1, x - 28, y, 100, 232, 4, 16);
        }
        if (knowledgeTabShown() && !research.getKey().equals("KNOWLEDGETYPES")) {
            y = (height - paneHeight) / 2 + 32;
            int le = mouseInside(x - 48, y, 25, 16, mx, my) ? 0 : 3;
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            drawPopupAt(x - 48, y, 25, 16, mx, my, "tc.knowledge.name");
            UtilsFX.blit(g, tex1, x - 49 + le, y, 44, 232, 24 - le, 16);
            UtilsFX.blit(g, tex1, x - 29, y, 100, 232, 4, 16);
        }
        if (research.getStages() == null) return;
        ResearchStage stage = research.getStages()[currentStage];
        if (stage.getRecipes() != null) drawRecipeBookmarks(g, x, mx, my);
        if (page == 0 && side == 0 && !isComplete) drawRequirements(g, x, mx, my, stage);
        if (knowledgeTabShown() && research.getKey().equals("KNOWLEDGETYPES")) {
            drawKnowledges(g, x, (height - paneHeight) / 2 - 16 + 210, mx, my, true);
        }
        renderingCompound = false;
        if (showingAspects) {
            drawAspectsInsert(g, mx, my);
        } else if (showingKnowledge) {
            drawKnowledgesInsert(g, mx, my);
        } else if (shownRecipe != null) {
            drawRecipe(g, mx, my);
        } else if (stage.getWarp() > 0 && !isComplete) {
            int warp = Math.min(5, stage.getWarp());
            drawForbiddenWarning(g, x, y, mx, my, warp);
        }
    }

    private void drawForbiddenWarning(GuiGraphics g, int x, int y, int mx, int my, int warp) {
        GuiResearchBrowser.drawForbidden(g, x - 57, y - 40);
        String s = I18n.get("tc.forbidden.level." + warp);
        g.drawString(font, s, x - 56 - font.width(s) / 2, y - 43, 0xAA99FF, false);
        String text = I18n.get("tc.warp.warn");
        drawPopupAt(x - 67, y - 50, 20, 20, mx, my, text.replaceAll("%n", Matcher.quoteReplacement(s)));
    }

    /** The paper insert over the book (recipes, aspects, knowledge), drawn in front of the pages. */
    private int[] beginInsert(GuiGraphics g) {
        allowWithPagePopup = true;
        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - 256) / 2;
        int y = (height - 256) / 2;
        UtilsFX.blit(g, tex4, x, y, 0, 0, 255, 255);
        return new int[]{x, y};
    }

    private void endInsert(GuiGraphics g) {
        g.pose().popPose();
        allowWithPagePopup = false;
    }

    private void drawKnowledgesInsert(GuiGraphics g, int mx, int my) {
        int[] xy = beginInsert(g);
        drawKnowledges(g, xy[0] + 60, (height - paneHeight) / 2 + 75, mx, my, false);
        endInsert(g);
    }

    private void drawKnowledges(GuiGraphics g, int x, int y, int mx, int my, boolean inpage) {
        y -= 18;
        boolean drewSomething = false;
        int tc = 0;
        int ka = ResearchCategories.researchCategories.size();
        for (PlayerKnowledge.EnumKnowledgeType type : PlayerKnowledge.EnumKnowledgeType.values()) {
            int fc = 0;
            int hs = (int) (164.0f / ka);
            boolean b = false;
            for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
                if (!type.hasFields() && category != null) continue;
                int amt = playerKnowledge.getKnowledge(type, category);
                int par = playerKnowledge.getKnowledgeRaw(type, category) % type.getProgression();
                if (amt <= 0 && par <= 0) continue;
                drewSomething = true;
                int kx = x - 10 + (inpage ? 18 : hs) * fc;
                int ky = y - tc * (inpage ? 20 : 28);
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                g.pose().pushPose();
                g.pose().translate(kx, ky, 0.0f);
                g.pose().scale(0.0625f, 0.0625f, 0.0625f);
                UtilsFX.blit(g, KNOW_TYPE[type.ordinal()], 0, 0, 0, 0, 255, 255);
                if (type.hasFields() && category != null) {
                    g.setColor(1.0f, 1.0f, 1.0f, 0.75f);
                    g.pose().translate(0.0f, 0.0f, 1.0f);
                    g.pose().scale(0.66f, 0.66f, 0.66f);
                    UtilsFX.blit(g, category.icon, 66, 66, 0, 0, 255, 255);
                }
                g.pose().popPose();
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                g.pose().pushPose();
                g.pose().translate(0.0f, 0.0f, 5.0f);
                String s = "" + amt;
                g.drawString(font, s, kx + 16 - font.width(s), ky + 8, 0xFFFFFF, true);
                g.pose().popPose();
                s = I18n.get("tc.type." + type.name().toLowerCase());
                if (type.hasFields() && category != null) s = s + ": " + ResearchCategories.getCategoryName(category.key).getString();
                drawPopupAt(kx, ky, mx, my, s);
                if (par > 0) {
                    g.setColor(1.0f, 1.0f, 1.0f, 0.75f);
                    int l = (int) (par / (float) type.getProgression() * 16.0f);
                    UtilsFX.blit(g, tex1, kx, ky + 17, 0, 232, l, 2);
                    UtilsFX.blit(g, tex1, kx + l, ky + 17, l, 234, 16 - l, 2);
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
                ++fc;
                b = true;
            }
            if (b) ++tc;
        }
        if (inpage && drewSomething) {
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            UtilsFX.blit(g, tex1, x + 4, y - tc * 20 + 12, 24, 184, 96, 8);
        }
    }

    /** The background strip of a requirement row, with its popup. */
    private void drawRequirementRow(GuiGraphics g, int x, int y, int v, int mx, int my, String popup) {
        g.setColor(1.0f, 1.0f, 1.0f, 0.25f);
        UtilsFX.blit(g, tex1, x - 12, y - 1, 200, v, 56, 16);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        drawPopupAt(x - 15, y, mx, my, popup);
    }

    /** The tick over a requirement the player meets. */
    private void drawCheck(GuiGraphics g, int x, int y) {
        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        UtilsFX.blit(g, tex1, x + 8, y, 159, 207, 10, 10);
        g.pose().popPose();
    }

    private void drawRequirements(GuiGraphics g, int x, int mx, int my, ResearchStage stage) {
        int y = (height - paneHeight) / 2 - 16 + 210;
        boolean b = false;
        notePoints.clear();
        if (hasPlainResearch(stage)) {
            y -= 18;
            b = true;
            int shift = 24;
            drawRequirementRow(g, x, y, 232, mx, my, "tc.need.research");
            if (hasResearch != null) {
                if (hasResearch.length != stage.getResearch().length) hasResearch = new boolean[stage.getResearch().length];
                int ss = stage.getResearch().length > 6 ? 110 / stage.getResearch().length : 18;
                for (int a = 0; a < stage.getResearch().length; ++a) {
                    String key = stage.getResearch()[a];
                    if (key.startsWith("rn_")) continue;
                    String iconName = stage.getResearchIcon() != null && a < stage.getResearchIcon().length ? stage.getResearchIcon()[a] : null;
                    ResourceLocation iconLoc = iconName != null ? ResourceLocation.tryParse(iconName.toLowerCase()) : null;
                    Object loc = iconLoc != null ? iconLoc : dummyResearch;
                    String text = I18n.get("research." + key + ".text");
                    if (key.startsWith("!")) {
                        Aspect as = Aspect.aspects.get(key.replaceAll("!", ""));
                        if (as != null) {
                            loc = as;
                            text = as.getName();
                        }
                    }
                    ResearchEntry re = ResearchCategories.getResearch(key);
                    float[] color = {1.0f, 1.0f, 1.0f};
                    if (re != null && re.getIcons() != null) {
                        int idx = (int) (System.currentTimeMillis() / 1000L % re.getIcons().length);
                        loc = re.getIcons()[idx];
                        text = re.getLocalizedName().getString();
                    } else if (key.startsWith("m_")) {
                        loc = dummyMap;
                    } else if (key.startsWith("c_")) {
                        loc = dummyChest;
                    } else if (key.startsWith("f_")) {
                        loc = dummyFlask;
                    } else {
                        color = new float[]{0.5f, 0.75f, 1.0f};
                    }
                    int ix = x - 15 + shift;
                    if (loc instanceof Aspect as) {
                        int cc = as.getColor();
                        g.setColor(((cc >> 16) & 0xFF) / 255.0f, ((cc >> 8) & 0xFF) / 255.0f, (cc & 0xFF) / 255.0f, 1.0f);
                        UtilsFX.drawTexturedQuadFull(g, as.getImage(), ix, y);
                    } else if (loc instanceof ResourceLocation rl) {
                        if (!UtilsFX.hasTexture(rl)) rl = dummyResearch;
                        g.setColor(color[0], color[1], color[2], 1.0f);
                        UtilsFX.drawTexturedQuadFull(g, rl, ix, y);
                    } else if (loc instanceof ItemStack stack) {
                        g.renderItem(stack, ix, y);
                    }
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    if (hasResearch[a]) drawCheck(g, ix, y);
                    drawPopupAt(ix, y, mx, my, text);
                    shift += ss;
                }
            }
        }
        if (stage.getObtain() != null) {
            y -= 18;
            b = true;
            drawRequirementRow(g, x, y, 216, mx, my, "tc.need.obtain");
            if (hasItem != null) {
                if (hasItem.length != stage.getObtain().length) hasItem = new boolean[stage.getObtain().length];
                drawRequirementStacks(g, x, y, mx, my, stage.getObtain(), hasItem);
            }
        }
        if (stage.getCraft() != null) {
            y -= 18;
            b = true;
            drawRequirementRow(g, x, y, 200, mx, my, "tc.need.craft");
            if (hasCraft != null) {
                if (hasCraft.length != stage.getCraft().length) hasCraft = new boolean[stage.getCraft().length];
                drawRequirementStacks(g, x, y, mx, my, stage.getCraft(), hasCraft);
            }
        }
        if (hasNotes(stage)) {
            // Reborn: the research notes take the knowledge row. Click one to write it (ink and paper).
            y -= 18;
            b = true;
            int shift = 24;
            drawRequirementRow(g, x, y, 184, mx, my, "tc.need.know");
            if (hasResearch != null) {
                int ss = stage.getResearch().length > 6 ? 110 / stage.getResearch().length : 18;
                for (int a = 0; a < stage.getResearch().length; ++a) {
                    String key = stage.getResearch()[a];
                    if (!key.startsWith("rn_")) continue;
                    ItemStack note = ResearchNoteManager.noteStack(key);
                    int ix = x - 15 + shift;
                    if (note != null) {
                        notePoints.put(new int[]{ix, y}, key);
                        g.renderItem(note, ix, y);
                    }
                    if (a < hasResearch.length && hasResearch[a]) drawCheck(g, ix, y);
                    ResearchEntry target = ResearchCategories.getResearch(ResearchNoteManager.getStrippedKey(key));
                    if ((shownRecipe == null || allowWithPagePopup) && mouseInside(ix, y, 16, 16, mx, my)) {
                        tipText = new ArrayList<>(List.of(
                                I18n.get("tc.researchtheory", target != null ? target.getLocalizedName().getString() : key),
                                I18n.get("researchnote.click")));
                    }
                    shift += ss;
                }
            }
        }
        if (stage.getKnow() != null) {
            y -= 18;
            b = true;
            int shift = 24;
            drawRequirementRow(g, x, y, 184, mx, my, "tc.need.know");
            if (hasKnow != null) {
                if (hasKnow.length != stage.getKnow().length) hasKnow = new boolean[stage.getKnow().length];
                int ss = stage.getKnow().length > 6 ? 110 / stage.getKnow().length : 18;
                for (int idx = 0; idx < stage.getKnow().length; ++idx) {
                    ResearchStage.Knowledge kn = stage.getKnow()[idx];
                    int kx = x - 15 + shift;
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    g.pose().pushPose();
                    g.pose().translate(kx, y, 0.0f);
                    g.pose().scale(0.0625f, 0.0625f, 0.0625f);
                    UtilsFX.blit(g, KNOW_TYPE[kn.type.ordinal()], 0, 0, 0, 0, 255, 255);
                    if (kn.type.hasFields() && kn.category != null) {
                        g.pose().translate(32.0f, 32.0f, 1.0f);
                        g.pose().scale(0.75f, 0.75f, 0.75f);
                        UtilsFX.blit(g, kn.category.icon, 0, 0, 0, 0, 255, 255);
                    }
                    g.pose().popPose();
                    String am = (hasKnow[idx] ? "" : ChatFormatting.RED) + "" + kn.amount;
                    g.pose().pushPose();
                    g.pose().translate(kx + 16 - font.width(am) / 2.0f, y + 12, 5.0f);
                    g.pose().scale(0.5f, 0.5f, 0.5f);
                    g.drawString(font, am, 0, 0, 0xFFFFFF, true);
                    g.pose().popPose();
                    if (hasKnow[idx]) drawCheck(g, kx, y);
                    String s = I18n.get("tc.type." + kn.type.name().toLowerCase());
                    if (kn.type.hasFields() && kn.category != null) s = s + ": " + ResearchCategories.getCategoryName(kn.category.key).getString();
                    drawPopupAt(kx, y, mx, my, s);
                    shift += ss;
                }
            }
        }
        if (hasNoRequirements(stage)) b = true;
        if (b) {
            y -= 12;
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            UtilsFX.blit(g, tex1, x + 4, y - 2, 24, 184, 96, 8);
            if (hasAllRequisites) {
                hrx = x + 20;
                hry = y - 6;
                if (hold) {
                    String s2 = I18n.get("tc.stage.hold");
                    g.drawString(font, s2, (int) (x + 52 - font.width(s2) / 2.0f), y - 4, 0xFFFFFF, true);
                } else {
                    if (mouseInside(hrx, hry, 64, 12, mx, my)) {
                        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    } else {
                        g.setColor(0.8f, 0.8f, 0.9f, 1.0f);
                    }
                    UtilsFX.blit(g, tex1, hrx, hry, 84, 216, 64, 12);
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    String s2 = I18n.get("tc.stage.complete");
                    g.drawString(font, s2, (int) (x + 52 - font.width(s2) / 2.0f), y - 4, 0xFFFFFF, true);
                }
            }
        }
    }

    private void drawRequirementStacks(GuiGraphics g, int x, int y, int mx, int my, Object[] reqs, boolean[] has) {
        int shift = 24;
        int ss = reqs.length > 6 ? 110 / reqs.length : 18;
        for (int idx = 0; idx < reqs.length; ++idx) {
            drawStackAt(g, displayStack(reqs[idx], idx), x - 15 + shift, y, mx, my, true, true);
            if (has[idx]) drawCheck(g, x - 15 + shift, y);
            shift += ss;
        }
    }

    private void drawRecipeBookmarks(GuiGraphics g, int x, int mx, int my) {
        Random rng = new Random(rhash);
        int y = (height - paneHeight) / 2 - 8;
        allowWithPagePopup = true;
        if (!recipeOutputs.isEmpty()) {
            int space = Math.min(25, 200 / recipeOutputs.size());
            for (ResourceLocation rk : recipeOutputs.keySet()) {
                List<Object> list = recipeOutputs.get(rk);
                if (list == null || list.isEmpty()) continue;
                int i = Math.floorMod(cycle, list.size());
                if (list.get(i) == null) continue;
                int sh = rng.nextInt(3);
                int le = rng.nextInt(3) + (mouseInside(x + 280, y - 1, 30, 16, mx, my) ? 0 : 3);
                if (rk.equals(shownRecipe)) {
                    g.setColor(1.0f, 0.5f, 0.5f, 1.0f);
                } else {
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
                UtilsFX.blit(g, tex1, x + 280 + sh, y - 1, 120 + le, 232, 28, 16);
                UtilsFX.blit(g, tex1, x + 280 + sh, y - 1, 116, 232, 4, 16);
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                if (list.get(i) instanceof ItemStack stack) {
                    drawStackAt(g, stack, x + 287 + sh - le, y - 1, mx, my, false, false);
                } else if (list.get(i) instanceof Part[][][] parts) {
                    BlueprintBlockAccess ba = blockAccessIcons.computeIfAbsent(rk, k -> new BlueprintBlockAccess(parts, true));
                    renderBluePrint(g, ba, x + 295 + sh - le, y + 6 + parts.length, 4.0f, parts, -5000, -5000, null);
                }
                y += space;
            }
        }
        allowWithPagePopup = false;
    }

    private void generateRecipesLists(ResearchStage stage, ResearchAddendum[] addenda) {
        recipeLists.clear();
        recipeOutputs.clear();
        if (stage == null || stage.getRecipes() == null) return;
        for (ResourceLocation rk : stage.getRecipes()) addRecipesToList(rk, recipeLists, recipeOutputs, rk);
        if (addenda == null) return;
        for (ResearchAddendum addendum : addenda) {
            if (addendum.getRecipes() != null && ThaumcraftCapabilities.knowsResearchStrict(player(), addendum.getResearch())) {
                for (ResourceLocation rk2 : addendum.getRecipes()) addRecipesToList(rk2, recipeLists, recipeOutputs, rk2);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addRecipesToList(ResourceLocation rk, LinkedHashMap<ResourceLocation, List<Object>> recipeLists2,
                                  LinkedHashMap<ResourceLocation, List<Object>> recipeOutputs2, ResourceLocation rkey) {
        Object recipe = findRecipe(rk);
        if (recipe == null) return;
        if (recipe instanceof List<?> group) {
            for (Object rl : group) addRecipesToList((ResourceLocation) rl, recipeLists2, recipeOutputs2, rk);
            return;
        }
        List<Object> list = recipeLists2.computeIfAbsent(rkey, k -> new ArrayList<>());
        List<Object> outputs = recipeOutputs2.computeIfAbsent(rkey, k -> new ArrayList<>());
        Player player = player();
        if (recipe instanceof ThaumcraftApi.BluePrint r) {
            if (ThaumcraftCapabilities.knowsResearchStrict(player, r.getResearch())) {
                list.add(r);
                outputs.add(r.getDisplayStack() != null ? r.getDisplayStack() : r.getParts());
            }
        } else if (recipe instanceof CrucibleRecipe re) {
            ItemStack is = cycle(re.getCatalyst(), 0);
            if (!is.isEmpty() && ThaumcraftCapabilities.knowsResearchStrict(player, re.getResearch())) {
                list.add(re);
                outputs.add(re.getRecipeOutput());
            }
        } else if (recipe instanceof InfusionRecipe re2) {
            ItemStack is;
            if (re2 instanceof InfusionEnchantmentRecipe) {
                ItemStack in = cycle(re2.getRecipeInput(), 0);
                is = in.isEmpty() ? ItemStack.EMPTY : re2.getRecipeOutput(player, in.copy(), null);
            } else {
                is = re2.getRecipeOutput();
            }
            if (!is.isEmpty() && ThaumcraftCapabilities.knowsResearchStrict(player, re2.research)) {
                list.add(re2);
                outputs.add(is);
            }
        } else if (recipe instanceof IArcaneRecipe re3) {
            ItemStack is = re3.getResultItem(minecraft.level.registryAccess());
            if (!is.isEmpty() && ThaumcraftCapabilities.knowsResearchStrict(player, re3.getResearch())) {
                list.add(re3);
                outputs.add(is);
            }
        } else if (recipe instanceof CraftingRecipe re4) {
            list.add(re4);
            outputs.add(re4.getResultItem(minecraft.level.registryAccess()));
        }
    }

    private void drawRecipe(GuiGraphics g, int mx, int my) {
        int[] xy = beginInsert(g);
        int x = xy[0];
        int y = xy[1];
        List<Object> list = recipeLists.get(shownRecipe);
        if (list == null || list.isEmpty()) list = drilldownLists.get(shownRecipe);
        if (list != null && !list.isEmpty()) {
            hasRecipePages = list.size() > 1;
            recipePageMax = list.size() - 1;
            if (recipePage > recipePageMax) recipePage = recipePageMax;
            Object recipe = list.get(recipePage % list.size());
            if (recipe instanceof IArcaneRecipe r) {
                drawArcaneCraftingPage(g, x + 128, y + 128, mx, my, r);
            } else if (recipe instanceof CraftingRecipe r) {
                drawCraftingPage(g, x + 128, y + 128, mx, my, r);
            } else if (recipe instanceof CrucibleRecipe r) {
                drawCruciblePage(g, x + 128, y + 128, mx, my, r);
            } else if (recipe instanceof InfusionRecipe r) {
                drawInfusionPage(g, x + 128, y + 128, mx, my, r);
            } else if (recipe instanceof ThaumcraftApi.BluePrint r) {
                drawCompoundCraftingPage(g, x + 128, y + 128, mx, my, r);
                renderingCompound = true;
            }
            if (hasRecipePages) {
                float bob = Mth.sin(player().tickCount / 3.0f) * 0.2f + 0.1f;
                if (recipePage > 0) drawTexturedModalRectScaled(g, tex1, x + 40, y + 232, 0, 184, 12, 8, bob);
                if (recipePage < recipePageMax) drawTexturedModalRectScaled(g, tex1, x + 204, y + 232, 12, 184, 12, 8, bob);
            }
        }
        endInsert(g);
    }

    private void drawCompoundCraftingPage(GuiGraphics g, int x, int y, int mx, int my, ThaumcraftApi.BluePrint recipe) {
        if (recipe.getParts() == null) return;
        if (blockAccess == null) blockAccess = new BlueprintBlockAccess(recipe.getParts(), false);
        int ySize = recipe.getParts().length;
        int xSize = recipe.getParts()[0].length;
        int zSize = recipe.getParts()[0][0].length;
        String text = I18n.get("recipe.type.construct");
        g.drawString(font, text, x - font.width(text) / 2, y - 104, 0x505050, false);
        int s = Math.max(Math.max(xSize, zSize), ySize) * 2;
        float scale = 38 - s;
        renderBluePrint(g, blockAccess, x, y, scale, recipe.getParts(), mx, my, recipe.getIngredientList());
        g.setColor(1.0f, 1.0f, 1.0f, mouseInside(x + 80, y + 100, 8, 8, mx, my) ? 1.0f : 0.75f);
        UtilsFX.blit(g, tex1, x + 80, y + 100, cycleMultiblockLines ? 168 : 160, 224, 8, 8);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderBluePrint(GuiGraphics g, BlueprintBlockAccess ba, int x, int y, float scale, Part[][][] blueprint, int mx, int my, ItemStack[] ingredients) {
        BlockRenderDispatcher blockRender = minecraft.getBlockRenderer();
        int ySize = blueprint.length;
        int xSize = blueprint[0].length;
        int zSize = blueprint[0][0].length;
        transX = x - xSize / 2;
        transY = y - (float) Math.sqrt(ySize * ySize + xSize * xSize + zSize * zSize) / 2.0f;
        g.pose().pushPose();
        // Flattened in depth as in the original, just over the paper.
        g.pose().translate(transX, transY, 100.0f + Math.max(ySize, Math.max(xSize, zSize)));
        g.pose().scale(scale, -scale, 1.0f);
        g.pose().mulPose(Axis.XP.rotationDegrees(rotX));
        g.pose().mulPose(Axis.YP.rotationDegrees(rotY));
        g.pose().translate(zSize / -2.0f, ySize / -2.0f, xSize / -2.0f);
        MultiBufferSource.BufferSource buffers = g.bufferSource();
        for (int h = 0; h < ySize; ++h) {
            for (int l = 0; l < xSize; ++l) {
                for (int w = 0; w < zSize; ++w) {
                    BlockPos pos = new BlockPos(l, h, w);
                    BlockState state = ba.getBlockState(pos);
                    if (state.isAir()) continue;
                    g.pose().pushPose();
                    g.pose().translate(l, h, w);
                    try {
                        if (state.getRenderShape() == RenderShape.MODEL) {
                            blockRender.renderSingleBlock(state, g.pose(), buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
                        }
                        if (state.getBlock() instanceof EntityBlock eb) {
                            BlockEntity te = eb.newBlockEntity(pos, state);
                            if (te != null) {
                                te.setLevel(minecraft.level);
                                BlockEntityRenderer<BlockEntity> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(te);
                                if (renderer != null) {
                                    renderer.render(te, minecraft.getTimer().getGameTimeDeltaPartialTick(false), g.pose(), buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    g.pose().popPose();
                }
            }
        }
        g.flush();
        g.pose().popPose();
        if (ingredients != null) {
            for (int a = 0; a < ingredients.length; ++a) {
                if (ingredients[a] != null && !ingredients[a].isEmpty()) {
                    drawStackAt(g, ingredients[a], x - 85 + a * 17, y + 90, mx, my, true, true);
                }
            }
        }
    }

    private void drawAspectsInsert(GuiGraphics g, int mx, int my) {
        int[] xy = beginInsert(g);
        drawAspectPage(g, xy[0] + 60, xy[1] + 24, mx, my);
        endInsert(g);
    }

    private void drawAspectPage(GuiGraphics g, int x, int y, int mx, int my) {
        if (knownPlayerAspects == null || knownPlayerAspects.size() <= 0) return;
        int count = -1;
        int start = aspectsPage * 5;
        for (Aspect aspect : knownPlayerAspects.getAspectsSortedByName()) {
            if (++count < start) continue;
            if (count > start + 4) break;
            if (aspect.getImage() == null) continue;
            int ty = y + count % 5 * 40;
            if (mx >= x && my >= ty && mx < x + 40 && my < ty + 40) {
                g.pose().pushPose();
                g.pose().translate(x - 2, ty - 2, 0.0f);
                g.pose().scale(2.0f, 2.0f, 1.0f);
                g.setColor(1.0f, 1.0f, 1.0f, 0.5f);
                UtilsFX.drawTexturedQuadFull(g, tex3, 0.0f, 0.0f);
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                g.pose().popPose();
            }
            g.pose().pushPose();
            g.pose().translate(x + 2, ty + 2, 0.0f);
            g.pose().scale(1.5f, 1.5f, 1.5f);
            UtilsFX.drawTag(g, 0, 0, aspect, 0.0f, 0);
            g.pose().popPose();
            drawSmallCentered(g, aspect.getName(), x + 16, ty + 29);
            if (aspect.getComponents() != null) {
                for (int c = 0; c < 2; ++c) {
                    Aspect comp = aspect.getComponents()[c];
                    boolean known = ThaumcraftCapabilities.getAspects(player()).isKnowAspect(comp);
                    g.pose().pushPose();
                    g.pose().translate(x + (c == 0 ? 60 : 102), ty + 4, 0.0f);
                    g.pose().scale(1.25f, 1.25f, 1.25f);
                    if (known) {
                        UtilsFX.drawTag(g, 0, 0, comp, 0.0f, 0);
                    } else {
                        g.setColor(0.8f, 0.8f, 0.8f, 1.0f);
                        UtilsFX.drawTexturedQuadFull(g, dummyResearch, 0.0f, 0.0f);
                        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    }
                    g.pose().popPose();
                    if (known) drawSmallCentered(g, comp.getName(), x + 22 + (c == 0 ? 50 : 92), ty + 29);
                }
                g.drawString(font, "=", x + 9 + 32, ty + 12, 0x999999, false);
                g.drawString(font, "+", x + 10 + 79, ty + 12, 0x999999, false);
            } else {
                g.drawString(font, I18n.get("tc.aspect.primal"), x + 54, ty + 12, 0x777777, false);
            }
        }
        float bob = Mth.sin(player().tickCount / 3.0f) * 0.2f + 0.1f;
        if (aspectsPage > 0) drawTexturedModalRectScaled(g, tex1, x - 20, y + 208, 0, 184, 12, 8, bob);
        if (aspectsPage < maxAspectPages - 1) drawTexturedModalRectScaled(g, tex1, x + 144, y + 208, 12, 184, 12, 8, bob);
    }

    private void drawSmallCentered(GuiGraphics g, String text, int x, int y) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(0.5f, 0.5f, 0.5f);
        g.drawString(font, text, -font.width(text) / 2, 0, 0x505050, false);
        g.pose().popPose();
    }

    private void drawPageTitle(GuiGraphics g, String key, int x, int y) {
        String text = I18n.get(key);
        g.drawString(font, text, x - font.width(text) / 2, y - 104, 0x505050, false);
    }

    private void drawArcaneCraftingPage(GuiGraphics g, int x, int y, int mx, int my, IArcaneRecipe recipe) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(2.0f, 2.0f, 1.0f);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        UtilsFX.blit(g, tex2, -26, -26, 112, 15, 52, 52);
        UtilsFX.blit(g, tex2, -8, -46, 20, 3, 16, 16);
        g.setColor(1.0f, 1.0f, 1.0f, 0.4f);
        UtilsFX.blit(g, tex2, -6, 40, 68, 76, 12, 12);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        g.pose().popPose();
        String text = "" + recipe.getVis();
        int offset = font.width(text);
        g.drawString(font, text, x - offset / 2, y + 90, 0x505050, false);
        drawPopupAt(x - offset / 2 - 15, y + 75, 30, 30, mx, my, "wandtable.text1");
        drawStackAt(g, recipe.getResultItem(minecraft.level.registryAccess()), x - 8, y - 84, mx, my, false, true);
        AspectList crystals = recipe.getCrystals();
        if (crystals != null) {
            int a = 0;
            int sz = crystals.size();
            for (Aspect aspect : crystals.getAspects()) {
                ItemStack crystal = ConfigItems.crystalEssence.get().makeCrystal(aspect, crystals.getAmount(aspect));
                drawStackAt(g, crystal, x + 4 - sz * 10 + a * 20, y + 59, mx, my, true, true);
                ++a;
            }
        }
        if (recipe instanceof ShapedArcaneRecipe shaped && shaped.getCraftingRecipe() instanceof ShapedRecipe sr) {
            drawPageTitle(g, "recipe.type.arcane", x, y);
            drawShapedGrid(g, x, y, mx, my, sr.getWidth(), sr.getHeight(), recipe.getIngredients());
        }
        if (recipe instanceof ShapelessArcaneRecipe) {
            drawPageTitle(g, "recipe.type.arcane.shapeless", x, y);
            drawShapelessGrid(g, x, y, mx, my, recipe.getIngredients());
        }
    }

    private void drawShapedGrid(GuiGraphics g, int x, int y, int mx, int my, int rw, int rh, List<Ingredient> items) {
        for (int i = 0; i < rw && i < 3; ++i) {
            for (int j = 0; j < rh && j < 3; ++j) {
                Ingredient ing = items.get(i + j * rw);
                if (ing != null) drawStackAt(g, cycle(ing, i + j * rw), x - 40 + i * 32, y - 40 + j * 32, mx, my, true, true);
            }
        }
    }

    private void drawShapelessGrid(GuiGraphics g, int x, int y, int mx, int my, List<Ingredient> items) {
        for (int k = 0; k < items.size() && k < 9; ++k) {
            if (items.get(k) != null) drawStackAt(g, cycle(items.get(k), k), x - 40 + k % 3 * 32, y - 40 + k / 3 * 32, mx, my, true, true);
        }
    }

    private void drawCraftingPage(GuiGraphics g, int x, int y, int mx, int my, CraftingRecipe recipe) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(2.0f, 2.0f, 1.0f);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        UtilsFX.blit(g, tex2, -26, -26, 60, 15, 51, 52);
        UtilsFX.blit(g, tex2, -8, -46, 20, 3, 16, 16);
        g.pose().popPose();
        drawStackAt(g, recipe.getResultItem(minecraft.level.registryAccess()), x - 8, y - 84, mx, my, false, true);
        if (recipe instanceof ShapedRecipe sr) {
            drawPageTitle(g, "recipe.type.workbench", x, y);
            drawShapedGrid(g, x, y, mx, my, sr.getWidth(), sr.getHeight(), recipe.getIngredients());
        } else {
            drawPageTitle(g, "recipe.type.workbenchshapeless", x, y);
            drawShapelessGrid(g, x, y, mx, my, recipe.getIngredients());
        }
    }

    /** The aspects of a crucible or infusion recipe in rows of "perRow", the last row centered. */
    private void drawAspectRows(GuiGraphics g, AspectList aspects, int perRow, int sx, int sy0, int mx, int my) {
        int total = 0;
        int rows = (aspects.size() - 1) / perRow;
        int shift = (perRow - aspects.size() % perRow) * 10;
        int sy = sy0 - 10 * rows;
        for (Aspect tag : aspects.getAspectsSortedByName()) {
            int m = total / perRow >= rows && (rows > 1 || aspects.size() < perRow) ? 1 : 0;
            int vx = sx + total % perRow * 20 + shift * m;
            int vy = sy + total / perRow * 20;
            UtilsFX.drawTag(g, vx, vy, tag, aspects.getAmount(tag), 0);
            if (mx >= vx && my >= vy && mx < vx + 16 && my < vy + 16) {
                tipText = Arrays.asList(tag.getName(), tag.getLocalizedDescription().getString());
            }
            ++total;
        }
    }

    private void drawCruciblePage(GuiGraphics g, int x, int y, int mx, int my, CrucibleRecipe rc) {
        drawPageTitle(g, "recipe.type.crucible", x, y);
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(2.0f, 2.0f, 1.0f);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        UtilsFX.blit(g, tex2, -28, -29, 0, 3, 56, 17);
        g.pose().translate(0.0f, 32.0f, 0.0f);
        UtilsFX.blit(g, tex2, -28, -44, 0, 20, 56, 48);
        g.pose().translate(0.0f, -8.0f, 0.0f);
        UtilsFX.blit(g, tex2, -25, -50, 100, 84, 11, 13);
        g.pose().popPose();
        drawAspectRows(g, rc.getAspects(), 3, x - 28, y + 8, mx, my);
        drawStackAt(g, rc.getRecipeOutput(), x - 8, y - 50, mx, my, false, true);
        drawStackAt(g, cycle(rc.getCatalyst(), 0), x - 64, y - 56, mx, my, true, true);
    }

    private void drawInfusionPage(GuiGraphics g, int x, int y, int mx, int my, InfusionRecipe ri) {
        List<Ingredient> components = ri.getComponents();
        AspectList aspects = ri.getAspects();
        ItemStack output = ri.getRecipeOutput();
        ItemStack firstInput = cycle(ri.getRecipeInput(), 0);
        if (ri instanceof InfusionEnchantmentRecipe && !firstInput.isEmpty()) {
            List<ItemStack> com2 = new ArrayList<>();
            aspects = ri.getAspects(player(), firstInput, com2);
            output = ri.getRecipeOutput(null, firstInput, com2);
        }
        drawPageTitle(g, "recipe.type.infusion", x, y);
        g.pose().pushPose();
        g.pose().translate(x, y + 20, 0.0f);
        g.pose().scale(2.0f, 2.0f, 1.0f);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        UtilsFX.blit(g, tex2, -28, -56, 0, 3, 56, 17);
        g.pose().translate(0.0f, 19.0f, 0.0f);
        UtilsFX.blit(g, tex2, -28, -55, 200, 77, 60, 44);
        g.pose().popPose();
        drawAspectRows(g, aspects, 5, x - 48, y + 50, mx, my);
        drawStackAt(g, output, x - 8, y - 85, mx, my, false, true);
        ItemStack rinp = cycle(ri.getRecipeInput(), 0).copy();
        drawStackAt(g, rinp, x - 8, y - 16, mx, my, true, true);
        int le = components.size();
        List<ItemStack> cmps = new ArrayList<>();
        if (le > 0) {
            float pieSlice = 360 / le;
            float currentRot = -90.0f;
            int total = 0;
            for (Ingredient ingredient : components) {
                int vx = x + (int) (Mth.cos(currentRot / 180.0f * Mth.PI) * 40.0f) - 8;
                int vy = y - 8 + (int) (Mth.sin(currentRot / 180.0f * Mth.PI) * 40.0f) - 8;
                currentRot += pieSlice;
                ItemStack is = cycle(ingredient, total);
                drawStackAt(g, is.copyWithCount(1), vx, vy, mx, my, true, true);
                cmps.add(is.copy());
                ++total;
            }
        }
        int inst = Math.min(5, ri.getInstability(player(), rinp, cmps) / 2);
        String text = I18n.get("tc.inst") + " " + I18n.get("tc.inst." + inst);
        g.drawString(font, text, x - font.width(text) / 2, y + 94, 0x505050, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            history.clear();
            if (shownRecipe != null || showingAspects || showingKnowledge) {
                shownRecipe = null;
                blockAccess = null;
                showingAspects = false;
                showingKnowledge = false;
                playSound(SoundsTC.PAGE.get(), 0.4f, 1.1f);
            } else {
                minecraft.setScreen(new GuiResearchBrowser(guiMapX, guiMapY));
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            prevPage();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            nextPage();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            goBack();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void nextPage() {
        if (page < maxPages - 2) {
            page += 2;
            lastCycle = 0L;
            cycle = -1;
            playSound(SoundsTC.PAGE.get(), 0.66f, 1.0f);
        }
    }

    private void prevPage() {
        if (page >= 2) {
            page -= 2;
            lastCycle = 0L;
            cycle = -1;
            playSound(SoundsTC.PAGE.get(), 0.66f, 1.0f);
        }
    }

    private void goBack() {
        if (!history.isEmpty()) {
            playSound(SoundsTC.PAGE.get(), 0.66f, 1.0f);
            shownRecipe = history.pop();
            blockAccess = null;
        } else {
            shownRecipe = null;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int par1 = (int) mouseX;
        int par2 = (int) mouseY;
        checkRequisites();
        int var4 = (width - paneWidth) / 2;
        int var5 = (height - paneHeight) / 2;
        int mx = par1 - hrx;
        int my = par2 - hry;
        if (shownRecipe == null && !hold && hasAllRequisites && mx >= 0 && my >= 0 && mx < 64 && my < 12) {
            PacketDistributor.sendToServer(new PacketSyncProgressToServer(research.getKey(), false, true, true));
            playSound(SoundsTC.WRITE.get(), 0.66f, 1.0f);
            lastCheck = 0L;
            lastStage = currentStage;
            hold = true;
            keyCache.clear();
            drilldownLists.clear();
        }
        for (Map.Entry<int[], String> e : notePoints.entrySet()) {
            int[] p = e.getKey();
            if (shownRecipe == null && par1 >= p[0] && par1 <= p[0] + 16 && par2 >= p[1] && par2 <= p[1] + 16) {
                // Reborn hookMouseClicked: without tools, paper or ink the book closes with a chat message.
                if (!ResearchNoteManager.playerHasInk(player(), true)) {
                    onClose();
                } else {
                    PacketDistributor.sendToServer(new PacketGivePlayerNoteToServer(e.getValue()));
                    playSound(SoundsTC.WRITE.get(), 0.66f, 1.0f);
                }
                return true;
            }
        }
        if (knownPlayerAspects != null && playerKnowledge.isResearchComplete("FIRSTSTEPS")) {
            mx = par1 - (var4 - 48);
            my = par2 - (var5 + 8);
            if (mx >= 0 && my >= 0 && mx < 25 && my < 16) {
                shownRecipe = null;
                showingKnowledge = false;
                showingAspects = !showingAspects;
                blockAccess = null;
                history.clear();
                if (aspectsPage > maxAspectPages) aspectsPage = 0;
                playSound(SoundsTC.PAGE.get(), 0.7f, 0.9f);
            }
        }
        if (knowledgeTabShown() && !research.getKey().equals("KNOWLEDGETYPES")) {
            mx = par1 - (var4 - 48);
            my = par2 - (var5 + 31);
            if (mx >= 0 && my >= 0 && mx < 25 && my < 16) {
                shownRecipe = null;
                showingAspects = false;
                showingKnowledge = !showingKnowledge;
                blockAccess = null;
                history.clear();
                playSound(SoundsTC.PAGE.get(), 0.7f, 0.9f);
            }
        }
        mx = par1 - (var4 + 205);
        my = par2 - (var5 + 192);
        if (showingAspects && aspectsPage < maxAspectPages - 1 && mx >= 0 && my >= 0 && mx < 14 && my < 14) {
            ++aspectsPage;
            playSound(SoundsTC.PAGE.get(), 0.7f, 0.9f);
        }
        mx = par1 - (var4 + 38);
        my = par2 - (var5 + 192);
        if (showingAspects && aspectsPage > 0 && mx >= 0 && my >= 0 && mx < 14 && my < 14) {
            --aspectsPage;
            playSound(SoundsTC.PAGE.get(), 0.7f, 0.9f);
        }
        if (!recipeLists.isEmpty()) {
            int aa = 0;
            int space = Math.min(25, 200 / recipeLists.size());
            for (ResourceLocation rk : recipeLists.keySet()) {
                mx = par1 - (var4 + 280);
                my = par2 - (var5 - 8 + aa * space);
                if (mx >= 0 && my >= 0 && mx < 30 && my < 16) {
                    shownRecipe = rk.equals(shownRecipe) ? null : rk;
                    showingAspects = false;
                    showingKnowledge = false;
                    blockAccess = null;
                    history.clear();
                    playSound(SoundsTC.PAGE.get(), 0.7f, 0.9f);
                    break;
                }
                ++aa;
            }
        }
        mx = par1 - (var4 + 205);
        my = par2 - (var5 + 192);
        if (hasRecipePages && recipePage < recipePageMax && mx >= 0 && my >= 0 && mx < 14 && my < 14) {
            ++recipePage;
            playSound(SoundsTC.PAGE.get(), 0.7f, 0.9f);
        }
        mx = par1 - (var4 + 38);
        my = par2 - (var5 + 192);
        if (hasRecipePages && recipePage > 0 && mx >= 0 && my >= 0 && mx < 14 && my < 14) {
            --recipePage;
            playSound(SoundsTC.PAGE.get(), 0.7f, 0.9f);
        }
        mx = par1 - (var4 + 261);
        my = par2 - (var5 + 189);
        if (shownRecipe == null && mx >= 0 && my >= 0 && mx < 14 && my < 10) nextPage();
        mx = par1 - (var4 - 17);
        my = par2 - (var5 + 189);
        if (shownRecipe == null && mx >= 0 && my >= 0 && mx < 14 && my < 10) prevPage();
        mx = par1 - (var4 + 118);
        my = par2 - (var5 + 190);
        if (mx >= 0 && my >= 0 && mx < 20 && my < 12) goBack();
        mx = par1 - (var4 + 210);
        my = par2 - (var5 + 190);
        if (renderingCompound && mx >= 0 && my >= 0 && mx < 10 && my < 10) {
            playSound(SoundsTC.CLACK.get(), 0.66f, 1.0f);
            cycleMultiblockLines = !cycleMultiblockLines;
        }
        for (Reference ref : reference) {
            if (par1 >= ref.x() && par2 >= ref.y() && par1 < ref.x() + 16 && par2 < ref.y() + 16) {
                playSound(SoundsTC.PAGE.get(), 0.66f, 1.0f);
                if (shownRecipe != null) history.push(shownRecipe);
                shownRecipe = ref.recipe();
                recipePage = ref.page();
                if (!drilldownLists.containsKey(shownRecipe)) {
                    addRecipesToList(shownRecipe, drilldownLists, new LinkedHashMap<>(), shownRecipe);
                }
                blockAccess = null;
                break;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Reborn: theorycraft knowledge is gone, so the knowledge tab never shows. */
    private boolean knowledgeTabShown() {
        return false;
    }

    /** The research a stage asks for, without the research notes (those get their own row). */
    private static boolean hasPlainResearch(ResearchStage stage) {
        return stage.getResearch() != null && Arrays.stream(stage.getResearch()).anyMatch(k -> !k.startsWith("rn_"));
    }

    private static boolean hasNotes(ResearchStage stage) {
        return stage.getResearch() != null && Arrays.stream(stage.getResearch()).anyMatch(k -> k.startsWith("rn_"));
    }

    /** A stage with nothing to meet (its knowledge was removed) still needs the "complete" button. */
    private static boolean hasNoRequirements(ResearchStage stage) {
        return stage.getResearch() == null && stage.getObtain() == null && stage.getCraft() == null;
    }

    void drawPopupAt(int x, int y, int mx, int my, String text) {
        drawPopupAt(x, y, 16, 16, mx, my, text);
    }

    void drawPopupAt(int x, int y, int w, int h, int mx, int my, String text) {
        if ((shownRecipe == null || allowWithPagePopup) && mx >= x && my >= y && mx < x + w && my < y + h) {
            ArrayList<String> s = new ArrayList<>();
            s.add(I18n.get(text));
            tipText = s;
        }
    }

    boolean mouseInside(int x, int y, int w, int h, int mx, int my) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    /** UtilsFX.renderItemStack and the item tooltip; clickthrough stacks link to the recipe that makes them. */
    void drawStackAt(GuiGraphics g, ItemStack itemstack, int x, int y, int mx, int my, boolean clickthrough, boolean overlay) {
        if (itemstack == null || itemstack.isEmpty()) return;
        g.renderItem(itemstack, x, y);
        if (overlay) g.renderItemDecorations(font, itemstack, x, y);
        if ((shownRecipe == null || allowWithPagePopup) && mx >= x && my >= y && mx < x + 16 && my < y + 16) {
            List<String> addtext = new ArrayList<>();
            for (Component c : getTooltipFromItem(minecraft, itemstack)) addtext.add(c.getString());
            if (clickthrough) {
                String ref = getCraftingRecipeKey(itemstack);
                if (ref != null) {
                    String[] sr = ref.split(";", 2);
                    if (sr.length > 1) {
                        addtext.add(ChatFormatting.BLUE + "" + ChatFormatting.ITALIC + I18n.get("recipe.clickthrough"));
                        reference.add(new Reference(x, y, ResourceLocation.parse(sr[0]), Integer.parseInt(sr[1])));
                    } else {
                        // The original meant to say this but its "UNKNOWN" key was lowercased and never matched.
                        addtext.add(ChatFormatting.DARK_RED + "" + ChatFormatting.ITALIC + I18n.get("recipe.unknown"));
                    }
                }
            }
            tipText = addtext;
        }
    }

    public void drawTexturedModalRectScaled(GuiGraphics g, ResourceLocation tex, int par1, int par2, int par3, int par4, int par5, int par6, float scale) {
        g.pose().pushPose();
        g.pose().translate(par1 + par5 / 2.0f, par2 + par6 / 2.0f, 0.0f);
        g.pose().scale(1.0f + scale, 1.0f + scale, 1.0f);
        UtilsFX.quad(g, tex, -par5 / 2.0f, -par6 / 2.0f, par5 / 2.0f, par6 / 2.0f,
                par3 / 256.0f, (par3 + par5) / 256.0f, par4 / 256.0f, (par4 + par6) / 256.0f);
        g.pose().popPose();
    }

    private void parsePages() {
        checkRequisites();
        pages.clear();
        if (research.getStages() == null) return;
        boolean complete = false;
        currentStage = playerKnowledge.getResearchStage(research.getKey()) - 1;
        while (currentStage >= research.getStages().length) {
            --currentStage;
            complete = true;
        }
        if (currentStage < 0) currentStage = 0;
        ResearchStage stage = research.getStages()[currentStage];
        ResearchAddendum[] addenda = null;
        if (research.getAddenda() != null && complete) addenda = research.getAddenda();
        generateRecipesLists(stage, addenda);
        String rawText = stage.getTextLocalized().getString();
        if (addenda != null) {
            int ac = 0;
            for (ResearchAddendum addendum : addenda) {
                if (ThaumcraftCapabilities.knowsResearchStrict(player(), addendum.getResearch())) {
                    ++ac;
                    rawText = rawText + "<PAGE>" + Component.translatable("tc.addendumtext", ac).getString() + "<BR>" + addendum.getTextLocalized().getString();
                }
            }
        }
        rawText = rawText.replaceAll("<BR>", "~B\n\n");
        rawText = rawText.replaceAll("<BR/>", "~B\n\n");
        rawText = rawText.replaceAll("<LINE>", "~L");
        rawText = rawText.replaceAll("<LINE/>", "~L");
        rawText = rawText.replaceAll("<DIV>", "~D");
        rawText = rawText.replaceAll("<DIV/>", "~D");
        rawText = rawText.replaceAll("<PAGE>", "~P");
        rawText = rawText.replaceAll("<PAGE/>", "~P");
        ArrayList<PageImage> images = new ArrayList<>();
        for (String s : rawText.split("<IMG>")) {
            int i = s.indexOf("</IMG>");
            if (i >= 0) {
                String clean = s.substring(0, i);
                PageImage pi = PageImage.parse(clean);
                if (pi == null) {
                    rawText = rawText.replaceFirst(Pattern.quote(clean), "\n");
                } else {
                    images.add(pi);
                    rawText = rawText.replaceFirst(Pattern.quote(clean), "~I");
                }
            }
        }
        rawText = rawText.replaceAll("<IMG>", "");
        rawText = rawText.replaceAll("</IMG>", "");
        List<String> firstPassText = new ArrayList<>();
        String[] temp = rawText.split("~P");
        for (int a = 0; a < temp.length; ++a) {
            String[] temp2 = temp[a].split("~D");
            for (int x = 0; x < temp2.length; ++x) {
                String[] temp3 = temp2[x].split("~L");
                for (int b = 0; b < temp3.length; ++b) {
                    String[] temp4 = temp3[b].split("~I");
                    for (int c = 0; c < temp4.length; ++c) {
                        firstPassText.add(temp4[c]);
                        if (c != temp4.length - 1) firstPassText.add("~I");
                    }
                    if (b != temp3.length - 1) firstPassText.add("~L");
                }
                if (x != temp2.length - 1) firstPassText.add("~D");
            }
            if (a != temp.length - 1) firstPassText.add("~P");
        }
        List<String> parsedText = new ArrayList<>();
        for (String s2 : firstPassText) parsedText.addAll(UtilsFX.wrap(font, s2, 140));
        int lineHeight = font.lineHeight;
        int heightRemaining = 182;
        int dividerSpace = 0;
        if (research.getKey().equals("KNOWLEDGETYPES")) {
            heightRemaining -= 2;
            int tc = 0;
            for (PlayerKnowledge.EnumKnowledgeType type : PlayerKnowledge.EnumKnowledgeType.values()) {
                for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
                    if (!type.hasFields() && category != null) continue;
                    if (playerKnowledge.getKnowledgeRaw(type, category) > 0) {
                        ++tc;
                        break;
                    }
                }
            }
            heightRemaining -= 20 * tc;
            dividerSpace = 12;
        }
        if (!isComplete) {
            if (stage.getCraft() != null) {
                heightRemaining -= 18;
                dividerSpace = 15;
            }
            if (stage.getObtain() != null) {
                heightRemaining -= 18;
                dividerSpace = 15;
            }
            if (stage.getKnow() != null) {
                heightRemaining -= 18;
                dividerSpace = 15;
            }
            if (hasPlainResearch(stage)) {
                heightRemaining -= 18;
                dividerSpace = 15;
            }
            if (hasNotes(stage)) {
                heightRemaining -= 18;
                dividerSpace = 15;
            }
            if (hasNoRequirements(stage)) dividerSpace = 15;
        }
        heightRemaining -= dividerSpace;
        Page page1 = new Page();
        ArrayList<PageImage> tempImages = new ArrayList<>();
        for (String line : parsedText) {
            if (line.contains("~I")) {
                if (!images.isEmpty()) tempImages.add(images.remove(0));
                line = "";
            }
            if (line.contains("~L")) {
                tempImages.add(PILINE);
                line = "";
            }
            if (line.contains("~D")) {
                tempImages.add(PIDIV);
                line = "";
            }
            if (line.contains("~P")) {
                heightRemaining = 210;
                pages.add(page1.copy());
                page1 = new Page();
                line = "";
            }
            if (!line.isEmpty()) {
                line = line.trim();
                page1.contents.add(line);
                heightRemaining -= lineHeight;
                if (line.endsWith("~B")) heightRemaining -= (int) (lineHeight * 0.66);
            }
            while (!tempImages.isEmpty() && heightRemaining >= tempImages.get(0).ah + 2) {
                heightRemaining -= tempImages.get(0).ah + 2;
                page1.contents.add(tempImages.remove(0));
            }
            if (heightRemaining < lineHeight && !page1.contents.isEmpty()) {
                heightRemaining = 210;
                pages.add(page1.copy());
                page1 = new Page();
            }
        }
        if (!page1.contents.isEmpty()) pages.add(page1.copy());
        page1 = new Page();
        heightRemaining = 210;
        while (!tempImages.isEmpty()) {
            if (heightRemaining < tempImages.get(0).ah + 2) {
                heightRemaining = 210;
                pages.add(page1.copy());
                page1 = new Page();
            } else {
                heightRemaining -= tempImages.get(0).ah + 2;
                page1.contents.add(tempImages.remove(0));
            }
        }
        if (!page1.contents.isEmpty()) pages.add(page1.copy());
        rhash = research.getKey().hashCode() + currentStage * 50;
        maxPages = pages.size();
    }

    private void checkRequisites() {
        if (research.getStages() == null) return;
        Player player = player();
        isComplete = playerKnowledge.isResearchComplete(research.getKey());
        while (currentStage >= research.getStages().length) --currentStage;
        if (currentStage < 0) return;
        hasAllRequisites = true;
        hasItem = null;
        hasCraft = null;
        hasResearch = null;
        hasKnow = null;
        ResearchStage stage = research.getStages()[currentStage];
        Object[] o = stage.getObtain();
        if (o != null) {
            hasItem = new boolean[o.length];
            for (int a = 0; a < o.length; ++a) {
                if (!(hasItem[a] = InventoryUtils.isPlayerCarryingAmount(player, o[a]))) hasAllRequisites = false;
            }
        }
        Object[] c = stage.getCraft();
        if (c != null) {
            hasCraft = new boolean[c.length];
            for (int a = 0; a < c.length; ++a) {
                hasCraft[a] = playerKnowledge.isResearchKnown("[#]" + stage.getCraftReference()[a]);
                if (!hasCraft[a]) hasAllRequisites = false;
            }
        }
        String[] r = stage.getResearch();
        if (r != null) {
            hasResearch = new boolean[r.length];
            for (int a = 0; a < r.length; ++a) {
                hasResearch[a] = ThaumcraftCapabilities.knowsResearchStrict(player, r[a]);
                if (!hasResearch[a]) hasAllRequisites = false;
            }
        }
        ResearchStage.Knowledge[] k = stage.getKnow();
        if (k != null) {
            hasKnow = new boolean[k.length];
            for (int a = 0; a < k.length; ++a) {
                hasKnow[a] = playerKnowledge.getKnowledge(k[a].type, k[a].category) >= k[a].amount;
                if (!hasKnow[a]) hasAllRequisites = false;
            }
        }
    }

    private int findRecipePage(ResourceLocation rk, ItemStack stack, int start) {
        Object recipe = findRecipe(rk);
        if (recipe == null) return -1;
        Player player = player();
        if (recipe instanceof List<?> group) {
            int g = 0;
            for (Object rl : group) {
                int q = findRecipePage((ResourceLocation) rl, stack, g);
                if (q >= 0) return q;
                ++g;
            }
        }
        if (recipe instanceof CrucibleRecipe cr && ItemStack.isSameItem(cr.getRecipeOutput(), stack)) {
            return ThaumcraftCapabilities.knowsResearchStrict(player, cr.getResearch()) ? start : -99;
        } else if (recipe instanceof InfusionRecipe ir && ItemStack.isSameItem(ir.getRecipeOutput(), stack)) {
            return ThaumcraftCapabilities.knowsResearchStrict(player, ir.getResearch()) ? start : -99;
        } else if (recipe instanceof Recipe<?> re && !(recipe instanceof CrucibleRecipe) && !(recipe instanceof InfusionRecipe)
                && ItemStack.isSameItem(re.getResultItem(minecraft.level.registryAccess()), stack)) {
            if (recipe instanceof IArcaneRecipe ar && !ThaumcraftCapabilities.knowsResearchStrict(player, ar.getResearch())) return -99;
            return start;
        }
        return -1;
    }

    private String getCraftingRecipeKey(ItemStack stack) {
        int key = ItemStack.hashItemAndComponents(stack);
        if (keyCache.containsKey(key)) return keyCache.get(key);
        for (ResearchCategory rcl : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : rcl.research.values()) {
                if (ri.getStages() == null) continue;
                for (ResearchStage stage : ri.getStages()) {
                    if (stage.getRecipes() == null) continue;
                    for (ResourceLocation rec : stage.getRecipes()) {
                        int result = findRecipePage(rec, stack, 0);
                        if (result != -1) {
                            String s = result == -99 ? "UNKNOWN" : rec + ";" + result;
                            keyCache.put(key, s);
                            return s;
                        }
                    }
                }
            }
        }
        keyCache.put(key, null);
        return null;
    }

    // ---- Recipe catalog ---------------------------------------------------------------------------------
    // The original looked research recipe names up in the API catalog, the crafting registry and the recipe
    // groups. Here: the API catalog (blueprints, display recipes), then the client's recipe manager by id, by
    // name (recipes now sit in type folders, "arcane/thaumometer" for "thaumometer"), and by recipe group.

    private static final HashMap<ResourceLocation, Optional<Object>> RECIPE_CACHE = new HashMap<>();
    private static RecipeManager cachedManager;

    /** The recipe a research names: a recipe, a blueprint, or a list of recipe ids (a group). Null if none. */
    public static Object findRecipe(ResourceLocation rk) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        RecipeManager rm = mc.level.getRecipeManager();
        if (rm != cachedManager) {
            RECIPE_CACHE.clear();
            cachedManager = rm;
        }
        Optional<Object> cached = RECIPE_CACHE.get(rk);
        if (cached == null) {
            cached = Optional.ofNullable(lookupRecipe(rm, rk, 0));
            RECIPE_CACHE.put(rk, cached);
        }
        return cached.orElse(null);
    }

    @SuppressWarnings("unchecked")
    private static Object lookupRecipe(RecipeManager rm, ResourceLocation rk, int depth) {
        if (depth > 4) return null;
        Object recipe = CommonInternals.getCatalogRecipe(rk);
        if (recipe != null) return recipe;
        Object fake = CommonInternals.getCatalogRecipeFake(rk);
        if (fake instanceof ResourceLocation alias) return lookupRecipe(rm, alias, depth + 1);
        if (fake instanceof Function<?, ?> f) return ((Function<RecipeManager, Object>) f).apply(rm);
        if (fake != null) return fake;
        Optional<RecipeHolder<?>> holder = rm.byKey(rk);
        if (holder.isPresent()) return holder.get().value();
        RecipeHolder<?> byName = null;
        List<ResourceLocation> group = new ArrayList<>();
        for (RecipeHolder<?> h : rm.getRecipes()) {
            ResourceLocation id = h.id();
            if (!id.getNamespace().equals(rk.getNamespace())) continue;
            String path = id.getPath();
            if (byName == null && path.substring(path.lastIndexOf('/') + 1).equals(rk.getPath())) byName = h;
            if (rk.toString().equals(h.value().getGroup())) group.add(id);
        }
        if (byName != null) return byName.value();
        if (!group.isEmpty()) {
            group.sort(null);
            return group;
        }
        return null;
    }

    /** The item a recipe makes, for the search. */
    public static ItemStack recipeOutput(Object recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (recipe instanceof ThaumcraftApi.BluePrint bp) return bp.getDisplayStack() != null ? bp.getDisplayStack() : ItemStack.EMPTY;
        if (recipe instanceof CrucibleRecipe cr) return cr.getRecipeOutput();
        if (recipe instanceof InfusionRecipe ir) return ir.getRecipeOutput();
        if (recipe instanceof Recipe<?> r && mc.level != null) return r.getResultItem(mc.level.registryAccess());
        return ItemStack.EMPTY;
    }

    /** InventoryUtils.cycleItemStack: one of the ingredient's items, changing every second. */
    static ItemStack cycle(Ingredient ingredient, int counter) {
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) return ItemStack.EMPTY;
        int idx = (int) ((System.currentTimeMillis() / 1000L + counter) % items.length);
        return items[idx];
    }

    /** A stage requirement as a stack to show: the item, an item of the tag, or a book with the enchantment. */
    static ItemStack displayStack(Object req, int counter) {
        if (req instanceof ItemStack stack) return stack;
        if (req instanceof String tag) {
            ResourceLocation id = ResourceLocation.tryParse(tag);
            if (id == null) return ItemStack.EMPTY;
            return cycle(Ingredient.of(TagKey.create(Registries.ITEM, id)), counter);
        }
        if (req instanceof ResearchStage.EnchantmentRequirement ench) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return ItemStack.EMPTY;
            Optional<Holder.Reference<Enchantment>> holder = mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(ench.enchantment());
            return holder.map(h -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(h, ench.level()))).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    private static class Page {
        final ArrayList<Object> contents = new ArrayList<>();

        Page copy() {
            Page p = new Page();
            p.contents.addAll(contents);
            return p;
        }
    }

    private static class PageImage {
        int x;
        int y;
        int w;
        int h;
        int aw;
        int ah;
        float scale;
        ResourceLocation loc;

        /** "namespace:path:x:y:w:h:scale", a part of a texture laid out on 256x256. */
        static PageImage parse(String text) {
            String[] s = text.split(":");
            if (s.length != 7) return null;
            try {
                PageImage pi = new PageImage();
                pi.loc = ResourceLocation.fromNamespaceAndPath(s[0], s[1]);
                pi.x = Integer.parseInt(s[2]);
                pi.y = Integer.parseInt(s[3]);
                pi.w = Integer.parseInt(s[4]);
                pi.h = Integer.parseInt(s[5]);
                pi.scale = Float.parseFloat(s[6]);
                pi.aw = (int) (pi.w * pi.scale);
                pi.ah = (int) (pi.h * pi.scale);
                if (pi.ah > 208 || pi.aw > 140) return null;
                return pi;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /** The blocks of a blueprint, layer by layer from the bottom; the top "sliceLine" layers can be hidden. */
    public static class BlueprintBlockAccess {
        private final Part[][][] data;
        private final BlockState[][][] structure;
        public int sliceLine = 0;

        public BlueprintBlockAccess(Part[][][] data, boolean target) {
            this.data = new Part[data.length][data[0].length][data[0][0].length];
            for (int y = 0; y < data.length; ++y) {
                for (int x = 0; x < data[0].length; ++x) {
                    System.arraycopy(data[y][x], 0, this.data[y][x], 0, data[0][0].length);
                }
            }
            structure = new BlockState[data.length][data[0].length][data[0][0].length];
            if (target) {
                for (int y = 0; y < this.data.length; ++y) {
                    Matrix matrix = new Matrix(this.data[y]);
                    matrix.Rotate90DegRight(3);
                    this.data[y] = matrix.getMatrix();
                }
            }
            for (int y = 0; y < data.length; ++y) {
                for (int x = 0; x < data[0].length; ++x) {
                    for (int z = 0; z < data[0][0].length; ++z) {
                        structure[data.length - y - 1][x][z] = target ? convertTarget(x, y, z) : convert(x, y, z);
                    }
                }
            }
        }

        private static BlockState toState(Object o) {
            if (o instanceof ItemStack stack && stack.getItem() instanceof BlockItem bi) return bi.getBlock().defaultBlockState();
            if (o instanceof Block block) return block.defaultBlockState();
            if (o instanceof BlockState state) return state;
            if (o instanceof net.minecraft.world.level.material.Fluid fluid) return fluid.defaultFluidState().createLegacyBlock();
            return Blocks.AIR.defaultBlockState();
        }

        private BlockState convert(int x, int y, int z) {
            Part p = data[y][x][z];
            if (p == null || p.getSource() == null) return Blocks.AIR.defaultBlockState();
            return toState(p.getSource());
        }

        private BlockState convertTarget(int x, int y, int z) {
            Part p = data[y][x][z];
            if (p == null) return Blocks.AIR.defaultBlockState();
            if (p.getTarget() == null) return convert(x, y, z);
            return toState(p.getTarget());
        }

        public BlockState getBlockState(BlockPos pos) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            if (sliceLine > structure.length) sliceLine = 0;
            if (y >= 0 && y < structure.length - sliceLine && x >= 0 && x < structure[y].length && z >= 0 && z < structure[y][x].length) {
                return structure[y][x][z];
            }
            return Blocks.AIR.defaultBlockState();
        }
    }
}
