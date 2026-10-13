package thaumcraft.common.tiles.essentia;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.registry.TCBlockEntities;

/** Port of TileTubeOneway: takes suction only from the side its facing points away from. */
public class TileTubeOneway extends TileTube {
    public TileTubeOneway(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_ONEWAY.get(), pos, state);
    }

    @Override
    void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(filter, restrict, true);
    }

    @Override
    void equalizeWithNeighbours(boolean directional) {
        super.equalizeWithNeighbours(true);
    }
}
