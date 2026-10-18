package thaumcraft.client.lib.events;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Port of ShaderHandler and the warp parts of RenderEventHandler: the screen effects of the warp potions.
 * Deadly Gaze drains the colors and darkens the edges of the screen (the warp vignette, pulsing), Blurred Vision
 * blurs it, Unnatural Hunger tints it red and Sun Scorned makes it glow. The config disableShaders turns the post
 * effects off.
 * 1.21: the post effects are post chains of their own, run over the frame before the HUD as the original did; the
 * shaders were moved to the thaumcraft namespace and to GLSL 150. As in the original, the vignette is drawn twice a
 * frame (before and after the portal overlay), so it also eases in twice as fast.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ShaderHandler {
    public static int warpVignette = 0;
    public static final int SHADER_DESAT = 0;
    public static final int SHADER_BLUR = 1;
    public static final int SHADER_HUNGER = 2;
    public static final int SHADER_SUNSCORNED = 3;
    static final ResourceLocation[] SHADER_RESOURCES = { Thaumcraft.id("shaders/post/desaturatetc.json"), Thaumcraft.id("shaders/post/blurtc.json"),
            Thaumcraft.id("shaders/post/hunger.json"), Thaumcraft.id("shaders/post/sunscorned.json") };
    static final Map<Integer, PostChain> shaderGroups = new HashMap<>();
    private static final ResourceLocation VIGNETTE = Thaumcraft.id("textures/misc/vignette.png");
    static boolean resetShaders = false;
    static int oldDisplayWidth = 0;
    static int oldDisplayHeight = 0;
    static float prevVignetteBrightness = 0.0f;
    static float targetBrightness = 1.0f;

    private ShaderHandler() {
    }

    /** The shader and vignette part of playerTick. */
    @SubscribeEvent
    static void playerTick(PlayerTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!event.getEntity().level().isClientSide || event.getEntity() != mc.player) {
            return;
        }
        checkShaders(event.getEntity(), mc);
        if (warpVignette > 0) {
            --warpVignette;
            targetBrightness = 0.0f;
        } else {
            targetBrightness = 1.0f;
        }
    }

    static void checkShaders(Player player, Minecraft mc) {
        if (player.hasEffect(TCMobEffects.DEATH_GAZE)) {
            warpVignette = 10;
        }
        check(player, mc, TCMobEffects.DEATH_GAZE, SHADER_DESAT);
        check(player, mc, TCMobEffects.BLURRED_VISION, SHADER_BLUR);
        check(player, mc, TCMobEffects.UNNATURAL_HUNGER, SHADER_HUNGER);
        check(player, mc, TCMobEffects.SUN_SCORNED, SHADER_SUNSCORNED);
    }

    private static void check(Player player, Minecraft mc, Holder<MobEffect> effect, int id) {
        if (player.hasEffect(effect)) {
            if (!shaderGroups.containsKey(id)) {
                try {
                    setShader(new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), SHADER_RESOURCES[id]), id);
                } catch (JsonSyntaxException | IOException ex) {
                    Thaumcraft.LOGGER.warn("Failed to load shader {}", SHADER_RESOURCES[id], ex);
                }
            }
        } else if (shaderGroups.containsKey(id)) {
            deactivateShader(id);
        }
    }

    static void setShader(PostChain target, int shaderId) {
        if (shaderGroups.containsKey(shaderId)) {
            shaderGroups.get(shaderId).close();
            shaderGroups.remove(shaderId);
        }
        if (target == null) {
            deactivateShader(shaderId);
        } else {
            resetShaders = true;
            shaderGroups.put(shaderId, target);
        }
    }

    public static void deactivateShader(int shaderId) {
        if (shaderGroups.containsKey(shaderId)) {
            shaderGroups.get(shaderId).close();
        }
        shaderGroups.remove(shaderId);
    }

    /** renderShaders: the post effects over the whole frame, before the HUD is drawn. */
    @SubscribeEvent
    static void renderShaders(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null && !shaderGroups.isEmpty()) {
            for (int id : shaderGroups.keySet().toArray(new Integer[0])) {
                deactivateShader(id);
            }
            return;
        }
        if (!Config.DISABLE_SHADERS.get() && !shaderGroups.isEmpty()) {
            updateShaderFrameBuffers(mc);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            float partial = event.getPartialTick().getGameTimeDeltaTicks();
            for (PostChain sg : shaderGroups.values()) {
                try {
                    sg.process(partial);
                } catch (Exception ex) {
                }
            }
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.enableDepthTest();
        }
    }

    private static void updateShaderFrameBuffers(Minecraft mc) {
        int w = mc.getMainRenderTarget().width;
        int h = mc.getMainRenderTarget().height;
        if (resetShaders || w != oldDisplayWidth || h != oldDisplayHeight) {
            for (PostChain sg : shaderGroups.values()) {
                sg.resize(w, h);
            }
            oldDisplayWidth = w;
            oldDisplayHeight = h;
            resetShaders = false;
        }
    }

    @SubscribeEvent
    static void renderOverlayPre(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS)) {
            renderVignette(event.getGuiGraphics(), targetBrightness);
        }
    }

    @SubscribeEvent
    static void renderOverlayPost(RenderGuiLayerEvent.Post event) {
        if (event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS)) {
            renderVignette(event.getGuiGraphics(), targetBrightness);
        }
    }

    /** renderVignette: darkens the edges of the screen as the brightness drops, pulsing a little. */
    static void renderVignette(GuiGraphics g, float brightness) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        brightness = 1.0f - brightness;
        prevVignetteBrightness += (float) ((brightness - prevVignetteBrightness) * 0.01);
        if (prevVignetteBrightness > 0.0f) {
            float b = prevVignetteBrightness * (1.0f + Mth.sin(mc.player.tickCount / 2.0f) * 0.1f);
            int k = g.guiWidth();
            int l = g.guiHeight();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            g.setColor(b, b, b, 1.0f);
            g.blit(VIGNETTE, 0, 0, -90, 0.0f, 0.0f, k, l, k, l);
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}
