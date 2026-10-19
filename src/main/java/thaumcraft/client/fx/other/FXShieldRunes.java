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
import net.minecraft.world.entity.monster.Monster;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.particles.FXBase;
import thaumcraft.client.lib.obj.ObjModel;
import thaumcraft.client.lib.render.TCRenderTypes;
import thaumcraft.common.entities.monster.cult.EntityCultist;

/**
 * Port of FXShieldRunes: a flash of runes on a half sphere (models/obj/hemis.obj) facing where a hit came from.
 * Monsters show the ripple frames, everything else the rune frames; with no entity (the pedestal stabilizer) it is
 * tinted purple.
 */
public class FXShieldRunes extends FXBase {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "models/obj/hemis.obj");
    private static final ResourceLocation[] tex1 = new ResourceLocation[15];
    private static final ResourceLocation[] tex2 = new ResourceLocation[15];
    Entity target;
    float yaw;
    float pitch;

    static {
        for (int a = 0; a < 15; ++a) {
            tex1[a] = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/models/ripple" + (a + 1) + ".png");
            tex2[a] = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/models/hemis" + (a + 1) + ".png");
        }
    }

    public FXShieldRunes(ClientLevel world, double d, double d1, double d2, Entity target, int age, float yaw, float pitch) {
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
        this.yaw = yaw;
        this.pitch = pitch;
        if (target != null) {
            x = xo = target.getX();
            y = yo = (target.getBoundingBox().minY + target.getBoundingBox().maxY) / 2.0;
            z = zo = target.getZ();
        }
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        }
        if (target != null) {
            x = target.getX();
            y = (target.getBoundingBox().minY + target.getBoundingBox().maxY) / 2.0;
            z = target.getZ();
        }
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
        float b = 1.0f;
        ResourceLocation tex;
        if (target instanceof Monster && !(target instanceof EntityCultist)) {
            tex = tex1[frame - 1];
            b = 0.5f;
        } else {
            tex = tex2[frame - 1];
        }
        PoseStack pose = new PoseStack();
        pose.translate(relX(cam, f), relY(cam, f), relZ(cam, f));
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - yaw));
        pose.mulPose(Axis.XP.rotationDegrees(-pitch));
        float th = target == null ? 2.0f : target.getBbHeight();
        pose.scale(0.2f * th, 0.2f * th, 0.2f * th);
        float a = Math.min(1.0f, (1.0f - fade) * 3.0f);
        int color = target == null ? FastColor.ARGB32.colorFromFloat(a, 0.65f, 0.1f, 0.5f) : FastColor.ARGB32.colorFromFloat(a, b, b, b);
        ObjModel.load(MODEL, true).renderAll(pose, buffers.getBuffer(TCRenderTypes.runes(tex, true)), 0xF000F0, OverlayTexture.NO_OVERLAY, color);
    }
}
