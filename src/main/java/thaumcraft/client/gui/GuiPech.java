package thaumcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.common.container.ContainerPech;
import thaumcraft.common.lib.SoundsTC;

/** Port of GuiPech: the dice light up when the offer can be traded, and clicking them rolls. */
public class GuiPech extends AbstractContainerScreen<ContainerPech> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_pech.png");

    public GuiPech(ContainerPech menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 175;
        imageHeight = 232;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** drawGuiContainerForegroundLayer was empty. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        graphics.blit(TEX, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.canTrade()) {
            graphics.blit(TEX, leftPos + 67, topPos + 24, 176, 0, 25, 25);
        }
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        boolean handled = super.mouseClicked(mx, my, button);
        double var7 = mx - (leftPos + 67);
        double var8 = my - (topPos + 24);
        if (var7 >= 0 && var8 >= 0 && var7 < 25 && var8 < 25 && menu.canTrade()) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            minecraft.player.playSound(SoundsTC.PECH_DICE.get(), 0.5f, 0.95f + minecraft.player.getRandom().nextFloat() * 0.1f);
            return true;
        }
        return handled;
    }
}
