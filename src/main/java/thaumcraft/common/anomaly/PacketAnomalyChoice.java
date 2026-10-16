package thaumcraft.common.anomaly;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** Server to a seer: the anomaly cards to choose from and the ticks left. No cards closes the choice. */
public record PacketAnomalyChoice(List<String> ids, int ticks) implements CustomPacketPayload {
    public static final Type<PacketAnomalyChoice> TYPE = new Type<>(Thaumcraft.id("anomaly_choice"));
    public static final PacketAnomalyChoice CLOSE = new PacketAnomalyChoice(List.of(), 0);
    public static final StreamCodec<ByteBuf, PacketAnomalyChoice> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)), PacketAnomalyChoice::ids,
            ByteBufCodecs.VAR_INT, PacketAnomalyChoice::ticks,
            PacketAnomalyChoice::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAnomalyChoice msg, IPayloadContext context) {
        AnomalyClientState.setChoice(msg.ids(), msg.ticks(), context.player().level().getGameTime());
    }
}
