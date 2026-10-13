package thaumcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.common.container.ContainerFocusPouch;

/** Port of GuiFocusPouch: the 18 slots, with the hotbar slot holding the pouch covered. */
public class GuiFocusPouch extends AbstractContainerScreen<ContainerFocusPouch> {
    private static final ResourceLocation tex = Thaumcraft.id("textures/gui/gui_focuspouch.png");
    private final int blockSlot;
    private final Inventory inventory;

    public GuiFocusPouch(ContainerFocusPouch menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventory = inventory;
        blockSlot = inventory.selected;
        imageWidth = 175;
        imageHeight = 232;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.hand != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return;
        }
        RenderSystem.enableBlend();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300.0f);
        graphics.blit(tex, 8 + blockSlot * 18, 209, 240, 0, 16, 16);
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        if (menu.hand == net.minecraft.world.InteractionHand.MAIN_HAND && inventory.items.get(blockSlot).isEmpty() && minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
            return;
        }
        RenderSystem.enableBlend();
        graphics.blit(tex, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }
}
