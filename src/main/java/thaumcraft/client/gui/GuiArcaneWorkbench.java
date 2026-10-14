package thaumcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.container.ContainerArcaneWorkbench;
import thaumcraft.common.tiles.crafting.TileArcaneWorkbench;

/** Port of GuiArcaneWorkbench: shows the vis available and the cost, and makes the needed crystal slots glow. */
public class GuiArcaneWorkbench extends AbstractContainerScreen<ContainerArcaneWorkbench> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/arcaneworkbench.png");
    private final TileArcaneWorkbench tileEntity;

    public GuiArcaneWorkbench(ContainerArcaneWorkbench menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        tileEntity = menu.getTile();
        imageHeight = 234;
        imageWidth = 190;
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
        int var5 = leftPos;
        int var6 = topPos;
        RenderSystem.enableBlend();
        graphics.blit(TEX, var5, var6, 0, 0, imageWidth, imageHeight);
        int cost = 0;
        IArcaneRecipe result = menu.currentArcaneRecipe();
        AspectList crystals = null;
        // TODO: vis discount from casters and robes (CasterManager.getTotalVisDiscount).
        if (result != null) {
            cost = thaumcraft.common.anomaly.AnomalyModifiers.arcaneVis(minecraft != null ? minecraft.level : null, result.getVis());
            crystals = result.getCrystals();
        }
        if (crystals != null) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            int ticks = minecraft != null && minecraft.player != null ? minecraft.player.tickCount : 0;
            for (Aspect a : crystals.getAspects()) {
                int id = crystalSlot(a);
                if (id < 0) continue;
                int col = a.getColor();
                graphics.setColor((col >> 16 & 255) / 255.0f, (col >> 8 & 255) / 255.0f, (col & 255) / 255.0f, 0.33f);
                graphics.pose().pushPose();
                graphics.pose().translate(var5 + ContainerArcaneWorkbench.xx[id] + 7.5f, var6 + ContainerArcaneWorkbench.yy[id] + 8.0f, 0.0f);
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(id * 60 + ticks % 360 + partialTicks));
                graphics.pose().scale(0.5f, 0.5f, 1.0f);
                graphics.blit(TEX, -32, -32, 192, 0, 64, 64);
                graphics.pose().popPose();
            }
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.disableBlend();
        graphics.pose().pushPose();
        graphics.pose().translate(var5 + 168, var6 + 46, 0.0f);
        graphics.pose().scale(0.5f, 0.5f, 1.0f);
        String text = tileEntity.auraVisClient + " " + Component.translatable("workbench.available").getString();
        int ll = font.width(text) / 2;
        graphics.drawString(font, text, -ll, 0, tileEntity.auraVisClient < cost ? 0xEE6E6E : 0x6E6EEE, false);
        graphics.pose().popPose();
        if (cost > 0) {
            if (tileEntity.auraVisClient < cost && minecraft != null && minecraft.level != null) {
                // The result it would make, greyed out.
                ItemStack ghost = result.getResultItem(minecraft.level.registryAccess());
                graphics.renderItem(ghost, var5 + 160, var6 + 64);
                graphics.renderItemDecorations(font, ghost, var5 + 160, var6 + 64, "");
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 300);
                graphics.fill(var5 + 160, var6 + 64, var5 + 176, var6 + 80, 0xA8545454);
                graphics.pose().popPose();
            }
            graphics.pose().pushPose();
            graphics.pose().translate(var5 + 168, var6 + 38, 0.0f);
            graphics.pose().scale(0.5f, 0.5f, 1.0f);
            text = cost + " " + Component.translatable("workbench.cost").getString();
            ll = font.width(text) / 2;
            graphics.drawString(font, text, -ll, 0, 0xC0FFFF, false);
            graphics.pose().popPose();
        }
    }

    private static int crystalSlot(Aspect aspect) {
        for (int i = 0; i < ContainerArcaneWorkbench.CRYSTALS.length; i++) {
            if (ContainerArcaneWorkbench.CRYSTALS[i] == aspect) return i;
        }
        return -1;
    }
}
