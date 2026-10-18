package thaumcraft.client.lib;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Multiplies the color of everything drawn through it. Stands in for the GL11.glColor calls the 1.12 renderers made
 * before drawing a model, since LivingEntityRenderer always draws the model in white.
 */
public final class TintedBufferSource implements MultiBufferSource {
    private final MultiBufferSource parent;
    private final float r;
    private final float g;
    private final float b;
    private final float a;

    public TintedBufferSource(MultiBufferSource parent, float r, float g, float b, float a) {
        this.parent = parent;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return new Tinted(parent.getBuffer(type));
    }

    private final class Tinted implements VertexConsumer {
        private final VertexConsumer vc;

        Tinted(VertexConsumer vc) {
            this.vc = vc;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            vc.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            vc.setColor((int) (red * r), (int) (green * g), (int) (blue * b), (int) (alpha * a));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            vc.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            vc.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            vc.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            vc.setNormal(x, y, z);
            return this;
        }
    }
}
