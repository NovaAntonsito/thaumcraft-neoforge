package voidexpeditions.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.client.ClientHooks;

/** Server to client: the player is in a portal with a sigil, so the perk choice opens. */
public record OpenPerkChoicePayload(List<String> offered, int stage, int length) implements CustomPacketPayload {
    public static final Type<OpenPerkChoicePayload> TYPE = new Type<>(VoidExpeditions.id("open_perk_choice"));
    public static final StreamCodec<ByteBuf, OpenPerkChoicePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenPerkChoicePayload::offered,
            ByteBufCodecs.VAR_INT, OpenPerkChoicePayload::stage,
            ByteBufCodecs.VAR_INT, OpenPerkChoicePayload::length,
            OpenPerkChoicePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenPerkChoicePayload msg, IPayloadContext context) {
        ClientHooks.openPerkChoice(msg.offered, msg.stage, msg.length);
    }
}
