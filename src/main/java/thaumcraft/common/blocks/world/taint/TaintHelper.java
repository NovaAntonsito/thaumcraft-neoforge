package thaumcraft.common.blocks.world.taint;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import thaumcraft.Config;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.monster.tainted.EntityTaintSeed;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * Port of TaintHelper: where the taint seeds are, and how taint fibres spread from them.
 * <p>
 * Materials are gone since 1.20, so ThaumcraftMaterials.MATERIAL_TAINT is {@link #isTaint} (the taint blocks and flux
 * goo), and the vanilla materials the spread looks at are tags or the note block instrument the blocks got from their
 * old material (wood is the bass, rock the bass drum).
 */
public class TaintHelper {
    private static final ConcurrentHashMap<ResourceKey<Level>, List<BlockPos>> taintSeeds = new ConcurrentHashMap<>();

    public static void addTaintSeed(Level world, BlockPos pos) {
        taintSeeds.computeIfAbsent(world.dimension(), k -> new CopyOnWriteArrayList<>()).add(pos);
    }

    public static void removeTaintSeed(Level world, BlockPos pos) {
        List<BlockPos> locs = taintSeeds.get(world.dimension());
        if (locs != null && !locs.isEmpty()) {
            locs.remove(pos);
        }
    }

    public static boolean isNearTaintSeed(Level world, BlockPos pos) {
        double area = Config.TAINT_SPREAD_AREA.get() * Config.TAINT_SPREAD_AREA.get();
        List<BlockPos> locs = taintSeeds.get(world.dimension());
        if (locs != null && !locs.isEmpty()) {
            for (BlockPos p : locs) {
                if (p.distSqr(pos) <= area) {
                    if (EntityUtils.getEntitiesInRange(world, p, null, EntityTaintSeed.class, 1.0).isEmpty()) {
                        removeTaintSeed(world, p);
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAtTaintSeedEdge(Level world, BlockPos pos) {
        int a = Config.TAINT_SPREAD_AREA.get();
        double area = a * a;
        double fringe = a * 0.8 * (a * 0.8);
        List<BlockPos> locs = taintSeeds.get(world.dimension());
        if (locs != null && !locs.isEmpty()) {
            for (BlockPos p : locs) {
                double d = p.distSqr(pos);
                if (d < area && d > fringe) {
                    return true;
                }
            }
        }
        return false;
    }

    /** getMaterial() == ThaumcraftMaterials.MATERIAL_TAINT. */
    public static boolean isTaint(BlockState state) {
        Block b = state.getBlock();
        return b instanceof ITaintBlock || b instanceof BlockFluxGoo;
    }

    /** Block.isReplaceable of 1.12. Flux goo can be replaced while it is less than half full. */
    public static boolean isReplaceable(BlockState state) {
        if (state.getBlock() instanceof BlockFluxGoo) {
            return state.getValue(BlockFluxGoo.LEVEL) < 4;
        }
        return state.canBeReplaced();
    }

    public static void spreadFibres(Level world, BlockPos pos) {
        spreadFibres(world, pos, false);
    }

    public static void spreadFibres(Level world, BlockPos pos, boolean ignore) {
        if (!ignore && Config.WUSS_MODE.get()) {
            return;
        }
        float mod = 0.001f + AuraHandler.getFluxSaturation(world, pos) * 2.0f;
        if (!ignore && world.random.nextFloat() > Config.TAINT_SPREAD_RATE.get() / 100.0f * mod) {
            return;
        }
        if (isNearTaintSeed(world, pos)) {
            int xx = pos.getX() + world.random.nextInt(3) - 1;
            int yy = pos.getY() + world.random.nextInt(3) - 1;
            int zz = pos.getZ() + world.random.nextInt(3) - 1;
            BlockPos t = new BlockPos(xx, yy, zz);
            if (t.equals(pos)) {
                return;
            }
            BlockState bs = world.getBlockState(t);
            float bh = bs.getDestroySpeed(world, t);
            if (bh < 0.0f || bh > 10.0f) {
                return;
            }
            boolean leaves = bs.is(BlockTags.LEAVES);
            Block fibre = ConfigBlocks.taintFibre.get();
            if (!leaves && bs.getFluidState().isEmpty() && (bs.isAir() || isReplaceable(bs) || bs.is(BlockTags.FLOWERS) || isPlantable(bs))
                    && isAdjacentToSolidBlock(world, t) && !BlockTaintFibre.isOnlyAdjacentToTaint(world, t)) {
                world.setBlockAndUpdate(t, fibre.defaultBlockState());
                world.blockEvent(t, fibre, 1, 0);
                AuraHelper.drainFlux(world, t, 0.01f, false);
                return;
            }
            if (leaves) {
                Direction face = null;
                if (world.random.nextFloat() < 0.6 && (face = getFaceBlockTouching(world, t, ConfigBlocks.taintLog.get())) != null) {
                    world.setBlockAndUpdate(t, ConfigBlocks.taintFeature.get().defaultBlockState().setValue(BlockTaintFeature.FACING, face.getOpposite()));
                } else {
                    world.setBlockAndUpdate(t, fibre.defaultBlockState());
                    world.blockEvent(t, fibre, 1, 0);
                    AuraHelper.drainFlux(world, t, 0.01f, false);
                }
                return;
            }
            if (BlockTaintFibre.isHemmedByTaint(world, t) && bh < 5.0f) {
                if (Utils.isWoodLog(world, t) && !isTaint(bs)) {
                    world.setBlockAndUpdate(t, ConfigBlocks.taintLog.get().defaultBlockState().setValue(BlockTaintLog.AXIS, getBlockAxis(bs)));
                    return;
                }
                if (isCrustMaterial(bs)) {
                    setTaint(world, t, ConfigBlocks.taintCrust.get());
                    return;
                }
                if (isSoilMaterial(bs)) {
                    setTaint(world, t, ConfigBlocks.taintSoil.get());
                    return;
                }
                if (BlockUtils.isRock(bs)) {
                    setTaint(world, t, ConfigBlocks.taintRock.get());
                    return;
                }
            }
            if ((bs.is(ConfigBlocks.taintSoil.get()) || bs.is(ConfigBlocks.taintRock.get())) && world.isEmptyBlock(t.above())
                    && AuraHelper.getFlux(world, t) >= 5.0f && world.random.nextFloat() < Config.TAINT_SPREAD_RATE.get() / 100.0f * 0.33
                    && isAtTaintSeedEdge(world, t)) {
                EntityTaintSeed e = new EntityTaintSeed(world);
                e.moveTo(t.getX() + 0.5f, t.above().getY(), t.getZ() + 0.5f, world.random.nextInt(360), 0.0f);
                if (e.getCanSpawnHere()) {
                    AuraHelper.drainFlux(world, t, 5.0f, false);
                    world.addFreshEntity(e);
                }
            }
        }
    }

    private static void setTaint(Level world, BlockPos t, Block block) {
        world.setBlockAndUpdate(t, block.defaultBlockState());
        world.blockEvent(t, block, 1, 0);
        AuraHelper.drainFlux(world, t, 0.01f, false);
    }

    /** IPlantable of 1.12: bushes (flowers, saplings, crops, mushrooms...), cactus and sugar cane. */
    private static boolean isPlantable(BlockState bs) {
        Block b = bs.getBlock();
        return b instanceof BushBlock || b instanceof CactusBlock || b instanceof SugarCaneBlock;
    }

    /** Mushroom blocks and Material.GOURD, CACTUS, CORAL, SPONGE and WOOD (the bass instrument). */
    private static boolean isCrustMaterial(BlockState bs) {
        return bs.is(Blocks.RED_MUSHROOM_BLOCK) || bs.is(Blocks.BROWN_MUSHROOM_BLOCK) || bs.is(Blocks.PUMPKIN) || bs.is(Blocks.CARVED_PUMPKIN)
                || bs.is(Blocks.JACK_O_LANTERN) || bs.is(Blocks.MELON) || bs.is(Blocks.CACTUS) || bs.is(BlockTags.CORAL_BLOCKS)
                || bs.is(Blocks.SPONGE) || bs.is(Blocks.WET_SPONGE) || bs.instrument() == NoteBlockInstrument.BASS;
    }

    /** Material.SAND, GROUND, GRASS and CLAY. */
    private static boolean isSoilMaterial(BlockState bs) {
        return bs.is(BlockTags.SAND) || bs.is(BlockTags.DIRT) || bs.is(Blocks.GRAVEL) || bs.is(Blocks.CLAY) || bs.is(Blocks.FARMLAND)
                || bs.is(Blocks.DIRT_PATH) || bs.is(Blocks.SOUL_SAND);
    }

    /** BlockUtils.isAdjacentToSolidBlock: a neighbour with a solid face towards pos. */
    public static boolean isAdjacentToSolidBlock(Level world, BlockPos pos) {
        for (Direction face : Direction.values()) {
            BlockPos np = pos.relative(face);
            if (world.getBlockState(np).isFaceSturdy(world, np, face.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    /** BlockUtils.getFaceBlockTouching. */
    private static Direction getFaceBlockTouching(Level world, BlockPos pos, Block bs) {
        for (Direction face : Direction.values()) {
            if (world.getBlockState(pos.relative(face)).is(bs)) {
                return face;
            }
        }
        return null;
    }

    /** BlockUtils.getBlockAxis: the axis property of a log, Y if it has none. */
    private static Direction.Axis getBlockAxis(BlockState state) {
        return state.hasProperty(BlockStateProperties.AXIS) ? state.getValue(BlockStateProperties.AXIS) : Direction.Axis.Y;
    }
}
