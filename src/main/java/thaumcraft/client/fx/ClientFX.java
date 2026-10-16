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

/**
 * Client side of the FX packets. Stands in for the original FXDispatcher.
 * TODO: FXEssentiaStream, arcLightning, the scan glow, the scan highlight sparkles and the block runes are custom
 * particles in the original (stage 10); these use vanilla particles.
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

    /** spark: a short burst at a point (unstable infusion and problem stabilizers). */
    public static void spark(double x, double y, double z, float r, float g, float b) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        world.addParticle(new DustParticleOptions(new Vector3f(r, g, b), 1.5f), x, y, z, 0, 0, 0);
        world.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
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
            DustParticleOptions dust = new DustParticleOptions(rgb(c), 4.0f);
            for (int a = 0; a < 3; ++a) {
                world.addAlwaysVisibleParticle(dust, true, x, y, z, 0, 0, 0);
            }
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
                blockRunes(world, target.getX() - 0.5, target.getY() + target.getEyeHeight() / 2.0f, target.getZ() - 0.5);
            }
        } else {
            BlockHitResult mop = ItemThaumometer.rayTrace(world, player);
            if (mop.getType() == HitResult.Type.BLOCK) {
                for (int a = 0; a < 10; ++a) {
                    blockRunes(world, mop.getBlockPos().getX(), mop.getBlockPos().getY() + 0.25, mop.getBlockPos().getZ());
                }
            }
        }
    }

    /** FXDispatcher.blockRunes: the original's purple rune glyphs; vanilla enchanting glyphs stand in. */
    private static void blockRunes(Level world, double x, double y, double z) {
        RandomSource rand = world.random;
        world.addParticle(ParticleTypes.ENCHANT, x + 0.5 + rand.nextGaussian() * 0.3, y + 0.5 + rand.nextGaussian() * 0.3,
                z + 0.5 + rand.nextGaussian() * 0.3, 0, 0.03, 0);
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
            scanHighlight(world, target.getBoundingBox());
        }
        RenderEventHandler.thaumTarget = target;
        BlockHitResult mop = rayTraceWild(world, player);
        if (mop.getType() == HitResult.Type.BLOCK && ScanningManager.isThingStillScannable(player, mop.getBlockPos())) {
            BlockPos p = mop.getBlockPos();
            VoxelShape shape = world.getBlockState(p).getShape(world, p);
            scanHighlight(world, (shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds()).move(p));
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

    /** FXDispatcher.scanHighlight: light blue sparkles scattered over the faces of the box. */
    public static void scanHighlight(Level world, AABB bb) {
        RandomSource rand = world.random;
        int num = Mth.ceil(bb.getSize() * 2.0);
        double ax = (bb.minX + bb.maxX) / 2.0;
        double ay = (bb.minY + bb.maxY) / 2.0;
        double az = (bb.minZ + bb.maxZ) / 2.0;
        for (Direction face : Direction.values()) {
            double mx = 0.5 + face.getStepX() * 0.51;
            double my = 0.5 + face.getStepY() * 0.51;
            double mz = 0.5 + face.getStepZ() * 0.51;
            for (int a = 0; a < num * 2; ++a) {
                double x = mx + rand.nextGaussian() * (bb.maxX - bb.minX);
                double y = my + rand.nextGaussian() * (bb.maxY - bb.minY);
                double z = mz + rand.nextGaussian() * (bb.maxZ - bb.minZ);
                x = Mth.clamp(x, bb.minX - ax, bb.maxX - ax);
                y = Mth.clamp(y, bb.minY - ay, bb.maxY - ay);
                z = Mth.clamp(z, bb.minZ - az, bb.maxZ - az);
                float r = Mth.nextInt(rand, 16, 32) / 255.0f;
                float g = Mth.nextInt(rand, 132, 165) / 255.0f;
                float b = Mth.nextInt(rand, 223, 239) / 255.0f;
                world.addParticle(new DustParticleOptions(new Vector3f(r, g, b), 0.4f + (float) rand.nextGaussian() * 0.1f),
                        ax + x, ay + y, az + z, 0, 0, 0);
            }
        }
    }

    /**
     * drawVentParticles: a puff of colored gas. TODO: FXVent (stage 10); for now colored dust that drifts
     * along the given motion.
     */
    public static void drawVentParticles(Level world, double x, double y, double z, double vx, double vy, double vz, int color) {
        DustParticleOptions dust = new DustParticleOptions(rgb(color), 1.5f);
        world.addParticle(dust, x, y, z, vx * 10.0, vy * 10.0, vz * 10.0);
    }

    /**
     * FXDispatcher.drawWispyMotes: faint motes that drift off a magic mirror. TODO: FXWispy (stage 10); for now
     * small dust of the same color.
     */
    public static void drawWispyMotes(Level world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b) {
        world.addParticle(new DustParticleOptions(new Vector3f(r, g, b), 0.6f), x, y, z, vx, vy, vz);
    }

    /** FXDispatcher.drawWispyMotesOnBlock: a pale mote somewhere on top of the block. TODO: age and gravity with FXWispy. */
    public static void drawWispyMotesOnBlock(Level world, BlockPos pp, int age, float grav) {
        RandomSource rand = world.random;
        drawWispyMotes(world, pp.getX() + rand.nextFloat(), pp.getY(), pp.getZ() + rand.nextFloat(), 0.0, 0.0, 0.0,
                0.4f + rand.nextFloat() * 0.6f, 0.6f + rand.nextFloat() * 0.4f, 0.6f + rand.nextFloat() * 0.4f);
    }

    /**
     * FXDispatcher.voidStreak: a dark streak pulled from a flux rift into the void siphon. TODO: FXVoidStreak
     * (stage 10); for now reverse portal particles along the line.
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

    /**
     * FXDispatcher.drawCurlyWisp: the wisps a flux rift gives off. TODO: FXCurlyWisp (stage 10); for now dust
     * of the same color and size.
     */
    public static void drawCurlyWisp(Level world, double x, double y, double z, float size, float r, float g, float b) {
        world.addParticle(new DustParticleOptions(new Vector3f(r, g, b), Mth.clamp(size * 4.0f, 0.5f, 4.0f)), x, y, z, 0, 0.02, 0);
    }

    /**
     * FXDispatcher.drawBlockMistParticles: colored mist rising from inside a block's bounds. TODO: FXGeneric
     * (stage 10); for now dust of the same color.
     */
    public static void blockMist(BlockPos p, int c) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        VoxelShape shape = world.getBlockState(p).getShape(world, p);
        AABB bs = shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();
        RandomSource rand = world.random;
        DustParticleOptions dust = new DustParticleOptions(rgb(c), 1.5f);
        for (int a = 0; a < 8; ++a) {
            double x = p.getX() + bs.minX + rand.nextFloat() * (bs.maxX - bs.minX);
            double y = p.getY() + bs.minY + rand.nextFloat() * (bs.maxY - bs.minY);
            double z = p.getZ() + bs.minZ + rand.nextFloat() * (bs.maxZ - bs.minZ);
            world.addParticle(dust, x, y, z, rand.nextGaussian() * 0.01, rand.nextFloat() * 0.075, rand.nextGaussian() * 0.01);
        }
    }

    /** FXDispatcher.furnaceLavaFx: a lava spark spat out of the infernal furnace's opening. */
    public static void furnaceLava(BlockPos pos, int facingX, int facingZ) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        RandomSource rand = world.random;
        float qx = facingX == 0 ? (rand.nextFloat() - rand.nextFloat()) * 0.5f : facingX * rand.nextFloat();
        float qz = facingZ == 0 ? (rand.nextFloat() - rand.nextFloat()) * 0.5f : facingZ * rand.nextFloat();
        world.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5f + (rand.nextFloat() - rand.nextFloat()) * 0.3f + facingX,
                pos.getY() + 0.3f, pos.getZ() + 0.5f + (rand.nextFloat() - rand.nextFloat()) * 0.3f + facingZ,
                0.15f * qx, 0.2f * rand.nextFloat(), 0.15f * qz);
    }

    /**
     * FXDispatcher.visSparkle: a mote of vis drifting from a spot in the air to the block that draws it (the recharge
     * pedestal). TODO: FXVisSparkle with the aspect color (stage 10); for now the enchanting glyphs, which fly the
     * same way.
     */
    public static void visSparkle(int x, int y, int z, int x2, int y2, int z2, int color) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        RandomSource rand = world.random;
        double tx = x2 + 0.4 + rand.nextFloat() * 0.2f;
        double ty = y2 + 0.4 + rand.nextFloat() * 0.2f;
        double tz = z2 + 0.4 + rand.nextFloat() * 0.2f;
        world.addParticle(ParticleTypes.ENCHANT, tx, ty, tz, x + rand.nextFloat() - tx, y + rand.nextFloat() - ty, z + rand.nextFloat() - tz);
    }

    /**
     * FXDispatcher.drawLevitatorParticles: pale motes drifting away from the levitator. TODO: FXGeneric (stage 10);
     * for now small white dust.
     */
    public static void drawLevitatorParticles(Level world, double x, double y, double z, double vx, double vy, double vz) {
        world.addParticle(new DustParticleOptions(new Vector3f(0.9f, 0.9f, 1.0f), 0.5f), x, y, z, vx, vy, vz);
    }

    /** FXDispatcher.waterTrailFx: a stream of water drops from the everfull urn to what it fills. */
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

    /** FXDispatcher.jarSplashFx: a splash over a full urn. */
    public static void jarSplash(Level world, double x, double y, double z) {
        RandomSource rand = world.random;
        world.addParticle(ParticleTypes.SPLASH, x + rand.nextGaussian() * 0.1, y, z + rand.nextGaussian() * 0.1, 0.0, 0.0, 0.0);
    }
}
