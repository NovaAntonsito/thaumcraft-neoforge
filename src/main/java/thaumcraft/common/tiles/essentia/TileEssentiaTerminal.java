package thaumcraft.common.tiles.essentia;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.registry.TCBlockEntities;

/**
 * The essentia terminal: sees every jar reached through the tubes connected to it, whatever way they point, and
 * fills or empties phials from them. Tubes connect to any side; it never pulls or pushes essentia by itself. A
 * closed valve cuts the network. Not in Thaumcraft (what Thaumic Energistics gave, without AE2).
 */
public class TileEssentiaTerminal extends BlockEntity implements IEssentiaTransport {
    /** How many tubes and jars one search looks at, at most. */
    public static final int MAX_NODES = 1024;

    public TileEssentiaTerminal(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ESSENTIA_TERMINAL.get(), pos, state);
    }

    /** Every jar on the network: a walk through the tubes, stopping at jars and closed valves. */
    public static List<TileJarFillable> findJars(Level level, BlockPos start) {
        List<TileJarFillable> jars = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty() && seen.size() < MAX_NODES) {
            BlockPos pos = queue.poll();
            if (!(level.getBlockEntity(pos) instanceof IEssentiaTransport node)) continue;
            if (node instanceof TileTubeValve valve && !valve.allowFlow) continue;
            for (Direction d : Direction.values()) {
                BlockPos next = pos.relative(d);
                if (!node.isConnectable(d) || seen.contains(next) || !level.isLoaded(next)) continue;
                BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, pos, d);
                if (te == null) continue;
                seen.add(next);
                if (te instanceof TileJarFillable jar) {
                    jars.add(jar);
                } else if (te instanceof TileTube) {
                    queue.add(next);
                }
            }
        }
        return jars;
    }

    // ---- IEssentiaTransport: connects on every side, moves nothing ----

    @Override
    public boolean isConnectable(Direction face) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Nullable
    @Override
    public Aspect getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Nullable
    @Override
    public Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
