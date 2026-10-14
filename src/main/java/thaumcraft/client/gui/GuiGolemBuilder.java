package thaumcraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemProperties;
import thaumcraft.api.golems.parts.GolemAddon;
import thaumcraft.api.golems.parts.GolemArm;
import thaumcraft.api.golems.parts.GolemHead;
import thaumcraft.api.golems.parts.GolemLeg;
import thaumcraft.api.golems.parts.GolemMaterial;
import thaumcraft.common.container.ContainerGolemBuilder;
import thaumcraft.common.golems.GolemProperties;
import thaumcraft.common.lib.network.misc.PacketGolemBuilderToServer;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.crafting.TileGolemBuilder;

/**
 * Port of GuiGolemBuilder: pick a material, head, arms, legs and addon among the researched ones, see the traits,
 * stats and components (greyed when missing), and press the golem. The GuiHoverButton / GuiScrollButton /
 * GuiGolemCraftButton widgets are drawn and handled here directly.
 */
public class GuiGolemBuilder extends AbstractContainerScreen<ContainerGolemBuilder> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_golembuilder.png");
    private static final ResourceLocation BASE = Thaumcraft.id("textures/gui/gui_base.png");
    private static final ResourceLocation MAT_ICON = Thaumcraft.id("textures/items/golem.png");
    static int headIndex = 0;
    static int matIndex = 0;
    static int armIndex = 0;
    static int legIndex = 0;
    static int addonIndex = 0;

    private final TileGolemBuilder builder;
    private final Player player;
    private final List<GolemHead> valHeads = new ArrayList<>();
    private final List<GolemMaterial> valMats = new ArrayList<>();
    private final List<GolemArm> valArms = new ArrayList<>();
    private final List<GolemLeg> valLegs = new ArrayList<>();
    private final List<GolemAddon> valAddons = new ArrayList<>();
    private IGolemProperties props = GolemProperties.fromLong(0L);
    private float hearts = 0.0f;
    private float armor = 0.0f;
    private float damage = 0.0f;
    private int cost = 0;
    private boolean allfound = false;
    private ItemStack[] components = null;
    private boolean[] owns = null;
    private boolean disableAll = false;
    private final List<Hover> hovers = new ArrayList<>();
    private final List<Hover> compHovers = new ArrayList<>();
    private final List<Scroll> scrolls = new ArrayList<>();

    /** GuiHoverButton: a 16x16 icon centered on (x, y) with a tooltip. icon is a texture, an aspect or a stack. */
    private record Hover(int x, int y, Component name, Component desc, Object icon, int color) {
        boolean over(int mx, int my) {
            return mx >= x - 8 && my >= y - 8 && mx < x + 8 && my < y + 8;
        }
    }

    /** GuiScrollButton: a 10x10 arrow. */
    private record Scroll(int id, int x, int y, boolean minus) {
        boolean over(double mx, double my) {
            return mx >= x && my >= y && mx < x + 10 && my < y + 10;
        }
    }

    public GuiGolemBuilder(ContainerGolemBuilder menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        builder = menu.getBuilder();
        player = inventory.player;
        imageWidth = 208;
        imageHeight = 224;
    }

    @Override
    protected void init() {
        super.init();
        valHeads.clear();
        for (GolemHead head : GolemHead.getHeads()) {
            if (ThaumcraftCapabilities.knowsResearchStrict(player, head.research)) valHeads.add(head);
        }
        valMats.clear();
        for (GolemMaterial mat : GolemMaterial.getMaterials()) {
            if (ThaumcraftCapabilities.knowsResearchStrict(player, mat.research)) valMats.add(mat);
        }
        valArms.clear();
        for (GolemArm arm : GolemArm.getArms()) {
            if (ThaumcraftCapabilities.knowsResearchStrict(player, arm.research)) valArms.add(arm);
        }
        valLegs.clear();
        for (GolemLeg leg : GolemLeg.getLegs()) {
            if (ThaumcraftCapabilities.knowsResearchStrict(player, leg.research)) valLegs.add(leg);
        }
        valAddons.clear();
        for (GolemAddon addon : GolemAddon.getAddons()) {
            if (ThaumcraftCapabilities.knowsResearchStrict(player, addon.research)) valAddons.add(addon);
        }
        if (headIndex >= valHeads.size()) headIndex = 0;
        if (matIndex >= valMats.size()) matIndex = 0;
        if (armIndex >= valArms.size()) armIndex = 0;
        if (legIndex >= valLegs.size()) legIndex = 0;
        if (addonIndex >= valAddons.size()) addonIndex = 0;
        gatherInfo();
    }

    private void gatherInfo() {
        hovers.clear();
        scrolls.clear();
        if (valHeads.isEmpty() || valMats.isEmpty() || valArms.isEmpty() || valLegs.isEmpty() || valAddons.isEmpty()) {
            components = null;
            allfound = false;
            return;
        }
        int gl = leftPos;
        int gt = topPos;
        if (valHeads.size() > 1) {
            scrolls.add(new Scroll(0, gl + 112 - 5 - 6, gt - 5 + 16 + 8, true));
            scrolls.add(new Scroll(1, gl + 112 - 5 + 22, gt - 5 + 16 + 8, false));
        }
        if (valMats.size() > 1) {
            scrolls.add(new Scroll(2, gl + 16 - 5 - 6, gt - 5 + 16 + 8, true));
            scrolls.add(new Scroll(3, gl + 16 - 5 + 22, gt - 5 + 16 + 8, false));
        }
        if (valArms.size() > 1) {
            scrolls.add(new Scroll(4, gl + 112 - 5 - 6, gt - 5 + 40 + 8, true));
            scrolls.add(new Scroll(5, gl + 112 - 5 + 22, gt - 5 + 40 + 8, false));
        }
        if (valLegs.size() > 1) {
            scrolls.add(new Scroll(6, gl + 112 - 5 - 6, gt - 5 + 64 + 8, true));
            scrolls.add(new Scroll(7, gl + 112 - 5 + 22, gt - 5 + 64 + 8, false));
        }
        if (valAddons.size() > 1) {
            scrolls.add(new Scroll(8, gl + 16 - 5 - 6, gt - 5 + 64 + 8, true));
            scrolls.add(new Scroll(9, gl + 16 - 5 + 22, gt - 5 + 64 + 8, false));
        }
        GolemHead head = valHeads.get(headIndex);
        GolemMaterial mat = valMats.get(matIndex);
        GolemArm arm = valArms.get(armIndex);
        GolemLeg leg = valLegs.get(legIndex);
        GolemAddon addon = valAddons.get(addonIndex);
        hovers.add(new Hover(gl + 120, gt + 24, head.getLocalizedName(), head.getLocalizedDescription(), head.icon, -1));
        hovers.add(new Hover(gl + 24, gt + 24, mat.getLocalizedName(), mat.getLocalizedDescription(), MAT_ICON, mat.itemColor));
        hovers.add(new Hover(gl + 120, gt + 48, arm.getLocalizedName(), arm.getLocalizedDescription(), arm.icon, -1));
        hovers.add(new Hover(gl + 120, gt + 72, leg.getLocalizedName(), leg.getLocalizedDescription(), leg.icon, -1));
        if (!addon.key.equalsIgnoreCase("none")) {
            hovers.add(new Hover(gl + 24, gt + 72, addon.getLocalizedName(), addon.getLocalizedDescription(), addon.icon, -1));
        }
        props = GolemProperties.fromLong(0L);
        props.setHead(head);
        props.setMaterial(mat);
        props.setArms(arm);
        props.setLegs(leg);
        props.setAddon(addon);
        PacketDistributor.sendToServer(new PacketGolemBuilderToServer(builder.getBlockPos(), props.toLong(), true));
        redoComps();
        EnumGolemTrait[] tags = props.getTraits().toArray(new EnumGolemTrait[0]);
        if (tags.length > 0) {
            int yy = tags.length <= 4 ? (tags.length - 1) % 4 * 8 : 24;
            int xx = (tags.length - 1) / 4 % 4 * 8;
            int i = 0;
            int q = 0;
            for (EnumGolemTrait tag : tags) {
                hovers.add(new Hover(gl + 72 + q * 16 - xx, gt + 48 + 16 * i - yy, tag.getLocalizedName(), tag.getLocalizedDescription(), tag.icon, -1));
                if (++i > 3) {
                    i = 0;
                    ++q;
                }
            }
        }
        // The original's "hh *= (int) 0.75" (always 0) is a decompile artifact.
        int hh = 10 + props.getMaterial().healthMod;
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) hh = (int) (hh * 0.75);
        hearts = hh / 2.0f;
        int aa = props.getMaterial().armor;
        if (props.hasTrait(EnumGolemTrait.ARMORED)) aa = (int) Math.max(aa * 1.5, aa + 1);
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) aa = (int) (aa * 0.75);
        armor = aa / 2.0f;
        double dd = props.hasTrait(EnumGolemTrait.FIGHTER) ? props.getMaterial().damage : 0.0;
        if (props.hasTrait(EnumGolemTrait.BRUTAL)) dd = Math.max(dd * 1.5, dd + 1.0);
        damage = (float) (dd / 2.0);
    }

    private void redoComps() {
        compHovers.clear();
        if (props.getMaterial() == null) return;
        allfound = true;
        cost = props.getTraits().size() * 2;
        components = props.generateComponents();
        if (components.length >= 1) {
            owns = new boolean[components.length];
            for (int a = 0; a < components.length; ++a) {
                cost += components[a].getCount();
                owns[a] = builder.hasStuff != null && builder.hasStuff.length > a && builder.hasStuff[a];
                if (!owns[a]) owns[a] = InventoryUtils.isPlayerCarryingAmount(player, components[a]);
                if (!owns[a]) allfound = false;
            }
            compHovers.add(new Hover(leftPos + 152, topPos + 24, Component.literal(Aspect.MECHANISM.getName()),
                    Aspect.MECHANISM.getLocalizedDescription(), Aspect.MECHANISM, -1));
            int i = 1;
            int q = 0;
            for (ItemStack stack : components) {
                compHovers.add(new Hover(leftPos + 152 + q * 16, topPos + 24 + 16 * i, stack.getHoverName(), null, stack, -1));
                if (++i > 3) {
                    i = 0;
                    ++q;
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        for (Hover h : hovers) {
            if (h.over(mouseX, mouseY)) {
                drawHoverTooltip(graphics, h, mouseX, mouseY);
                return;
            }
        }
        for (Hover h : compHovers) {
            if (h.over(mouseX, mouseY)) {
                drawHoverTooltip(graphics, h, mouseX, mouseY);
                return;
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** GuiHoverButton.drawButtonForegroundLayer: a stack's own tooltip, or the name with the description in blue italics. */
    private void drawHoverTooltip(GuiGraphics graphics, Hover h, int mouseX, int mouseY) {
        if (h.icon instanceof ItemStack stack) {
            graphics.renderTooltip(font, stack, mouseX, mouseY);
            return;
        }
        List<Component> text = new ArrayList<>();
        text.add(h.name);
        if (h.desc != null) text.add(h.desc.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE));
        graphics.renderComponentTooltip(font, text, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(TEX, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (components != null && components.length > 0) {
            int i = 1;
            int q = 0;
            graphics.setColor(1.0f, 1.0f, 1.0f, 0.5f);
            for (int a = 0; a < components.length; ++a) {
                if (!owns[a]) {
                    graphics.blit(TEX, leftPos + 144 + q * 16, topPos + 16 + 16 * i, 240, 0, 16, 16);
                }
                if (++i > 3) {
                    i = 0;
                    ++q;
                }
            }
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        if (builder.cost > 0 && builder.maxCost > 0) {
            graphics.blit(TEX, leftPos + 145, topPos + 89, 209, 89, (int) (46.0f * (1.0f - builder.cost / (float) builder.maxCost)), 6);
            if (!disableAll) {
                disableAll = true;
                redoComps();
            }
        } else if (disableAll) {
            disableAll = false;
            redoComps();
        }
        graphics.drawCenteredString(font, "" + hearts, leftPos + 48, topPos + 108, 0xFFFFFF);
        graphics.drawCenteredString(font, "" + armor, leftPos + 72, topPos + 108, 0xFFFFFF);
        graphics.drawCenteredString(font, "" + damage, leftPos + 97, topPos + 108, 0xFFFFFF);
        // GuiGolemCraftButton
        int cx = leftPos + 120;
        int cy = topPos + 104;
        boolean craftOn = !disableAll && allfound;
        boolean craftHover = mouseX >= cx && mouseY >= cy && mouseX < cx + 24 && mouseY < cy + 16;
        float cc = craftOn && craftHover ? 1.0f : 0.9f;
        graphics.setColor(cc, cc, cc, cc);
        graphics.blit(TEX, cx, cy, 216, 64, 24, 16);
        if (!craftOn) graphics.blit(TEX, cx, cy, 216, 40, 24, 16);
        // GuiScrollButton
        for (Scroll s : scrolls) {
            float c = !disableAll && s.over(mouseX, mouseY) ? 1.0f : 0.9f;
            graphics.setColor(c, c, c, c);
            graphics.blit(BASE, s.x, s.y, s.minus ? 20 : 30, 0, 10, 10);
        }
        // GuiHoverButton icons
        for (Hover h : hovers) drawHoverIcon(graphics, h, mouseX, mouseY);
        for (Hover h : compHovers) drawHoverIcon(graphics, h, mouseX, mouseY);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawHoverIcon(GuiGraphics graphics, Hover h, int mouseX, int mouseY) {
        boolean over = !disableAll && h.over(mouseX, mouseY);
        int x = h.x - 8;
        int y = h.y - 8;
        float r = (h.color >> 16 & 0xFF) / 255.0f;
        float g = (h.color >> 8 & 0xFF) / 255.0f;
        float b = (h.color & 0xFF) / 255.0f;
        if (h.icon instanceof Aspect aspect) {
            int c = aspect.getColor();
            float div = over ? 255.0f : 290.0f;
            graphics.setColor((c >> 16 & 0xFF) / div, (c >> 8 & 0xFF) / div, (c & 0xFF) / div, over ? 1.0f : 0.9f);
            graphics.blit(aspect.getImage(), x, y, 0, 0, 16, 16, 16, 16);
        } else if (h.icon instanceof ResourceLocation tex) {
            float m = over ? 1.0f : 0.9f;
            graphics.setColor(r * m, g * m, b * m, m);
            graphics.blit(tex, x, y, 0, 0, 16, 16, 16, 16);
        } else if (h.icon instanceof ItemStack stack) {
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            graphics.renderItem(stack, x, y - (over ? 1 : 0));
            graphics.renderItemDecorations(font, stack, x, y - (over ? 1 : 0));
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (components != null && components.length > 0) {
            graphics.drawString(font, "" + cost, 162 - font.width("" + cost), 24, 0xFFFFFF);
        }
        graphics.blit(TEX, 12, 12, 228, 124, 24, 24);
        graphics.blit(TEX, 12, 60, 228, 124, 24, 24);
        graphics.blit(TEX, 108, 12, 228, 124, 24, 24);
        graphics.blit(TEX, 108, 36, 228, 124, 24, 24);
        graphics.blit(TEX, 108, 60, 228, 124, 24, 24);
        if (ContainerGolemBuilder.redo) {
            redoComps();
            ContainerGolemBuilder.redo = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !disableAll) {
            for (Scroll s : scrolls) {
                if (s.over(mouseX, mouseY)) {
                    scroll(s.id);
                    return true;
                }
            }
            int cx = leftPos + 120;
            int cy = topPos + 104;
            if (allfound && mouseX >= cx && mouseY >= cy && mouseX < cx + 24 && mouseY < cy + 16) {
                PacketDistributor.sendToServer(new PacketGolemBuilderToServer(builder.getBlockPos(), props.toLong(), false));
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 99);
                disableAll = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void scroll(int id) {
        switch (id) {
            case 0 -> headIndex = headIndex - 1 < 0 ? valHeads.size() - 1 : headIndex - 1;
            case 1 -> headIndex = headIndex + 1 >= valHeads.size() ? 0 : headIndex + 1;
            case 2 -> matIndex = matIndex - 1 < 0 ? valMats.size() - 1 : matIndex - 1;
            case 3 -> matIndex = matIndex + 1 >= valMats.size() ? 0 : matIndex + 1;
            case 4 -> armIndex = armIndex - 1 < 0 ? valArms.size() - 1 : armIndex - 1;
            case 5 -> armIndex = armIndex + 1 >= valArms.size() ? 0 : armIndex + 1;
            case 6 -> legIndex = legIndex - 1 < 0 ? valLegs.size() - 1 : legIndex - 1;
            case 7 -> legIndex = legIndex + 1 >= valLegs.size() ? 0 : legIndex + 1;
            case 8 -> addonIndex = addonIndex - 1 < 0 ? valAddons.size() - 1 : addonIndex - 1;
            case 9 -> addonIndex = addonIndex + 1 >= valAddons.size() ? 0 : addonIndex + 1;
            default -> {
            }
        }
        gatherInfo();
    }
}
