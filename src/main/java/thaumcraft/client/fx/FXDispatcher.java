package thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.client.fx.particles.FXBlockRunes;
import thaumcraft.client.fx.particles.FXBoreParticles;
import thaumcraft.client.fx.particles.FXBoreSparkle;
import thaumcraft.client.fx.particles.FXBreakingFade;
import thaumcraft.client.fx.particles.FXFireMote;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.client.fx.particles.FXGenericP2E;
import thaumcraft.client.fx.particles.FXPlane;
import thaumcraft.client.fx.particles.FXSlimyBubble;
import thaumcraft.client.fx.particles.FXSmokeSpiral;
import thaumcraft.client.fx.particles.FXVent;
import thaumcraft.client.fx.particles.FXVent2;
import thaumcraft.client.fx.particles.FXVisSparkle;
import thaumcraft.client.fx.particles.FXWispEG;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.crafting.TileCrucible;

/**
 * Port of FXDispatcher, the client effects. The original was an instance (FXDispatcher.INSTANCE); these are static.
 * TODO (stage 10b): the beams and streams (beamCont, beamBore, essentiaTrailFx, boreTrailFx, waterTrailFx,
 * voidStreak, arcLightning, arcBolt), blockWard, drawPedestalShield and the GUI sparkles (drawSimpleSparkleGui).
 */
public final class FXDispatcher {
    private FXDispatcher() {
    }

    public static ClientLevel getWorld() {
        return Minecraft.getInstance().level;
    }

    private static RandomSource rand() {
        return getWorld().random;
    }

    private static float red(int c) {
        return ((c >> 16) & 0xFF) / 255.0f;
    }

    private static float green(int c) {
        return ((c >> 8) & 0xFF) / 255.0f;
    }

    private static float blue(int c) {
        return (c & 0xFF) / 255.0f;
    }

    private static AABB blockBounds(BlockPos p) {
        VoxelShape shape = getWorld().getBlockState(p).getShape(getWorld(), p);
        return shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();
    }

    private static void addVanilla(Particle fx) {
        Minecraft.getInstance().particleEngine.add(fx);
    }

    public static void drawFireMote(float x, float y, float z, float vx, float vy, float vz, float r, float g, float b, float alpha, float scale) {
        if (getWorld() == null) return;
        boolean bb = rand().nextBoolean();
        FXFireMote glow = new FXFireMote(getWorld(), x, y, z, vx, vy, vz, r, g, b, bb ? scale / 3.0f : scale, bb ? 1 : 0);
        glow.setAlphaF(alpha);
        ParticleEngine.addEffect(getWorld(), glow);
    }

    public static void drawAlumentum(float x, float y, float z, float vx, float vy, float vz, float r, float g, float b, float alpha, float scale) {
        if (getWorld() == null) return;
        FXFireMote glow = new FXFireMote(getWorld(), x, y, z, vx, vy, vz, r, g, b, scale, 1);
        glow.setAlphaF(alpha);
        ParticleEngine.addEffect(getWorld(), glow);
    }

