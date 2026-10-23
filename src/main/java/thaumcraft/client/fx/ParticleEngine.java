package thaumcraft.client.fx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.particles.FXBase;

/**
 * Port of ParticleEngine: Thaumcraft's own particle list, drawn with the particle sheet
 * (textures/misc/particles.png) in four layers: 0 additive, 1 alpha blended, 2 additive through walls and 3 alpha
 * blended through walls. The GUI layers (4 and 5) are not ported yet.
 * The original kept one list per dimension; the client only has one world at a time, so the lists are emptied when
 * it changes.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ParticleEngine {
    public static final ResourceLocation particleTexture = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/misc/particles.png");
    private static final int LAYERS = 4;
    @SuppressWarnings("unchecked")
    private static final List<Particle>[] particles = new List[] { new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>() };
    private static final List<ParticleDelay> particlesDelayed = new ArrayList<>();
    private static ClientLevel world;
    /**
     * The vanilla particle shader with the original's alpha cutoff (1/255 instead of 0.1), so faint motes are not
     * cut away. The vanilla shader stands in if it fails to load.
     */
    private static ShaderInstance shader;

    private ParticleEngine() {
    }

    @SubscribeEvent
    static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "particle_tc"),
                    DefaultVertexFormat.PARTICLE), s -> shader = s);
        } catch (Exception e) {
            Thaumcraft.LOGGER.error("Thaumcraft: could not load the particle shader, using the vanilla one", e);
            shader = null;
        }
    }

    /** Empties the lists when the client world changes. */
    private static boolean sync() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != world) {
            for (List<Particle> parts : particles) parts.clear();
            particlesDelayed.clear();
            world = level;
        }
        return level != null;
    }

    private static int layerOf(Particle fx) {
        int layer = fx instanceof FXBase b ? b.getFXLayer() : 0;
        return Math.max(0, Math.min(LAYERS - 1, layer));
    }

    private static int getParticleLimit() {
        int ps = Minecraft.getInstance().options.particles().get().getId();
        return ps == 2 ? 500 : ps == 1 ? 1000 : 2000;
    }

    public static void addEffect(Level level, Particle fx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !sync()) return;
        int ps = mc.options.particles().get().getId();
        if (mc.getFps() < 30) ++ps;
        if (mc.level.random.nextInt(3) < ps) return;
        particles[layerOf(fx)].add(fx);
    }

    /** For the effects the original gave to the vanilla effect renderer (arcs, bolts, shields): no random skipping. */
    public static void addEffectAlways(Level level, Particle fx) {
        if (Minecraft.getInstance().level == null || !sync()) return;
        particles[layerOf(fx)].add(fx);
    }

    public static void addEffectWithDelay(Level level, Particle fx, int delay) {
        if (!sync()) return;
        particlesDelayed.add(new ParticleDelay(fx, delay));
    }

    @SubscribeEvent
    static void updateParticles(ClientTickEvent.Pre event) {
        if (!sync()) return;
        ClientFX.tickEssentiaSources();
        if (!particlesDelayed.isEmpty()) {
            Iterator<ParticleDelay> i = particlesDelayed.iterator();
            List<Particle> ready = new ArrayList<>();
            while (i.hasNext()) {
                ParticleDelay pd = i.next();
                if (--pd.delay <= 0) {
                    ready.add(pd.particle);
                    i.remove();
                }
            }
            for (Particle p : ready) addEffect(world, p);
        }
        int limit = getParticleLimit();
        for (List<Particle> parts : particles) {
            // Indexed: a particle's tick may spawn new ones into this list.
            for (int j = 0; j < parts.size(); ++j) {
                Particle particle = parts.get(j);
                try {
                    particle.tick();
                } catch (Exception e) {
                    particle.remove();
                }
            }
            // One pass each instead of shifting the array per dead particle, or per add once at the limit.
            parts.removeIf(p -> !p.isAlive());
            int excess = parts.size() - limit;
            if (excess > 0) parts.subList(0, excess).clear();
        }
    }

    @SubscribeEvent
    static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || world == null) return;
        boolean any = false;
        for (List<Particle> parts : particles) any |= !parts.isEmpty();
        if (!any) return;
        Minecraft mc = Minecraft.getInstance();
        Camera camera = event.getCamera();
        float frame = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Frustum frustum = event.getFrustum();
        LightTexture light = mc.gameRenderer.lightTexture();
        light.turnOnLightLayer();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(() -> shader != null ? shader : GameRenderer.getParticleShader());
        RenderSystem.setShaderTexture(0, particleTexture);
        for (int layer = LAYERS - 1; layer >= 0; --layer) {
            List<Particle> parts = particles[layer];
            if (parts.isEmpty()) continue;
            if (layer == 0 || layer == 2) {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            } else {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }
            if (layer >= 2) RenderSystem.disableDepthTest();
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            for (Particle particle : parts) {
                if (frustum != null && particle instanceof FXBase b && !b.isCustomRender() && !frustum.isVisible(b.getCullBox(frame))) {
                    continue;
                }
                try {
                    particle.render(buffer, camera, frame);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                    CrashReportCategory category = crashreport.addCategory("Particle being rendered");
                    category.setDetail("Particle", particle::toString);
                    category.setDetail("Particle Type", "THAUMCRAFT_PARTICLE_TEXTURE");
                    throw new ReportedException(crashreport);
                }
            }
            MeshData mesh = buffer.build();
            if (mesh != null) BufferUploader.drawWithShader(mesh);
            if (layer >= 2) RenderSystem.enableDepthTest();
        }
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        light.turnOffLightLayer();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        boolean drew = false;
        for (int layer = LAYERS - 1; layer >= 0; --layer) {
            for (Particle particle : particles[layer]) {
                if (particle instanceof FXBase b && b.isCustomRender()) {
                    b.renderCustom(buffers, camera, frame);
                    drew = true;
                }
            }
        }
        if (drew) buffers.endBatch();
    }

    private static final class ParticleDelay {
        final Particle particle;
        int delay;

        ParticleDelay(Particle particle, int delay) {
            this.particle = particle;
            this.delay = delay;
        }
    }
}
