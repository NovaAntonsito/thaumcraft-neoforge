package thaumcraft.client.fx;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.client.lib.events.RenderEventHandler;
import thaumcraft.common.items.tools.ItemThaumometer;
import thaumcraft.common.lib.utils.EntityUtils;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.common.tiles.crafting.TileInfusionMatrix;
import thaumcraft.client.fx.particles.FXGeneric;

/**
 * Client side of the FX packets. Stands in for the original FXDispatcher.
 * The effects themselves are in FXDispatcher; these are the packet handlers and the thaumometer.
 * TODO (stage 10b): essentiaTrail, arcLightning, voidStreak, waterTrail and sonic are beams and streams in the original
 * (FXEssentiaStream, FXArc, FXVoidStream, FXSonic); vanilla particles stand in for them.
 */
public final class ClientFX {
    private ClientFX() {
    }

    private static Vector3f rgb(int color) {
        return new Vector3f(((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f);
    }

    /** essentiaTrailFx: a line of colored dust from the source to the target. The matrix target is one block lower. */
    public static void essentiaTrail(BlockPos from, BlockPos to, int color) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        if (world.getBlockEntity(to) instanceof TileInfusionMatrix) {
            to = to.below();
        }
        DustParticleOptions dust = new DustParticleOptions(rgb(color), 0.8f);
        double x1 = from.getX() + 0.5, y1 = from.getY() + 0.5, z1 = from.getZ() + 0.5;
        double dx = to.getX() + 0.5 - x1, dy = to.getY() + 0.5 - y1, dz = to.getZ() + 0.5 - z1;
        int steps = Math.max(4, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 4));
        RandomSource rand = world.random;
        for (int i = 0; i <= steps; ++i) {
            double f = i / (double) steps;
            double arc = Math.sin(f * Math.PI) * 0.5;
            world.addParticle(dust, x1 + dx * f + rand.nextGaussian() * 0.03, y1 + dy * f + arc, z1 + dz * f + rand.nextGaussian() * 0.03, 0, 0, 0);
        }
    }

