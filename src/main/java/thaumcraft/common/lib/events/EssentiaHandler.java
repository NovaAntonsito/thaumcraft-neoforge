package thaumcraft.common.lib.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.lib.network.fx.PacketFXEssentiaSource;
import thaumcraft.common.tiles.devices.TileMirrorEssentia;

/**
 * Port of EssentiaHandler: lets a tile (the infusion matrix) pull essentia from nearby IAspectSource tiles
 * such as jars, or push essentia into them. Sources are cached per tile, nearest first; a failed search waits
 * 10 seconds before retrying. Essentia mirrors are sources too; ignoreMirror skips them, so a mirror serving
 * its twin never loops back through another mirror.
 */
public final class EssentiaHandler {
    static final long DELAY = 10000L;
    private static final Map<GlobalPos, List<BlockPos>> sources = new HashMap<>();
    private static final Map<GlobalPos, Long> sourcesDelay = new HashMap<>();

    private EssentiaHandler() {
    }

    public static boolean drainEssentia(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range, int ext) {
        return drainEssentia(tile, aspect, direction, range, false, ext);
    }

    public static boolean drainEssentia(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range, boolean ignoreMirror, int ext) {
        Level world = tile.getLevel();
        if (world == null) return false;
        GlobalPos tileLoc = GlobalPos.of(world.dimension(), tile.getBlockPos());
        if (!sources.containsKey(tileLoc)) {
            getSources(world, tileLoc, direction, range);
            return sources.containsKey(tileLoc) && drainEssentia(tile, aspect, direction, range, ignoreMirror, ext);
        }
        for (BlockPos source : sources.get(tileLoc)) {
            BlockEntity sourceTile = world.getBlockEntity(source);
            if (!(sourceTile instanceof IAspectSource as)) {
                break;
            }
            if (as.isBlocked()) {
                continue;
            }
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) {
                continue;
            }
            if (as.takeFromContainer(aspect, 1)) {
                BlockPos p = tile.getBlockPos();
                if (world instanceof ServerLevel level) {
                    PacketDistributor.sendToPlayersNear(level, null, p.getX(), p.getY(), p.getZ(), 32.0,
                            new PacketFXEssentiaSource(p, (byte) (p.getX() - source.getX()), (byte) (p.getY() - source.getY()),
                                    (byte) (p.getZ() - source.getZ()), aspect.getColor(), ext));
                }
                return true;
            }
        }
        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    public static boolean findEssentia(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range) {
        return findEssentia(tile, aspect, direction, range, false);
    }

