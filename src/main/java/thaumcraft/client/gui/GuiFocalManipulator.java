package thaumcraft.client.gui;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusMedium;
import thaumcraft.api.casters.FocusMediumRoot;
import thaumcraft.api.casters.FocusModSplit;
import thaumcraft.api.casters.FocusNode;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.container.ContainerFocalManipulator;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.playerdata.PacketFocusNameToServer;
import thaumcraft.common.lib.network.playerdata.PacketFocusNodesToServer;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.crafting.FocusElementNode;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;

/**
 * Port of GuiFocalManipulator. The parts list on the left, the node tree in the middle (drag to scroll), the
 * costs and the crystals on the right and the settings of the selected node under them.
 * 1.21: the original plugin buttons (GuiImageButton, GuiHoverButton, GuiSliderTC, GuiFocusSettingSpinnerButton)
 * are small inner classes with the same drawing and click rules. Nodes are added by element key instead of class.
 */
public class GuiFocalManipulator extends AbstractContainerScreen<ContainerFocalManipulator> {
    private static final ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_wandtable.png");
    private static final ResourceLocation tex2 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_wandtable2.png");
    private static final ResourceLocation tex3 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_wandtable3.png");
    private static final ResourceLocation texbase = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/gui_base.png");
    static final ResourceLocation iMedium = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/foci/_medium.png");
    static final ResourceLocation iEffect = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/foci/_effect.png");
    private static final DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");

    private final TileFocalManipulator table;
    private int isMouseButtonDown = 0;
    protected int mouseX = 0;
    protected int mouseY = 0;
    protected double curMouseX;
    protected double curMouseY;
    ImageButton buttonConfirm;
    Slider scrollbarParts;
    Slider scrollbarMainSide;
    Slider scrollbarMainBottom;
    private EditBox nameField;
    private final List<Widget> buttonList = new ArrayList<>();
    private Widget selectedButton;
    int totalComplexity = 0;
    int maxComplexity = 0;
    int lastNodeHover = -1;
    DecimalFormat myFormatter = new DecimalFormat("#######.##");
    ArrayList<String> shownParts = new ArrayList<>();
    int partsStart = 0;
    ItemStack[] components = null;
    boolean valid = false;
    private int nodeID = 0;
    int sMinX = 0;
    int sMinY = 0;
    int sMaxX = 0;
    int sMaxY = 0;
    int selectedNode = -1;
    float costCast = 0.0f;
    int costXp = 0;
    int costVis = 0;
    int scrollX = 0;
    int scrollY = 0;

    public GuiFocalManipulator(ContainerFocalManipulator menu, Inventory inv, Component title) {
        super(menu, inv, title);
        table = menu.getTile();
        imageWidth = 231;
        imageHeight = 231;
        buttonConfirm = new ImageButton(0, 0, 0, 24, 16, null, I18n.get("wandtable.text3"), texbase, 232, 240, 24, 16);
    }

    @Override
    protected void init() {
        super.init();
        nameField = null;
        gatherInfo(false);
        setupNameField();
        if (table.focusName.isEmpty() && !table.getItem(0).isEmpty()) {
            table.focusName = table.getItem(0).getHoverName().getString();
        }
        nameField.setValue(table.focusName);
    }

    private void setupNameField() {
        int i = (width - imageWidth) / 2;
        int j = (height - imageHeight) / 2;
        nameField = new EditBox(font, i + 30, j + 11, 170, 12, Component.empty());
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
        nameField.setMaxLength(50);
    }

