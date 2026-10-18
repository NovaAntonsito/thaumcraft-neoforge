package thaumcraft.client.gui;

import java.util.HashMap;
import java.util.TreeMap;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.casters.ICaster;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.lib.events.KeyHandler;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.items.casters.ItemFocusPouch;
import net.minecraft.core.NonNullList;
import thaumcraft.common.lib.network.misc.PacketFocusChangeToServer;

/**
 * Port of the focus radial from WandRenderingHandler (handleFociRadial, getFociInfo, renderFocusRadialHUD).
 * 1.21: a screen that does not pause the game. Releasing F picks the hovered focus, clicking a focus picks it, as in
 * the original. The radial grows when opened; it closes at once instead of shrinking back.
 * Foci inside focus pouches count too: worn ones first, then the main inventory.
 */
public class GuiFocusRadial extends Screen {
    static final ResourceLocation R1 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/radial.png");
    static final ResourceLocation R2 = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/radial2.png");

    final TreeMap<String, Integer> foci = new TreeMap<>();
    final HashMap<String, ItemStack> fociItem = new HashMap<>();
    final HashMap<String, Boolean> fociHover = new HashMap<>();
    final HashMap<String, Float> fociScale = new HashMap<>();
    float radialHudScale = 0.0f;
    long lastTime = 0L;
    boolean picked = false;

    public GuiFocusRadial() {
        super(Component.empty());
        getFociInfo(Minecraft.getInstance().player);
        lastTime = System.currentTimeMillis();
    }

