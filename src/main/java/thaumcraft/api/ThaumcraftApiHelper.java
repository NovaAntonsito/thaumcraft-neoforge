package thaumcraft.api;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.api.aspects.IEssentiaTransport;

/** Partial port of ThaumcraftApiHelper. The rest comes with the features that use it. */
public final class ThaumcraftApiHelper {
    private ThaumcraftApiHelper() {
    }

    /** The essentia transport tile next to pos on that face, if it connects back towards pos. */
    @Nullable
    public static BlockEntity getConnectableTile(BlockGetter world, BlockPos pos, Direction face) {
        BlockEntity te = world.getBlockEntity(pos.relative(face));
        if (te instanceof IEssentiaTransport transport && transport.isConnectable(face.getOpposite())) {
            return te;
        }
        return null;
    }

    /** Sets one byte of a long, byte 0 being the most significant (ByteBuffer order). */
    public static long setByteInLong(long data, byte b, int index) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(8);
        bb.putLong(0, data);
        bb.put(index, b);
        return bb.getLong(0);
    }

    public static byte getByteInLong(long data, int index) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(8);
        bb.putLong(0, data);
        return bb.get(index);
    }
}
