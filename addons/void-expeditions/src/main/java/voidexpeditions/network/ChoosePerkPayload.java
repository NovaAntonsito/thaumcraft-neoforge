package voidexpeditions.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.run.Expeditions;

/** Client to server: the perk picked among the offered ones, by its place in the list. */
public record ChoosePerkPayload(int index) implements CustomPacketPayload {
    public static final Type<ChoosePerkPayload> TYPE = new Type<>(VoidExpeditions.id("choose_perk"));
    public static final StreamCodec<ByteBuf, ChoosePerkPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(ChoosePerkPayload::new, ChoosePerkPayload::index);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChoosePerkPayload msg, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) Expeditions.choosePerk(player, msg.index);
    }
}
