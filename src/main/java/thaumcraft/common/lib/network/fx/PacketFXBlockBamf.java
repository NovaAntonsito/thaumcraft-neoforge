package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.FXDispatcher;

/**
 * Port of PacketFXBlockBamf: a puff of smoke (drawBamf), with the poof sound and the wisps when asked. The color
 * -9999 means the default purple.
 */
public record PacketFXBlockBamf(double x, double y, double z, int color, byte flags, byte face) implements CustomPacketPayload {
    public static final Type<PacketFXBlockBamf> TYPE = new Type<>(Thaumcraft.id("fx_block_bamf"));

    public static final StreamCodec<ByteBuf, PacketFXBlockBamf> STREAM_CODEC = StreamCodec.of((buf, m) -> {
        buf.writeDouble(m.x);
        buf.writeDouble(m.y);
        buf.writeDouble(m.z);
        buf.writeInt(m.color);
        buf.writeByte(m.flags);
        buf.writeByte(m.face);
    }, buf -> new PacketFXBlockBamf(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt(), buf.readByte(), buf.readByte()));

    public PacketFXBlockBamf(double x, double y, double z, int color, boolean sound, boolean flair, Direction side) {
        this(x, y, z, color, (byte) ((sound ? 1 : 0) | (flair ? 2 : 0)), side != null ? (byte) side.ordinal() : (byte) -1);
    }

    public PacketFXBlockBamf(BlockPos pos, int color, boolean sound, boolean flair, Direction side) {
        this(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, color, sound, flair, side);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBlockBamf message, IPayloadContext context) {
        Direction side = message.face >= 0 ? Direction.from3DDataValue(message.face) : null;
        boolean sound = (message.flags & 1) != 0;
        boolean flair = (message.flags & 2) != 0;
        if (message.color != -9999) {
            FXDispatcher.drawBamf(message.x, message.y, message.z, message.color, sound, flair, side);
        } else {
            FXDispatcher.drawBamf(message.x, message.y, message.z, sound, flair, side);
        }
    }
}
