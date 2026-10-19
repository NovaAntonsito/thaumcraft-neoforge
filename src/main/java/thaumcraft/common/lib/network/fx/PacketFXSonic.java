package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** Port of PacketFXSonic: the screech ripple of an eldritch guardian, drawn at the head of the source entity. */
public record PacketFXSonic(int source) implements CustomPacketPayload {
    public static final Type<PacketFXSonic> TYPE = new Type<>(Thaumcraft.id("fx_sonic"));
    public static final StreamCodec<ByteBuf, PacketFXSonic> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketFXSonic::source,
            PacketFXSonic::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXSonic message, IPayloadContext context) {
        Entity p = context.player().level().getEntity(message.source);
        if (p != null) {
            thaumcraft.client.fx.FXDispatcher.sonic(p);
        }
    }
}
