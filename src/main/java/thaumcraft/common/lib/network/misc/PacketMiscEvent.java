package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.lib.events.RenderEventHandler;

/** Port of PacketMiscEvent: small client events. The heartbeat of warp, and the eldritch fog (long and short). */
public record PacketMiscEvent(byte eventType, int value) implements CustomPacketPayload {
    public static final byte WARP_EVENT = 0;
    public static final byte MIST_EVENT = 1;
    public static final byte MIST_EVENT_SHORT = 2;
    public static final Type<PacketMiscEvent> TYPE = new Type<>(Thaumcraft.id("misc_event"));
    public static final StreamCodec<ByteBuf, PacketMiscEvent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, PacketMiscEvent::eventType,
            ByteBufCodecs.VAR_INT, PacketMiscEvent::value,
            PacketMiscEvent::new);

    public PacketMiscEvent(byte eventType) {
        this(eventType, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMiscEvent message, IPayloadContext context) {
        RenderEventHandler.processMiscEvent(message.eventType);
    }
}
