package thaumcraft.client.gui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.tiles.crafting.TileInfusionMatrix;
import thaumcraft.common.tiles.essentia.TileAlembic;
import thaumcraft.common.tiles.essentia.TileJarFillable;

/**
 * Temporary readout for essentia containers: looking at a jar or alembic shows its essentia and amount next to
 * the crosshair. In TC6 this comes from the goggles of revealing (drawTagsOnContainer); until they are ported
 * it is always on.
 */
public final class EssentiaHud {
    public static final ResourceLocation ID = Thaumcraft.id("essentia_hud");

    private EssentiaHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.level == null || mc.screen != null
                || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockEntity be = mc.level.getBlockEntity(hit.getBlockPos());
        if (be instanceof TileJarFillable jar) {
            draw(graphics, mc, jar.aspect, jar.amount, TileJarFillable.CAPACITY, jar.aspectFilter);
        } else if (be instanceof TileAlembic alembic) {
            draw(graphics, mc, alembic.aspect, alembic.amount, alembic.maxAmount, alembic.aspectFilter);
        } else if (be instanceof TileInfusionMatrix matrix && matrix.active) {
            drawMatrix(graphics, mc, matrix);
        }
    }

    /** The matrix's stability text (getIGogglesText) and, while crafting, the essentia it still needs. */
    private static void drawMatrix(GuiGraphics graphics, Minecraft mc, TileInfusionMatrix matrix) {
        int x = graphics.guiWidth() / 2 + 10;
        int y = graphics.guiHeight() / 2 - 8;
        for (Component line : matrix.getIGogglesText()) {
            graphics.drawString(mc.font, line, x, y, 0xFFFFFF, true);
            y += 10;
        }
        AspectList needed = matrix.getAspects();
        if (matrix.crafting && !needed.isEmpty()) {
            y += 2;
            int ax = x;
            for (Aspect aspect : needed.getAspects()) {
                drawIcon(graphics, aspect, ax, y);
                graphics.drawString(mc.font, String.valueOf(needed.getAmount(aspect)), ax + 10, y + 9, 0xFFFFFF, true);
                ax += 22;
            }
        }
    }

    private static void draw(GuiGraphics graphics, Minecraft mc, @Nullable Aspect aspect, int amount, int capacity,
                             @Nullable Aspect filter) {
        int x = graphics.guiWidth() / 2 + 10;
        int y = graphics.guiHeight() / 2 - 8;
        MutableComponent text;
        if (aspect != null && amount > 0) {
            drawIcon(graphics, aspect, x, y);
            text = aspect.getDisplayName()
                    .append(Component.literal(" " + amount + "/" + capacity).withStyle(ChatFormatting.WHITE));
        } else {
            if (filter != null) {
                drawIcon(graphics, filter, x, y);
            }
            text = Component.translatable("tooltip.thaumcraft.essentia.empty").withStyle(ChatFormatting.GRAY);
            if (filter != null) {
                text.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                        .append(filter.getDisplayName())
                        .append(Component.literal(")").withStyle(ChatFormatting.GRAY));
            }
        }
        int textX = aspect != null && amount > 0 || filter != null ? x + 18 : x;
        graphics.drawString(mc.font, text, textX, y + 4, 0xFFFFFF, true);
    }

    /** The aspect's 32x32 icon at 16x16, tinted with its color like UtilsFX.drawTag. */
    private static void drawIcon(GuiGraphics graphics, Aspect aspect, int x, int y) {
        int c = aspect.getColor();
        RenderSystem.enableBlend();
        graphics.setColor(((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f, 1.0f);
        graphics.blit(aspect.getImage(), x, y, 16, 16, 0, 0, 32, 32, 32, 32);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}
