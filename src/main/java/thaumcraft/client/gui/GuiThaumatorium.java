package thaumcraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

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
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.container.ContainerThaumatorium;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer;
import thaumcraft.common.tiles.crafting.TileThaumatorium;

/**
 * Port of GuiThaumatorium. Under the catalyst slot, two columns of three recipes (scrolled by the arrows); a
 * click picks or drops one. On the right, the essentia of the picked recipes (one per second when there are
 * several) with how much has been drawn in.
 */
public class GuiThaumatorium extends AbstractContainerScreen<ContainerThaumatorium> {
    private static final ResourceLocation TEX = Thaumcraft.id("textures/gui/gui_thaumatorium.png");
    private final TileThaumatorium inventory;
    private final Player player;
    private int index = 0;
    private List<ResourceLocation> hashList = new ArrayList<>();
    private long lastHLUpdate = 0L;

    public GuiThaumatorium(ContainerThaumatorium menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 175;
        imageHeight = 216;
        inventory = menu.getTile();
        player = inv.player;
        inventory.updateRecipes(player);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        ResourceLocation hovered = recipeAt(mouseX, mouseY);
        CrucibleRecipe cr = hovered != null ? inventory.getRecipe(hovered) : null;
        if (cr != null) {
            graphics.renderTooltip(font, cr.getRecipeOutput(), mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mx, int my) {
        int k = leftPos;
        int l = topPos;
        RenderSystem.enableBlend();
        graphics.blit(TEX, k, l, 0, 0, imageWidth, imageHeight);
        long t = System.currentTimeMillis();
        if (t > lastHLUpdate) {
            // The client works the list out itself; the original did it when the catalyst slot changed.
            inventory.updateRecipes(player);
            hashList = inventory.generateRecipeHashlist();
            lastHLUpdate = t + 500L;
        }
        if (!hashList.isEmpty()) {
            if (index > hashList.size() / 2) {
                index = hashList.size() / 2;
            }
            if (index < 0 || hashList.size() <= 6) {
                index = 0;
            }
            if (hashList.size() > 6) {
                if (index > 0) {
                    graphics.blit(TEX, k + 82, l + 56, 176, 56, 8, 11);
                }
                if (index < hashList.size() / 2.0f - 3.0f) {
                    graphics.blit(TEX, k + 82, l + 93, 176, 93, 8, 11);
                }
            }
        }
        drawAspects(graphics, k, l, t);
        if (inventory.maxRecipes > 1) {
            graphics.pose().pushPose();
            graphics.pose().translate(k + 64, l + 48, 0.0f);
            graphics.pose().scale(0.5f, 0.5f, 1.0f);
            String text = inventory.recipeHash.size() + "/" + inventory.maxRecipes;
            int ll = font.width(text) / 2;
            graphics.drawString(font, text, -ll, 0, 0xFFFFFF, false);
            graphics.pose().popPose();
        }
        drawOutput(graphics, k, l, t);
        RenderSystem.disableBlend();
    }

    private void drawAspects(GuiGraphics graphics, int k, int l, long time) {
        if (inventory.recipeHash.isEmpty()) {
            return;
        }
        ResourceLocation hash = inventory.recipeHash.get((int) (time / 1000L % inventory.recipeHash.size()));
        CrucibleRecipe cr = inventory.getRecipe(hash);
        if (cr == null) {
            return;
        }
        int count = 0;
        int px = 0;
        int py = 0;
        for (Aspect aspect : cr.getAspects().getAspectsSortedByName()) {
            graphics.blit(TEX, k + 98 + 16 * px, l + 40 + 20 * py, 176, 4, 12, 3);
            int i1 = (int) (inventory.essentia.getAmount(aspect) / (float) cr.getAspects().getAmount(aspect) * 12.0f);
            int c = aspect.getColor();
            graphics.setColor((c >> 16 & 0xFF) / 255.0f, (c >> 8 & 0xFF) / 255.0f, (c & 0xFF) / 255.0f, 1.0f);
            graphics.blit(TEX, k + 98 + 16 * px, l + 40 + 20 * py, 176, 0, i1, 3);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (++px > 1) {
                px = 0;
                ++py;
            }
            if (++count >= 8) {
                break;
            }
        }
        count = 0;
        px = 0;
        py = 0;
        for (Aspect aspect : cr.getAspects().getAspectsSortedByName()) {
            UtilsFX.drawTag(graphics, k + 96 + 16 * px, l + 24 + 20 * py, aspect, cr.getAspects().getAmount(aspect), 0);
            if (++px > 1) {
                px = 0;
                ++py;
            }
            if (++count >= 8) {
                break;
            }
        }
    }

    private void drawOutput(GuiGraphics graphics, int x, int y, long time) {
        int px = 0;
        int py = 0;
        int q = 0;
        int idx = 0;
        for (ResourceLocation hash : hashList) {
            if (q++ < index * 2) {
                continue;
            }
            CrucibleRecipe cr = inventory.getRecipe(hash);
            if (cr == null) {
                continue;
            }
            drawOutputIcon(graphics, x + 48 + px * 16, y + 56 + py * 16, hash, cr, time);
            if (++px > 1) {
                px = 0;
                ++py;
            }
            if (++idx >= 6) {
                break;
            }
        }
    }

    private void drawOutputIcon(GuiGraphics graphics, int x, int y, ResourceLocation id, CrucibleRecipe cr, long time) {
        if (inventory.recipeHash.contains(id)) {
            ResourceLocation hash = inventory.recipeHash.get((int) (time / 1000L % inventory.recipeHash.size()));
            graphics.setColor(1.0f, 1.0f, 1.0f, 0.7f);
            graphics.blit(TEX, x, y, 176, 8, 16, 16);
            if (inventory.recipeHash.size() > 1 && hash.equals(id)) {
                graphics.blit(TEX, x, y, 176, 8, 16, 16);
            }
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        ItemStack out = cr.getRecipeOutput();
        graphics.renderItem(out, x, y);
        graphics.renderItemDecorations(font, out, x, y);
    }

    /** The recipe under the mouse, if any. */
    private ResourceLocation recipeAt(double mx, double my) {
        int px = 0;
        int py = 0;
        int q = 0;
        int idx = 0;
        for (ResourceLocation hash : hashList) {
            if (q++ < index * 2) {
                continue;
            }
            if (inventory.getRecipe(hash) == null) {
                continue;
            }
            double x = mx - (leftPos + 48 + px * 16);
            double y = my - (topPos + 56 + py * 16);
            if (x >= 0 && y >= 0 && x < 16 && y < 16) {
                return hash;
            }
            if (++px > 1) {
                px = 0;
                ++py;
            }
            if (++idx >= 6) {
                break;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        ResourceLocation hash = recipeAt(mx, my);
        if (hash != null) {
            PacketDistributor.sendToServer(new PacketSelectThaumotoriumRecipeToServer(inventory.getBlockPos(), hash));
            player.playSound(SoundsTC.HHON.get(), 0.3f, 1.0f);
            lastHLUpdate = 0L;
            return true;
        }
        if (hashList.size() > 6) {
            if (index > 0) {
                double x2 = mx - (leftPos + 82);
                double y2 = my - (topPos + 56);
                if (x2 >= 0 && y2 >= 0 && x2 < 8 && y2 < 11) {
                    --index;
                    player.playSound(SoundsTC.CLACK.get(), 0.4f, 1.0f);
                    lastHLUpdate = 0L;
                    return true;
                }
            }
            if (index < hashList.size() / 2.0f - 3.0f) {
                double x2 = mx - (leftPos + 82);
                double y2 = my - (topPos + 93);
                if (x2 >= 0 && y2 >= 0 && x2 < 8 && y2 < 11) {
                    ++index;
                    player.playSound(SoundsTC.CLACK.get(), 0.4f, 1.0f);
                    lastHLUpdate = 0L;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
