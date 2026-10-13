package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusEngine;

/** Port of PacketFXFocusEffect: a spray of focus effect particles along a direction. */
public record PacketFXFocusEffect(float x, float y, float z, float mx, float my, float mz, String parts) implements CustomPacketPayload {
    public static final Type<PacketFXFocusEffect> TYPE = new Type<>(Thaumcraft.id("fx_focus_effect"));
    public static final StreamCodec<ByteBuf, PacketFXFocusEffect> STREAM_CODEC = StreamCodec.of((buf, m) -> {
        buf.writeFloat(m.x);
        buf.writeFloat(m.y);
        buf.writeFloat(m.z);
        buf.writeFloat(m.mx);
        buf.writeFloat(m.my);
        buf.writeFloat(m.mz);
        net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.encode(buf, m.parts);
    }, buf -> new PacketFXFocusEffect(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.decode(buf)));

    public PacketFXFocusEffect(float x, float y, float z, float mx, float my, float mz, String[] parts) {
        this(x, y, z, mx, my, mz, String.join("%", parts));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXFocusEffect message, IPayloadContext context) {
        Level world = context.player().level();
        String[] partKeys = message.parts.split("%");
        int amt = Math.max(1, 10 / partKeys.length);
        for (String k : partKeys) {
            if (FocusEngine.getElement(k) instanceof FocusEffect part) {
                for (int a = 0; a < amt; ++a) {
                    part.renderParticleFX(world, message.x, message.y, message.z, message.mx + world.random.nextGaussian() / 20.0,
                            message.my + world.random.nextGaussian() / 20.0, message.mz + world.random.nextGaussian() / 20.0);
                }
            }
        }
    }
}
