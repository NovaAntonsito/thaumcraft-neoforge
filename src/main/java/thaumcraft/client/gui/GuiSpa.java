package thaumcraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import thaumcraft.Thaumcraft;
import thaumcraft.common.container.ContainerSpa;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.devices.TileSpa;

/** Port of GuiSpa: the salts slot, the mix switch and the tank gauge. */
public class GuiSpa extends AbstractContainerScreen<ContainerSpa> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_spa.png");

    public GuiSpa(ContainerSpa menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        TileSpa spa = menu.spa;
        int mposx = mouseX - (leftPos + 104);
        int mposy = mouseY - (topPos + 10);
        if (mposx >= 0 && mposy >= 0 && mposx < 10 && mposy < 55) {
            FluidStack fluid = spa.tank.getFluid();
            if (!fluid.isEmpty()) {
                List<Component> list = new ArrayList<>();
                list.add(fluid.getHoverName());
                list.add(Component.literal(fluid.getAmount() + " mb"));
                graphics.renderComponentTooltip(font, list, mouseX, mouseY);
            }
        }
        mposx = mouseX - (leftPos + 88);
        mposy = mouseY - (topPos + 34);
        if (mposx >= 0 && mposy >= 0 && mposx < 10 && mposy < 10) {
            graphics.renderTooltip(font, Component.translatable(spa.getMix() ? "text.spa.mix.true" : "text.spa.mix.false"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        TileSpa spa = menu.spa;
        int k = leftPos;
        int l = topPos;
        graphics.blit(TEX, k, l, 0, 0, imageWidth, imageHeight);
        if (spa.getMix()) {
            graphics.blit(TEX, k + 89, l + 35, 208, 16, 8, 8);
        } else {
            graphics.blit(TEX, k + 89, l + 35, 208, 32, 8, 8);
        }
        FluidStack fluid = spa.tank.getFluid();
        if (!fluid.isEmpty()) {
            IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
            TextureAtlasSprite icon = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(fluid));
            int cc = ext.getTintColor(fluid);
            float bar = spa.tank.getFluidAmount() / (float) spa.tank.getCapacity();
            // renderFluid: six half-size tiles, then the background over the empty part.
            for (int a = 0; a < 6; ++a) {
                graphics.blit(k + 107, l + 15 + a * 8, 0, 8, 8, icon, ((cc >> 16) & 0xFF) / 255.0f, ((cc >> 8) & 0xFF) / 255.0f,
                        (cc & 0xFF) / 255.0f, 1.0f);
            }
            graphics.blit(TEX, k + 107, l + 15, 107, 15, 10, (int) (48.0f - 48.0f * bar));
        }
        graphics.blit(TEX, k + 106, l + 11, 232, 0, 10, 55);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int var7 = (int) mx - (leftPos + 89);
        int var8 = (int) my - (topPos + 35);
        if (var7 >= 0 && var8 >= 0 && var7 < 8 && var8 < 8) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            minecraft.player.playSound(SoundsTC.CLACK.get(), 0.4f, 1.0f);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }
}
