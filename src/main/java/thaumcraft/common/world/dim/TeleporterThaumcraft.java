package thaumcraft.common.world.dim;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.config.ConfigBlocks;

/**
 * Port of TC4's TeleporterThaumcraft. The entity keeps its x and z; it lands next to the closest eldritch portal
 * within 128 blocks (one block off on each axis, at the portal's height). It never builds a portal: with none found
 * the entity keeps its coordinates.
 * The original searched every block in the square; this searches the loaded chunks in it (the chunk the entity
 * arrives in is loaded, and generated, first). The original's 600 tick destination cache is not kept.
 */
public final class TeleporterThaumcraft {
    private static final int RANGE = 128;

    private TeleporterThaumcraft() {
    }

    public static DimensionTransition transition(ServerLevel target, Entity entity) {
        int l = Mth.floor(entity.getX());
        int i1 = Mth.floor(entity.getZ());
        target.getChunk(l >> 4, i1 >> 4);
        BlockPos found = findPortal(target, entity, l, i1);
        Vec3 dest;
        if (found != null) {
            double d8 = found.getX() + 0.5 + (target.random.nextBoolean() ? 1 : -1);
            double d4 = found.getZ() + 0.5 + (target.random.nextBoolean() ? 1 : -1);
            dest = new Vec3(d8, found.getY(), d4);
        } else {
            dest = entity.position();
        }
        return new DimensionTransition(target, dest, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                DimensionTransition.PLACE_PORTAL_TICKET);
    }

    private static BlockPos findPortal(ServerLevel world, Entity entity, int l, int i1) {
        double d3 = -1.0;
        BlockPos best = null;
        for (int cx = (l - RANGE) >> 4; cx <= (l + RANGE) >> 4; ++cx) {
            for (int cz = (i1 - RANGE) >> 4; cz <= (i1 + RANGE) >> 4; ++cz) {
                LevelChunk chunk = world.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                LevelChunkSection[] sections = chunk.getSections();
                for (int s = sections.length - 1; s >= 0; --s) {
                    LevelChunkSection section = sections[s];
                    if (section.hasOnlyAir() || !section.maybeHas(st -> st.is(ConfigBlocks.eldritchPortal.get()))) {
                        continue;
                    }
                    int baseY = world.getSectionYFromSectionIndex(s) << 4;
                    for (int y = 15; y >= 0; --y) {
                        for (int x = 0; x < 16; ++x) {
                            for (int z = 0; z < 16; ++z) {
                                int k1 = (cx << 4) + x;
                                int l1 = (cz << 4) + z;
                                if (Math.abs(k1 - l) > RANGE || Math.abs(l1 - i1) > RANGE
                                        || !section.getBlockState(x, y, z).is(ConfigBlocks.eldritchPortal.get())) {
                                    continue;
                                }
                                double d5 = k1 + 0.5 - entity.getX();
                                double d6 = l1 + 0.5 - entity.getZ();
                                double d4 = baseY + y + 0.5 - entity.getY();
                                double d7 = d5 * d5 + d4 * d4 + d6 * d6;
                                if (d3 < 0.0 || d7 < d3) {
                                    d3 = d7;
                                    best = new BlockPos(k1, baseY + y, l1);
                                }
                            }
                        }
                    }
                }
            }
        }
        return best;
    }
}
