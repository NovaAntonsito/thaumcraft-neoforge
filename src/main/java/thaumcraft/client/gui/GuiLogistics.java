package thaumcraft.client.gui;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.container.ContainerLogistics;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.misc.PacketLogisticsRequestToServer;
import thaumcraft.common.lib.network.misc.PacketMiscStringToServer;

/**
 * Port of GuiLogistics: pick one of the stacks the Provide seals around offer, choose how many and request them.
 * The GuiScrollButton, GuiPlusMinusButton, GuiSliderTC and GuiImageButton widgets are drawn and handled here.
 */
public class GuiLogistics extends AbstractContainerScreen<ContainerLogistics> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_logistics.png");
    private static final ResourceLocation BASE = Thaumcraft.id("textures/gui/gui_base.png");

    int selectedSlot = -1;
    final ContainerLogistics con;
    long lu = 0L;
    int lastStackSize = 1;
    int stackSize = 1;
    boolean stacksizeUpdated = false;
    ItemStack selectedStack = null;
    int lastScrollPos = 0;
    Slider scrollbar;
    Slider countbar;
    EditBox searchField;

    public GuiLogistics(ContainerLogistics menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        con = menu;
        imageWidth = 215;
        imageHeight = 215;
    }

    /** GuiSliderTC: a thin bar with a knob, dragged while the mouse is held. */
    static class Slider {
        final int x;
        final int y;
        final int width;
        final int height;
        float min;
        float max;
        final boolean vertical;
        float sliderPosition;
        boolean isMouseDown;
        boolean visible = true;

        Slider(int x, int y, int w, int h, float min, float max, float defaultValue, boolean vertical) {
            this.x = x;
            this.y = y;
            width = w;
            height = h;
            this.min = min;
            this.max = max;
            sliderPosition = max > min ? (defaultValue - min) / (max - min) : 0.0f;
            this.vertical = vertical;
        }

        float getMax() {
            return max;
        }

        void setMax(float max) {
            this.max = max;
            sliderPosition = 0.0f;
        }

        float getSliderValue() {
            return min + (max - min) * sliderPosition;
        }

        /** 1.21: guarded against an empty range, which made the 1.12 slider NaN. */
        void setSliderValue(float value) {
            sliderPosition = max > min ? (value - min) / (max - min) : 0.0f;
        }

        private void moveTo(double mx, double my) {
            sliderPosition = vertical ? (float) (my - (y + 4)) / (height - 8) : (float) (mx - (x + 4)) / (width - 8);
            sliderPosition = Math.max(0.0f, Math.min(1.0f, sliderPosition));
        }

        boolean over(double mx, double my) {
            return mx >= x && my >= y && mx < x + width && my < y + height;
        }

        void render(GuiGraphics g, int mx, int my) {
            if (!visible) {
                return;
            }
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            g.pose().pushPose();
            if (vertical) {
                g.pose().translate(x + 2, y, 0.0f);
                g.pose().scale(1.0f, height / 32.0f, 1.0f);
                UtilsFX.blit(g, BASE, 0, 0, 240, 176, 4, 32);
            } else {
                g.pose().translate(x, y + 2, 0.0f);
                g.pose().scale(width / 32.0f, 1.0f, 1.0f);
                UtilsFX.blit(g, BASE, 0, 0, 208, 176, 32, 4);
            }
            g.pose().popPose();
            if (isMouseDown) {
                moveTo(mx, my);
            }
            if (vertical) {
                UtilsFX.blit(g, BASE, x, y + (int) (sliderPosition * (height - 8)), 20, 20, 8, 8);
            } else {
                UtilsFX.blit(g, BASE, x + (int) (sliderPosition * (width - 8)), y, 20, 20, 8, 8);
            }
        }

        boolean mousePressed(double mx, double my) {
            if (visible && over(mx, my)) {
                moveTo(mx, my);
                return isMouseDown = true;
            }
            return false;
        }
    }

    @Override
    protected void init() {
        super.init();
        scrollbar = new Slider(leftPos + 196, topPos + 28, 8, 149, 0.0f, (float) con.end, (float) con.start, true);
        countbar = new Slider(leftPos + 24, topPos + 196, 32, 8, 1.0f, 64.0f, 1.0f, false);
        searchField = new EditBox(font, leftPos + 143, topPos + 196, 55, font.lineHeight, Component.empty());
        searchField.setMaxLength(10);
        searchField.setBordered(true);
        searchField.setTextColor(16777215);
        searchField.setCanLoseFocus(true);
        searchField.setValue("");
    }

    private boolean selected() {
        return selectedSlot >= 0;
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        UtilsFX.blit(graphics, TEX, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (selected()) {
            UtilsFX.blit(graphics, TEX, leftPos + 17 + selectedSlot % 9 * 19, topPos + 17 + selectedSlot / 9 * 19, 222, 46, 20, 20);
        }
        // GuiScrollButton 0 (up) and 1 (down)
        drawScroll(graphics, leftPos + 195, topPos + 16, true, mouseX, mouseY);
        drawScroll(graphics, leftPos + 195, topPos + 180, false, mouseX, mouseY);
        scrollbar.render(graphics, mouseX, mouseY);
        countbar.visible = selected();
        countbar.render(graphics, mouseX, mouseY);
        if (selected()) {
            // GuiPlusMinusButton 2 and 3
            drawPlusMinus(graphics, leftPos + 13, topPos + 195, true, mouseX, mouseY);
            drawPlusMinus(graphics, leftPos + 57, topPos + 195, false, mouseX, mouseY);
            // GuiImageButton 7: centred on its position, label at half size
            int bx = leftPos + 116;
            int by = topPos + 200;
            boolean over = overRequest(mouseX, mouseY);
            float cc = over ? 1.0f : 0.9f;
            graphics.setColor(cc, cc, cc, 1.0f);
            UtilsFX.blit(graphics, BASE, bx - 20, by - 6, 37, 82, 40, 13);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            graphics.pose().pushPose();
            graphics.pose().translate(bx, by, 0.0f);
            graphics.pose().scale(0.5f, 0.5f, 1.0f);
            graphics.drawCenteredString(font, Component.translatable("tc.logistics.request"), 0, -4, over ? 16777120 : 16777215);
            graphics.pose().popPose();
        }
        searchField.render(graphics, mouseX, mouseY, partialTicks);
        if (!searchField.isFocused() && searchField.getValue().isEmpty()) {
            graphics.drawString(font, Component.translatable("tc.logistics.search"), leftPos + 146, topPos + 197, 2236962, false);
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawScroll(GuiGraphics g, int x, int y, boolean minus, int mx, int my) {
        float c = mx >= x && my >= y && mx < x + 10 && my < y + 10 ? 1.0f : 0.9f;
        g.setColor(c, c, c, c);
        UtilsFX.blit(g, BASE, x, y, 67, minus ? 0 : 10, 10, 10);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawPlusMinus(GuiGraphics g, int x, int y, boolean minus, int mx, int my) {
        float c = mx >= x && my >= y && mx < x + 10 && my < y + 10 ? 1.0f : 0.9f;
        g.setColor(c, c, c, c);
        UtilsFX.blit(g, BASE, x, y, minus ? 0 : 10, 0, 10, 10);
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private boolean overRequest(double mx, double my) {
        int bx = leftPos + 116;
        int by = topPos + 200;
        return mx >= bx - 20 && my >= by - 6 && mx < bx + 20 && my < by + 7;
    }

    /** drawGuiContainerForegroundLayer: refreshes the list every second and keeps the sliders and selection in step. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        long ct = System.currentTimeMillis();
        if (ct > lu) {
            lu = ct + 1000L;
            send(22);
        }
        if (scrollbar.getMax() != con.end) {
            scrollbar.setMax((float) con.end);
        }
        int sv = Math.round(scrollbar.getSliderValue());
        if (sv != lastScrollPos) {
            lastScrollPos = sv;
            send(100 + lastScrollPos);
        } else if (con.updated && sv != con.start) {
            scrollbar.setSliderValue((float) con.start);
            con.updated = false;
        }
        if (selected() && selectedStack != null && !selectedStack.isEmpty()
                && !ItemStack.isSameItemSameComponents(selectedStack, menu.getSlot(selectedSlot).getItem())) {
            selectedSlot = -1;
            for (Slot slot : menu.slots) {
                if (ItemStack.isSameItemSameComponents(selectedStack, slot.getItem())) {
                    selectedSlot = slot.index;
                    break;
                }
            }
        }
        if (selected() && !menu.getSlot(selectedSlot).hasItem()) {
            selectedSlot = -1;
        }
        if (selected() && menu.getSlot(selectedSlot).hasItem()) {
            ItemStack stack = menu.getSlot(selectedSlot).getItem();
            if (countbar.getMax() != stack.getCount()) {
                countbar.setMax((float) stack.getCount());
            }
            int sv2 = Math.round(countbar.getSliderValue());
            if (sv2 != lastStackSize) {
                lastStackSize = sv2;
                stackSize = lastStackSize;
            } else if (stacksizeUpdated && sv2 != stackSize) {
                countbar.setSliderValue((float) stackSize);
                stacksizeUpdated = false;
            }
            String s = "" + stackSize;
            graphics.drawString(font, s, 83 - font.width(s) / 2, 196, 3355443, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY < 0) {
            send(0);
        } else if (scrollY > 0) {
            send(1);
        }
        return true;
    }

    @Override
    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slotIn, slotId, mouseButton, type);
        if (slotIn != null && slotId < 81 && slotIn.hasItem()) {
            minecraft.player.playSound(SoundsTC.CLACK.get(), 0.66f, 1.0f);
            selectedSlot = slotId;
            selectedStack = slotIn.getItem();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean in = searchField.isMouseOver(mouseX, mouseY);
        searchField.setFocused(in);
        if (in) {
            searchField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (mouseButton == 0) {
            if (scrollbar.mousePressed(mouseX, mouseY) || countbar.mousePressed(mouseX, mouseY)) {
                return true;
            }
            int b = buttonAt(mouseX, mouseY);
            if (b >= 0) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                actionPerformed(b);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        scrollbar.isMouseDown = false;
        countbar.isMouseDown = false;
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    private int buttonAt(double mx, double my) {
        if (in(mx, my, leftPos + 195, topPos + 16, 10, 10)) return 0;
        if (in(mx, my, leftPos + 195, topPos + 180, 10, 10)) return 1;
        if (selected()) {
            if (in(mx, my, leftPos + 13, topPos + 195, 10, 10)) return 2;
            if (in(mx, my, leftPos + 57, topPos + 195, 10, 10)) return 3;
            if (overRequest(mx, my)) return 7;
        }
        return -1;
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private void actionPerformed(int id) {
        if (id == 1) {
            send(0);
        }
        if (id == 0) {
            send(1);
        }
        if (selected() && menu.getSlot(selectedSlot).hasItem()) {
            ItemStack stack = menu.getSlot(selectedSlot).getItem();
            if (id == 2) {
                --stackSize;
                if (stackSize < 1) {
                    stackSize = 1;
                }
                stacksizeUpdated = true;
            }
            if (id == 3) {
                ++stackSize;
                if (stackSize > stack.getCount()) {
                    stackSize = stack.getCount();
                }
                stacksizeUpdated = true;
            }
            if (id == 7) {
                ItemStack s2 = stack.copy();
                s2.setCount(1);
                PacketDistributor.sendToServer(new PacketLogisticsRequestToServer(con.target, con.side, s2, stackSize));
            }
        }
    }

    // ---- keyboard: the search field takes every key while it has focus, so the inventory key does not close the GUI ----

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            String old = searchField.getValue();
            searchField.keyPressed(keyCode, scanCode, modifiers);
            searchChanged(old);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchField.isFocused()) {
            String old = searchField.getValue();
            searchField.charTyped(codePoint, modifiers);
            searchChanged(old);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void searchChanged(String old) {
        if (!old.equals(searchField.getValue())) {
            PacketDistributor.sendToServer(new PacketMiscStringToServer(0, searchField.getValue()));
        }
    }
}
