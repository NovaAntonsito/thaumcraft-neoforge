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
 */
public final class ClientFX {
    private ClientFX() {
    }

    private static Vector3f rgb(int color) {
        return new Vector3f(((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f);
    }

    /** EssentiaHandler.sourceFX: the essentia flows waiting for the next client tick, by source, target and color. */
    private static final java.util.Map<String, BlockPos[]> essentiaSources = new java.util.LinkedHashMap<>();
    private static final java.util.Map<String, int[]> essentiaSourceData = new java.util.HashMap<>();
    private static int tickCount;

    /** PacketFXEssentiaSource.onMessage: start is the tile drawing the essentia, end its source. */
    public static void essentiaSource(BlockPos start, BlockPos end, int color, int ext) {
        String key = start.getX() + ":" + start.getY() + ":" + start.getZ() + ":" + end.getX() + ":" + end.getY() + ":" + end.getZ() + ":" + color;
        if (!essentiaSources.containsKey(key)) {
            essentiaSources.put(key, new BlockPos[] { start, end });
            essentiaSourceData.put(key, new int[] { color, ext });
        }
    }

    /** RenderEventHandler.clientWorldTick: one essentia stream per queued flow; the matrix draws from below itself. */
    public static void tickEssentiaSources() {
        ++tickCount;
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        for (String fxk : essentiaSources.keySet().toArray(new String[0])) {
            BlockPos[] fx = essentiaSources.remove(fxk);
            int[] data = essentiaSourceData.remove(fxk);
            int mod = world.getBlockEntity(fx[0]) instanceof TileInfusionMatrix ? -1 : 0;
            FXDispatcher.essentiaTrailFx(fx[1], fx[0].above(mod), tickCount, data[0], 0.1f, data[1]);
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

}
