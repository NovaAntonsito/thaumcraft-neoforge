package thaumcraft.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import thaumcraft.client.fx.particles.FXGeneric;

/** The particle trails of the foci (FocusEffect*.renderParticleFX in the original), kept client side. */
public final class FocusFX {
    private FocusFX() {
    }

    public static void exchange(double x, double y, double z, double vx, double vy, double vz) {
        ClientLevel world = FXDispatcher.getWorld();
        if (world == null) return;
        RandomSource rand = world.random;
        FXGeneric fb = new FXGeneric(world, x, y, z, vx + rand.nextGaussian() * 0.01, vy + rand.nextGaussian() * 0.01, vz + rand.nextGaussian() * 0.01);
        fb.setMaxAge(9);
        fb.setRBGColorF(0.25f + rand.nextFloat() * 0.25f, 0.25f + rand.nextFloat() * 0.25f, 0.25f + rand.nextFloat() * 0.25f);
        fb.setAlphaF(0.0f, 0.6f, 0.6f, 0.0f);
        fb.setGridSize(64);
        fb.setParticles(448, 9, 1);
        fb.setScale(0.5f, 0.25f);
        fb.setGravity((float)(rand.nextGaussian() * 0.009999999776482582));
        fb.setRandomMovementScale(0.0025f, 0.0025f, 0.0025f);
        ParticleEngine.addEffect(world, fb);
    }

    public static void flux(double x, double y, double z, double vx, double vy, double vz) {
        ClientLevel world = FXDispatcher.getWorld();
        if (world == null) return;
        RandomSource rand = world.random;
        FXGeneric fb = new FXGeneric(world, x, y, z, vx + rand.nextGaussian() * 0.01, vy + rand.nextGaussian() * 0.01, vz + rand.nextGaussian() * 0.01);
        fb.setMaxAge((int)(15.0f + 10.0f * rand.nextFloat()));
        fb.setRBGColorF(0.25f + rand.nextFloat() * 0.25f, 0.0f, 0.25f + rand.nextFloat() * 0.25f);
        fb.setAlphaF(0.0f, 1.0f, 1.0f, 0.0f);
        fb.setGridSize(64);
        fb.setParticles(128, 14, 1);
        fb.setScale(2.0f + rand.nextFloat(), 0.25f + rand.nextFloat() * 0.25f);
        fb.setLoop(true);
        fb.setSlowDown(0.9);
        fb.setGravity((float)(rand.nextGaussian() * 0.10000000149011612));
        fb.setRandomMovementScale(0.0125f, 0.0125f, 0.0125f);
        fb.setRotationSpeed((float)rand.nextGaussian());
        ParticleEngine.addEffectWithDelay(world, fb, rand.nextInt(4));
    }

    public static void breakFocus(double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        ClientLevel world = FXDispatcher.getWorld();
        if (world == null) return;
        RandomSource rand = world.random;
        FXGeneric fb = new FXGeneric(world, posX, posY, posZ, motionX, motionY, motionZ);
        fb.setMaxAge(6 + rand.nextInt(6));
        int q = rand.nextInt(4);
        fb.setParticles(704 + q * 3, 3, 1);
        fb.setSlowDown(0.8);
        fb.setScale((float)(1.7000000476837158 + rand.nextGaussian() * 0.30000001192092896));
        ParticleEngine.addEffect(world, fb);
    }

    public static void heal(double x, double y, double z, double vx, double vy, double vz) {
        ClientLevel world = FXDispatcher.getWorld();
        if (world == null) return;
        RandomSource rand = world.random;
        FXGeneric fb = new FXGeneric(world, x, y, z, vx + rand.nextGaussian() * 0.01, vy + rand.nextGaussian() * 0.01, vz + rand.nextGaussian() * 0.01);
        fb.setMaxAge((int)(10.0f + 10.0f * rand.nextFloat()));
        fb.setRBGColorF(1.0f, 1.0f, 1.0f);
        fb.setAlphaF(0.0f, 0.7f, 0.7f, 0.0f);
        fb.setGridSize(64);
        fb.setParticles(0, 1, 1);
        fb.setScale(rand.nextFloat() * 2.0f, rand.nextFloat());
        fb.setSlowDown(0.8);
        fb.setGravity((float)(rand.nextGaussian() * 0.10000000149011612));
        fb.setRandomMovementScale(0.0125f, 0.0125f, 0.0125f);
        fb.setRotationSpeed((float)rand.nextGaussian());
        ParticleEngine.addEffectWithDelay(world, fb, rand.nextInt(4));
    }

    public static void rift(double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        ClientLevel world = FXDispatcher.getWorld();
        if (world == null) return;
        RandomSource rand = world.random;
        FXGeneric fb = new FXGeneric(world, posX, posY, posZ, motionX, motionY, motionZ);
        fb.setMaxAge(16 + rand.nextInt(16));
        fb.setParticles(384 + rand.nextInt(16), 1, 1);
        fb.setSlowDown(0.75);
        fb.setAlphaF(1.0f, 0.0f);
        fb.setScale((float)(0.699999988079071 + rand.nextGaussian() * 0.30000001192092896));
        fb.setRBGColorF(0.25f, 0.25f, 1.0f);
        fb.setRandomMovementScale(0.01f, 0.01f, 0.01f);
        ParticleEngine.addEffectWithDelay(world, fb, 0);
    }

    public static void frost(double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        ClientLevel world = FXDispatcher.getWorld();
        if (world == null) return;
        RandomSource rand = world.random;
        FXGeneric fb = new FXGeneric(world, posX, posY, posZ, motionX, motionY, motionZ);
        fb.setMaxAge(40 + rand.nextInt(40));
        fb.setAlphaF(1.0f, 0.0f);
        fb.setParticles(8, 1, 1);
        fb.setGravity(0.033f);
        fb.setSlowDown(0.8);
        fb.setRandomMovementScale(0.0025f, 1.0E-4f, 0.0025f);
        fb.setScale((float)(0.699999988079071 + rand.nextGaussian() * 0.30000001192092896));
        fb.setRotationSpeed(rand.nextFloat() * 3.0f, (float)rand.nextGaussian() / 4.0f);
        ParticleEngine.addEffectWithDelay(world, fb, 0);
    }

    public static void curse(double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        ClientLevel world = FXDispatcher.getWorld();
        if (world == null) return;
        RandomSource rand = world.random;
        FXGeneric fb = new FXGeneric(world, posX, posY, posZ, motionX, motionY, motionZ);
        fb.setMaxAge(8);
        fb.setRBGColorF(0.41f + rand.nextFloat() * 0.2f, 0.0f, 0.019f + rand.nextFloat() * 0.2f);
        fb.setAlphaF(0.0f, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 0.0f);
        fb.setGridSize(16);
        fb.setParticles(72 + rand.nextInt(4), 1, 1);
        fb.setScale(2.0f + rand.nextFloat() * 4.0f);
        fb.setLoop(false);
        fb.setSlowDown(0.9);
        fb.setGravity(0.0f);
        fb.setRotationSpeed(rand.nextFloat(), 0.0f);
        ParticleEngine.addEffectWithDelay(world, fb, rand.nextInt(4));
    }
}
