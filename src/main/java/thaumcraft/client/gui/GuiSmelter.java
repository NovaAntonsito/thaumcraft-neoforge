package thaumcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.common.container.ContainerSmelter;
import thaumcraft.common.tiles.essentia.TileSmelter;

/** Port of GuiSmelter: fuel flame, cook progress and the slurry gauge. */
public class GuiSmelter extends AbstractContainerScreen<ContainerSmelter> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_smelter.png");
    private final TileSmelter furnaceInventory;

    public GuiSmelter(ContainerSmelter menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        furnaceInventory = menu.getTile();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** The original drew no titles. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int k = leftPos;
        int l = topPos;
        RenderSystem.enableBlend();
        graphics.blit(TEX, k, l, 0, 0, imageWidth, imageHeight);
        if (furnaceInventory.getBurnTimeRemainingScaled(20) > 0) {
            int i1 = furnaceInventory.getBurnTimeRemainingScaled(20);
            graphics.blit(TEX, k + 80, l + 26 + 20 - i1, 176, 20 - i1, 16, i1);
        }
        int i1 = furnaceInventory.getCookProgressScaled(46);
        graphics.blit(TEX, k + 106, l + 13 + 46 - i1, 216, 46 - i1, 9, i1);
        i1 = furnaceInventory.getVisScaled(48);
        graphics.blit(TEX, k + 61, l + 12 + 48 - i1, 200, 48 - i1, 8, i1);
        graphics.blit(TEX, k + 60, l + 8, 232, 0, 10, 55);
        RenderSystem.disableBlend();
    }
}
