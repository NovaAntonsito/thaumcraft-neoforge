package thaumcraft.common.golems.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import thaumcraft.Thaumcraft;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.seals.ISealConfigFilter;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.ISealGui;

/**
 * Port of SealBaseGUI. The GuiGolemCategoryButton, GuiGolemRedstoneButton, GuiPlusMinusButton, GuiGolemLockButton,
 * GuiGolemBWListButton, GuiGolemPropButton and GuiHoverButton widgets are drawn and handled here directly.
 */
public class SealBaseGUI extends AbstractContainerScreen<SealBaseContainer> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_base.png");

    private enum Kind { CATEGORY, REDSTONE, PLUSMINUS, LOCK, BWLIST, PROP, HOVER }

    /** One of the original GuiButtons. x/y are the top left corner, except CATEGORY and HOVER where they are the centre. */
    private record Btn(Kind kind, int id, int x, int y, int w, int h, int icon, boolean flag, Object data) {
        int left() {
            return kind == Kind.CATEGORY ? x - 8 : kind == Kind.HOVER ? x - w / 2 : x;
        }

        int top() {
            return kind == Kind.CATEGORY ? y - 8 : kind == Kind.HOVER ? y - h / 2 : y;
        }

        boolean over(double mx, double my) {
            return mx >= left() && my >= top() && mx < left() + w && my < top() + h;
        }
    }

    private final ISealEntity seal;
    private final int middleX;
    private final int middleY;
    private int category = -1;
    private final int[] categories;
    private final List<Btn> buttons = new ArrayList<>();

    public SealBaseGUI(SealBaseContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        seal = menu.getSeal();
        imageWidth = 176;
        imageHeight = 232;
        middleX = imageWidth / 2;
        middleY = (imageHeight - 72) / 2 - 8;
        if (seal.getSeal() instanceof ISealGui gui) {
            categories = gui.getGuiCategories();
        } else {
            categories = new int[] {0, 4};
        }
    }

    @Override
    protected void init() {
        super.init();
        setupCategories();
    }

    private boolean isOwner() {
        return seal.getOwner() != null && seal.getOwner().equals(minecraft.player.getUUID().toString());
    }

    private void setupCategories() {
        buttons.clear();
        int c = 0;
        float slice = 60.0f / categories.length;
        float start = -180.0f + (categories.length - 1) * slice / 2.0f;
        if (slice > 24.0f) {
            slice = 24.0f;
        }
        if (slice < 12.0f) {
            slice = 12.0f;
        }
        for (int cat : categories) {
            if (category < 0) {
                category = cat;
            }
            if (categories.length > 1) {
                int xx = (int) (Mth.cos((start - c * slice) / 180.0f * (float) Math.PI) * 86.0f);
                int yy = (int) (Mth.sin((start - c * slice) / 180.0f * (float) Math.PI) * 86.0f);
                buttons.add(new Btn(Kind.CATEGORY, c, leftPos + middleX + xx, topPos + middleY + yy, 16, 16, cat, category == cat, "button.category." + cat));
            }
            ++c;
        }
        int xx2 = (int) (Mth.cos((start - c * slice) / 180.0f * (float) Math.PI) * 86.0f);
        int yy2 = (int) (Mth.sin((start - c * slice) / 180.0f * (float) Math.PI) * 86.0f);
        buttons.add(new Btn(Kind.REDSTONE, 27, leftPos + middleX + xx2 - 8, topPos + middleY + yy2 - 8, 16, 16, 0, false, null));
        switch (category) {
            case 0 -> {
                plusMinus(80, leftPos + middleX - 5 - 14, topPos + middleY - 17, true);
                plusMinus(81, leftPos + middleX - 5 + 14, topPos + middleY - 17, false);
                plusMinus(82, leftPos + middleX + 18 - 12, topPos + middleY + 4, true);
                plusMinus(83, leftPos + middleX + 18 + 11, topPos + middleY + 4, false);
                buttons.add(new Btn(Kind.LOCK, 25, leftPos + middleX - 32, topPos + middleY, 16, 16, 0, false, null));
            }
            case 1 -> {
                if (seal.getSeal() instanceof ISealConfigFilter cf) {
                    int s = cf.getFilterSize();
                    int sy = 16 + (s - 1) / 3 * 12;
                    buttons.add(new Btn(Kind.BWLIST, 20, leftPos + middleX - 8, topPos + middleY + (s - 1) / 3 * 24 - sy + 27, 16, 16, 0, false, null));
                }
            }
            case 2 -> {
                plusMinus(90, leftPos + middleX - 5 - 14, topPos + middleY - 25, true);
                plusMinus(91, leftPos + middleX - 5 + 14, topPos + middleY - 25, false);
                plusMinus(92, leftPos + middleX - 5 - 14, topPos + middleY, true);
                plusMinus(93, leftPos + middleX - 5 + 14, topPos + middleY, false);
                plusMinus(94, leftPos + middleX - 5 - 14, topPos + middleY + 25, true);
                plusMinus(95, leftPos + middleX - 5 + 14, topPos + middleY + 25, false);
            }
            case 3 -> {
                if (seal.getSeal() instanceof ISealConfigToggles cp) {
                    int n = cp.getToggles().length;
                    int s2 = n < 4 ? 8 : n < 6 ? 7 : n < 9 ? 6 : 5;
                    int h = (n - 1) * s2;
                    int w = 12;
                    for (ISealConfigToggles.SealToggle prop : cp.getToggles()) {
                        int ww = 12 + Math.min(100, font.width(Component.translatable(prop.getName())));
                        ww /= 2;
                        if (ww > w) {
                            w = ww;
                        }
                    }
                    int p = 0;
                    for (ISealConfigToggles.SealToggle prop : cp.getToggles()) {
                        buttons.add(new Btn(Kind.PROP, 30 + p, leftPos + middleX - w, topPos + middleY - 5 - h + p * (s2 * 2), 8, 8, 0, false, prop));
                        ++p;
                    }
                }
            }
            case 4 -> {
                if (seal.getSeal() == null) {
                    break;
                }
                traitButtons(seal.getSeal().getRequiredTags(), 500, topPos + middleY - 8);
                traitButtons(seal.getSeal().getForbiddenTags(), 600, topPos + middleY + 24);
            }
            default -> {
            }
        }
    }

    private void plusMinus(int id, int x, int y, boolean minus) {
        buttons.add(new Btn(Kind.PLUSMINUS, id, x, y, 10, 10, 0, minus, null));
    }

    private void traitButtons(EnumGolemTrait[] tags, int baseId, int y) {
        if (tags != null && tags.length > 0) {
            int p = 0;
            for (EnumGolemTrait tag : tags) {
                buttons.add(new Btn(Kind.HOVER, baseId + p, leftPos + middleX + p * 18 - (tags.length - 1) * 9, y, 16, 16, 0, false, tag));
                ++p;
            }
        }
    }

    @Override
    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(TEX, leftPos + middleX - 80, topPos + middleY - 80, 96, 0, 160, 160);
        graphics.blit(TEX, leftPos, topPos + 143, 0, 167, 176, 89);
        graphics.drawCenteredString(font, Component.translatable("button.category." + category), leftPos + middleX, topPos + middleY - 64, 0xFFFFFF);
        switch (category) {
            case 0 -> {
                graphics.blit(TEX, leftPos + middleX + 17, topPos + middleY + 3, 2, 18, 12, 12);
                if (seal.getColor() >= 1 && seal.getColor() <= 16) {
                    int c = DyeColor.byId(seal.getColor() - 1).getTextureDiffuseColor();
                    graphics.setColor((c >> 16 & 0xFF) / 255.0f, (c >> 8 & 0xFF) / 255.0f, (c & 0xFF) / 255.0f, 1.0f);
                    graphics.blit(TEX, leftPos + middleX + 20, topPos + middleY + 6, 74, 31, 6, 6);
                    graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
                int mx = mouseX - leftPos;
                int my = mouseY - topPos;
                if (mx >= middleX + 5 && mx <= middleX + 41 && my >= middleY + 3 && my <= middleY + 15) {
                    Component s;
                    if (seal.getColor() >= 1 && seal.getColor() <= 16) {
                        s = Component.translatable("golem.prop.color", Component.translatable("color." + colorName(seal.getColor())));
                    } else {
                        s = Component.translatable("golem.prop.colorall");
                    }
                    graphics.drawCenteredString(font, s, leftPos + middleX + 23, topPos + middleY + 17, 0xFFFFFF);
                }
                graphics.drawCenteredString(font, Component.translatable("golem.prop.priority"), leftPos + middleX, topPos + middleY - 28, 12299007);
                graphics.drawCenteredString(font, "" + seal.getPriority(), leftPos + middleX, topPos + middleY - 16, 0xFFFFFF);
                if (isOwner()) {
                    graphics.drawCenteredString(font, Component.translatable("golem.prop.owner"), leftPos + middleX, topPos + middleY + 32, 12299007);
                }
            }
            case 1 -> {
                if (seal.getSeal() instanceof ISealConfigFilter cf) {
                    int s3 = cf.getFilterSize();
                    int sx = 16 + (s3 - 1) % 3 * 12;
                    int sy = 16 + (s3 - 1) / 3 * 12;
                    for (int a = 0; a < s3; ++a) {
                        int x = a % 3;
                        int y = a / 3;
                        graphics.blit(TEX, leftPos + middleX + x * 24 - sx, topPos + middleY + y * 24 - sy, 0, 56, 32, 32);
                    }
                }
            }
            case 2 -> {
                graphics.drawCenteredString(font, Component.translatable("button.caption.y"), leftPos + middleX, topPos + middleY - 24 - 9, 14540253);
                graphics.drawCenteredString(font, Component.translatable("button.caption.x"), leftPos + middleX, topPos + middleY - 9, 14540253);
                graphics.drawCenteredString(font, Component.translatable("button.caption.z"), leftPos + middleX, topPos + middleY + 24 - 9, 14540253);
                graphics.drawCenteredString(font, "" + seal.getArea().getY(), leftPos + middleX, topPos + middleY - 24, 0xFFFFFF);
                graphics.drawCenteredString(font, "" + seal.getArea().getX(), leftPos + middleX, topPos + middleY, 0xFFFFFF);
                graphics.drawCenteredString(font, "" + seal.getArea().getZ(), leftPos + middleX, topPos + middleY + 24, 0xFFFFFF);
            }
            case 4 -> {
                graphics.drawCenteredString(font, Component.translatable("button.caption.required"), leftPos + middleX, topPos + middleY - 26, 14540253);
                graphics.drawCenteredString(font, Component.translatable("button.caption.forbidden"), leftPos + middleX, topPos + middleY + 6, 14540253);
            }
            default -> {
            }
        }
        for (Btn b : buttons) {
            drawButton(graphics, b, mouseX, mouseY);
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /** 1.12 EnumDyeColor names: light gray was "silver", which is what the lang keys use. */
    private static String colorName(int color) {
        DyeColor dye = DyeColor.byId(color - 1);
        return dye == DyeColor.LIGHT_GRAY ? "silver" : dye.getName();
    }

    private void drawButton(GuiGraphics graphics, Btn b, int mouseX, int mouseY) {
        boolean over = b.over(mouseX, mouseY);
        switch (b.kind) {
            case CATEGORY -> {
                float c = b.flag || over ? 1.0f : 0.7f;
                graphics.setColor(c, c, c, c);
                graphics.blit(TEX, b.x - 8, b.y - 8, b.icon * 16, 120, 16, 16);
            }
            case REDSTONE -> {
                setHoverColor(graphics, over);
                graphics.blit(TEX, b.x, b.y, seal.isRedstoneSensitive() ? 64 : 80, 136, 16, 16);
            }
            case PLUSMINUS -> {
                setHoverColor(graphics, over);
                graphics.blit(TEX, b.x, b.y, b.flag ? 0 : 10, 0, 10, 10);
            }
            case LOCK -> {
                setHoverColor(graphics, over);
                graphics.blit(TEX, b.x, b.y, seal.isLocked() ? 32 : 48, 136, 16, 16);
            }
            case BWLIST -> {
                setHoverColor(graphics, over);
                boolean black = seal.getSeal() instanceof ISealConfigFilter cf && cf.isBlacklist();
                graphics.blit(TEX, b.x, b.y, black ? 0 : 16, 136, 16, 16);
            }
            case PROP -> {
                ISealConfigToggles.SealToggle prop = (ISealConfigToggles.SealToggle) b.data;
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                graphics.blit(TEX, b.x - 2, b.y - 2, 2, 18, 12, 12);
                if (prop.getValue()) {
                    graphics.blit(TEX, b.x - 2, b.y - 2, 18, 18, 12, 12);
                }
                graphics.drawString(font, Component.translatable(prop.getName()), b.x + 12, b.y, 0xFFFFFF);
            }
            case HOVER -> {
                EnumGolemTrait tag = (EnumGolemTrait) b.data;
                setHoverColor(graphics, over);
                graphics.blit(tag.icon, b.left(), b.top(), 0, 0, 16, 16, 16, 16);
            }
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void setHoverColor(GuiGraphics graphics, boolean over) {
        float c = over ? 1.0f : 0.9f;
        graphics.setColor(c, c, c, c);
    }

    /** The hover texts the buttons drew in 1.12, and the GuiHoverButton tooltip. Drawn above the slots. */
    private void drawButtonOverlay(GuiGraphics graphics, Btn b) {
        switch (b.kind) {
            case CATEGORY -> {
                Component s = Component.translatable((String) b.data);
                graphics.drawString(font, s, b.x - 10 - font.width(s), b.y - 4, 0xFFFFFF);
            }
            case REDSTONE -> {
                Component s = Component.translatable(seal.isRedstoneSensitive() ? "golem.prop.redon" : "golem.prop.redoff");
                graphics.drawString(font, s, b.x - 2 - font.width(s), b.y + 4, 0xFFFFFF);
            }
            case LOCK -> graphics.drawCenteredString(font, Component.translatable(seal.isLocked() ? "golem.prop.lock" : "golem.prop.unlock"), b.x + 8, b.y + 17, 0xFFFFFF);
            case BWLIST -> {
                boolean black = seal.getSeal() instanceof ISealConfigFilter cf && cf.isBlacklist();
                graphics.drawCenteredString(font, Component.translatable(black ? "button.bl" : "button.wl"), b.x + 8, b.y + 17, 0xFFFFFF);
            }
            default -> {
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 300.0f);
        if (category == 1 && seal.getSeal() instanceof ISealConfigFilter cp && !cp.isBlacklist()) {
            for (int i1 = 0; i1 < cp.getFilterSize() && i1 < menu.slots.size(); ++i1) {
                Slot slot = menu.slots.get(i1);
                if (slot.isActive() && !slot.getItem().isEmpty()) {
                    int sz = cp.getFilterSlotSize(i1);
                    String s = sz == 0 ? ChatFormatting.GOLD + "*" : String.valueOf(sz);
                    graphics.drawString(font, s, leftPos + slot.x + 19 - 2 - font.width(s), topPos + slot.y + 6 + 3, 0xFFFFFF);
                }
            }
        }
        Btn hovered = null;
        for (Btn b : buttons) {
            if (b.over(mouseX, mouseY)) {
                hovered = b;
                drawButtonOverlay(graphics, b);
                break;
            }
        }
        graphics.pose().popPose();
        if (hovered != null && hovered.kind == Kind.HOVER) {
            EnumGolemTrait tag = (EnumGolemTrait) hovered.data;
            List<Component> text = new ArrayList<>();
            text.add(tag.getLocalizedName());
            text.add(tag.getLocalizedDescription().copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE));
            graphics.renderComponentTooltip(font, text, mouseX + 4, mouseY);
            return;
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Btn b : buttons) {
                if (b.kind != Kind.HOVER && b.over(mouseX, mouseY)) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    actionPerformed(b);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private void actionPerformed(Btn button) {
        int id = button.id;
        if (button.kind == Kind.CATEGORY && id < categories.length && categories[id] != category) {
            category = categories[id];
            menu.category = category;
            menu.setupCategories();
            send(id);
            setupCategories();
        } else if (category == 0 && id == 25 && isOwner()) {
            seal.setLocked(!seal.isLocked());
            send(seal.isLocked() ? 25 : 26);
        } else if (category == 1 && seal.getSeal() instanceof ISealConfigFilter cp && id == 20) {
            cp.setBlacklist(!cp.isBlacklist());
            send(cp.isBlacklist() ? 20 : 21);
        } else if (category == 3 && seal.getSeal() instanceof ISealConfigToggles cp2 && id >= 30 && id < 30 + cp2.getToggles().length) {
            cp2.setToggle(id - 30, !cp2.getToggles()[id - 30].getValue());
            send(cp2.getToggles()[id - 30].getValue() ? id : id + 30);
        } else if (id == 27 && isOwner()) {
            seal.setRedstoneSensitive(!seal.isRedstoneSensitive());
            send(seal.isRedstoneSensitive() ? 27 : 28);
        } else if (button.kind != Kind.CATEGORY) {
            send(id);
        }
    }
}
