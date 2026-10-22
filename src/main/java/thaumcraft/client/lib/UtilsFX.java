package thaumcraft.client.lib;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.Aspect;

/** Partial port of UtilsFX: the quad and aspect-icon helpers (world and GUI). */
public final class UtilsFX {
    private UtilsFX() {
    }

    /**
     * renderQuadCentered: a square of the given size in the XY plane, centered on the origin and facing -Z,
     * with the texture upright for someone looking at it from -Z.
     */
    public static void renderQuadCentered(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture, float size,
                                          float r, float g, float b, float a, int light) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(texture));
        PoseStack.Pose p = pose.last();
        float h = size / 2.0f;
        vertex(vc, p, h, h, 0, 0, r, g, b, a, light);
        vertex(vc, p, -h, h, 1, 0, r, g, b, a, light);
        vertex(vc, p, -h, -h, 1, 1, r, g, b, a, light);
        vertex(vc, p, h, -h, 0, 1, r, g, b, a, light);
    }

    /** drawTag: the aspect's icon in its color, size wide, centered on the origin and facing -Z. */
    public static void drawTag(PoseStack pose, MultiBufferSource buffers, Aspect aspect, float size, int light) {
        int color = aspect.getColor();
        renderQuadCentered(pose, buffers, aspect.getImage(), size,
                ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1.0f, light);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose p, float x, float y, float u, float v,
                               float r, float g, float b, float a, int light) {
        vc.addVertex(p, x, y, 0).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(p, 0, 0, -1);
    }

    // ---- GUI helpers: the 2D GL11 drawing of the original on GuiGraphics ----

    public static final ResourceLocation nodeTexture = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/auranodes.png");
    private static final Map<ResourceLocation, int[]> TEXTURE_SIZES = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> TEXTURE_PRESENT = new HashMap<>();

    /** A textured quad with float corners and UVs (0-1), tinted by GuiGraphics.setColor. */
    public static void quad(GuiGraphics g, ResourceLocation tex, float x1, float y1, float x2, float y2, float u1, float u2, float v1, float v2) {
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        Matrix4f m = g.pose().last().pose();
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.addVertex(m, x1, y1, 0).setUv(u1, v1);
        b.addVertex(m, x1, y2, 0).setUv(u1, v2);
        b.addVertex(m, x2, y2, 0).setUv(u2, v2);
        b.addVertex(m, x2, y1, 0).setUv(u2, v1);
        BufferUploader.drawWithShader(b.buildOrThrow());
    }

    /** drawTexturedModalRect: a w x h part of a texture laid out on 256x256, with blending on. */
    public static void blit(GuiGraphics g, ResourceLocation tex, int x, int y, int u, int v, int w, int h) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(tex, x, y, u, v, w, h);
    }

    /** drawTexturedQuadFull: the whole texture on a 16x16 square. */
    public static void drawTexturedQuadFull(GuiGraphics g, ResourceLocation tex, float x, float y) {
        quad(g, tex, x, y, x + 16, y + 16, 0, 1, 0, 1);
    }

    /** drawTexturedQuadF: part of the texture (u, v, w, h in sixteenths) on a 16x16 square. */
    public static void drawTexturedQuadF(GuiGraphics g, ResourceLocation tex, float x, float y, float u, float v, float w, float h) {
        quad(g, tex, x, y, x + 16, y + 16, u / 16f, (u + w) / 16f, v / 16f, (v + h) / 16f);
    }

    /** The pixel size of a texture (GL_TEXTURE_WIDTH/HEIGHT in the original), read once from its resource. */
    public static int[] getTextureSize(ResourceLocation tex) {
        return TEXTURE_SIZES.computeIfAbsent(tex, t -> {
            try (InputStream in = Minecraft.getInstance().getResourceManager().open(t); NativeImage img = NativeImage.read(in)) {
                return new int[]{img.getWidth(), img.getHeight()};
            } catch (Exception e) {
                return new int[]{16, 16};
            }
        });
    }

    public static boolean hasTexture(ResourceLocation tex) {
        // Asked for every research icon on every frame of the browser: looked up once.
        return TEXTURE_PRESENT.computeIfAbsent(tex, t -> Minecraft.getInstance().getResourceManager().getResource(t).isPresent());
    }

    /** renderQuadCentered for GUIs: one frame of a gridX x gridY sheet, scale pixels wide, centered on the origin. */
    public static void renderQuadCentered(GuiGraphics g, ResourceLocation tex, int gridX, int gridY, int frame, float scale,
                                          float red, float green, float blue, boolean additive, float opacity) {
        int xm = frame % gridX;
        int ym = frame / gridY;
        float f1 = xm / (float) gridX;
        float f3 = ym / (float) gridY;
        if (additive) RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        g.setColor(red, green, blue, opacity);
        float h = scale / 2.0f;
        quad(g, tex, -h, -h, h, h, f1, f1 + 1.0f / gridX, f3, f3 + 1.0f / gridY);
        g.setColor(1, 1, 1, 1);
        RenderSystem.defaultBlendFunc();
    }

    /** drawTag for GUIs: the aspect's icon in its color, and the amount in the corner. */
    public static void drawTag(GuiGraphics g, int x, int y, Aspect aspect, float amount, int bonus) {
        drawTag(g, x, y, aspect, amount, bonus, 1.0f, false);
    }

    public static void drawTag(GuiGraphics g, int x, int y, Aspect aspect, float amount, int bonus, float alpha, boolean bw) {
        if (aspect == null) return;
        int c = aspect.getColor();
        if (bw) {
            g.setColor(0.1f, 0.1f, 0.1f, alpha * 0.8f);
        } else {
            g.setColor(((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f, alpha);
        }
        drawTexturedQuadFull(g, aspect.getImage(), x, y);
        g.setColor(1, 1, 1, 1);
        if (amount > 0.0f) {
            Font font = Minecraft.getInstance().font;
            String am = new DecimalFormat("#######.##").format(amount);
            int sw = font.width(am);
            g.pose().pushPose();
            g.pose().translate(0, 0, 1);
            g.pose().scale(0.5f, 0.5f, 0.5f);
            int tx = 32 - sw + x * 2;
            int ty = 32 - font.lineHeight + y * 2;
            for (Direction e : Direction.Plane.HORIZONTAL) {
                g.drawString(font, am, tx + e.getStepX(), ty + e.getStepZ(), 0, false);
            }
            g.drawString(font, am, tx, ty, 0xFFFFFF, false);
            g.pose().popPose();
        }
        if (bonus > 0) {
            // The sparkle marks points the research table adds: 16 frames on the 1024px particle sheet (Reborn's drawTexturedQuad1024).
            Minecraft mc = Minecraft.getInstance();
            int px = 16 * ((mc.player != null ? mc.player.tickCount : 0) % 16);
            quad(g, PARTICLE_TEXTURE, x - 4, y - 4, x + 12, y + 12, px / 1024f, (px + 16) / 1024f, 80 / 1024f, 96 / 1024f);
            if (bonus > 1) {
                Font font = mc.font;
                String am2 = "" + bonus;
                int sw2 = font.width(am2) / 2;
                g.pose().pushPose();
                g.pose().translate(0, 0, 1);
                g.pose().scale(0.5f, 0.5f, 0.5f);
                g.drawString(font, am2, 8 - sw2 + x * 2, 15 - font.lineHeight + y * 2, 0xFFFFFF, true);
                g.pose().popPose();
            }
        }
    }

    public static final ResourceLocation PARTICLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/particles.png");

    /**
     * drawCustomTooltip: a tooltip whose lines starting with "@@" are drawn at half size. Lines may use legacy
     * formatting codes. subTipColor (a hex digit) colors the first line and greys the rest; -99 keeps them.
     */
    public static void drawCustomTooltip(GuiGraphics g, Font fr, List<String> textList, int x, int y, int subTipColor) {
        if (textList.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int max = 240;
        boolean flip = false;
        if (max + 24 + x > screenW) {
            max = screenW - x - 24;
            if (max < 120) {
                max = 240;
                flip = true;
            }
        }
        List<String> lines = new ArrayList<>();
        for (String line : textList) {
            boolean small = line.startsWith("@@");
            for (String part : wrap(fr, small ? line.substring(2) : line, small ? max * 2 : max)) {
                lines.add(small ? "@@" + part : part);
            }
        }
        int widest = 0;
        int totalHeight = -2;
        for (String line : lines) {
            boolean small = line.startsWith("@@");
            int w = fr.width(small ? line.substring(2) : line);
            if (small) w /= 2;
            widest = Math.max(widest, w);
            totalHeight += small ? 7 : 10;
        }
        int sX = x + 12;
        int sY = y - 12;
        if (lines.size() > 1) totalHeight += 2;
        if (sY + totalHeight > screenH) sY = screenH - totalHeight - 5;
        if (flip) sX -= widest + 24;
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        int bg = 0xF0100010;
        g.fillGradient(sX - 3, sY - 4, sX + widest + 3, sY - 3, bg, bg);
        g.fillGradient(sX - 3, sY + totalHeight + 3, sX + widest + 3, sY + totalHeight + 4, bg, bg);
        g.fillGradient(sX - 3, sY - 3, sX + widest + 3, sY + totalHeight + 3, bg, bg);
        g.fillGradient(sX - 4, sY - 3, sX - 3, sY + totalHeight + 3, bg, bg);
        g.fillGradient(sX + widest + 3, sY - 3, sX + widest + 4, sY + totalHeight + 3, bg, bg);
        int b1 = 0x505000FF;
        int b2 = (b1 & 0xFEFEFE) >> 1 | (b1 & 0xFF000000);
        g.fillGradient(sX - 3, sY - 3 + 1, sX - 3 + 1, sY + totalHeight + 3 - 1, b1, b2);
        g.fillGradient(sX + widest + 2, sY - 3 + 1, sX + widest + 3, sY + totalHeight + 3 - 1, b1, b2);
        g.fillGradient(sX - 3, sY - 3, sX + widest + 3, sY - 3 + 1, b1, b1);
        g.fillGradient(sX - 3, sY + totalHeight + 2, sX + widest + 3, sY + totalHeight + 3, b2, b2);
        for (int i = 0; i < lines.size(); ++i) {
            String tl = lines.get(i);
            boolean small = tl.startsWith("@@");
            if (small) tl = tl.substring(2);
            if (subTipColor != -99) tl = (i == 0 ? "\u00a7" + Integer.toHexString(subTipColor) : "\u00a77") + tl;
            g.pose().pushPose();
            g.pose().translate(sX, sY, 1);
            if (small) {
                g.pose().scale(0.5f, 0.5f, 1.0f);
                sY += 7;
            } else {
                sY += 10;
            }
            g.drawString(fr, tl, 0, small ? 3 : 0, -1, true);
            g.pose().popPose();
            if (i == 0) sY += 2;
        }
        g.pose().popPose();
    }

    /** listFormattedStringToWidth: word wrap that keeps the active formatting codes on the next lines. */
    public static List<String> wrap(Font fr, String text, int width) {
        List<String> out = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            String carry = "";
            for (String word : paragraph.split(" ", -1)) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (line.length() > 0 && fr.width(candidate) > width) {
                    out.add(line.toString());
                    carry = formatting(carry + line);
                    line = new StringBuilder(carry).append(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            out.add(line.toString());
        }
        return out;
    }

    /** The formatting codes in effect at the end of a string (a color resets the styles before it). */
    private static String formatting(String s) {
        String color = "";
        StringBuilder styles = new StringBuilder();
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) != '\u00a7') continue;
            char c = Character.toLowerCase(s.charAt(i + 1));
            if ("0123456789abcdef".indexOf(c) >= 0) {
                color = "\u00a7" + c;
                styles.setLength(0);
            } else if (c == 'r') {
                color = "";
                styles.setLength(0);
            } else if ("klmno".indexOf(c) >= 0) {
                styles.append('\u00a7').append(c);
            }
        }
        return color + styles;
    }
}
