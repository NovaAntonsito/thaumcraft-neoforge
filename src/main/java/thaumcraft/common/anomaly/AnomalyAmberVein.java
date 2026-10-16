package thaumcraft.common.anomaly;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ConfigBlocks;

/**
 * Amber vein (positive, local). A spot 24 to 48 blocks from a player fills with amber and cinnabar ore in the
 * ground just under the surface, and its chunk gains {@link #VIS} vis. A column of light marks it while it lasts.
 * The ore stays when the anomaly ends.
 */
public class AnomalyAmberVein extends AnomalyType {
    public static final int RADIUS = 4;
    public static final float VIS = 150.0f;

    public AnomalyAmberVein() {
        super("amber_vein", Polarity.POSITIVE);
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
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, column);
        BlockPos center = surface.below(2);
        int placed = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS), center.offset(RADIUS, RADIUS, RADIUS))) {
            if (pos.distSqr(center) > RADIUS * RADIUS) continue;
            BlockState state = level.getBlockState(pos);
            if (!replaceable(state)) continue;
            float r = level.random.nextFloat();
            if (r < 0.3f) {
                level.setBlock(pos, ConfigBlocks.oreAmber.get().defaultBlockState(), 2);
                placed++;
            } else if (r < 0.45f) {
                level.setBlock(pos, ConfigBlocks.oreCinnabar.get().defaultBlockState(), 2);
                placed++;
            }
        }
        AuraHelper.addVis(level, center, VIS);
        anomaly.data().putInt("ores", placed);
        anomaly.setLocation(surface);
    }

    private static boolean replaceable(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY);
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        BlockPos loc = anomaly.getLocation();
        if (loc == null || !level.isLoaded(loc)) return;
        for (int i = 0; i < 12; i++) {
            level.sendParticles(ParticleTypes.END_ROD, loc.getX() + 0.5, loc.getY() + 1.0 + i * 2.0, loc.getZ() + 0.5, 2, 0.15, 1.0, 0.15, 0.0);
        }
        level.sendParticles(ParticleTypes.WAX_ON, loc.getX() + 0.5, loc.getY() + 0.5, loc.getZ() + 0.5, 6, 2.5, 0.5, 2.5, 0.0);
    }
}