    protected void actionPerformed(Widget button) {
        if (button.id == 0 && buttonConfirm != null && buttonConfirm.active) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        } else if (button.id >= 10) {
            gatherInfo(true);
        }
    }

    // ---- keyboard: the name field takes every key while it has focus, so the inventory key does not close the GUI ----

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField != null && nameField.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            String old = nameField.getValue();
            nameField.keyPressed(keyCode, scanCode, modifiers);
            nameChanged(old);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (nameField != null && nameField.isFocused()) {
            String old = nameField.getValue();
            nameField.charTyped(codePoint, modifiers);
            nameChanged(old);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void nameChanged(String old) {
        if (!old.equals(nameField.getValue())) {
            table.focusName = nameField.getValue();
            PacketDistributor.sendToServer(new PacketFocusNameToServer(table.getBlockPos(), table.focusName));
        }
    }

    // ---- drawing ----

    @Override
    public void render(GuiGraphics g, int mx, int my, float par3) {
        super.render(g, mx, my, par3);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;
        if (GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
            if ((isMouseButtonDown == 0 || isMouseButtonDown == 1) && isPointInRegion(leftPos + 63, topPos + 31, 136, 160, mx + leftPos, my + topPos)) {
                if (isMouseButtonDown == 0) {
                    isMouseButtonDown = 1;
                } else {
                    boolean b = scrollbarParts != null && scrollbarParts.hovered;
                    if (scrollbarMainBottom != null && !scrollbarMainBottom.hovered && !b) {
                        scrollX -= mx - mouseX;
                        curMouseX = scrollX;
                        if (scrollX > scrollbarMainBottom.getMax()) {
                            scrollX = (int) scrollbarMainBottom.getMax();
                        }
                        if (scrollX < scrollbarMainBottom.getMin()) {
                            scrollX = (int) scrollbarMainBottom.getMin();
                        }
                        scrollbarMainBottom.setSliderValue(scrollX);
                    }
                    if (scrollbarMainSide != null && !scrollbarMainSide.hovered && !b) {
                        scrollY -= my - mouseY;
                        curMouseY = scrollY;
                        if (scrollY > scrollbarMainSide.getMax()) {
                            scrollY = (int) scrollbarMainSide.getMax();
                        }
                        if (scrollY < scrollbarMainSide.getMin()) {
                            scrollY = (int) scrollbarMainSide.getMin();
                        }
                        scrollbarMainSide.setSliderValue(scrollY);
                    }
                }
            }
            mouseX = mx;
            mouseY = my;
        } else {
            isMouseButtonDown = 0;
        }
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        int count = 0;
        int index = 0;
        for (String sk : shownParts) {
            if (++count - 1 < partsStart) {
                continue;
            }
            g.pose().pushPose();
            g.pose().translate(0.0f, 0.0f, 5.0f);
            FocusNode node = (FocusNode) FocusEngine.getElement(sk);
            drawPart(g, node, gx + 38, 43 + gy + 25 * index, node.getType() == IFocusElement.EnumUnitType.MOD ? 24.0f : 32.0f, 220,
                    isPointInRegion(gx + 38 - 10, 32 + gy + 24 * index, 20, 20, mx + leftPos, my + topPos));
            g.pose().popPose();
            if (++index > 5) {
                break;
            }
        }
        count = 0;
        index = 0;
        for (String sk : shownParts) {
            if (++count - 1 < partsStart) {
                continue;
            }
            FocusNode node = (FocusNode) FocusEngine.getElement(sk);
            if (isPointInRegion(gx + 38 - 10, 33 + gy + 25 * index, 20, 20, mx + leftPos, my + topPos)) {
                drawHoveringTextFixed(g, genPartText(node, -1), gx + 44, 46 + gy + 24 * index, font, width - (leftPos + imageWidth - 16));
            }
            if (++index > 5) {
                break;
            }
        }
        if (lastNodeHover >= 0) {
            FocusElementNode fn = table.data.get(lastNodeHover);
            if (fn != null && fn.node != null) {
                int xx = leftPos + 140 - scrollX + fn.x * 24;
                int yy = topPos + 50 - scrollY + fn.y * 32;
                drawHoveringTextFixed(g, genPartText(fn.node, lastNodeHover), xx, yy, font, width - (leftPos + imageWidth - 16));
            }
        }
        buttonConfirm.active = table.vis <= 0.0f && valid;
        RenderSystem.disableBlend();
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (table.data != null && !table.data.isEmpty() && nameField != null) {
            nameField.render(g, mx, my, par3);
        }
        for (Widget w : buttonList) {
            if (w.hovered) {
                w.drawForeground(g, mx, my);
                break;
            }
        }
        renderTooltip(g, mx, my);
    }

    private List<String> genPartText(FocusNode node, int idx) {
        List<String> list = new ArrayList<>();
        if (node != null) {
            FocusElementNode placed = null;
            if (idx >= 0) {
                placed = table.data.get(idx);
            }
            list.add(I18n.get(node.getUnlocalizedName()));
            list.add(ChatFormatting.DARK_PURPLE + I18n.get(node.getUnlocalizedText()));
            int c = node.getComplexity();
            if (placed != null) {
                c = (int) (node.getComplexity() * placed.complexityMultiplier);
            }
            list.add(ChatFormatting.GOLD + I18n.get("focuspart.com") + (placed != null && placed.complexityMultiplier > 1.0f ? ChatFormatting.RED : "") + " " + c);
            float p = node.getPowerMultiplier();
            if (placed != null) {
                p = placed.getPower(table.data);
            }
            if (p != 1.0f) {
                list.add(ChatFormatting.GOLD + I18n.get("focuspart.eff") + (p < 1.0f ? ChatFormatting.RED : ChatFormatting.GREEN) + " x" + DECIMALFORMAT.format(p));
            }
            if (node instanceof FocusEffect fe) {
                float d = fe.getDamageForDisplay(placed == null ? 1.0f : placed.getPower(table.data));
                if (d > 0.0f) {
                    list.add(ChatFormatting.DARK_RED + I18n.get("attribute.modifier.equals.0", DECIMALFORMAT.format(d), I18n.get("attribute.name.generic.attack_damage")));
                } else if (d < 0.0f) {
                    d *= -1.0f;
                    list.add(ChatFormatting.DARK_GREEN + I18n.get("attribute.modifier.equals.0", DECIMALFORMAT.format(d), I18n.get("focus.heal.power")));
                }
            }
        }
        return list;
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // drawGuiContainerForegroundLayer: the scrollbars drive the scroll values.
        if (scrollbarMainSide != null) {
            int sv = Math.round(scrollbarMainSide.getSliderValue());
            if (sv != scrollY) {
                scrollY = sv;
            }
        }
        if (scrollbarMainBottom != null) {
            int sv = Math.round(scrollbarMainBottom.getSliderValue());
            if (sv != scrollX) {
                scrollX = sv;
            }
        }
        if (scrollbarParts != null) {
            int sv = Math.round(scrollbarParts.getSliderValue());
            if (sv != partsStart) {
                partsStart = sv;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float par1, int par2, int par3) {
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        int k = (width - imageWidth) / 2;
        int l = (height - imageHeight) / 2;
        UtilsFX.blit(g, tex2, k, l, 0, 0, imageWidth, imageHeight);
        UtilsFX.blit(g, tex3, k - 71, l - 3, 0, 0, 71, 239);
        if (table.getItem(0).isEmpty() || table.doGuiReset) {
            if (table.doGuiReset) {
                resetNodes();
            } else {
                table.data.clear();
                gatherInfo(false);
            }
            table.focusName = "";
            if (!table.getItem(0).isEmpty()) {
                table.focusName = table.getItem(0).getHoverName().getString();
                nameField.setValue(table.focusName);
            }
            table.doGuiReset = false;
        }
        g.pose().pushPose();
        drawNodes(g, leftPos + 132 - scrollX, topPos + 48 - scrollY, par2, par3);
        g.pose().translate(0.0f, 0.0f, 1.0f);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        UtilsFX.blit(g, tex, k, l, 0, 0, imageWidth, imageHeight);
        if (maxComplexity > 0) {
            g.drawString(font, totalComplexity + "/" + maxComplexity, k + 242, l + 36, totalComplexity > maxComplexity ? 16151160 : 16761407, true);
        }
        g.drawString(font, "" + costXp, k + 242, l + 50, costXp > minecraft.player.experienceLevel ? 16151160 : 10092429, true);
        int v = costVis;
        if (table.vis > 0.0f) {
            v = (int) table.vis;
        }
        g.drawString(font, ChatFormatting.AQUA + "" + v, k + 242, l + 64, 10092429, true);
        if (costCast > 0.0f) {
            String cost = myFormatter.format(costCast);
            g.drawString(font, ChatFormatting.AQUA + I18n.get("item.Focus.cost1") + ": " + cost, k + 230, l + 80, 10092429, true);
        }
        if (components != null && components.length > 0) {
            g.drawString(font, ChatFormatting.GOLD + I18n.get("wandtable.text4"), k + 230, l + 92, 10092429, true);
        }
        if (table.focusName != null && !table.data.isEmpty()) {
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            UtilsFX.blit(g, texbase, leftPos + 24, topPos + 8, 192, 224, 8, 14);
            int a;
            for (a = 1; a < 22; ++a) {
                UtilsFX.blit(g, texbase, leftPos + 24 + a * 8, topPos + 8, 200, 224, 8, 14);
            }
            UtilsFX.blit(g, texbase, leftPos + 24 + a * 8, topPos + 8, 208, 224, 8, 14);
        }
        // GuiScreen.drawScreen drew the buttons right after the background.
        for (Widget w : buttonList) {
            w.render(g, par2, par3);
        }
        g.pose().popPose();
    }

    /** isPointInRegion as in GuiContainer: the point is taken relative to the GUI corner. */
    private boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
        pointX -= leftPos;
        pointY -= topPos;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

    private void drawClippedRect(GuiGraphics g, int x, int y, int sx, int sy, int w, int h) {
        if (isPointInRegion(48, 26, 166, 174, x + w / 2, y + h / 2)) {
            UtilsFX.blit(g, tex, x, y, sx, sy, w, h);
        }
    }

    private void drawNodes(GuiGraphics g, int x, int y, int mx, int my) {
        g.pose().pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (table.data != null && !table.data.isEmpty()) {
            int hover = -1;
            for (FocusElementNode fn : table.data.values()) {
                int xx = x + fn.x * 24;
                int yy = y + fn.y * 32;
                boolean mouseover = isPointInRegion(leftPos + 63, topPos + 31, 136, 160, mx + leftPos, my + topPos)
                        && isPointInRegion(xx - 10, yy - 10, 20, 20, mx + leftPos, my + topPos);
                if (mouseover && fn.parent >= 0) {
                    hover = fn.id;
                }
                if (fn.node != null) {
                    if (isPointInRegion(48, 16, 154, 192, xx - 8, yy - 8)) {
                        drawPart(g, fn.node, xx, yy, 32.0f, 220, mouseover);
                    }
                } else {
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    drawClippedRect(g, xx - 12, yy - 12, 120, 232, 24, 24);
                }
                if (selectedNode == fn.id || (mouseover && fn.parent >= 0)) {
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    drawClippedRect(g, xx - 12, yy - 12, 96, 232, 24, 24);
                }
                FocusElementNode parent = table.data.get(fn.parent);
                if (parent != null) {
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    drawClippedRect(g, xx - 6, yy - 22, 54, 232, 12, 12);
                    if (parent.node instanceof FocusModSplit) {
                        for (int q = Math.abs(fn.x - parent.x), a = 0; a < q; ++a) {
                            if (fn.x < parent.x) {
                                if (a == 0) {
                                    drawClippedRect(g, xx - 4, yy - 36, 8, 240, 16, 16);
                                } else {
                                    drawClippedRect(g, xx - 12 + a * 24, yy - 36, 72, 240, 24, 16);
                                }
                            } else if (a == 0) {
                                drawClippedRect(g, xx - 12, yy - 36, 24, 240, 16, 16);
                            } else {
                                drawClippedRect(g, xx - 12 - a * 24, yy - 36, 72, 240, 24, 16);
                            }
                        }
                    }
                    if (fn.node != null) {
                        continue;
                    }
                    int s = parent.target && parent.trajectory ? 4 : 0;
                    if (!isPointInRegion(48, 16, 168, 192, xx - 4, yy - 4)) {
                        continue;
                    }
                    g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    if (parent.target) {
                        g.pose().pushPose();
                        g.pose().translate(xx - s, yy, 0.0f);
                        g.pose().scale(0.5f, 0.5f, 0.5f);
                        UtilsFX.blit(g, tex, -8, -8, 152, 240, 16, 16);
                        g.pose().popPose();
                    }
                    if (!parent.trajectory) {
                        continue;
                    }
                    g.pose().pushPose();
                    g.pose().translate(xx + s, yy, 0.0f);
                    g.pose().scale(0.5f, 0.5f, 0.5f);
                    UtilsFX.blit(g, tex, -8, -8, 168, 240, 16, 16);
                    g.pose().popPose();
                }
            }
            if (hover >= 0 && lastNodeHover != hover) {
                playRollover();
                lastNodeHover = hover;
            }
            if (hover < 0) {
                lastNodeHover = -1;
            }
            if (selectedNode >= 0) {
                drawNodeSettings(g, selectedNode);
            }
        }
        g.pose().popPose();
    }

    private void drawNodeSettings(GuiGraphics g, int selectedNode2) {
        FocusElementNode fn = table.data.get(selectedNode2);
        if (fn != null && fn.node != null && !fn.node.getSettingList().isEmpty()) {
            int a = 0;
            for (String sk : fn.node.getSettingList()) {
                NodeSetting setting = fn.node.getSetting(sk);
                if (setting.getResearch() != null && !ThaumcraftCapabilities.knowsResearchStrict(minecraft.player, setting.getResearch())) {
                    continue;
                }
                int x = leftPos + imageWidth;
                int y = topPos + imageHeight - 10 - fn.node.getSettingList().size() * 26 + a * 26;
                g.drawString(font, ChatFormatting.GOLD + setting.getLocalizedName(), x, y, 16777215, true);
                ++a;
            }
        }
    }

    // ---- mouse ----

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int i = (int) mouseX;
        int j = (int) mouseY;
        if (scrollbarParts != null && shownParts.size() > 6 && isPointInRegion(24, 24, 32, 157, i, j)) {
            if (scrollY > 0) {
                if (partsStart > 0) {
                    --partsStart;
                    scrollbarParts.setSliderValue(partsStart);
                }
            } else if (scrollY < 0 && partsStart < shownParts.size() - 6) {
                ++partsStart;
                scrollbarParts.setSliderValue(partsStart);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selectedButton != null && button == 0) {
            selectedButton.mouseReleased();
            selectedButton = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int par3) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (par3 == 0) {
            for (Widget w : new ArrayList<>(buttonList)) {
                if (w.mousePressed(mx, my)) {
                    selectedButton = w;
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    actionPerformed(w);
                    break;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, par3);
        int gx = (width - imageWidth) / 2;
        int gy = (height - imageHeight) / 2;
        if (table.vis <= 0.0f && table.data != null && !table.data.isEmpty()) {
            if (nameField != null) {
                boolean in = nameField.isMouseOver(mouseX, mouseY);
                nameField.setFocused(in);
                if (in) {
                    nameField.mouseClicked(mouseX, mouseY, par3);
                }
            }
            if (lastNodeHover >= 0) {
                selectedNode = lastNodeHover;
                if (par3 == 1 && table.data.get(selectedNode).node != null) {
                    FocusElementNode fn = table.data.get(selectedNode);
                    if (table.data.get(fn.parent).node != null) {
                        addNodeAt(table.data.get(fn.parent).node.getKey(), fn.parent, true);
                    }
                }
                gatherInfo(false);
                playButtonClick();
            }
            int count = 0;
            int index = 0;
            if (selectedNode >= 0) {
                for (String sk : shownParts) {
                    if (++count - 1 < partsStart) {
                        continue;
                    }
                    if (isPointInRegion(gx + 28, gy + 32 + 24 * index, 20, 20, mx + leftPos, my + topPos)) {
                        addNodeAt(sk, selectedNode, true);
                        return true;
                    }
                    if (++index > 5) {
                        break;
                    }
                }
            }
        }
        return true;
    }

    private void playButtonClick() {
        minecraft.player.playSound(SoundsTC.CLACK.get(), 0.4f, 1.0f);
    }

    private void playRollover() {
        minecraft.player.playSound(SoundsTC.CLACK.get(), 0.4f, 2.0f);
    }

    protected void drawHoveringTextFixed(GuiGraphics g, List<String> listin, int x, int y, Font font, int textWidth) {
        if (listin.isEmpty()) {
            return;
        }
        List<String> list = new ArrayList<>();
        for (String s : listin) {
            s = trimStringNewline(s);
            list.addAll(UtilsFX.wrap(font, s, textWidth));
        }
        int k = 0;
        for (String s : list) {
            int l = font.width(s);
            if (l > k) {
                k = l;
            }
        }
        int j2 = x + 12;
        int k2 = y - 12;
        int i1 = 8;
        if (list.size() > 1) {
            i1 += 2 + (list.size() - 1) * 10;
        }
        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, 300.0f);
        int j3 = -267386864;
        g.fillGradient(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j3, j3);
        g.fillGradient(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j3, j3);
        g.fillGradient(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j3, j3);
        g.fillGradient(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j3, j3);
        g.fillGradient(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j3, j3);
        int k3 = 1347420415;
        int l2 = (k3 & 0xFEFEFE) >> 1 | (k3 & 0xFF000000);
        g.fillGradient(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k3, l2);
        g.fillGradient(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k3, l2);
        g.fillGradient(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k3, k3);
        g.fillGradient(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l2, l2);
        for (int i2 = 0; i2 < list.size(); ++i2) {
            g.drawString(font, list.get(i2), j2, k2, -1, true);
            if (i2 == 0) {
                k2 += 2;
            }
            k2 += 10;
        }
        g.pose().popPose();
    }

    private String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    /**
     * A focus part: its colored frame (medium or effect) and its icon on top, turned a quarter as in the original.
     * bright dims the part (the research browser passes 50 for unknown research).
     */
    public static void drawPart(GuiGraphics g, FocusNode node, int x, int y, float scale, int bright, boolean mouseover) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().mulPose(Axis.ZN.rotationDegrees(90.0f));
        // As in the original this never matches (the root key is "ROOT"), so the root is drawn like any medium.
        boolean root = node.getType() == IFocusElement.EnumUnitType.MOD || node.getKey().equals("thaumcraft.ROOT");
        if (root) {
            scale *= 2.0f;
        }
        Color color = new Color(FocusEngine.getElementColor(node.getKey()));
        float r = color.getRed() / 255.0f;
        float gr = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        switch (node.getType()) {
            case EFFECT -> UtilsFX.renderQuadCentered(g, iEffect, 1, 1, 0, (float) (scale * 0.9 + (mouseover ? 2 : 0)), r, gr, b, false, 1.0f);
            case MEDIUM -> {
                if (!root) {
                    UtilsFX.renderQuadCentered(g, iMedium, 1, 1, 0, (float) (scale * 0.9 + (mouseover ? 2 : 0)), r, gr, b, false, 1.0f);
                }
            }
            default -> {
            }
        }
        g.pose().translate(0.0f, 0.0f, 1.0f);
        float br = Math.min(1.0f, bright / 220.0f);
        ResourceLocation icon = FocusEngine.getElementIcon(node.getKey());
        if (icon != null) {
            UtilsFX.renderQuadCentered(g, icon, 1, 1, 0, scale / 2.0f + (mouseover ? 2 : 0), br, br, br, false, 1.0f);
        }
        g.pose().popPose();
    }

    // ---- the node tree ----

    private int getNextId() {
        while (table.data.containsKey(nodeID)) {
            ++nodeID;
        }
        return nodeID;
    }

    private void cullChildren(int idx) {
        if (table.data.containsKey(idx)) {
            for (int i : table.data.get(idx).children) {
                cullChildren(i);
                table.data.remove(i);
            }
        }
    }

    private ArrayList<Integer> addNodeAt(String nodeKey, int idx, boolean gather) {
        ArrayList<Integer> ret = new ArrayList<>();
        boolean same = false;
        FocusElementNode previous = null;
        if (table.data.containsKey(idx)) {
            cullChildren(idx);
            if (table.data.get(idx).node != null && table.data.get(idx).node.getKey().equals(nodeKey)) {
                same = true;
            } else {
                previous = table.data.remove(idx);
            }
        }
        try {
            FocusElementNode fn;
            FocusNode node;
            if (!same) {
                fn = new FocusElementNode();
                node = (FocusNode) FocusEngine.getElement(nodeKey);
                fn.node = node;
                if (previous != null) {
                    fn.y = previous.y;
                    fn.x = previous.x;
                }
                fn.id = getNextId();
                ret.add(fn.id);
                selectedNode = fn.id;
                if (previous != null && table.data.containsKey(previous.parent)) {
                    fn.parent = previous.parent;
                    int[] c = table.data.get(previous.parent).children;
                    for (int a = 0; a < c.length; ++a) {
                        if (c[a] == previous.id) {
                            table.data.get(previous.parent).children[a] = fn.id;
                            break;
                        }
                    }
                }
                fn.target = node.canSupply(FocusNode.EnumSupplyType.TARGET);
                fn.trajectory = node.canSupply(FocusNode.EnumSupplyType.TRAJECTORY);
                table.data.put(nodeID, fn);
            } else {
                fn = table.data.get(idx);
                node = fn.node;
            }
            if (fn.target || fn.trajectory) {
                if (node instanceof FocusModSplit) {
                    FocusElementNode blank1 = new FocusElementNode();
                    blank1.parent = fn.id;
                    blank1.id = getNextId();
                    ret.add(nodeID);
                    blank1.x = fn.x - 1;
                    blank1.y = fn.y + 1;
                    table.data.put(nodeID, blank1);
                    selectedNode = nodeID;
                    FocusElementNode blank2 = new FocusElementNode();
                    blank2.parent = fn.id;
                    blank2.x = fn.x + 1;
                    blank2.y = fn.y + 1;
                    blank2.id = getNextId();
                    ret.add(nodeID);
                    table.data.put(nodeID, blank2);
                    fn.children = new int[]{blank1.id, blank2.id};
                } else {
                    FocusElementNode blank3 = new FocusElementNode();
                    blank3.parent = fn.id;
                    blank3.x = fn.x;
                    blank3.y = fn.y + 1;
                    blank3.id = getNextId();
                    ret.add(nodeID);
                    table.data.put(nodeID, blank3);
                    fn.children = new int[]{blank3.id};
                    selectedNode = nodeID;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (gather) {
            calcNodeTreeLayout();
            gatherInfo(true);
        }
        return ret;
    }

    private void processLeftNodes(FocusElementNode start, Bounds bounds) {
        if (start.children.length > 0) {
            processLeftNodes(table.data.get(start.children[0]), bounds);
        }
        int ox = 0;
        if (start.children.length == 1) {
            ox = bounds.x - 1;
            bounds.x = table.data.get(start.children[0]).x;
        }
        start.x = bounds.x;
        if (start.children.length == 1) {
            bounds.x = ox;
        }
        ++bounds.x;
        if (start.children.length > 1) {
            processLeftNodes(table.data.get(start.children[1]), bounds);
        }
    }

    private void centerNodes(int start, Bounds bounds) {
        int x = bounds.x / 2;
        FocusElementNode sn = table.data.get(start);
        moveNodes(sn, x);
    }

    private void moveNodes(FocusElementNode start, int amt) {
        for (int ci : start.children) {
            moveNodes(table.data.get(ci), amt);
        }
        start.x -= amt;
    }

    private void calcNodeTreeLayout() {
        int fsi = -1;
        for (FocusElementNode node : table.data.values()) {
            if (fsi < 0 && node.node instanceof FocusModSplit) {
                fsi = node.id;
            }
        }
        if (fsi >= 0) {
            Bounds bounds = new Bounds();
            processLeftNodes(table.data.get(fsi), bounds);
            centerNodes(fsi, bounds);
        }
        for (FocusElementNode node : table.data.values()) {
            if (node.node instanceof FocusModSplit) {
                if (table.data.containsKey(node.parent) && table.data.get(node.parent).node != null && !(table.data.get(node.parent).node instanceof FocusModSplit)) {
                    node.x = table.data.get(node.parent).x;
                } else {
                    int xx = 0;
                    for (int a : node.children) {
                        xx += table.data.get(a).x;
                    }
                    xx /= node.children.length;
                    node.x = xx;
                }
            }
        }
        if (selectedNode >= 0 && !table.data.containsKey(selectedNode)) {
            selectedNode = -1;
        }
    }

    private void resetNodes() {
        nodeID = 0;
        table.data.clear();
        ArrayList<Integer> r1 = addNodeAt(new FocusMediumRoot().getKey(), 0, false);
        selectedNode = r1.get(1);
        calcNodeTreeLayout();
        gatherInfo(true);
    }

    private void calcScrollBounds() {
        sMinX = 0;
        sMinY = 0;
        sMaxX = 0;
        sMaxY = 0;
        for (FocusElementNode fn : table.data.values()) {
            if (fn.x < sMinX) {
                sMinX = fn.x;
            }
            if (fn.y < sMinY) {
                sMinY = fn.y;
            }
            if (fn.x > sMaxX) {
                sMaxX = fn.x;
            }
            if (fn.y > sMaxY) {
                sMaxY = fn.y;
            }
        }
    }

    private void gatherPartsList() {
        shownParts.clear();
        if (minecraft == null) {
            return;
        }
        if (selectedNode >= 0 && table.data.containsKey(selectedNode)) {
            partsStart = 0;
            ArrayList<String> pMed = new ArrayList<>();
            ArrayList<String> pEff = new ArrayList<>();
            ArrayList<String> pMod = new ArrayList<>();
            ArrayList<String> excluded = new ArrayList<>();
            boolean hasExlusive = false;
            boolean hasMedium = false;
            for (FocusElementNode fn : table.data.values()) {
                if (fn.node instanceof FocusMedium) {
                    hasMedium = !(fn.node instanceof FocusMediumRoot);
                    if (fn.node.isExclusive()) {
                        hasExlusive = true;
                        break;
                    }
                }
                if (fn.node != null && fn.node.isExclusive()) {
                    excluded.add(fn.node.getKey());
                }
            }
            FocusElementNode node = table.data.get(selectedNode);
            FocusElementNode parent = table.data.get(node.parent);
            if (parent != null && parent.node != null) {
                for (String key : FocusEngine.elements.keySet()) {
                    IFocusElement element = FocusEngine.getElement(key);
                    if (element == null || !ThaumcraftCapabilities.knowsResearchStrict(minecraft.player, element.getResearch())) {
                        continue;
                    }
                    if (element.getKey().equals("thaumcraft.ROOT")) {
                        continue;
                    }
                    if (!(element instanceof FocusNode fn2)) {
                        continue;
                    }
                    if (excluded.contains(fn2.getKey())) {
                        continue;
                    }
                    if (fn2.mustBeSupplied() == null) {
                        continue;
                    }
                    for (FocusNode.EnumSupplyType type : fn2.mustBeSupplied()) {
                        if (parent.node.canSupply(type)) {
                            switch (element.getType()) {
                                case EFFECT -> pEff.add(key);
                                case MEDIUM -> {
                                    if (!hasExlusive && (!((FocusMedium) element).isExclusive() || !hasMedium)) {
                                        pMed.add(key);
                                    }
                                }
                                case MOD -> pMod.add(key);
                            }
                            break;
                        }
                    }
                }
            }
            Collections.sort(pMed);
            Collections.sort(pEff);
            Collections.sort(pMod);
            shownParts.addAll(pMed);
            shownParts.addAll(pEff);
            shownParts.addAll(pMod);
        }
    }

    private void gatherInfo(boolean sync) {
        buttonList.clear();
        buttonList.add(buttonConfirm);
        buttonConfirm.x = leftPos + 242;
        buttonConfirm.y = topPos + 18;
        buttonList.add(new HoverButton(1, leftPos + 241, topPos + 39, 32, 16, I18n.get("wandtable.text5"), null, ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/complex.png")));
        buttonList.add(new HoverButton(2, leftPos + 241, topPos + 53, 32, 16, I18n.get("wandtable.text2"), null, ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/costxp.png")));
        buttonList.add(new HoverButton(3, leftPos + 241, topPos + 67, 32, 16, I18n.get("wandtable.text1"), null, ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/gui/costvis.png")));
        FocusElementNode fn = table.data.get(selectedNode);
        if (fn != null && fn.node != null && !fn.node.getSettingList().isEmpty()) {
            int a = 0;
            for (String sk : fn.node.getSettingList()) {
                NodeSetting setting = fn.node.getSetting(sk);
                if (setting.getResearch() != null && !ThaumcraftCapabilities.knowsResearchStrict(minecraft.player, setting.getResearch())) {
                    continue;
                }
                int x = leftPos + imageWidth;
                int y = topPos + imageHeight + 3 - fn.node.getSettingList().size() * 26 + a * 26;
                int w = 32;
                if (setting.getType() instanceof NodeSetting.NodeSettingIntList) {
                    w = 72;
                }
                buttonList.add(new SettingSpinner(10 + a, x, y, w, setting));
                ++a;
            }
        }
        shownParts.clear();
        components = null;
        if (table.getItem(0).isEmpty()) {
            return;
        }
        HashMap<String, Integer> compCount = new HashMap<>();
        totalComplexity = 0;
        maxComplexity = 0;
        if (menu.getSlot(0).hasItem()) {
            ItemStack is = menu.getSlot(0).getItem();
            if (is.getItem() instanceof ItemFocus focus) {
                maxComplexity = focus.getMaxComplexity();
            }
        }
        boolean emptyNodes = false;
        AspectList crystals = new AspectList();
        if (table.data != null && !table.data.isEmpty()) {
            for (FocusElementNode node : table.data.values()) {
                if (node.node != null) {
                    int a2 = 0;
                    if (compCount.containsKey(node.node.getKey())) {
                        a2 = compCount.get(node.node.getKey());
                    }
                    ++a2;
                    node.complexityMultiplier = 0.5f * (a2 + 1);
                    compCount.put(node.node.getKey(), a2);
                    totalComplexity += (int) (node.node.getComplexity() * node.complexityMultiplier);
                    if (node.node.getAspect() == null) {
                        continue;
                    }
                    crystals.add(node.node.getAspect(), 1);
                } else {
                    emptyNodes = true;
                }
            }
        }
        costCast = totalComplexity / 5.0f;
        costVis = totalComplexity * 10 + maxComplexity / 5;
        // The original used Math.min here (always 0 or 1); the table charges Math.max(1, ...), so show that.
        costXp = (int) Math.max(1L, Math.round(Math.sqrt(totalComplexity)));
        boolean validCrystals = false;
        if (crystals.getAspects().length > 0) {
            validCrystals = true;
            components = new ItemStack[crystals.getAspects().length];
            int r = 0;
            for (Aspect as : crystals.getAspects()) {
                components[r] = ConfigItems.crystalEssence.get().makeCrystal(as, crystals.getAmount(as));
                ++r;
            }
            for (ItemStack component : components) {
                if (!InventoryUtils.isPlayerCarryingAmount(minecraft.player, component)) {
                    validCrystals = false;
                }
            }
            int i = 0;
            int q = 0;
            int z = 0;
            for (ItemStack stack : components) {
                buttonList.add(new HoverButton(11 + z, leftPos + 234 + i * 16, topPos + 110 + 16 * q, 16, 16, stack.getHoverName().getString(), null, stack));
                if (++i > 4) {
                    i = 0;
                    ++q;
                }
                ++z;
            }
        }
        gatherPartsList();
        scrollbarParts = null;
        if (shownParts.size() > 6) {
            scrollbarParts = new Slider(4, leftPos + 51, topPos + 30, 8, 149, 0.0f, (float) (shownParts.size() - 6), 0.0f, true);
            buttonList.add(scrollbarParts);
        }
        // The original compared the table's last charged cost (table.xpCost) here; the shown cost is what counts.
        valid = totalComplexity <= maxComplexity && !emptyNodes && validCrystals
                && (costXp <= minecraft.player.experienceLevel || minecraft.player.getAbilities().instabuild);
        calcScrollBounds();
        if (scrollY > (sMaxY - 3) * 32) {
            scrollY = (sMaxY - 3) * 32;
        }
        if (scrollX > sMaxX * 24) {
            scrollX = sMaxX * 24;
        }
        if (scrollX < sMinX * 24) {
            scrollX = sMinX * 24;
        }
        scrollbarMainSide = null;
        scrollbarMainBottom = null;
        if (sMaxY * 32 > 130) {
            scrollbarMainSide = new Slider(5, leftPos + 203, topPos + 32, 8, 156, 0.0f, (float) ((sMaxY - 3) * 32), (float) scrollY, true);
            buttonList.add(scrollbarMainSide);
        } else {
            scrollY = 0;
        }
        if (sMinX * 24 < -70 || sMaxX * 24 > 70) {
            scrollbarMainBottom = new Slider(6, leftPos + 64, topPos + 195, 132, 8, (float) (sMinX * 24), (float) (sMaxX * 24), (float) scrollX, false);
            buttonList.add(scrollbarMainBottom);
        } else {
            scrollX = 0;
        }
        if (table.focusName.isEmpty() && !table.getItem(0).isEmpty()) {
            table.focusName = table.getItem(0).getHoverName().getString();
        }
        if (nameField == null) {
            setupNameField();
        }
        nameField.setValue(table.focusName);
        if (sync) {
            PacketDistributor.sendToServer(new PacketFocusNodesToServer(table.getBlockPos(), table.data, table.focusName));
        }
    }

    private static class Bounds {
        int x = 0;
    }

    // ---- the original plugin buttons ----

    abstract static class Widget {
        final int id;
        int x;
        int y;
        final int width;
        final int height;
        boolean hovered;

        Widget(int id, int x, int y, int width, int height) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        abstract void render(GuiGraphics g, int mx, int my);

        /** GuiButton.mousePressed: the rectangle from the corner. */
        boolean mousePressed(int mx, int my) {
            return mx >= x && my >= y && mx < x + width && my < y + height;
        }

        void mouseReleased() {
        }

        void drawForeground(GuiGraphics g, int mx, int my) {
        }
    }

    /** GuiImageButton: a centered part of a texture; dimmed while not active. */
    class ImageButton extends Widget {
        final String displayString;
        final String description;
        final ResourceLocation loc;
        final int lx;
        final int ly;
        final int ww;
        final int hh;
        boolean active = true;

        ImageButton(int id, int x, int y, int width, int height, String text, String description, ResourceLocation loc, int lx, int ly, int ww, int hh) {
            super(id, x, y, width, height);
            displayString = text;
            this.description = description;
            this.loc = loc;
            this.lx = lx;
            this.ly = ly;
            this.ww = ww;
            this.hh = hh;
        }

        @Override
        void render(GuiGraphics g, int xx, int yy) {
            hovered = xx >= x - width / 2 && yy >= y - height / 2 && xx < x - width / 2 + width && yy < y - height / 2 + height;
            float cc = hovered ? 1.0f : 0.9f;
            float ac = 1.0f;
            if (!active) {
                cc = 0.5f;
                ac = 0.9f;
            }
            g.setColor(cc, cc, cc, ac);
            UtilsFX.blit(g, loc, x - ww / 2, y - hh / 2, lx, ly, ww, hh);
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (displayString != null) {
                g.pose().pushPose();
                g.pose().translate(x, y, 0.0f);
                g.pose().scale(0.5f, 0.5f, 1.0f);
                g.drawCenteredString(font, displayString, 0, -4, hovered ? 16777120 : 16777215);
                g.pose().popPose();
            }
        }

        @Override
        void drawForeground(GuiGraphics g, int xx, int yy) {
            ArrayList<String> text = new ArrayList<>();
            if (displayString != null) {
                text.add(displayString);
            }
            int m = 8;
            if (description != null) {
                m = 0;
                text.add("§o§9" + description);
            }
            UtilsFX.drawCustomTooltip(g, font, text, xx + 4, yy + m, -99);
        }

        @Override
        boolean mousePressed(int mx, int my) {
            return active && mx >= x - width / 2 && my >= y - height / 2 && mx < x - width / 2 + width && my < y - height / 2 + height;
        }
    }

    /** GuiHoverButton: an icon or an item that only shows a tooltip. */
    class HoverButton extends Widget {
        final String displayString;
        final String description;
        final Object tex;

        HoverButton(int id, int x, int y, int width, int height, String text, String description, Object tex) {
            super(id, x, y, width, height);
            displayString = text;
            this.description = description;
            this.tex = tex;
        }

        @Override
        void render(GuiGraphics g, int xx, int yy) {
            hovered = xx >= x - width / 2 && yy >= y - height / 2 && xx < x - width / 2 + width && yy < y - height / 2 + height;
            float c = hovered ? 1.0f : 0.9f;
            g.setColor(c, c, c, hovered ? 1.0f : 0.9f);
            if (tex instanceof ResourceLocation loc) {
                RenderSystem.enableBlend();
                g.blit(loc, x - width / 2, y - height / 2, 0, 0, 16, 16, 16, 16);
            }
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (tex instanceof ItemStack stack) {
                g.renderItem(stack, x - width / 2, y - height / 2 - (hovered ? 1 : 0));
            }
        }

        @Override
        void drawForeground(GuiGraphics g, int xx, int yy) {
            if (tex instanceof ItemStack stack) {
                g.renderTooltip(font, stack, xx, yy);
                return;
            }
            List<String> text = new ArrayList<>();
            text.add(displayString);
            int m = 8;
            if (description != null) {
                m = 0;
                text.add("§o§9" + description);
            }
            UtilsFX.drawCustomTooltip(g, font, text, xx + 4, yy + m, -99);
        }

        @Override
        boolean mousePressed(int mx, int my) {
            return false;
        }
    }

    /** GuiSliderTC: a thin bar with a knob, dragged while the mouse is held. */
    static class Slider extends Widget {
        final float min;
        final float max;
        final boolean vertical;
        float sliderPosition;
        boolean isMouseDown;

        Slider(int id, int x, int y, int w, int h, float min, float max, float defaultValue, boolean vertical) {
            super(id, x, y, w, h);
            this.min = min;
            this.max = max;
            sliderPosition = (defaultValue - min) / (max - min);
            this.vertical = vertical;
        }

        float getMax() {
            return max;
        }

        float getMin() {
            return min;
        }

        float getSliderValue() {
            return min + (max - min) * sliderPosition;
        }

        void setSliderValue(float value) {
            sliderPosition = (value - min) / (max - min);
        }

        private void moveTo(int mx, int my) {
            sliderPosition = vertical ? (my - (y + 4)) / (float) (height - 8) : (mx - (x + 4)) / (float) (width - 8);
            sliderPosition = Math.max(0.0f, Math.min(1.0f, sliderPosition));
        }

        @Override
        void render(GuiGraphics g, int mx, int my) {
            hovered = mx >= x && my >= y && mx < x + width && my < y + height;
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            g.pose().pushPose();
            if (vertical) {
                g.pose().translate(x + 2, y, 0.0f);
                g.pose().scale(1.0f, height / 32.0f, 1.0f);
                UtilsFX.blit(g, texbase, 0, 0, 240, 176, 4, 32);
            } else {
                g.pose().translate(x, y + 2, 0.0f);
                g.pose().scale(width / 32.0f, 1.0f, 1.0f);
                UtilsFX.blit(g, texbase, 0, 0, 208, 176, 32, 4);
            }
            g.pose().popPose();
            if (isMouseDown) {
                moveTo(mx, my);
            }
            if (vertical) {
                UtilsFX.blit(g, texbase, x, y + (int) (sliderPosition * (height - 8)), 20, 20, 8, 8);
            } else {
                UtilsFX.blit(g, texbase, x + (int) (sliderPosition * (width - 8)), y, 20, 20, 8, 8);
            }
        }

        @Override
        boolean mousePressed(int mx, int my) {
            if (super.mousePressed(mx, my)) {
                moveTo(mx, my);
                return isMouseDown = true;
            }
            return false;
        }

        @Override
        void mouseReleased() {
            isMouseDown = false;
        }
    }

    /** GuiFocusSettingSpinnerButton: the value of a node setting between two arrows. */
    class SettingSpinner extends Widget {
        final NodeSetting setting;

        SettingSpinner(int id, int x, int y, int width, NodeSetting ns) {
            super(id, x, y, width, 10);
            setting = ns;
        }

        @Override
        void render(GuiGraphics g, int xx, int yy) {
            hovered = xx >= x && yy >= y && xx < x + width + 10 && yy < y + height;
            float c = hovered ? 1.0f : 0.9f;
            g.setColor(c, c, c, c);
            UtilsFX.blit(g, texbase, x, y, 20, 0, 10, 10);
            UtilsFX.blit(g, texbase, x + width, y, 30, 0, 10, 10);
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            String s = setting.getValueText();
            g.drawString(font, s, x + (width + 10) / 2 - font.width(s) / 2, y + 1, 16777215, true);
        }

        @Override
        boolean mousePressed(int mouseX, int mouseY) {
            if (mouseX >= x && mouseY >= y && mouseX < x + 10 && mouseY < y + height) {
                setting.decrement();
                return true;
            }
            if (mouseX >= x + width && mouseY >= y && mouseX < x + width + 10 && mouseY < y + height) {
                setting.increment();
                return true;
            }
            return false;
        }
    }
}
