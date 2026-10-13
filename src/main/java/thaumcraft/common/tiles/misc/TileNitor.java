package thaumcraft.common.tiles.misc;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.registry.TCBlockEntities;

/**
 * Port of TileNitor: draws the nitor flame every tick on the client.
 * TODO: the original FXDispatcher.drawNitorFlames and drawNitorCore (stage 10). Vanilla particles for now.
 */
public class TileNitor extends BlockEntity {
    int count;

    public TileNitor(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NITOR.get(), pos, state);
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, TileNitor te) {
        int c = state.getMapColor(world, pos).col;
        Vector3f color = new Vector3f(((c >> 16) & 0xFF) / 255.0f, ((c >> 8) & 0xFF) / 255.0f, (c & 0xFF) / 255.0f);
        if (world.random.nextInt(3) == 0) {
            world.addParticle(new DustParticleOptions(color, 0.75f),
                    pos.getX() + 0.5f + world.random.nextGaussian() * 0.025, pos.getY() + 0.45f + world.random.nextGaussian() * 0.025,
                    pos.getZ() + 0.5f + world.random.nextGaussian() * 0.025,
                    world.random.nextGaussian() * 0.0025, world.random.nextFloat() * 0.06, world.random.nextGaussian() * 0.0025);
        }
        if (te.count++ % 10 == 0) {
            world.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5f, pos.getY() + 0.49f, pos.getZ() + 0.5f, 0.0, 0.0, 0.0);
        }
    }
}
