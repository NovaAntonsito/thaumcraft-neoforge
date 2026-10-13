package thaumcraft.common.lib.network.fx;

import java.awt.Color;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFX;

/**
 * Port of PacketFXZap: the bolt focus arc. 1.21: drawn with the existing lightning arc (the width is kept in the
 * packet but not used until the FX stage).
 */
public record PacketFXZap(Vec3 source, Vec3 target, int color, float width) implements CustomPacketPayload {
    public static final Type<PacketFXZap> TYPE = new Type<>(Thaumcraft.id("fx_zap"));
    private static final StreamCodec<ByteBuf, Vec3> VEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x, ByteBufCodecs.DOUBLE, Vec3::y, ByteBufCodecs.DOUBLE, Vec3::z, Vec3::new);
    public static final StreamCodec<ByteBuf, PacketFXZap> STREAM_CODEC = StreamCodec.composite(
            VEC, PacketFXZap::source,
            VEC, PacketFXZap::target,
            ByteBufCodecs.INT, PacketFXZap::color,
            ByteBufCodecs.FLOAT, PacketFXZap::width,
            PacketFXZap::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXZap message, IPayloadContext context) {
        Color c = new Color(message.color);
        ClientFX.arcLightning(message.source.x, message.source.y, message.source.z, message.target.x, message.target.y, message.target.z,
                c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
    }
}
