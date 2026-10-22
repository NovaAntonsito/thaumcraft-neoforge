package thaumcraft.client.fx.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.particles.FXBase;
import thaumcraft.client.lib.obj.ObjModel;
import thaumcraft.client.lib.render.TCRenderTypes;

/**
 * Port of FXSonic: the ripple an eldritch guardian screams, a stretched half sphere (models/obj/hemis.obj) in front
 * of its head with the ripple frames, following where it looks.
 */
public class FXSonic extends FXBase {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "models/obj/hemis.obj");
    private static final ResourceLocation[] RIPPLE = new ResourceLocation[16];

    static {
        for (int i = 1; i < RIPPLE.length; ++i) {
            RIPPLE[i] = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/models/ripple" + i + ".png");
        }
    }
    Entity target;
    float yaw;
    float pitch;

    public FXSonic(ClientLevel world, double d, double d1, double d2, Entity target, int age) {
        super(world, d, d1, d2);
        rCol = 1.0f;
        gCol = 1.0f;
        bCol = 1.0f;
        gravity = 0.0f;
        xd = 0.0;
        yd = 0.0;
        zd = 0.0;
        lifetime = age + random.nextInt(age / 2);
        setSize(0.01f, 0.01f);
        particleScale = 1.0f;
        this.target = target;
        yaw = target.getYHeadRot();
        pitch = target.getXRot();
        x = xo = target.getX();
        y = yo = target.getY() + target.getEyeHeight();
        z = zo = target.getZ();
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        }
        x = target.getX();
        y = target.getY() + target.getEyeHeight();
        z = target.getZ();
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public boolean isCustomRender() {
        return true;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
    }

    @Override
    public void renderCustom(MultiBufferSource buffers, Camera cam, float f) {
        float fade = (age + f) / lifetime;
        int frame = Math.min(15, (int) (14.0f * fade) + 1);
        ResourceLocation tex = RIPPLE[frame];
        PoseStack pose = new PoseStack();
        pose.translate(relX(cam, f), relY(cam, f), relZ(cam, f));
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
        pose.translate(0.0, 0.0, 2.0f * target.getBbHeight() + target.getBbWidth() / 2.0f);
        pose.scale(0.25f * target.getBbHeight(), 0.25f * target.getBbHeight(), -1.0f * target.getBbHeight());
        float b = 0.5f;
        ObjModel.load(MODEL, true).renderAll(pose, buffers.getBuffer(TCRenderTypes.runes(tex, false)), 0xF000F0, OverlayTexture.NO_OVERLAY,
                FastColor.ARGB32.colorFromFloat(1.0f, b, b, b));
    }
}
