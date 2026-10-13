package thaumcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.common.container.ContainerHandMirror;

/** Port of GuiHandMirror: the slot, with the hotbar slot holding the mirror covered. */
public class GuiHandMirror extends AbstractContainerScreen<ContainerHandMirror> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_handmirror.png");
    private final int ci;

    public GuiHandMirror(ContainerHandMirror menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        ci = inventory.selected;
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
        graphics.blit(TEX, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        RenderSystem.enableBlend();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300.0f);
        graphics.blit(TEX, leftPos + 8 + ci * 18, topPos + 142, 240, 0, 16, 16);
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }
}