    public static boolean findEssentia(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range, boolean ignoreMirror) {
        Level world = tile.getLevel();
        if (world == null) return false;
        GlobalPos tileLoc = GlobalPos.of(world.dimension(), tile.getBlockPos());
        if (!sources.containsKey(tileLoc)) {
            getSources(world, tileLoc, direction, range);
            return sources.containsKey(tileLoc) && findEssentia(tile, aspect, direction, range, ignoreMirror);
        }
        for (BlockPos source : sources.get(tileLoc)) {
            BlockEntity sourceTile = world.getBlockEntity(source);
            if (!(sourceTile instanceof IAspectSource as)) {
                break;
            }
            if (as.isBlocked()) {
                continue;
            }
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) {
                continue;
            }
            if (as.doesContainerContainAmount(aspect, 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Puts one point of essentia into a nearby source: containers that already hold something first, then empty
     * ones. Used by the essentia mirror to fill the jars by its twin.
     */
    public static boolean addEssentia(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range, boolean ignoreMirror, int ext) {
        Level world = tile.getLevel();
        if (world == null) return false;
        GlobalPos tileLoc = GlobalPos.of(world.dimension(), tile.getBlockPos());
        if (!sources.containsKey(tileLoc)) {
            getSources(world, tileLoc, direction, range);
            return sources.containsKey(tileLoc) && addEssentia(tile, aspect, direction, range, ignoreMirror, ext);
        }
        List<BlockPos> empties = new ArrayList<>();
        for (BlockPos source : sources.get(tileLoc)) {
            BlockEntity sourceTile = world.getBlockEntity(source);
            if (!(sourceTile instanceof IAspectSource as)) {
                break;
            }
            if (as.isBlocked()) {
                continue;
            }
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) {
                continue;
            }
            if (as.doesContainerAccept(aspect) && (as.getAspects() == null || as.getAspects().visSize() == 0)) {
                empties.add(source);
            } else if (as.doesContainerAccept(aspect) && as.addToContainer(aspect, 1) <= 0) {
                sendFX(world, tile.getBlockPos(), source, aspect, ext);
                return true;
            }
        }
        for (BlockPos source : empties) {
            BlockEntity sourceTile = world.getBlockEntity(source);
            if (!(sourceTile instanceof IAspectSource as)) {
                break;
            }
            if (aspect != null && as.doesContainerAccept(aspect) && as.addToContainer(aspect, 1) <= 0) {
                sendFX(world, tile.getBlockPos(), source, aspect, ext);
                return true;
            }
        }
        sources.remove(tileLoc);
        sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        return false;
    }

    /**
     * Is there a nearby source that would take this essentia? The original fell back to findEssentia when the
     * sources were not cached yet; that is kept.
     */
    public static boolean canAcceptEssentia(BlockEntity tile, Aspect aspect, @Nullable Direction direction, int range, boolean ignoreMirror) {
        Level world = tile.getLevel();
        if (world == null) return false;
        GlobalPos tileLoc = GlobalPos.of(world.dimension(), tile.getBlockPos());
        if (!sources.containsKey(tileLoc)) {
            getSources(world, tileLoc, direction, range);
            return sources.containsKey(tileLoc) && findEssentia(tile, aspect, direction, range, ignoreMirror);
        }
        for (BlockPos source : sources.get(tileLoc)) {
            BlockEntity sourceTile = world.getBlockEntity(source);
            if (!(sourceTile instanceof IAspectSource as)) {
                break;
            }
            if (ignoreMirror && sourceTile instanceof TileMirrorEssentia) {
                continue;
            }
            if (!as.isBlocked() && as.doesContainerAccept(aspect)) {
                return true;
            }
        }
        return false;
    }

    /** The essentia trail goes from the receiving source back to the tile, as in the original. */
    private static void sendFX(Level world, BlockPos tilePos, BlockPos source, Aspect aspect, int ext) {
        if (world instanceof ServerLevel level) {
            PacketDistributor.sendToPlayersNear(level, null, tilePos.getX(), tilePos.getY(), tilePos.getZ(), 32.0,
                    new PacketFXEssentiaSource(source, (byte) (source.getX() - tilePos.getX()), (byte) (source.getY() - tilePos.getY()),
                            (byte) (source.getZ() - tilePos.getZ()), aspect.getColor(), ext));
        }
    }

    private static void getSources(Level world, GlobalPos tileLoc, @Nullable Direction direction, int range) {
        Long d = sourcesDelay.get(tileLoc);
        if (d != null) {
            if (d > System.currentTimeMillis()) {
                return;
            }
            sourcesDelay.remove(tileLoc);
        }
        BlockPos origin = tileLoc.pos();
        BlockEntity sourceTile = world.getBlockEntity(origin);
        List<BlockPos> sourceList = new ArrayList<>();
        int start = 0;
        if (direction == null) {
            start = -range;
            direction = Direction.UP;
        }
        for (int aa = -range; aa <= range; ++aa) {
            for (int bb = -range; bb <= range; ++bb) {
                for (int cc = start; cc < range; ++cc) {
                    if (aa == 0 && bb == 0 && cc == 0) continue;
                    int xx = origin.getX();
                    int yy = origin.getY();
                    int zz = origin.getZ();
                    if (direction.getStepY() != 0) {
                        xx += aa;
                        yy += cc * direction.getStepY();
                        zz += bb;
                    } else if (direction.getStepX() == 0) {
                        xx += aa;
                        yy += bb;
                        zz += cc * direction.getStepZ();
                    } else {
                        xx += cc * direction.getStepX();
                        yy += aa;
                        zz += bb;
                    }
                    BlockPos p = new BlockPos(xx, yy, zz);
                    BlockEntity te = world.getBlockEntity(p);
                    if (te instanceof IAspectSource && !isTwinMirror(sourceTile, te, world)) {
                        sourceList.add(p);
                    }
                }
            }
        }
        if (!sourceList.isEmpty()) {
            sourceList.sort((a, b) -> Double.compare(a.distSqr(origin), b.distSqr(origin)));
            sources.put(tileLoc, sourceList);
        } else {
            sourcesDelay.put(tileLoc, System.currentTimeMillis() + DELAY);
        }
    }

    /** An essentia mirror never counts its own twin as a source. */
    private static boolean isTwinMirror(@Nullable BlockEntity sourceTile, BlockEntity te, Level world) {
        return sourceTile instanceof TileMirrorEssentia && te instanceof TileMirrorEssentia tm
                && sourceTile.getBlockPos().getX() == tm.linkX && sourceTile.getBlockPos().getY() == tm.linkY
                && sourceTile.getBlockPos().getZ() == tm.linkZ && world.dimension() == tm.linkDim;
    }

    public static void refreshSources(BlockEntity tile) {
        if (tile.getLevel() != null) {
            sources.remove(GlobalPos.of(tile.getLevel().dimension(), tile.getBlockPos()));
        }
    }
}
