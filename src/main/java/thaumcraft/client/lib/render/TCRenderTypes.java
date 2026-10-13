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
}
