package thaumcraft.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Port of ITaintBlock: a taint block that withers into something else away from a taint seed. */
public interface ITaintBlock {
    void die(Level world, BlockPos pos, BlockState state);
}
