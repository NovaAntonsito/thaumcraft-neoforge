package thaumcraft.common.tiles.essentia;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.registry.TCBlockEntities;

/** Port of TileTubeRestrict: passes on only half the suction. */
public class TileTubeRestrict extends TileTube {
    public TileTubeRestrict(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_RESTRICT.get(), pos, state);
    }

    @Override
    void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean dir) {
        super.calculateSuction(filter, true, dir);
    }
}
