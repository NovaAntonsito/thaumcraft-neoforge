package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** Port of PacketFXBlockArc: an arc of lightning from the center of a block to a point. */
public record PacketFXBlockArc(BlockPos pos, float tx, float ty, float tz, float r, float g, float b) implements CustomPacketPayload {
    public static final Type<PacketFXBlockArc> TYPE = new Type<>(Thaumcraft.id("fx_block_arc"));

    public static final StreamCodec<ByteBuf, PacketFXBlockArc> STREAM_CODEC = StreamCodec.of((buf, m) -> {
        BlockPos.STREAM_CODEC.encode(buf, m.pos);
        buf.writeFloat(m.tx);
        buf.writeFloat(m.ty);
        buf.writeFloat(m.tz);
        buf.writeFloat(m.r);
        buf.writeFloat(m.g);
        buf.writeFloat(m.b);
    }, buf -> new PacketFXBlockArc(BlockPos.STREAM_CODEC.decode(buf), buf.readFloat(), buf.readFloat(), buf.readFloat(),
            buf.readFloat(), buf.readFloat(), buf.readFloat()));

    /** Arc to the middle of an entity. */
    public PacketFXBlockArc(BlockPos pos, Entity source, float r, float g, float b) {
        this(pos, (float) source.getX(), (float) (source.getBoundingBox().minY + source.getBbHeight() / 2.0f), (float) source.getZ(), r, g, b);
    }

    /** Arc to the center of a block. */
    public PacketFXBlockArc(BlockPos pos, BlockPos target, float r, float g, float b) {
        this(pos, target.getX() + 0.5f, target.getY() + 0.5f, target.getZ() + 0.5f, r, g, b);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBlockArc msg, IPayloadContext context) {
        thaumcraft.client.fx.FXDispatcher.arcLightning(msg.tx, msg.ty, msg.tz, msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5, msg.r,
                msg.g, msg.b, 0.5f);
    }
}