    public static boolean hasFoci(Player player) {
        for (ItemStack item : thaumcraft.common.lib.utils.BaublesHelper.getBaubles(player)) {
            if (item.getItem() instanceof ItemFocusPouch fp) {
                for (ItemStack in : fp.getInventory(item)) {
                    if (in.getItem() instanceof ItemFocus f && f.getSortingHelper(in) != null) {
                        return true;
                    }
                }
            }
        }
        for (int a = 0; a < 36; ++a) {
            ItemStack item = player.getInventory().items.get(a);
            if (item.getItem() instanceof ItemFocus f && f.getSortingHelper(item) != null) {
                return true;
            }
            if (item.getItem() instanceof ItemFocusPouch fp) {
                for (ItemStack in : fp.getInventory(item)) {
                    if (in.getItem() instanceof ItemFocus f && f.getSortingHelper(in) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void getFociInfo(Player player) {
        foci.clear();
        fociItem.clear();
        fociHover.clear();
        fociScale.clear();
        int pouchcount = 0;
        for (ItemStack item : thaumcraft.common.lib.utils.BaublesHelper.getBaubles(player)) {
            if (item.getItem() instanceof ItemFocusPouch fp) {
                ++pouchcount;
                addPouchFoci(fp.getInventory(item), pouchcount);
            }
        }
        for (int a = 0; a < 36; ++a) {
            ItemStack item = player.getInventory().items.get(a);
            if (item.getItem() instanceof ItemFocus f) {
                String sh2 = f.getSortingHelper(item);
                if (sh2 == null) {
                    continue;
                }
                foci.put(sh2, a);
                fociItem.put(sh2, item.copy());
                fociScale.put(sh2, 1.0f);
                fociHover.put(sh2, false);
            }
            if (item.getItem() instanceof ItemFocusPouch fp) {
                ++pouchcount;
                addPouchFoci(fp.getInventory(item), pouchcount);
            }
        }
    }

    private void addPouchFoci(NonNullList<ItemStack> inv, int pouchcount) {
        for (int q = 0; q < inv.size(); ++q) {
            ItemStack in = inv.get(q);
            if (in.getItem() instanceof ItemFocus f) {
                String sh = f.getSortingHelper(in);
                if (sh != null) {
                    foci.put(sh, q + pouchcount * 1000);
                    fociItem.put(sh, in.copy());
                    fociScale.put(sh, 1.0f);
                    fociHover.put(sh, false);
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
    }

    private float getRadialChange(long time, long lasttime, long total) {
        return (time - lasttime) / (float) total;
    }

    private void pick(String key) {
        if (!picked) {
            picked = true;
            PacketDistributor.sendToServer(new PacketFocusChangeToServer(key));
        }
        onClose();
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (KeyHandler.keyF.matches(keyCode, scanCode)) {
            for (String key : fociHover.keySet()) {
                if (fociHover.get(key)) {
                    pick(key);
                    return true;
                }
            }
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (String key : fociHover.keySet()) {
                if (fociHover.get(key)) {
                    pick(key);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        long time = System.currentTimeMillis();
        if (time > lastTime) {
            for (String key : fociHover.keySet()) {
                if (fociHover.get(key)) {
                    if (fociScale.get(key) < 1.3f) {
                        fociScale.put(key, Math.min(1.3f, fociScale.get(key) + getRadialChange(time, lastTime, 150L)));
                    }
                } else if (fociScale.get(key) > 1.0f) {
                    fociScale.put(key, Math.max(1.0f, fociScale.get(key) - getRadialChange(time, lastTime, 250L)));
                }
            }
            if (radialHudScale < 1.0f) {
                radialHudScale = Math.min(1.0f, radialHudScale + getRadialChange(time, lastTime, 150L));
            }
        }
        lastTime = time;

        ItemStack s = player.getMainHandItem();
        if (!(s.getItem() instanceof ICaster)) {
            s = player.getOffhandItem();
        }
        if (!(s.getItem() instanceof ICaster wand) || fociItem.isEmpty()) {
            onClose();
            return;
        }
        Item focus = wand.getFocus(s);
        double sw = width;
        double sh = height;
        ItemStack tt = null;
        float w = 16.0f + fociItem.size() * 2.5f;
        float rot = partialTicks + player.tickCount % 720 / 2.0f;
        g.pose().pushPose();
        g.pose().translate((int) (sw / 2.0), (int) (sh / 2.0), 0.0);
        RenderSystem.enableBlend();
        g.pose().pushPose();
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rot));
        UtilsFX.renderQuadCentered(g, R1, 1, 1, 0, w * 2.75f * radialHudScale, 0.5f, 0.5f, 0.5f, false, 0.5f);
        g.pose().popPose();
        g.pose().pushPose();
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-rot));
        UtilsFX.renderQuadCentered(g, R2, 1, 1, 0, w * 2.55f * radialHudScale, 0.5f, 0.5f, 0.5f, false, 0.5f);
        g.pose().popPose();
        if (focus != null) {
            ItemStack item = wand.getFocusStack(s).copy();
            g.renderItem(item, -8, -8);
            int mx = (int) (mouseX - sw / 2.0);
            int my = (int) (mouseY - sh / 2.0);
            if (mx >= -10 && mx <= 10 && my >= -10 && my <= 10) {
                tt = item;
            }
        }
        g.pose().scale(radialHudScale, radialHudScale, radialHudScale);
        float currentRot = -90.0f * radialHudScale;
        float pieSlice = 360.0f / fociItem.size();
        String key = foci.firstKey();
        for (int a = 0; a < fociItem.size() && key != null; ++a) {
            double xx = Mth.cos(currentRot / 180.0f * (float) Math.PI) * w;
            double yy = Mth.sin(currentRot / 180.0f * (float) Math.PI) * w;
            currentRot += pieSlice;
            g.pose().pushPose();
            g.pose().translate((int) xx, (int) yy, 100.0);
            float sc = fociScale.get(key);
            g.pose().scale(sc, sc, sc);
            g.renderItem(fociItem.get(key), -8, -8);
            g.pose().popPose();
            int mx2 = (int) ((mouseX - sw / 2.0) / Math.max(radialHudScale, 0.01f) - xx);
            int my2 = (int) ((mouseY - sh / 2.0) / Math.max(radialHudScale, 0.01f) - yy);
            if (mx2 >= -10 && mx2 <= 10 && my2 >= -10 && my2 <= 10) {
                fociHover.put(key, true);
                tt = fociItem.get(key);
            } else {
                fociHover.put(key, false);
            }
            key = foci.higherKey(key);
        }
        g.pose().popPose();
        if (tt != null) {
            g.renderComponentTooltip(font, Screen.getTooltipFromItem(mc, tt), mouseX, mouseY);
        }
        RenderSystem.disableBlend();
    }
}
