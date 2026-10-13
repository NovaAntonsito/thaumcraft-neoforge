package thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.container.ContainerPotionSprayer;
import thaumcraft.common.tiles.devices.TilePotionSprayer;

/** Port of GuiPotionSprayer: the recipe aspects with their fill bars and the charge gauge. */
public class GuiPotionSprayer extends AbstractContainerScreen<ContainerPotionSprayer> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_potion_sprayer.png");

    public GuiPotionSprayer(ContainerPotionSprayer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 192;
        imageHeight = 233;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        TilePotionSprayer inventory = menu.sprayer;
        int k = leftPos;
        int l = topPos;
        graphics.blit(TEX, k, l, 0, 0, imageWidth, imageHeight);
        if (inventory.charges > 0) {
            int c = inventory.color;
            graphics.setColor(((c >> 16) & 0xFF) / 255.0f, ((c >> 8) & 0xFF) / 255.0f, (c & 0xFF) / 255.0f, 1.0f);
            int scroll = minecraft.player.tickCount % 256;
            graphics.blit(TEX, k + 128, l + 36 + (8 - inventory.charges) * 9, 232, scroll, 8, inventory.charges * 9);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        drawAspects(graphics, inventory, k, l);
        graphics.blit(TEX, k + 125, l + 28, 205, 28, 14, 88);
    }

    private void drawAspects(GuiGraphics graphics, TilePotionSprayer inventory, int k, int l) {
        int pos = 0;
        for (Aspect aspect : inventory.recipe.getAspectsSortedByName()) {
            graphics.setColor(0.2f, 0.2f, 0.2f, 1.0f);
            graphics.blit(TEX, k + 96 + 22 * (pos % 2), l + 46 + 16 * (pos / 2) - 14, 192, 56, 2, 14);
            int i1 = (int) (inventory.recipeProgress.getAmount(aspect) / (float) inventory.recipe.getAmount(aspect) * 14.0f);
            int c = aspect.getColor();
            graphics.setColor(((c >> 16) & 0xFF) / 255.0f, ((c >> 8) & 0xFF) / 255.0f, (c & 0xFF) / 255.0f, 1.0f);
            graphics.blit(TEX, k + 96 + 22 * (pos % 2), l + 46 + 16 * (pos / 2) - i1, 192, 56, 2, i1);
            ++pos;
        }
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        pos = 0;
        for (Aspect aspect : inventory.recipe.getAspectsSortedByName()) {
            UtilsFX.drawTag(graphics, k + 79 + 22 * (pos % 2), l + 31 + 16 * (pos / 2), aspect, (float) inventory.recipe.getAmount(aspect), 0);
            ++pos;
        }
    }
}
