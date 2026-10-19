package thaumcraft.common.tiles.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.registry.TCBlockEntities;

/** Port of TileEldritchCap: only there for its renderer. */
public class TileEldritchCap extends BlockEntity {
    public TileEldritchCap(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_CAP.get(), pos, state);
    }
}
