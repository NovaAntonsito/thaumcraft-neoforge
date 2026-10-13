package thaumcraft.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/** Port of TileLampArcane: every 5 ticks it may place an invisible light (effect_glimmer) in a dark spot it can see. */
public class TileLampArcane extends TileThaumcraft {
    public TileLampArcane(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LAMP_ARCANE.get(), pos, state);
    }

    public void serverTick() {
        if (level.getGameTime() % 5L == 0L && !gettingPower()) {
            int x = level.random.nextInt(16) - level.random.nextInt(16);
            int y = level.random.nextInt(16) - level.random.nextInt(16);
            int z = level.random.nextInt(16) - level.random.nextInt(16);
            BlockPos bp = worldPosition.offset(x, y, z);
            // getPrecipitationHeight: the first block from the top that blocks motion or holds liquid.
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, bp);
            if (bp.getY() > top.getY() + 4) {
                bp = top.above(4);
            }
            // The original clamps to y 5; worlds start lower now, so it is 5 above the bottom of the world.
            if (bp.getY() < level.getMinBuildHeight() + 5) {
                bp = new BlockPos(bp.getX(), level.getMinBuildHeight() + 5, bp.getZ());
            }
            BlockState glimmer = ConfigBlocks.effectGlimmer.get().defaultBlockState();
            if (level.isEmptyBlock(bp) && level.getBlockState(bp) != glimmer && level.getBrightness(LightLayer.BLOCK, bp) < 11
                    && BlockUtils.hasLOS(level, worldPosition, bp)) {
                level.setBlock(bp, glimmer, 3);
            }
        }
    }

    public void removeLights() {
        BlockState glimmer = ConfigBlocks.effectGlimmer.get().defaultBlockState();
        for (int x = -15; x <= 15; ++x) {
            for (int y = -15; y <= 15; ++y) {
                for (int z = -15; z <= 15; ++z) {
                    BlockPos bp = worldPosition.offset(x, y, z);
                    if (level.getBlockState(bp) == glimmer) {
                        level.removeBlock(bp, false);
                    }
                }
            }
        }
    }
}
