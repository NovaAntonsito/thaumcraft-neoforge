package thaumcraft.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.registry.TCBlockEntities;

/**
 * Port of TileNitor: draws the nitor flame every tick on the client.
 */
public class TileNitor extends BlockEntity {
    int count;

    public TileNitor(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NITOR.get(), pos, state);
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, TileNitor te) {
        FXDispatcher.drawNitorFlames(pos.getX() + 0.5f + world.random.nextGaussian() * 0.025, pos.getY() + 0.45f + world.random.nextGaussian() * 0.025,
                pos.getZ() + 0.5f + world.random.nextGaussian() * 0.025, world.random.nextGaussian() * 0.0025, world.random.nextFloat() * 0.06,
                world.random.nextGaussian() * 0.0025, state.getMapColor(world, pos).col, 0);
        if (te.count++ % 10 == 0) {
            FXDispatcher.drawNitorCore(pos.getX() + 0.5f, pos.getY() + 0.49f, pos.getZ() + 0.5f, 0.0, 0.0, 0.0);
        }
    }
}
