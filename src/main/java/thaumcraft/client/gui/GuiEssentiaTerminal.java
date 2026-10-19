package thaumcraft.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.container.ContainerEssentiaTerminal;
import thaumcraft.common.lib.SoundsTC;

/**
 * The essentia terminal screen, laid out like the golem logistics screen: the aspects on the network in a grid
 * (searchable), and for the one picked, how many phials to fill and a button to fill them from the player's empty
 * phials. A second button empties every filled phial the player carries into the jars. Not in Thaumcraft.
 */
public class GuiEssentiaTerminal extends AbstractContainerScreen<ContainerEssentiaTerminal> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_logistics.png");
    private static final ResourceLocation BASE = Thaumcraft.id("textures/gui/gui_base.png");
    private static final int COLUMNS = 9;
    /** Eight rows: the ninth holds the jar count and the deposit button. */
    private static final int ROWS = 8;

    private Aspect selected;
    private int count = 1;
    private int lastCount = 1;
    private boolean countUpdated = false;
    private GuiLogistics.Slider countbar;
    private EditBox searchField;
    /** The aspects shown, in grid order, rebuilt each frame from the totals and the search. */
    private final List<Aspect> shown = new ArrayList<>();

    public GuiEssentiaTerminal(ContainerEssentiaTerminal menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 215;
        imageHeight = 215;
    }

    @Override
    protected void init() {
        super.init();
        countbar = new GuiLogistics.Slider(leftPos + 24, topPos + 196, 32, 8, 1.0f, 64.0f, 1.0f, false);
        searchField = new EditBox(font, leftPos + 143, topPos + 196, 55, font.lineHeight, Component.empty());
        searchField.setMaxLength(12);
        searchField.setBordered(true);
        searchField.setTextColor(0xFFFFFF);
        searchField.setCanLoseFocus(true);
        searchField.setValue("");
    }

    private int amount(Aspect a) {
        return menu.amounts[ContainerEssentiaTerminal.ORDER.indexOf(a)];
    }

    /** The aspects with essentia on the network whose name or description match the search, by name. */
    private void rebuild() {
        shown.clear();
        String q = searchField.getValue().toLowerCase(Locale.ROOT);
        for (Aspect a : ContainerEssentiaTerminal.ORDER) {
            if (amount(a) <= 0) continue;
            if (!q.isEmpty() && !a.getName().toLowerCase(Locale.ROOT).contains(q)
                    && !a.getLocalizedDescription().getString().toLowerCase(Locale.ROOT).contains(q)) continue;
            shown.add(a);
        }
        shown.sort((x, y) -> x.getName().compareTo(y.getName()));
        if (shown.size() > COLUMNS * ROWS) shown.subList(COLUMNS * ROWS, shown.size()).clear();
        if (selected != null && amount(selected) < ContainerEssentiaTerminal.PHIAL) selected = null;
    }

    /** How many phials can be filled with the selected aspect: by essentia and by empty phials carried. */
    private int maxPhials() {
        if (selected == null) return 0;
        int empty = 0;
        Inventory inv = minecraft.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ConfigItems.phial.get()) && !ConfigItems.phial.get().isFilled(s)) empty += s.getCount();
        }
        return Math.min(64, Math.min(empty, amount(selected) / ContainerEssentiaTerminal.PHIAL));
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        rebuild();
        super.render(g, mouseX, mouseY, partialTicks);
        Aspect over = aspectAt(mouseX, mouseY);
        if (over != null) {
            List<Component> lines = new ArrayList<>();
            lines.add(over.getDisplayName());
            lines.add(over.getLocalizedDescription().withStyle(ChatFormatting.GRAY));
            int a = amount(over);
            lines.add(Component.translatable("tc.terminal.amount", a, a / ContainerEssentiaTerminal.PHIAL).withStyle(ChatFormatting.DARK_GRAY));
            g.renderComponentTooltip(font, lines, mouseX, mouseY);
        } else if (overButton(mouseX, mouseY, depositX(), depositY())) {
            g.renderTooltip(font, Component.translatable("tc.terminal.deposit.tip"), mouseX, mouseY);
        } else {
            renderTooltip(g, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        UtilsFX.blit(g, TEX, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        for (int i = 0; i < shown.size(); i++) {
            Aspect a = shown.get(i);
            int x = leftPos + 17 + i % COLUMNS * 19;
            int y = topPos + 17 + i / COLUMNS * 19;
            if (a == selected) UtilsFX.blit(g, TEX, x, y, 222, 46, 20, 20);
            UtilsFX.drawTag(g, x + 2, y + 2, a, amount(a), 0);
        }
        if (shown.isEmpty()) {
            Component msg = Component.translatable(menu.jarCount.get() == 0 ? "tc.terminal.nojars" : "tc.terminal.empty");
            g.drawCenteredString(font, msg, leftPos + imageWidth / 2, topPos + 95, 0xA0A0A0);
        }
        countbar.visible = selected != null;
        countbar.render(g, mouseX, mouseY);
        if (selected != null) {
            drawPlusMinus(g, leftPos + 13, topPos + 195, true, mouseX, mouseY);
            drawPlusMinus(g, leftPos + 57, topPos + 195, false, mouseX, mouseY);
            drawButton(g, leftPos + 116, topPos + 200, "tc.terminal.fill", maxPhials() > 0, mouseX, mouseY);
        }
        drawButton(g, depositX(), depositY(), "tc.terminal.deposit", true, mouseX, mouseY);
        searchField.render(g, mouseX, mouseY, partialTicks);
        if (!searchField.isFocused() && searchField.getValue().isEmpty()) {
            g.drawString(font, Component.translatable("tc.logistics.search"), leftPos + 146, topPos + 197, 0x222222, false);
        }
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private int depositX() {
        return leftPos + 178;
    }

    private int depositY() {
        return topPos + 180;
    }

    /** GuiImageButton of the logistics screen: centred on its position, label at half size. */
    private void drawButton(GuiGraphics g, int bx, int by, String key, boolean active, int mx, int my) {
        boolean over = active && overButton(mx, my, bx, by);
        float cc = !active ? 0.5f : over ? 1.0f : 0.9f;
        g.setColor(cc, cc, cc, 1.0f);
        UtilsFX.blit(g, BASE, bx - 20, by - 6, 37, 82, 40, 13);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        g.pose().pushPose();
        g.pose().translate(bx, by, 0.0f);
        g.pose().scale(0.5f, 0.5f, 1.0f);
        g.drawCenteredString(font, Component.translatable(key), 0, -4, !active ? 0x808080 : over ? 0xFFFFA0 : 0xFFFFFF);
        g.pose().popPose();
    }

    private static boolean overButton(double mx, double my, int bx, int by) {
        return mx >= bx - 20 && my >= by - 6 && mx < bx + 20 && my < by + 7;
    }

    private void drawPlusMinus(GuiGraphics g, int x, int y, boolean minus, int mx, int my) {
        float c = in(mx, my, x, y, 10, 10) ? 1.0f : 0.9f;
        g.setColor(c, c, c, c);
        UtilsFX.blit(g, BASE, x, y, minus ? 0 : 10, 0, 10, 10);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("tc.terminal.jars", menu.jarCount.get()), 19, 176, 0x333333, false);
        if (selected == null) return;
        int max = Math.max(1, maxPhials());
        if (countbar.getMax() != max) {
            countbar.setMax(max);
            count = Math.min(count, max);
            countbar.setSliderValue(count);
        }
        int sv = Math.round(countbar.getSliderValue());
        if (sv != lastCount) {
            lastCount = sv;
            count = sv;
        } else if (countUpdated && sv != count) {
            countbar.setSliderValue(count);
            countUpdated = false;
        }
        String s = "" + count;
        g.drawString(font, s, 83 - font.width(s) / 2, 196, 0x333333, false);
    }

    @Nullable
    private Aspect aspectAt(double mx, double my) {
        for (int i = 0; i < shown.size(); i++) {
            if (in(mx, my, leftPos + 17 + i % COLUMNS * 19 + 2, topPos + 17 + i / COLUMNS * 19 + 2, 16, 16)) return shown.get(i);
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inSearch = searchField.isMouseOver(mouseX, mouseY);
        searchField.setFocused(inSearch);
        if (inSearch) {
            searchField.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        if (button == 0) {
            if (countbar.mousePressed(mouseX, mouseY)) return true;
            Aspect a = aspectAt(mouseX, mouseY);
            if (a != null && amount(a) >= ContainerEssentiaTerminal.PHIAL) {
                minecraft.player.playSound(SoundsTC.CLACK.get(), 0.66f, 1.0f);
                selected = a;
                return true;
            }
            if (overButton(mouseX, mouseY, depositX(), depositY())) {
                click();
                send(ContainerEssentiaTerminal.DEPOSIT);
                return true;
            }
            if (selected != null) {
                if (in(mouseX, mouseY, leftPos + 13, topPos + 195, 10, 10)) {
                    click();
                    count = Math.max(1, count - 1);
                    countUpdated = true;
                    return true;
                }
                if (in(mouseX, mouseY, leftPos + 57, topPos + 195, 10, 10)) {
                    click();
                    count = Math.min(Math.max(1, maxPhials()), count + 1);
                    countUpdated = true;
                    return true;
                }
                if (overButton(mouseX, mouseY, leftPos + 116, topPos + 200) && maxPhials() > 0) {
                    click();
                    int n = Screen.hasShiftDown() ? maxPhials() : Math.min(count, maxPhials());
                    send(ContainerEssentiaTerminal.FILL + ContainerEssentiaTerminal.ORDER.indexOf(selected) * 64 + (n - 1));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void click() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        countbar.isMouseDown = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    // ---- the search field takes every key while it has focus, so the inventory key does not close the screen ----

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            searchField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchField.isFocused()) {
            searchField.charTyped(codePoint, modifiers);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
