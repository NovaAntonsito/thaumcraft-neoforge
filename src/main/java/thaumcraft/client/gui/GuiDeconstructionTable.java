package thaumcraft.client.gui;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.container.ContainerDeconstructionTable;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.crafting.TileDeconstructionTable;

/** Reborn's GuiDeconstructionTable: the break timer, and the aspect left behind, which is taken by clicking it. */
public class GuiDeconstructionTable extends AbstractContainerScreen<ContainerDeconstructionTable> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_decontable.png");
    private final TileDeconstructionTable tableInventory;

    public GuiDeconstructionTable(ContainerDeconstructionTable menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        tableInventory = menu.getTile();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (tableInventory.aspect != null) {
            int var7 = mouseX - (leftPos + 64);
            int var8 = mouseY - (topPos + 48);
            if (var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 300);
                UtilsFX.drawCustomTooltip(graphics, font, List.of(tableInventory.aspect.getDisplayName().getString(),
                        tableInventory.aspect.getLocalizedDescription().getString()), mouseX, mouseY - 8, 11);
                graphics.pose().popPose();
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** The original drew no titles. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        RenderSystem.enableBlend();
        graphics.blit(TEX, x, y, 0, 0, imageWidth, imageHeight);
        if (tableInventory.breaktime > 0) {
            int i1 = tableInventory.getBreakTimeScaled(46);
            graphics.blit(TEX, x + 93, y + 15 + 46 - i1, 176, 46 - i1, 9, i1);
        }
        if (tableInventory.aspect != null) {
            UtilsFX.drawTag(graphics, x + 64, y + 48, tableInventory.aspect, 0.0f, 0);
        }
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int var7 = (int) mx - (leftPos + 64);
        int var8 = (int) my - (topPos + 48);
        if (var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16 && tableInventory.aspect != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            playButtonAspect();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private void playButtonAspect() {
        var e = minecraft.getCameraEntity();
        e.level().playLocalSound(e.getX(), e.getY(), e.getZ(), SoundsTC.HHOFF.get(), SoundSource.BLOCKS, 0.2f,
                1.0f + e.level().random.nextFloat() * 0.1f, false);
    }
}
