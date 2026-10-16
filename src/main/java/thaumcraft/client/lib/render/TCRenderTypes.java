package thaumcraft.client.lib.render;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Render types the 1.12 code got from raw GL state: textured quads drawn on top of the world (depth test and culling
 * off, no depth writes), blended normally or added on top (GL_ONE).
 */
public final class TCRenderTypes extends RenderType {
    private static final Map<ResourceLocation, RenderType> OVERLAY_ADDITIVE = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> SEAL = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> SEAL_XRAY = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ENTITY_XRAY = new HashMap<>();

    private TCRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                          Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    /** A see-through-walls quad with additive blending, POSITION_TEX_COLOR. */
    public static RenderType overlayAdditive(ResourceLocation texture) {
        return OVERLAY_ADDITIVE.computeIfAbsent(texture, t -> create("thaumcraft_overlay_additive", DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS, 1536, false, true,
                CompositeState.builder().setShaderState(new ShaderStateShard(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader)).setTextureState(new TextureStateShard(t, false, false))
                        .setTransparencyState(LIGHTNING_TRANSPARENCY).setDepthTestState(NO_DEPTH_TEST).setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_WRITE).createCompositeState(false)));
    }

    /**
     * The golem seals and their area markers: normal blending, culling off, no depth writes, POSITION_TEX_COLOR.
     * With xray the depth test is off too, as the original did while the player sneaks.
     */
    public static RenderType seal(ResourceLocation texture, boolean xray) {
        return (xray ? SEAL_XRAY : SEAL).computeIfAbsent(texture, t -> create(xray ? "thaumcraft_seal_xray" : "thaumcraft_seal",
                DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1536, false, true,
                CompositeState.builder().setShaderState(new ShaderStateShard(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader))
                        .setTextureState(new TextureStateShard(t, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(xray ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(false)));
    }

    /**
     * An entity model seen through walls: entityTranslucent without the depth test and without depth writes.
     * Used for the faint golem outline while sneaking with a seal displayer in hand.
     */
    public static RenderType entityXray(ResourceLocation texture) {
        return ENTITY_XRAY.computeIfAbsent(texture, t -> create("thaumcraft_entity_xray", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 1536, true, true,
                CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new TextureStateShard(t, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)));
    }
}