    /** PacketFXInfusionSource.onMessage: registers a pedestal or entity the matrix is drawing from. */
    public static void infusionSource(BlockPos matrix, BlockPos source, int color) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        if (world.getBlockEntity(matrix) instanceof TileInfusionMatrix tile) {
            tile.addSourceFX(source, color, world.getBlockEntity(source) instanceof thaumcraft.common.tiles.crafting.TilePedestal ? 60 : 15);
        }
    }

    /** arcLightning: a jagged line of sparks. */
    public static void arcLightning(double x, double y, double z, double tx, double ty, double tz, float r, float g, float b) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), 0.6f);
        double dx = tx - x, dy = ty - y, dz = tz - z;
        int steps = Math.max(4, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 5));
        RandomSource rand = world.random;
        for (int i = 0; i <= steps; ++i) {
            double f = i / (double) steps;
            double jitter = Math.sin(f * Math.PI) * 0.15;
            double px = x + dx * f + rand.nextGaussian() * jitter;
            double py = y + dy * f + rand.nextGaussian() * jitter;
            double pz = z + dz * f + rand.nextGaussian() * jitter;
            world.addParticle(dust, px, py, pz, 0, 0, 0);
            if (rand.nextInt(3) == 0) world.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0, 0, 0);
        }
    }

    /** PacketFXScanSource.startScan: groups the ores in range and marks each group with its color. */
    public static void startScan(BlockPos pos, int r) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        int range = 4 + r * 4;
        List<BlockPos> positions = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-range, -range, -range), pos.offset(range, range, range))) {
            if (world.getBlockState(p).is(Tags.Blocks.ORES)) {
                positions.add(p.immutable());
            }
        }
        while (!positions.isEmpty()) {
            BlockPos start = positions.remove(0);
            List<BlockPos> coll = new ArrayList<>();
            coll.add(start);
            calcGroup(world, start, coll, positions);
            int c = getOreColor(world.getBlockState(start));
            double x = 0, y = 0, z = 0;
            for (BlockPos p2 : coll) {
                x += p2.getX() + 0.5;
                y += p2.getY() + 0.5;
                z += p2.getZ() + 0.5;
            }
            x /= coll.size();
            y /= coll.size();
            z /= coll.size();
            double dis = Math.sqrt(pos.getCenter().distanceToSqr(x, y, z));
            FXGeneric fb = new FXGeneric(world, x, y, z, 0.0, 0.0, 0.0);
            fb.setMaxAge(44);
            float cr = ((c >> 16) & 0xFF) / 255.0f, cg = ((c >> 8) & 0xFF) / 255.0f, cb = (c & 0xFF) / 255.0f;
            fb.setRBGColorF(cr, cg, cb);
            float q = (cr + cg + cb) / 3.0f;
            fb.setAlphaF(0.0f, 1.0f, 0.8f, 0.0f);
            fb.setParticles(240, 15, 1);
            fb.setGridSize(16);
            fb.setLoop(true);
            fb.setScale(9.0f);
            // Dark ores glow through the walls with alpha blending, the rest additively.
            fb.setLayer(q < 0.25f ? 3 : 2);
            fb.setRotationSpeed(0.0f);
            ParticleEngine.addEffectWithDelay(world, fb, (int) (dis * 3.0));
        }
    }

    private static void calcGroup(ClientLevel world, BlockPos start, List<BlockPos> coll, List<BlockPos> positions) {
        BlockState bs = world.getBlockState(start);
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    BlockPos t = start.offset(x, y, z);
                    if (world.getBlockState(t).equals(bs) && positions.remove(t)) {
                        coll.add(t);
                        if (positions.isEmpty()) return;
                        calcGroup(world, t, coll, positions);
                    }
                }
            }
        }
    }

    /** getOreColor: the original matched ore dictionary names; registry names carry the same words. */
    private static int getOreColor(BlockState state) {
        String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toUpperCase();
        if (name.contains("IRON")) return 14200723;
        if (name.contains("COAL")) return 1052688;
        if (name.contains("REDSTONE")) return 16711680;
        if (name.contains("GOLD")) return 16576075;
        if (name.contains("LAPIS")) return 1328572;
        if (name.contains("DIAMOND")) return 6155509;
        if (name.contains("EMERALD")) return 1564002;
        if (name.contains("QUARTZ")) return 15064789;
        if (name.contains("SILVER")) return 14342653;
        if (name.contains("TIN")) return 15724539;
        if (name.contains("COPPER")) return 16620629;
        if (name.contains("AMBER")) return 16626469;
        if (name.contains("CINNABAR")) return 10159368;
        return 12632256;
    }

    /** ItemThaumometer.drawFX: runes around the creature or block being scanned. */
    public static void thaumometerScan(Player player) {
        Level world = player.level();
        Entity target = EntityUtils.getPointedEntity(world, player, 1.0, 9.0, 0.0f, true);
        if (target != null) {
            for (int a = 0; a < 10; ++a) {
                FXDispatcher.blockRunes(target.getX() - 0.5, target.getY() + target.getEyeHeight() / 2.0f, target.getZ() - 0.5,
                        0.3f + world.random.nextFloat() * 0.7f, 0.0f, 0.3f + world.random.nextFloat() * 0.7f, (int) (target.getBbHeight() * 15.0f), 0.03f);
            }
        } else {
            BlockHitResult mop = ItemThaumometer.rayTrace(world, player);
            if (mop.getType() == HitResult.Type.BLOCK) {
                for (int a2 = 0; a2 < 10; ++a2) {
                    FXDispatcher.blockRunes(mop.getBlockPos().getX(), mop.getBlockPos().getY() + 0.25, mop.getBlockPos().getZ(),
                            0.3f + world.random.nextFloat() * 0.7f, 0.0f, 0.3f + world.random.nextFloat() * 0.7f, 15, 0.03f);
                }
            }
        }
    }

    /**
     * ItemThaumometer.onUpdate on the client: highlights a creature or block that still has something to teach,
     * and remembers the creature so its aspects are drawn over it. The block is found with a wobbling ray, so the
     * highlights wander around what the player looks at.
     */
    public static void thaumometerHeld(Player player) {
        Level world = player.level();
        Entity target = EntityUtils.getPointedEntity(world, player, 1.0, 16.0, 5.0f, true);
        if (target != null && ScanningManager.isThingStillScannable(player, target)) {
            FXDispatcher.scanHighlight(target);
        }
        RenderEventHandler.thaumTarget = target;
        BlockHitResult mop = rayTraceWild(world, player);
        if (mop.getType() == HitResult.Type.BLOCK && ScanningManager.isThingStillScannable(player, mop.getBlockPos())) {
            FXDispatcher.scanHighlight(mop.getBlockPos());
        }
    }

    /** getRayTraceResultFromPlayerWild: a 16 block ray with up to 24 degrees of random wobble. */
    private static BlockHitResult rayTraceWild(Level world, Player player) {
        RandomSource rand = world.random;
        float pitch = player.getXRot() + rand.nextInt(25) - rand.nextInt(25);
        float yaw = player.getYRot() + rand.nextInt(25) - rand.nextInt(25);
        Vec3 start = player.getEyePosition();
        float f3 = Mth.cos(-yaw * 0.017453292f - (float) Math.PI);
        float f4 = Mth.sin(-yaw * 0.017453292f - (float) Math.PI);
        float f5 = -Mth.cos(-pitch * 0.017453292f);
        float f6 = Mth.sin(-pitch * 0.017453292f);
        Vec3 end = start.add(f4 * f5 * 16.0, f6 * 16.0, f3 * f5 * 16.0);
        return world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }

    /**
     * FXDispatcher.voidStreak: a dark streak pulled from a flux rift into the void siphon. TODO: FXVoidStream
     * (stage 10b); for now reverse portal particles along the line.
     */
    public static void voidStreak(Level world, double x, double y, double z, double tx, double ty, double tz) {
        double dx = tx - x, dy = ty - y, dz = tz - z;
        int steps = Math.max(4, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3));
        RandomSource rand = world.random;
        for (int i = 0; i <= steps; ++i) {
            double f = i / (double) steps;
            world.addParticle(ParticleTypes.REVERSE_PORTAL, x + dx * f + rand.nextGaussian() * 0.05, y + dy * f + rand.nextGaussian() * 0.05,
                    z + dz * f + rand.nextGaussian() * 0.05, dx * 0.01, dy * 0.01, dz * 0.01);
        }
    }

    /** FXDispatcher.waterTrailFx: a stream of water drops from the everfull urn. TODO: FXEssentiaStream (stage 10b). */
    public static void waterTrail(BlockPos from, BlockPos to) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        double x1 = from.getX() + 0.5, y1 = from.getY() + 1.0, z1 = from.getZ() + 0.5;
        double dx = to.getX() + 0.5 - x1, dy = to.getY() + 1.0 - y1, dz = to.getZ() + 0.5 - z1;
        int steps = Math.max(4, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 4));
        RandomSource rand = world.random;
        for (int i = 0; i <= steps; ++i) {
            double f = i / (double) steps;
            // An arc: the stream rises over the middle.
            double arc = Math.sin(f * Math.PI) * 0.75;
            world.addParticle(ParticleTypes.SPLASH, x1 + dx * f + rand.nextGaussian() * 0.03, y1 + dy * f + arc, z1 + dz * f + rand.nextGaussian() * 0.03,
                    0.0, 0.0, 0.0);
        }
    }

    /** FXDispatcher.swarmParticleFX: one more FXSwarm somewhere within 2 blocks of its swarm. */
    public static Object swarmParticle(Entity target, float speed, float turnSpeed, float gravity) {
        ClientLevel world = (ClientLevel) target.level();
        RandomSource rand = world.random;
        thaumcraft.client.fx.particles.FXSwarm fx = new thaumcraft.client.fx.particles.FXSwarm(world,
                target.getX() + (rand.nextFloat() - rand.nextFloat()) * 2.0f, target.getY() + (rand.nextFloat() - rand.nextFloat()) * 2.0f,
                target.getZ() + (rand.nextFloat() - rand.nextFloat()) * 2.0f, target, 0.8f + rand.nextFloat() * 0.2f, rand.nextFloat() * 0.4f,
                1.0f - rand.nextFloat() * 0.2f, speed, turnSpeed, gravity);
        Minecraft.getInstance().particleEngine.add(fx);
        return fx;
    }

    public static boolean isParticleAlive(Object particle) {
        return particle instanceof net.minecraft.client.particle.Particle p && p.isAlive();
    }
    /** PacketFXSonic: the ripple an eldritch guardian screams from its head. TODO: FXSonic (stage 10); the sonic boom for now. */
    public static void sonic(Entity source) {
        source.level().addParticle(ParticleTypes.SONIC_BOOM, source.getX(), source.getY() + source.getEyeHeight(), source.getZ(), 0, 0, 0);
    }

}
