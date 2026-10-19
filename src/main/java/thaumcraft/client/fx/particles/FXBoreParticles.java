package thaumcraft.client.fx.particles;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of FXBoreParticles: bits of a block or item that fly to a point or an entity (infusion, the arcane bore).
 * Goes to the vanilla particle engine, which has the block and item textures.
 */
public class FXBoreParticles extends TextureSheetParticle {
    private BlockState blockInstance;
    private ItemStack itemInstance;
    private int side;
    private Entity target;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final float uo;
    private final float vo;

    public FXBoreParticles(ClientLevel par1World, double par2, double par4, double par6, double tx, double ty, double tz, BlockState par14Block, int par15) {
        super(par1World, par2, par4, par6, 0.0, 0.0, 0.0);
        blockInstance = par14Block;
        try {
            setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(par14Block));
        } catch (Exception e) {
            setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.STONE.defaultBlockState()));
            lifetime = 0;
        }
        rCol = 0.6f;
        gCol = 0.6f;
        bCol = 0.6f;
        quadSize = 0.1f * (random.nextFloat() * 0.3f + 0.4f);
        side = par15;
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int) (Mth.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 10.0f);
        if (base < 1) {
            base = 1;
        }
        lifetime = base / 2 + random.nextInt(base);
        float f3 = 0.01f;
        xd = (float) random.nextGaussian() * f3;
        yd = (float) random.nextGaussian() * f3;
        zd = (float) random.nextGaussian() * f3;
        gravity = 0.01f;
        uo = random.nextFloat() * 3.0f;
        vo = random.nextFloat() * 3.0f;
    }

    public FXBoreParticles(ClientLevel par1World, double par2, double par4, double par6, double tx, double ty, double tz, double sx, double sy, double sz,
            ItemStack item) {
        super(par1World, par2, par4, par6, 0.0, 0.0, 0.0);
        itemInstance = item;
        setSprite(Minecraft.getInstance().getItemRenderer().getModel(item, par1World, null, 0).getParticleIcon());
        rCol = 0.6f;
        gCol = 0.6f;
        bCol = 0.6f;
        quadSize = 0.1f * (random.nextFloat() * 0.3f + 0.4f);
        side = 0;
        targetX = tx;
        targetY = ty;
        targetZ = tz;
        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int) (Mth.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 10.0f);
        if (base < 1) {
            base = 1;
        }
        lifetime = base / 2 + random.nextInt(base);
        float f3 = 0.01f;
        xd = sx + (float) random.nextGaussian() * f3;
        yd = sy + (float) random.nextGaussian() * f3;
        zd = sz + (float) random.nextGaussian() * f3;
        gravity = 0.01f;
        Minecraft mc = Minecraft.getInstance();
        Entity renderentity = mc.getCameraEntity();
        int visibleDistance = 64;
        if (mc.options.graphicsMode().get() == GraphicsStatus.FAST) {
            visibleDistance = 32;
        }
        if (renderentity != null && Math.sqrt(renderentity.distanceToSqr(x, y, z)) > visibleDistance) {
            lifetime = 0;
        }
        uo = random.nextFloat() * 3.0f;
        vo = random.nextFloat() * 3.0f;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public void setAlphaF(float a) {
        alpha = a;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (target != null) {
            targetX = target.getX();
            targetY = target.getY() + target.getEyeHeight();
            targetZ = target.getZ();
        }
        if (age++ >= lifetime || (Mth.floor(x) == Mth.floor(targetX) && Mth.floor(y) == Mth.floor(targetY) && Mth.floor(z) == Mth.floor(targetZ))) {
            remove();
            return;
        }
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.95;
        zd *= 0.985;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double d11 = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double clamp = Math.min(0.25, d11 / 15.0);
        if (d11 < 2.0) {
            quadSize *= 0.9f;
        }
        dx /= d11;
        dy /= d11;
        dz /= d11;
        xd += dx * clamp;
        yd += dy * clamp;
        zd += dz * clamp;
        xd = Mth.clamp((float) xd, -clamp, clamp);
        yd = Mth.clamp((float) yd, -clamp, clamp);
        zd = Mth.clamp((float) zd, -clamp, clamp);
        xd += random.nextGaussian() * 0.005;
        yd += random.nextGaussian() * 0.005;
        zd += random.nextGaussian() * 0.005;
    }

    /** Tints the bits with the block's color at pos, or with the item's color when the block is not there. */
    public FXBoreParticles getObjectColor(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (blockInstance == null || level.getBlockState(pos) != blockInstance) {
            try {
                int var4 = mc.getItemColors().getColor(itemInstance, 0);
                rCol *= (var4 >> 16 & 0xFF) / 255.0f;
                gCol *= (var4 >> 8 & 0xFF) / 255.0f;
                bCol *= (var4 & 0xFF) / 255.0f;
            } catch (Exception ex) {
            }
            return this;
        }
        if (blockInstance.is(Blocks.GRASS_BLOCK) && side != 1) {
            return this;
        }
        try {
            int var4 = mc.getBlockColors().getColor(blockInstance, level, pos, 0);
            rCol *= (var4 >> 16 & 0xFF) / 255.0f;
            gCol *= (var4 >> 8 & 0xFF) / 255.0f;
            bCol *= (var4 & 0xFF) / 255.0f;
        } catch (Exception ex2) {
        }
        return this;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
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
