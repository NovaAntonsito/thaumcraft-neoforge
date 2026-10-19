package thaumcraft.common.tiles.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.registry.TCBlockEntities;

/** Port of TileEldritchNothing: only there for its renderer, on the void blocks that are uncovered. */
public class TileEldritchNothing extends BlockEntity {
    public TileEldritchNothing(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_NOTHING.get(), pos, state);
    }
}
