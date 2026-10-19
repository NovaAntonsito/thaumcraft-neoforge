package thaumcraft.common.lib.research;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.world.aura.AuraNodeRegistry;
import thaumcraft.common.world.objects.MoundLocations;
import thaumcraft.common.world.objects.StructureLocations;

/**
 * Not in Thaumcraft (dream branch): what a cartographic note can lead to. Each target has an aspect complexity for
 * the note's grid and a list of the places of its kind the world knows of; the solved note maps the closest one the
 * player has not mapped yet. Later content (the abandoned libraries, the void rifts) registers its own targets.
 */
public final class CartographyTargets {
    /** How far a note looks, in blocks. */
    public static final int RANGE = 3000;

    public record Target(String id, int complexity, int weight, BiFunction<ServerLevel, BlockPos, Collection<BlockPos>> places) {
    }

    private static final Map<String, Target> TARGETS = new LinkedHashMap<>();

    public static final Target BARROW = register(new Target("barrow", 3, 10, (level, pos) -> MoundLocations.get(level).all()));
    public static final Target ELDRITCH_RING = register(new Target("eldritch_ring", 6, 4,
            (level, pos) -> StructureLocations.get(level).all(StructureLocations.ELDRITCH_RING)));
    /** A node out of the ordinary: of a special type, or bright. */
    public static final Target NODE = register(new Target("node", 4, 8, (level, pos) -> {
        List<BlockPos> out = new ArrayList<>();
        for (AuraNodeRegistry.Entry e : AuraNodeRegistry.get(level).around(pos.getX() >> 4, pos.getZ() >> 4, RANGE >> 4)) {
            if (e.type() != NodeType.NORMAL || e.modifier() == NodeModifier.BRIGHT) out.add(e.pos());
        }
        return out;
    }));

    private CartographyTargets() {
    }

    public static Target register(Target target) {
        TARGETS.put(target.id(), target);
        return target;
    }

    @Nullable
    public static Target get(String id) {
        return TARGETS.get(id);
    }

    public static Target random(RandomSource rand) {
        int total = TARGETS.values().stream().mapToInt(Target::weight).sum();
        int r = rand.nextInt(total);
        for (Target t : TARGETS.values()) {
            r -= t.weight();
            if (r < 0) return t;
        }
        return BARROW;
    }

    /** The closest place of the target's kind within range of pos that is not in mapped, or null. */
    @Nullable
    public static BlockPos find(Target target, ServerLevel level, BlockPos pos, Set<Long> mapped) {
        BlockPos best = null;
        double bestDist = (double) RANGE * RANGE;
        for (BlockPos p : target.places().apply(level, pos)) {
            if (mapped.contains(p.asLong())) continue;
            double dx = p.getX() - pos.getX();
            double dz = p.getZ() - pos.getZ();
            double d = dx * dx + dz * dz;
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
