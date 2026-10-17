package thaumcraft.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.SoundsTC;

/**
 * Port of BlockTaintLog: a log taken over by taint. It keeps spreading fibres while a taint seed is near and melts
 * into flux goo once it is not. It counts as a log (minecraft:logs), so leaves around it stay alive and decay when it
 * goes, like the 1.12 breakBlock did.
 */
public class BlockTaintLog extends RotatedPillarBlock implements ITaintBlock {
    public BlockTaintLog() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.0f, 60.0f).sound(SoundsTC.SOUND_TYPE_GORE)
                .randomTicks().pushReaction(PushReaction.DESTROY));
    }

    @Override
    public void die(Level world, BlockPos pos, BlockState state) {
        world.setBlockAndUpdate(pos, ConfigBlocks.fluxGoo.get().defaultBlockState());
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!TaintHelper.isNearTaintSeed(world, pos)) {
            die(world, pos, state);
        } else {
            TaintHelper.spreadFibres(world, pos);
        }
    }
}
