package thaumcraft.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port of FXBreakingFade: the vanilla item breaking bits (ParticleBreaking), tinted and fading out over their life.
 * Goes to the vanilla particle engine, which has the item textures.
 */
public class FXBreakingFade extends TextureSheetParticle {
    private final float uo;
    private final float vo;

    public FXBreakingFade(ClientLevel worldIn, double x, double y, double z, double xs, double ys, double zs, Item item) {
        this(worldIn, x, y, z, item);
        xd *= 0.10000000149011612;
        yd *= 0.10000000149011612;
        zd *= 0.10000000149011612;
        xd += xs;
        yd += ys;
        zd += zs;
    }

    public FXBreakingFade(ClientLevel worldIn, double x, double y, double z, Item item) {
        super(worldIn, x, y, z, 0.0, 0.0, 0.0);
        setSprite(Minecraft.getInstance().getItemRenderer().getModel(new ItemStack(item), worldIn, null, 0).getParticleIcon());
        rCol = 1.0f;
        gCol = 1.0f;
        bCol = 1.0f;
        gravity = 1.0f;
        quadSize /= 2.0f;
        uo = random.nextFloat() * 3.0f;
        vo = random.nextFloat() * 3.0f;
    }

    public void setRBGColorF(float r, float g, float b) {
        setColor(r, g, b);
    }

    public void setAlphaF(float a) {
        alpha = a;
    }

    public void setParticleMaxAge(int particleMaxAge) {
        lifetime = particleMaxAge;
    }

    public void setParticleGravity(float f) {
        gravity = f;
    }

    public void setSpeed(double x, double y, double z) {
        xd = x;
        yd = y;
        zd = z;
    }

    public void boom() {
        float f = (float) (Math.random() + Math.random() + 1.0) * 0.15f;
        float f2 = Mth.sqrt((float) (xd * xd + yd * yd + zd * zd));
        xd = xd / f2 * f * 0.9640000000596046;
        yd = yd / f2 * f * 0.9640000000596046 + 0.10000000149011612;
        zd = zd / f2 * f * 0.9640000000596046;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        float a = alpha;
        alpha = a * (1.0f - age / (float) lifetime);
        super.render(buffer, camera, partialTicks);
        alpha = a;
    }

    @Override
    protected float getU0() {
        return sprite.getU(uo / 4.0f);
    }

    @Override
    protected float getU1() {
        return sprite.getU((uo + 1.0f) / 4.0f);
    }

    @Override
    protected float getV0() {
        return sprite.getV(vo / 4.0f);
    }

    @Override
    protected float getV1() {
        return sprite.getV((vo + 1.0f) / 4.0f);
    }
}
