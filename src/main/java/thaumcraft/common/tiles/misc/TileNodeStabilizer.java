package thaumcraft.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.registry.TCBlockEntities;

/**
 * Port of the Thaumcraft 4 TileNodeStabilizer. The lock itself is checked by the node (TileAuraNode.checkLock); this
 * only counts up the pistons and the bubble on the client while a node sits on it and it has no redstone signal.
 */
public class TileNodeStabilizer extends BlockEntity {
    public int count = 0;

    public TileNodeStabilizer(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE_STABILIZER.get(), pos, state);
    }

    public int getLock() {
        return getBlockState().is(ConfigBlocks.nodeStabilizerAdvanced.get()) ? 2 : 1;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileNodeStabilizer tile) {
        if (level.getBlockState(pos.above()).is(ConfigBlocks.auraNode.get()) && !level.hasNeighborSignal(pos)) {
            if (tile.count < 37) ++tile.count;
        } else if (tile.count > 0) {
            --tile.count;
        }
    }
}