    public static void drawTaintParticles(float x, float y, float z, float vx, float vy, float vz, float scale) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, vx, vy, vz);
        fb.setMaxAge(80 + rand().nextInt(20));
        fb.setRBGColorF(0.4f + rand().nextFloat() * 0.2f, 0.1f + rand().nextFloat() * 0.3f, 0.5f + rand().nextFloat() * 0.2f);
        fb.setAlphaF(0.75f, 0.0f);
        fb.setGridSize(16);
        fb.setParticles(57 + rand().nextInt(3), 1, 1);
        fb.setScale(scale, scale / 4.0f);
        fb.setLayer(1);
        fb.setSlowDown(0.9750000238418579);
        fb.setGravity(0.2f);
        fb.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -1.0f : 1.0f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawLightningFlash(double x, double y, double z, float r, float g, float b, float alpha, float scale) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(5 + rand().nextInt(5));
        fb.setGridSize(16);
        fb.setRBGColorF(r, g, b);
        fb.setAlphaF(alpha, 0.0f);
        fb.setParticles(108 + rand().nextInt(4), 1, 1);
        fb.setScale(scale);
        fb.setLayer(0);
        fb.setRotationSpeed(rand().nextFloat(), 0.0f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawGenericParticles(double x, double y, double z, double mx, double my, double mz, GenPart part) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, mx, my, mz);
        fb.setMaxAge(part.age);
        fb.setRBGColorF(part.redStart, part.greenStart, part.blueStart, part.redEnd, part.greenEnd, part.blueEnd);
        fb.setAlphaF(part.alpha);
        fb.setLoop(part.loop);
        fb.setParticles(part.partStart, part.partNum, part.partInc);
        fb.setScale(part.scale);
        fb.setLayer(part.layer);
        fb.setRotationSpeed(part.rotstart, part.rot);
        fb.setSlowDown(part.slowDown);
        fb.setGravity(part.grav);
        fb.setGridSize(part.grid);
        ParticleEngine.addEffectWithDelay(getWorld(), fb, part.delay);
    }

    public static void drawGenericParticles(double x, double y, double z, double x2, double y2, double z2, float r, float g, float b, float alpha,
            boolean loop, int start, int num, int inc, int age, int delay, float scale, float rot, int layer) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        fb.setMaxAge(age);
        fb.setRBGColorF(r, g, b);
        fb.setAlphaF(alpha);
        fb.setLoop(loop);
        fb.setParticles(start, num, inc);
        fb.setScale(scale);
        fb.setLayer(layer);
        fb.setRotationSpeed(rot);
        ParticleEngine.addEffectWithDelay(getWorld(), fb, delay);
    }

    public static void drawGenericParticles16(double x, double y, double z, double x2, double y2, double z2, float r, float g, float b, float alpha,
            boolean loop, int start, int num, int inc, int age, int delay, float scale, float rot, int layer) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        fb.setGridSize(16);
        fb.setMaxAge(age);
        fb.setRBGColorF(r, g, b);
        fb.setAlphaF(alpha);
        fb.setLoop(loop);
        fb.setParticles(start, num, inc);
        fb.setScale(scale);
        fb.setLayer(layer);
        fb.setRotationSpeed(rot);
        ParticleEngine.addEffectWithDelay(getWorld(), fb, delay);
    }

    public static void drawLevitatorParticles(double x, double y, double z, double x2, double y2, double z2) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        fb.setMaxAge(200 + rand().nextInt(100));
        fb.setRBGColorF(0.5f, 0.5f, 0.2f);
        fb.setAlphaF(0.3f, 0.0f);
        fb.setGridSize(16);
        fb.setParticles(56, 1, 1);
        fb.setScale(2.0f, 5.0f);
        fb.setLayer(0);
        fb.setSlowDown(1.0);
        fb.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -1.0f : 1.0f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawStabilizerParticles(double x, double y, double z, double x2, double y2, double z2, int life) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        fb.setMaxAge(life + rand().nextInt(life));
        fb.setRBGColorF(0.5f, 0.2f, 0.5f);
        fb.setAlphaF(0.3f, 0.0f);
        fb.setGridSize(16);
        fb.setParticles(72 + rand().nextInt(4), 1, 1);
        fb.setScale(1.0f, 10.0f);
        fb.setLayer(0);
        fb.setSlowDown(1.01);
        fb.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -1.0f : 1.0f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawGolemFlyParticles(double x, double y, double z, double x2, double y2, double z2) {
        if (getWorld() == null) return;
        try {
            FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
            fb.setMaxAge(20 + rand().nextInt(5));
            fb.setAlphaF(0.3f, 0.0f);
            fb.setGridSize(16);
            fb.setParticles(56, 1, 1);
            fb.setScale(1.5f, 3.0f, 8.0f);
            fb.setLayer(0);
            fb.setSlowDown(1.0);
            fb.setWind(0.001);
            fb.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -1.0f : 1.0f);
            ParticleEngine.addEffect(getWorld(), fb);
        } catch (Exception ex) {
        }
    }

    public static void drawPollutionParticles(BlockPos p) {
        if (getWorld() == null) return;
        float x = p.getX() + 0.2f + rand().nextFloat() * 0.6f;
        float y = p.getY() + 0.2f + rand().nextFloat() * 0.6f;
        float z = p.getZ() + 0.2f + rand().nextFloat() * 0.6f;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, (rand().nextFloat() - rand().nextFloat()) * 0.005, 0.02,
                (rand().nextFloat() - rand().nextFloat()) * 0.005);
        fb.setMaxAge(100 + rand().nextInt(60));
        fb.setRBGColorF(1.0f, 0.3f, 0.9f);
        fb.setAlphaF(0.5f, 0.0f);
        fb.setGridSize(16);
        fb.setParticles(56, 1, 1);
        fb.setScale(2.0f, 5.0f);
        fb.setLayer(1);
        fb.setSlowDown(1.0);
        fb.setWind(0.001);
        fb.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -1.0f : 1.0f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawBlockSparkles(BlockPos p, Vec3 start) {
        if (getWorld() == null) return;
        ClientLevel world = getWorld();
        AABB bs = blockBounds(p);
        // The original's bs.grow() result was thrown away.
        int num = (int) (bs.getSize() * 20.0);
        for (Direction face : Direction.values()) {
            BlockState state = world.getBlockState(p.relative(face));
            if (!state.isSolidRender(world, p.relative(face))) {
                if (!state.isFaceSturdy(world, p.relative(face), face.getOpposite())) {
                    boolean rx = face.getStepX() == 0;
                    boolean ry = face.getStepY() == 0;
                    boolean rz = face.getStepZ() == 0;
                    double mx = 0.5 + face.getStepX() * 0.51;
                    double my = 0.5 + face.getStepY() * 0.51;
                    double mz = 0.5 + face.getStepZ() * 0.51;
                    for (int a = 0; a < num * 2; ++a) {
                        double x = mx;
                        double y = my;
                        double z = mz;
                        if (rx) {
                            x += rand().nextGaussian() * 0.6;
                        }
                        if (ry) {
                            y += rand().nextGaussian() * 0.6;
                        }
                        if (rz) {
                            z += rand().nextGaussian() * 0.6;
                        }
                        x = Mth.clamp(x, bs.minX, bs.maxX);
                        y = Mth.clamp(y, bs.minY, bs.maxY);
                        z = Mth.clamp(z, bs.minZ, bs.maxZ);
                        float r = Mth.nextInt(rand(), 255, 255) / 255.0f;
                        float g = Mth.nextInt(rand(), 189, 255) / 255.0f;
                        float b = Mth.nextInt(rand(), 64, 255) / 255.0f;
                        Vec3 v1 = new Vec3(p.getX() + x, p.getY() + y, p.getZ() + z);
                        double delay = rand().nextInt(5) + v1.distanceTo(start) * 16.0;
                        drawSimpleSparkle(rand(), p.getX() + x, p.getY() + y, p.getZ() + z, 0.0, 0.0025, 0.0, 0.4f + (float) rand().nextGaussian() * 0.1f, r, g,
                                b, (int) delay, 1.0f, 0.01f, 16);
                    }
                }
            }
        }
    }

    public static void drawLineSparkle(RandomSource rand, double x, double y, double z, double x2, double y2, double z2, float scale, float r, float g,
            float b, int delay, float decay, float grav, int baseAge) {
        if (getWorld() == null) return;
        boolean sp = rand.nextFloat() < 0.2;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        int age = baseAge * 4 + rand().nextInt(baseAge);
        fb.setMaxAge(age);
        fb.setRBGColorF(r, g, b);
        fb.setAlphaF(0.0f, 1.0f, 0.0f);
        fb.setParticles(sp ? 320 : 512, 16, 1);
        fb.setLoop(true);
        fb.setGravity(grav);
        fb.setScale(scale, scale * 2.0f, scale);
        fb.setLayer(0);
        fb.setSlowDown(decay);
        fb.setRandomMovementScale(5.0E-5f, 0.0f, 5.0E-5f);
        ParticleEngine.addEffectWithDelay(getWorld(), fb, delay);
    }

    public static void drawSimpleSparkle(RandomSource rand, double x, double y, double z, double x2, double y2, double z2, float scale, float r, float g,
            float b, int delay, float decay, float grav, int baseAge) {
        if (getWorld() == null) return;
        boolean sp = rand.nextFloat() < 0.2;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        int age = baseAge * 4 + rand().nextInt(baseAge);
        fb.setMaxAge(age);
        fb.setRBGColorF(r, g, b);
        float[] alphas = new float[6 + rand.nextInt(age / 3)];
        for (int a = 1; a < alphas.length - 1; ++a) {
            alphas[a] = rand.nextFloat();
        }
        fb.setAlphaF(alphas);
        fb.setParticles(sp ? 320 : 512, 16, 1);
        fb.setLoop(true);
        fb.setGravity(grav);
        fb.setScale(scale, scale * 2.0f);
        fb.setLayer(0);
        fb.setSlowDown(decay);
        fb.setRandomMovementScale(5.0E-4f, 0.001f, 5.0E-4f);
        fb.setWind(5.0E-4);
        ParticleEngine.addEffectWithDelay(getWorld(), fb, delay);
    }

    public static void drawBlockMistParticles(BlockPos p, int c) {
        if (getWorld() == null) return;
        AABB bs = blockBounds(p);
        for (int a = 0; a < 8; ++a) {
            double x = p.getX() + bs.minX + rand().nextFloat() * (bs.maxX - bs.minX);
            double y = p.getY() + bs.minY + rand().nextFloat() * (bs.maxY - bs.minY);
            double z = p.getZ() + bs.minZ + rand().nextFloat() * (bs.maxZ - bs.minZ);
            FXGeneric fb = new FXGeneric(getWorld(), x, y, z, rand().nextGaussian() * 0.01, rand().nextFloat() * 0.075, rand().nextGaussian() * 0.01);
            fb.setMaxAge(50 + rand().nextInt(25));
            fb.setRBGColorF(red(c), green(c), blue(c));
            fb.setAlphaF(0.0f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.0f);
            fb.setGridSize(16);
            fb.setParticles(56, 1, 1);
            fb.setScale(5.0f, 1.0f);
            fb.setLayer(0);
            fb.setSlowDown(1.0);
            fb.setGravity(0.1f);
            fb.setWind(0.001);
            fb.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -1.0f : 1.0f);
            ParticleEngine.addEffect(getWorld(), fb);
        }
    }

    public static void drawFocusCloudParticle(double x, double y, double z, double mx, double my, double mz, int c) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, mx, my, mz);
        fb.setMaxAge(20 + rand().nextInt(10));
        fb.setRBGColorF(red(c), green(c), blue(c));
        fb.setAlphaF(0.0f, 0.66f, 0.0f);
        fb.setGridSize(16);
        fb.setParticles(56 + rand().nextInt(4), 1, 1);
        fb.setScale(5.0f + rand().nextFloat(), 10.0f + rand().nextFloat());
        fb.setLayer(0);
        fb.setSlowDown(0.99);
        fb.setWind(0.001);
        fb.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -0.25f : 0.25f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawWispyMotesOnBlock(BlockPos pp, int age, float grav) {
        if (getWorld() == null) return;
        drawWispyMotes(pp.getX() + rand().nextFloat(), pp.getY(), pp.getZ() + rand().nextFloat(), 0.0, 0.0, 0.0, age, 0.4f + rand().nextFloat() * 0.6f,
                0.6f + rand().nextFloat() * 0.4f, 0.6f + rand().nextFloat() * 0.4f, grav);
    }

    public static void drawWispyMotes(double d, double e, double f, double vx, double vy, double vz, int age, float grav) {
        if (getWorld() == null) return;
        drawWispyMotes(d, e, f, vx, vy, vz, age, 0.25f + rand().nextFloat() * 0.75f, 0.25f + rand().nextFloat() * 0.75f, 0.25f + rand().nextFloat() * 0.75f,
                grav);
    }

    public static void drawWispyMotes(double d, double e, double f, double vx, double vy, double vz, int age, float r, float g, float b, float grav) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), d, e, f, vx, vy, vz);
        fb.setMaxAge((int) (age + age / 2 * rand().nextFloat()));
        fb.setRBGColorF(r, g, b);
        fb.setAlphaF(0.0f, 0.6f, 0.6f, 0.0f);
        fb.setGridSize(64);
        fb.setParticles(512, 16, 1);
        fb.setScale(1.0f, 0.5f);
        fb.setLoop(true);
        fb.setWind(0.001);
        fb.setGravity(grav);
        fb.setRandomMovementScale(0.0025f, 0.0f, 0.0025f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawBlockMistParticlesFlat(BlockPos p, int c) {
        if (getWorld() == null) return;
        for (int a = 0; a < 6; ++a) {
            double x = p.getX() + rand().nextFloat();
            double y = p.getY() + rand().nextFloat() * 0.125f;
            double z = p.getZ() + rand().nextFloat();
            FXGeneric fb = new FXGeneric(getWorld(), x, y, z, (rand().nextFloat() - rand().nextFloat()) * 0.005, 0.005,
                    (rand().nextFloat() - rand().nextFloat()) * 0.005);
            fb.setMaxAge(400 + rand().nextInt(100));
            fb.setRBGColorF(red(c), green(c), blue(c));
            fb.setAlphaF(1.0f, 0.0f);
            fb.setGridSize(8);
            fb.setParticles(24, 1, 1);
            fb.setScale(2.0f, 5.0f);
            fb.setLayer(0);
            fb.setSlowDown(1.0);
            fb.setWind(0.001);
            fb.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -1.0f : 1.0f);
            ParticleEngine.addEffect(getWorld(), fb);
        }
    }

    public static void crucibleBubble(float x, float y, float z, float cr, float cg, float cb) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(15 + rand().nextInt(10));
        fb.setScale(rand().nextFloat() * 0.3f + 0.3f);
        fb.setRBGColorF(cr, cg, cb);
        fb.setRandomMovementScale(0.002f, 0.002f, 0.002f);
        fb.setGravity(-0.001f);
        fb.setParticle(64);
        fb.setFinalFrames(65, 66, 66);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void crucibleBoil(BlockPos pos, TileCrucible tile, int j) {
        if (getWorld() == null) return;
        for (int a = 0; a < 2; ++a) {
            FXGeneric fb = new FXGeneric(getWorld(), pos.getX() + 0.2f + rand().nextFloat() * 0.6f, pos.getY() + 0.1f + tile.getFluidHeight(),
                    pos.getZ() + 0.2f + rand().nextFloat() * 0.6f, 0.0, 0.002, 0.0);
            fb.setMaxAge((int) (7.0 + 8.0 / (Math.random() * 0.8 + 0.2)));
            fb.setScale(rand().nextFloat() * 0.3f + 0.2f);
            if (tile.aspects.size() == 0) {
                fb.setRBGColorF(1.0f, 1.0f, 1.0f);
            } else {
                int color = tile.aspects.getAspects()[rand().nextInt(tile.aspects.getAspects().length)].getColor();
                fb.setRBGColorF(red(color), green(color), blue(color));
            }
            fb.setRandomMovementScale(0.001f, 0.001f, 0.001f);
            fb.setGravity(-0.025f * j);
            fb.setParticle(64);
            fb.setFinalFrames(65, 66);
            ParticleEngine.addEffect(getWorld(), fb);
        }
    }

    public static void crucibleFroth(float x, float y, float z) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(4 + rand().nextInt(3));
        fb.setScale(rand().nextFloat() * 0.2f + 0.2f);
        fb.setRBGColorF(0.5f, 0.5f, 0.7f);
        fb.setRandomMovementScale(0.001f, 0.001f, 0.001f);
        fb.setGravity(0.1f);
        fb.setParticle(64);
        fb.setFinalFrames(65, 66);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void crucibleFrothDown(float x, float y, float z) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(12 + rand().nextInt(12));
        fb.setScale(rand().nextFloat() * 0.2f + 0.4f);
        fb.setRBGColorF(0.25f, 0.0f, 0.75f);
        fb.setAlphaF(0.8f);
        fb.setRandomMovementScale(0.001f, 0.001f, 0.001f);
        fb.setGravity(0.05f);
        fb.setNoClip(false);
        fb.setParticle(73);
        fb.setFinalFrames(65, 66);
        fb.setLayer(1);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawBamf(BlockPos p, boolean sound, boolean flair, Direction side) {
        drawBamf(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, sound, flair, side);
    }

    public static void drawBamf(BlockPos p, float r, float g, float b, boolean sound, boolean flair, Direction side) {
        drawBamf(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, r, g, b, sound, flair, side);
    }

    public static void drawBamf(BlockPos p, int color, boolean sound, boolean flair, Direction side) {
        drawBamf(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, color, sound, flair, side);
    }

    public static void drawBamf(double x, double y, double z, int color, boolean sound, boolean flair, Direction side) {
        drawBamf(x, y, z, red(color), green(color), blue(color), sound, flair, side);
    }

    public static void drawBamf(double x, double y, double z, boolean sound, boolean flair, Direction side) {
        drawBamf(x, y, z, 0.5f, 0.1f, 0.6f, sound, flair, side);
    }

    public static void drawBamf(double x, double y, double z, float r, float g, float b, boolean sound, boolean flair, Direction side) {
        if (getWorld() == null) return;
        if (sound) {
            getWorld().playLocalSound(x, y, z, SoundsTC.POOF.get(), SoundSource.BLOCKS, 0.4f, 1.0f + (float) rand().nextGaussian() * 0.05f, false);
        }
        for (int a = 0; a < 6 + rand().nextInt(3) + 2; ++a) {
            double vx = (0.05f + rand().nextFloat() * 0.05f) * (rand().nextBoolean() ? -1 : 1);
            double vy = (0.05f + rand().nextFloat() * 0.05f) * (rand().nextBoolean() ? -1 : 1);
            double vz = (0.05f + rand().nextFloat() * 0.05f) * (rand().nextBoolean() ? -1 : 1);
            if (side != null) {
                vx += side.getStepX() * 0.1f;
                vy += side.getStepY() * 0.1f;
                vz += side.getStepZ() * 0.1f;
            }
            FXGeneric fb2 = new FXGeneric(getWorld(), x + vx * 2.0, y + vy * 2.0, z + vz * 2.0, vx / 2.0, vy / 2.0, vz / 2.0);
            fb2.setMaxAge(20 + rand().nextInt(15));
            fb2.setRBGColorF(Mth.clamp(r * (1.0f + (float) rand().nextGaussian() * 0.1f), 0.0f, 1.0f),
                    Mth.clamp(g * (1.0f + (float) rand().nextGaussian() * 0.1f), 0.0f, 1.0f), Mth.clamp(b * (1.0f + (float) rand().nextGaussian() * 0.1f), 0.0f, 1.0f));
            fb2.setAlphaF(1.0f, 0.1f);
            fb2.setGridSize(16);
            fb2.setParticles(123, 5, 1);
            fb2.setScale(3.0f, 4.0f + rand().nextFloat() * 3.0f);
            fb2.setLayer(1);
            fb2.setSlowDown(0.7);
            fb2.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -1.0f : 1.0f);
            ParticleEngine.addEffect(getWorld(), fb2);
        }
        if (flair) {
            for (int a = 0; a < 2 + rand().nextInt(3); ++a) {
                double vx = (0.025f + rand().nextFloat() * 0.025f) * (rand().nextBoolean() ? -1 : 1);
                double vy = (0.025f + rand().nextFloat() * 0.025f) * (rand().nextBoolean() ? -1 : 1);
                double vz = (0.025f + rand().nextFloat() * 0.025f) * (rand().nextBoolean() ? -1 : 1);
                drawWispyMotes(x + vx * 2.0, y + vy * 2.0, z + vz * 2.0, vx, vy, vz, 15 + rand().nextInt(10), -0.01f);
            }
            FXGeneric fb3 = new FXGeneric(getWorld(), x, y, z, 0.0, 0.0, 0.0);
            fb3.setMaxAge(10 + rand().nextInt(5));
            fb3.setRBGColorF(1.0f, 0.9f, 1.0f);
            fb3.setAlphaF(1.0f, 0.0f);
            fb3.setGridSize(16);
            fb3.setParticles(77, 1, 1);
            fb3.setScale(10.0f + rand().nextFloat() * 2.0f, 0.0f);
            fb3.setLayer(0);
            fb3.setRotationSpeed(rand().nextFloat(), (float) rand().nextGaussian());
            ParticleEngine.addEffect(getWorld(), fb3);
        }
        for (int a = 0; a < (flair ? 2 : 0) + rand().nextInt(3); ++a) {
            drawCurlyWisp(x, y, z, 0.0, 0.0, 0.0, 1.0f, (0.9f + rand().nextFloat() * 0.1f + r) / 2.0f, (0.1f + g) / 2.0f,
                    (0.5f + rand().nextFloat() * 0.1f + b) / 2.0f, 0.75f, side, a, 0, 0);
        }
    }

    public static void drawCurlyWisp(double x, double y, double z, double vx, double vy, double vz, float scale, float r, float g, float b, float a,
            Direction side, int seed, int layer, int delay) {
        if (getWorld() == null) return;
        vx += (0.0025f + rand().nextFloat() * 0.005f) * (rand().nextBoolean() ? -1 : 1);
        vy += (0.0025f + rand().nextFloat() * 0.005f) * (rand().nextBoolean() ? -1 : 1);
        vz += (0.0025f + rand().nextFloat() * 0.005f) * (rand().nextBoolean() ? -1 : 1);
        if (side != null) {
            vx += side.getStepX() * 0.025f;
            vy += side.getStepY() * 0.025f;
            vz += side.getStepZ() * 0.025f;
        }
        FXGeneric fb2 = new FXGeneric(getWorld(), x + vx * 5.0, y + vy * 5.0, z + vz * 5.0, vx, vy, vz);
        if (seed > 0 && rand().nextBoolean()) {
            fb2.setAngles(90.0f * (float) rand().nextGaussian(), 90.0f * (float) rand().nextGaussian());
        }
        fb2.setMaxAge(25 + rand().nextInt(20 + 20 * seed));
        fb2.setRBGColorF(r, g, b, 0.1f, 0.0f, 0.1f);
        fb2.setAlphaF(a, 0.0f);
        fb2.setGridSize(16);
        fb2.setParticles(60 + rand().nextInt(4), 1, 1);
        fb2.setScale(5.0f * scale, (10.0f + rand().nextFloat() * 4.0f) * scale);
        fb2.setLayer(layer);
        fb2.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -2.0f - rand().nextFloat() * 2.0f : 2.0f + rand().nextFloat() * 2.0f);
        ParticleEngine.addEffectWithDelay(getWorld(), fb2, delay);
    }

    public static void pechsCurseTick(double posX, double posY, double posZ) {
        if (getWorld() == null) return;
        FXGeneric fb2 = new FXGeneric(getWorld(), posX, posY, posZ, 0.0, 0.0, 0.0);
        fb2.setAngles(90.0f * (float) rand().nextGaussian(), 90.0f * (float) rand().nextGaussian());
        fb2.setMaxAge(50 + rand().nextInt(50));
        fb2.setRBGColorF(0.9f, 0.1f, 0.5f, 0.1f + rand().nextFloat() * 0.1f, 0.0f, 0.5f + rand().nextFloat() * 0.1f);
        fb2.setAlphaF(0.75f, 0.0f);
        fb2.setGridSize(8);
        fb2.setParticles(28 + rand().nextInt(4), 1, 1);
        fb2.setScale(3.0f, 5.0f + rand().nextFloat() * 2.0f);
        fb2.setLayer(0);
        fb2.setRotationSpeed(rand().nextFloat(), rand().nextBoolean() ? -3.0f - rand().nextFloat() * 3.0f : 3.0f + rand().nextFloat() * 3.0f);
        ParticleEngine.addEffect(getWorld(), fb2);
        drawWispyMotes(posX, posY, posZ, 0.0, 0.0, 0.0, 10 + rand().nextInt(10), -0.01f);
    }

    public static void scanHighlight(BlockPos p) {
        if (getWorld() == null) return;
        scanHighlight(blockBounds(p).move(p));
    }

    public static void scanHighlight(Entity e) {
        scanHighlight(e.getBoundingBox());
    }

    public static void scanHighlight(AABB bb) {
        if (getWorld() == null) return;
        int num = Mth.ceil(bb.getSize() * 2.0);
        double ax = (bb.minX + bb.maxX) / 2.0;
        double ay = (bb.minY + bb.maxY) / 2.0;
        double az = (bb.minZ + bb.maxZ) / 2.0;
        for (Direction face : Direction.values()) {
            double mx = 0.5 + face.getStepX() * 0.51;
            double my = 0.5 + face.getStepY() * 0.51;
            double mz = 0.5 + face.getStepZ() * 0.51;
            for (int a = 0; a < num * 2; ++a) {
                double x = mx;
                double y = my;
                double z = mz;
                x += rand().nextGaussian() * (bb.maxX - bb.minX);
                y += rand().nextGaussian() * (bb.maxY - bb.minY);
                z += rand().nextGaussian() * (bb.maxZ - bb.minZ);
                x = Mth.clamp(x, bb.minX - ax, bb.maxX - ax);
                y = Mth.clamp(y, bb.minY - ay, bb.maxY - ay);
                z = Mth.clamp(z, bb.minZ - az, bb.maxZ - az);
                float r = Mth.nextInt(rand(), 16, 32) / 255.0f;
                float g = Mth.nextInt(rand(), 132, 165) / 255.0f;
                float b = Mth.nextInt(rand(), 223, 239) / 255.0f;
                drawSimpleSparkle(rand(), ax + x, ay + y, az + z, 0.0, 0.0, 0.0, 0.4f + (float) rand().nextGaussian() * 0.1f, r, g, b, rand().nextInt(10),
                        1.0f, 0.0f, 4);
            }
        }
    }

    public static void sparkle(float x, float y, float z, float r, float g, float b) {
        if (getWorld() == null) return;
        if (rand().nextInt(6) < 4) {
            drawGenericParticles(x, y, z, 0.0, 0.0, 0.0, r, g, b, 0.9f, true, 320, 16, 1, 6 + rand().nextInt(4), 0, 0.6f + rand().nextFloat() * 0.2f, 0.0f, 0);
        }
    }

    public static void visSparkle(int x, int y, int z, int x2, int y2, int z2, int color) {
        if (getWorld() == null) return;
        FXVisSparkle fb = new FXVisSparkle(getWorld(), x + rand().nextFloat(), y + rand().nextFloat(), z + rand().nextFloat(), x2 + 0.4 + rand().nextFloat() * 0.2f,
                y2 + 0.4 + rand().nextFloat() * 0.2f, z2 + 0.4 + rand().nextFloat() * 0.2f);
        fb.setRBGColorF(red(color), green(color), blue(color));
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void splooshFX(Entity e) {
        if (getWorld() == null) return;
        float f = rand().nextFloat() * (float) Math.PI * 2.0f;
        float f2 = rand().nextFloat() * 0.5f + 0.5f;
        float f3 = Mth.sin(f) * 2.0f * 0.5f * f2;
        float f4 = Mth.cos(f) * 2.0f * 0.5f * f2;
        FXBreakingFade fx = new FXBreakingFade(getWorld(), e.getX() + f3, e.getY() + rand().nextFloat() * e.getBbHeight(), e.getZ() + f4, Items.SLIME_BALL);
        if (rand().nextBoolean()) {
            fx.setRBGColorF(0.6f, 0.0f, 0.3f);
            fx.setAlphaF(0.4f);
        } else {
            fx.setRBGColorF(0.3f, 0.0f, 0.3f);
            fx.setAlphaF(0.6f);
        }
        fx.setParticleMaxAge((int) (66.0f / (rand().nextFloat() * 0.9f + 0.1f)));
        addVanilla(fx);
    }

    public static void taintsplosionFX(Entity e) {
        if (getWorld() == null) return;
        FXBreakingFade fx = new FXBreakingFade(getWorld(), e.getX(), e.getY() + rand().nextFloat() * e.getBbHeight(), e.getZ(), Items.SLIME_BALL);
        if (rand().nextBoolean()) {
            fx.setRBGColorF(0.6f, 0.0f, 0.3f);
            fx.setAlphaF(0.4f);
        } else {
            fx.setRBGColorF(0.3f, 0.0f, 0.3f);
            fx.setAlphaF(0.6f);
        }
        fx.setSpeed(Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 1.0, Math.random() * 2.0 - 1.0);
        fx.boom();
        fx.setParticleMaxAge((int) (66.0f / (rand().nextFloat() * 0.9f + 0.1f)));
        addVanilla(fx);
    }

    public static void tentacleAriseFX(Entity e) {
        if (getWorld() == null) return;
        float h = e.getBbHeight();
        for (int j = 0; j < 2.0f * h; ++j) {
            float f = rand().nextFloat() * (float) Math.PI * h;
            float f2 = rand().nextFloat() * 0.5f + 0.5f;
            float f3 = Mth.sin(f) * h * 0.25f * f2;
            float f4 = Mth.cos(f) * h * 0.25f * f2;
            FXBreakingFade fx = new FXBreakingFade(getWorld(), e.getX() + f3, e.getY(), e.getZ() + f4, Items.SLIME_BALL);
            fx.setRBGColorF(0.4f, 0.0f, 0.4f);
            fx.setAlphaF(0.5f);
            fx.setParticleMaxAge((int) (66.0f / (rand().nextFloat() * 0.9f + 0.1f)));
            addVanilla(fx);
            BlockState below = getWorld().getBlockState(e.blockPosition().below());
            if (!below.isAir()) {
                f = rand().nextFloat() * (float) Math.PI * h;
                f2 = rand().nextFloat() * 0.5f + 0.5f;
                f3 = Mth.sin(f) * h * 0.25f * f2;
                f4 = Mth.cos(f) * h * 0.25f * f2;
                getWorld().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, below), e.getX() + f3, e.getY(), e.getZ() + f4, 0.0, 0.0, 0.0);
            }
        }
    }

    public static void slimeJumpFX(Entity e, int i) {
        if (getWorld() == null) return;
        float f = rand().nextFloat() * (float) Math.PI * 2.0f;
        float f2 = rand().nextFloat() * 0.5f + 0.5f;
        float f3 = Mth.sin(f) * i * 0.5f * f2;
        float f4 = Mth.cos(f) * i * 0.5f * f2;
        FXBreakingFade fx = new FXBreakingFade(getWorld(), e.getX() + f3, (e.getBoundingBox().minY + e.getBoundingBox().maxY) / 2.0, e.getZ() + f4, Items.SLIME_BALL);
        fx.setRBGColorF(0.7f, 0.0f, 1.0f);
        fx.setAlphaF(0.4f);
        fx.setParticleMaxAge((int) (66.0f / (rand().nextFloat() * 0.9f + 0.1f)));
        addVanilla(fx);
    }

    public static void taintLandFX(Entity e) {
        if (getWorld() == null) return;
        float f = rand().nextFloat() * (float) Math.PI * 2.0f;
        float f2 = rand().nextFloat() * 0.5f + 0.5f;
        float f3 = Mth.sin(f) * 2.0f * 0.5f * f2;
        float f4 = Mth.cos(f) * 2.0f * 0.5f * f2;
        FXBreakingFade fx = new FXBreakingFade(getWorld(), e.getX() + f3, (e.getBoundingBox().minY + e.getBoundingBox().maxY) / 2.0, e.getZ() + f4, Items.SLIME_BALL);
        fx.setRBGColorF(0.1f, 0.0f, 0.1f);
        fx.setAlphaF(0.4f);
        fx.setParticleMaxAge((int) (66.0f / (rand().nextFloat() * 0.9f + 0.1f)));
        addVanilla(fx);
    }

    public static void drawInfusionParticles1(double x, double y, double z, BlockPos pos, ItemStack stack) {
        if (getWorld() == null) return;
        FXBoreParticles fb = new FXBoreParticles(getWorld(), x, y, z, pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5, (float) rand().nextGaussian() * 0.03f,
                (float) rand().nextGaussian() * 0.03f, (float) rand().nextGaussian() * 0.03f, stack).getObjectColor(pos);
        fb.setAlphaF(0.3f);
        addVanilla(fb);
    }

    public static void drawInfusionParticles2(double x, double y, double z, BlockPos pos, BlockState id, int md) {
        if (getWorld() == null) return;
        FXBoreParticles fb = new FXBoreParticles(getWorld(), x, y, z, pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5, id, md).getObjectColor(pos);
        fb.setAlphaF(0.3f);
        addVanilla(fb);
    }

    public static void drawInfusionParticles3(double x, double y, double z, int x2, int y2, int z2) {
        if (getWorld() == null) return;
        FXBoreSparkle fb = new FXBoreSparkle(getWorld(), x, y, z, x2 + 0.5, y2 - 0.5, z2 + 0.5);
        fb.setRBGColorF(0.4f + rand().nextFloat() * 0.2f, 0.2f, 0.6f + rand().nextFloat() * 0.3f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawInfusionParticles4(double x, double y, double z, int x2, int y2, int z2) {
        if (getWorld() == null) return;
        FXBoreSparkle fb = new FXBoreSparkle(getWorld(), x, y, z, x2 + 0.5, y2 - 0.5, z2 + 0.5);
        fb.setRBGColorF(0.2f, 0.6f + rand().nextFloat() * 0.3f, 0.3f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawVentParticles(double x, double y, double z, double x2, double y2, double z2, int color) {
        if (getWorld() == null) return;
        FXVent fb = new FXVent(getWorld(), x, y, z, x2, y2, z2, color);
        fb.setAlphaF(0.4f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawVentParticles(double x, double y, double z, double x2, double y2, double z2, int color, float scale) {
        if (getWorld() == null) return;
        FXVent fb = new FXVent(getWorld(), x, y, z, x2, y2, z2, color);
        fb.setAlphaF(0.4f);
        fb.setScale(scale);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawVentParticles2(double x, double y, double z, double x2, double y2, double z2, int color, float scale) {
        if (getWorld() == null) return;
        FXVent2 fb = new FXVent2(getWorld(), x, y, z, x2, y2, z2, color);
        fb.setAlphaF(0.4f);
        fb.setScale(scale);
        ParticleEngine.addEffect(getWorld(), fb);
        if (rand().nextInt(6) < 2) {
            drawGenericParticles(x, y, z, x2 / 2.0, y2 / 2.0, z2 / 2.0, 1.0f, 0.7f, 0.2f, 0.9f, true, 320, 16, 1, 10 + rand().nextInt(4), 0,
                    0.25f + rand().nextFloat() * 0.1f, 0.0f, 0);
        }
    }

    public static void spark(double d, double e, double f, float size, float r, float g, float b, float a) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), d, e, f, 0.0, 0.0, 0.0);
        fb.setMaxAge(5 + rand().nextInt(5));
        fb.setAlphaF(a);
        fb.setRBGColorF(r, g, b);
        fb.setGridSize(16);
        fb.setParticles(8 + rand().nextInt(3) * 16, 8, 1);
        fb.setScale(size);
        fb.setFlipped(rand().nextBoolean());
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void smokeSpiral(double x, double y, double z, float rad, int start, int miny, int color) {
        if (getWorld() == null) return;
        FXSmokeSpiral fx = new FXSmokeSpiral(getWorld(), x, y, z, rad, start, miny);
        fx.setRBGColorF(red(color), green(color), blue(color));
        ParticleEngine.addEffect(getWorld(), fx);
    }

    public static void wispFXEG(double posX, double posY, double posZ, Entity target) {
        if (getWorld() == null) return;
        for (int a = 0; a < 2; ++a) {
            FXWispEG ef = new FXWispEG(getWorld(), posX, posY, posZ, target);
            ParticleEngine.addEffect(getWorld(), ef);
        }
    }

    public static void burst(double sx, double sy, double sz, float size) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), sx, sy, sz, 0.0, 0.0, 0.0);
        fb.setMaxAge(31);
        fb.setGridSize(16);
        fb.setParticles(208, 31, 1);
        fb.setScale(size);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void excavateFX(BlockPos pos, LivingEntity p, int progress) {
        Minecraft.getInstance().levelRenderer.destroyBlockProgress(p.getId(), pos, progress);
    }

    public static void boreDigFx(int x, int y, int z, Entity e, BlockState bi, int md, int delay) {
        if (getWorld() == null) return;
        float p = 50.0f;
        for (int a = 0; a < p / delay; ++a) {
            if (rand().nextInt(4) == 0) {
                FXBoreSparkle fb = new FXBoreSparkle(getWorld(), x + rand().nextFloat(), y + rand().nextFloat(), z + rand().nextFloat(), e);
                ParticleEngine.addEffect(getWorld(), fb);
            } else {
                FXBoreParticles fb2 = new FXBoreParticles(getWorld(), x + rand().nextFloat(), y + rand().nextFloat(), z + rand().nextFloat(), e.getX(), e.getY(),
                        e.getZ(), bi, md);
                fb2.setTarget(e);
                addVanilla(fb2);
            }
        }
    }

    public static void essentiaDropFx(double x, double y, double z, float r, float g, float b, float alpha) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, rand().nextGaussian() * 0.004999999888241291, rand().nextGaussian() * 0.004999999888241291,
                rand().nextGaussian() * 0.004999999888241291);
        fb.setMaxAge(20 + rand().nextInt(10));
        fb.setRBGColorF(r, g, b);
        fb.setAlphaF(alpha);
        fb.setLoop(false);
        fb.setParticles(25, 1, 1);
        fb.setScale(0.4f + rand().nextFloat() * 0.2f, 0.2f);
        fb.setLayer(1);
        fb.setGravity(0.01f);
        fb.setRotationSpeed(0.0f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void jarSplashFx(double x, double y, double z) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x + rand().nextGaussian() * 0.07500000298023224, y, z + rand().nextGaussian() * 0.07500000298023224,
                rand().nextGaussian() * 0.014999999664723873, 0.075f + rand().nextFloat() * 0.05f, rand().nextGaussian() * 0.014999999664723873);
        fb.setMaxAge(20 + rand().nextInt(10));
        int c = 2650102;
        fb.setRBGColorF(red(c), green(c), blue(c));
        fb.setAlphaF(0.5f);
        fb.setLoop(false);
        fb.setParticles(73, 1, 1);
        fb.setScale(0.4f + rand().nextFloat() * 0.3f, 0.0f);
        fb.setLayer(1);
        fb.setGravity(0.3f);
        fb.setRotationSpeed(0.0f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void furnaceLavaFx(int x, int y, int z, int facingX, int facingZ) {
        if (getWorld() == null) return;
        float qx = facingX == 0 ? (rand().nextFloat() - rand().nextFloat()) * 0.5f : facingX * rand().nextFloat();
        float qz = facingZ == 0 ? (rand().nextFloat() - rand().nextFloat()) * 0.5f : facingZ * rand().nextFloat();
        getWorld().addParticle(ParticleTypes.LAVA, x + 0.5f + (rand().nextFloat() - rand().nextFloat()) * 0.3f + facingX * 1.0f, y + 0.3f,
                z + 0.5f + (rand().nextFloat() - rand().nextFloat()) * 0.3f + facingZ * 1.0f, 0.15f * qx, 0.2f * rand().nextFloat(), 0.15f * qz);
    }

    public static void blockRunes(double x, double y, double z, float r, float g, float b, int dur, float grav) {
        if (getWorld() == null) return;
        FXBlockRunes fb = new FXBlockRunes(getWorld(), x + 0.5, y + 0.5, z + 0.5, r, g, b, dur);
        fb.setGravity(grav);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void blockRunes2(double x, double y, double z, float r, float g, float b, int dur, float grav) {
        if (getWorld() == null) return;
        FXBlockRunes fb = new FXBlockRunes(getWorld(), x + 0.5, y + 0.5, z + 0.5, r, g, b, dur);
        fb.setGravity(grav);
        fb.setScale((float) (0.5 + rand().nextGaussian() * 0.10000000149011612));
        fb.setOffsetX(0.0);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawSlash(double x, double y, double z, double x2, double y2, double z2, int dur) {
        if (getWorld() == null) return;
        FXPlane fb = new FXPlane(getWorld(), x, y, z, x2, y2, z2, dur);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    /**
     * BlockFluxGoo.randomDisplayTick. The original set texture 64 with setParticleTextureIndex, which the render
     * overwrites with the start frame (0) until the final frames.
     */
    public static void fluxGooBubble(double x, double y, double z) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(2 + rand().nextInt(3));
        fb.setScale(rand().nextFloat() * 0.3f + 0.2f);
        fb.setRBGColorF(1.0f, 0.0f, 0.5f);
        fb.setRandomMovementScale(0.001f, 0.001f, 0.001f);
        fb.setGravity(-0.01f);
        fb.setAlphaF(0.25f);
        fb.setParticleTextureIndex(64);
        fb.setFinalFrames(65, 66);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    /** BlockFluidDeath.randomDisplayTick. */
    public static void slimyBubble(double x, double y, double z, float scale) {
        if (getWorld() == null) return;
        ParticleEngine.addEffect(getWorld(), new FXSlimyBubble(getWorld(), x, y, z, scale));
    }

    public static void bottleTaintBreak(double x, double y, double z) {
        if (getWorld() == null) return;
        for (int k1 = 0; k1 < 8; ++k1) {
            getWorld().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ConfigItems.bottleTaint.get())), x, y, z,
                    rand().nextGaussian() * 0.15, rand().nextDouble() * 0.2, rand().nextGaussian() * 0.15);
        }
        getWorld().playLocalSound(x, y, z, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0f, rand().nextFloat() * 0.1f + 0.9f, false);
    }

    public static void cultistSpawn(double x, double y, double z, double a, double b, double c) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, a, b, c);
        fb.setMaxAge(10 + rand().nextInt(10));
        fb.setRBGColorF(1.0f, 1.0f, 1.0f, 0.6f, 0.0f, 0.0f);
        fb.setAlphaF(0.8f);
        fb.setGridSize(16);
        fb.setParticles(160, 6, 1);
        fb.setScale(3.0f + rand().nextFloat() * 2.0f);
        fb.setLayer(1);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawWispyMotesEntity(double x, double y, double z, Entity e, float r, float g, float b) {
        if (getWorld() == null) return;
        FXGenericP2E fb = new FXGenericP2E(getWorld(), x, y, z, e);
        fb.setRBGColorF(r, g, b);
        fb.setAlphaF(0.6f);
        fb.setParticles(512, 16, 1);
        fb.setLoop(true);
        fb.setWind(0.001);
        fb.setRandomMovementScale(0.0025f, 0.0f, 0.0025f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawWispParticles(double x, double y, double z, double x2, double y2, double z2, int color, int a) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        fb.setMaxAge(10 + rand().nextInt(5));
        fb.setRBGColorF(red(color), green(color), blue(color));
        fb.setAlphaF(0.5f);
        fb.setLoop(true);
        fb.setGridSize(64);
        fb.setParticles(264, 8, 1);
        fb.setScale(1.0f + rand().nextFloat() * 0.25f, 0.05f);
        fb.setWind(2.5E-4);
        fb.setRandomMovementScale(0.0025f, 0.0f, 0.0025f);
        ParticleEngine.addEffectWithDelay(getWorld(), fb, a);
    }

    /** From BlockFluidPure.randomDisplayTick: faint motes rising off the surface. */
    public static void pureFluidMote(double x, double y, double z) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, 0.0, 0.0, 0.0);
        fb.setMaxAge(10 + rand().nextInt(10));
        fb.setScale(rand().nextFloat() * 0.3f + 0.3f);
        fb.setRBGColorF(1.0f, 1.0f, 1.0f);
        fb.setRandomMovementScale(0.001f, 0.001f, 0.001f);
        fb.setGravity(-0.01f);
        fb.setAlphaF(0.25f);
        fb.setParticle(64);
        fb.setFinalFrames(65, 66);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    /** From BlockResearchTable.randomDisplayTick: motes drift down onto a table holding a note. */
    public static void researchTableMote(BlockPos pos) {
        if (getWorld() == null) return;
        double xx = rand().nextGaussian() / 2.0;
        double zz = rand().nextGaussian() / 2.0;
        double yy = 1.5 + rand().nextFloat();
        int a = 40 + rand().nextInt(20);
        FXGeneric fb = new FXGeneric(getWorld(), pos.getX() + 0.5 + xx, pos.getY() + yy, pos.getZ() + 0.5 + zz, -xx / a, -(yy - 0.85) / a, -zz / a);
        fb.setMaxAge(a);
        fb.setRBGColorF(0.5f + rand().nextFloat() * 0.5f, 0.5f + rand().nextFloat() * 0.5f, 0.5f + rand().nextFloat() * 0.5f);
        fb.setAlphaF(0.0f, 0.25f, 0.5f, 0.75f, 0.0f);
        fb.setParticles(384 + rand().nextInt(16), 1, 1);
        fb.setScale(0.8f + rand().nextFloat() * 0.3f, 0.3f);
        fb.setLayer(0);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawNitorCore(double x, double y, double z, double x2, double y2, double z2) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        fb.setMaxAge(10);
        fb.setRBGColorF(1.0f, 1.0f, 1.0f);
        fb.setAlphaF(1.0f);
        fb.setParticles(457, 1, 1);
        fb.setScale(1.0f, 1.0f + (float) rand().nextGaussian() * 0.1f, 1.0f);
        fb.setLayer(1);
        fb.setRandomMovementScale(2.0E-4f, 2.0E-4f, 2.0E-4f);
        ParticleEngine.addEffect(getWorld(), fb);
    }

    public static void drawNitorFlames(double x, double y, double z, double x2, double y2, double z2, int color, int a) {
        if (getWorld() == null) return;
        FXGeneric fb = new FXGeneric(getWorld(), x, y, z, x2, y2, z2);
        fb.setMaxAge(10 + rand().nextInt(5));
        fb.setRBGColorF(red(color), green(color), blue(color));
        fb.setAlphaF(0.66f);
        fb.setLoop(true);
        fb.setGridSize(64);
        fb.setParticles(264, 8, 1);
        fb.setScale(3.0f + rand().nextFloat(), 0.05f);
        fb.setRandomMovementScale(0.0025f, 0.0f, 0.0025f);
        ParticleEngine.addEffectWithDelay(getWorld(), fb, a);
    }

    public static class GenPart {
        public int grid = 64;
        public int age = 0;
        public float redStart = 1.0f;
        public float greenStart = 1.0f;
        public float blueStart = 1.0f;
        public float redEnd = 1.0f;
        public float greenEnd = 1.0f;
        public float blueEnd = 1.0f;
        public float[] alpha = { 1.0f };
        public float[] scale = { 1.0f };
        public float rot;
        public float rotstart = 0.0f;
        public boolean loop = false;
        public int partStart = 0;
        public int partNum = 1;
        public int partInc = 1;
        public int layer = 0;
        public double slowDown = 0.9800000190734863;
        public float grav = 0.0f;
        public int delay = 0;
    }
}
