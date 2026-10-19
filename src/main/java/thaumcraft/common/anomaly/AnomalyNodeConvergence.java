package thaumcraft.common.anomaly;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.common.lib.network.fx.PacketFXBlockZap;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.tiles.misc.TileAuraNode;
import thaumcraft.common.world.ThaumcraftWorldGenerator;

/**
 * Node convergence (positive, local; dream branch). Somewhere 24 to 48 blocks from a player a bright node appears
 * with {@link #SATELLITES} small nodes around it. While it lasts they all regenerate a point a second; when it ends
 * the small nodes fall into the big one, which keeps their base aspects. A column of light marks it.
 */
public class AnomalyNodeConvergence extends AnomalyType {
    public static final int SATELLITES = 3;

    public AnomalyNodeConvergence() {
        super("node_convergence", Polarity.POSITIVE);
        weight(6).durationMinutes(8, 15);
    }

    @Override
    public void onStart(ServerLevel level, ActiveAnomaly anomaly) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;
        ServerPlayer target = players.get(level.random.nextInt(players.size()));
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        double dist = 24.0 + level.random.nextDouble() * 24.0;
        BlockPos column = BlockPos.containing(target.getX() + Math.cos(angle) * dist, target.getY(), target.getZ() + Math.sin(angle) * dist);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
        BlockPos center = surface.above(5);
        ThaumcraftWorldGenerator.createRandomNodeAt(level, center, level.random, false, false, false);
        if (!(level.getBlockEntity(center) instanceof TileAuraNode core)) return;
        AspectList doubled = new AspectList();
        for (Aspect a : core.getAspects().getAspects()) doubled.add(a, core.getAspects().getAmount(a) * 2);
        core.setAspects(doubled);
        core.setNodeModifier(NodeModifier.BRIGHT);
        core.nodeChange();
        ListTag list = new ListTag();
        for (int i = 0; i < SATELLITES; ++i) {
            BlockPos p = center.offset(level.random.nextInt(9) - 4, level.random.nextInt(5) - 2, level.random.nextInt(9) - 4);
            if (p.equals(center) || !level.isEmptyBlock(p)) continue;
            ThaumcraftWorldGenerator.createRandomNodeAt(level, p, level.random, false, false, true);
            if (level.getBlockEntity(p) instanceof TileAuraNode) list.add(NbtUtils.writeBlockPos(p));
        }
        CompoundTag data = anomaly.data();
        data.put("core", NbtUtils.writeBlockPos(center));
        data.put("satellites", list);
        anomaly.setLocation(surface);
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        BlockPos loc = anomaly.getLocation();
        if (loc == null || !level.isLoaded(loc)) return;
        for (int i = 0; i < 12; i++) {
            level.sendParticles(ParticleTypes.END_ROD, loc.getX() + 0.5, loc.getY() + 1.0 + i * 2.0, loc.getZ() + 0.5, 2, 0.15, 1.0, 0.15, 0.0);
        }
        for (BlockPos p : nodes(anomaly)) {
            if (level.isLoaded(p) && level.getBlockEntity(p) instanceof TileAuraNode n && n.regenerateOne()) {
                n.setChanged();
                n.syncTile(false);
            }
        }
    }

    @Override
    public void onEnd(ServerLevel level, ActiveAnomaly anomaly) {
        BlockPos corePos = NbtUtils.readBlockPos(anomaly.data(), "core").orElse(null);
        if (corePos == null || !level.isLoaded(corePos) || !(level.getBlockEntity(corePos) instanceof TileAuraNode core)) return;
        ListTag list = anomaly.data().getList("satellites", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < list.size(); ++i) {
            BlockPos p = NbtUtils.readBlockPos(wrap(list.get(i)), "p").orElse(null);
            if (p == null || !level.isLoaded(p) || !(level.getBlockEntity(p) instanceof TileAuraNode sat)) continue;
            for (Aspect a : sat.getAspectsBase().getAspects()) {
                int amt = sat.getAspectsBase().getAmount(a);
                core.getAspectsBase().add(a, amt);
                core.getAspects().add(a, sat.getAspects().getAmount(a));
            }
            level.removeBlock(p, false);
            TCNetwork.sendToAllAround(level, p.getX(), p.getY(), p.getZ(), 32.0, new PacketFXBlockZap(p.getX() + 0.5f, p.getY() + 0.5f,
                    p.getZ() + 0.5f, corePos.getX() + 0.5f, corePos.getY() + 0.5f, corePos.getZ() + 0.5f));
        }
        core.nodeChange();
    }

    private static List<BlockPos> nodes(ActiveAnomaly anomaly) {
        java.util.ArrayList<BlockPos> out = new java.util.ArrayList<>();
        NbtUtils.readBlockPos(anomaly.data(), "core").ifPresent(out::add);
        ListTag list = anomaly.data().getList("satellites", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < list.size(); ++i) {
            NbtUtils.readBlockPos(wrap(list.get(i)), "p").ifPresent(out::add);
        }
        return out;
    }

    private static CompoundTag wrap(Tag t) {
        CompoundTag c = new CompoundTag();
        c.put("p", t);
        return c;
    }
}
