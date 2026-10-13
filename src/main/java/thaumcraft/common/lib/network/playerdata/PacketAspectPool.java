package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.ClientPacketHandlers;

/** Reborn's PacketAspectPool: the new total of an aspect in the player's pool, and how much was just gained (shown on screen). */
public record PacketAspectPool(String key, int amount, int total) implements CustomPacketPayload {
    public static final Type<PacketAspectPool> TYPE = new Type<>(Thaumcraft.id("aspect_pool"));
    public static final StreamCodec<ByteBuf, PacketAspectPool> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketAspectPool::key,
            ByteBufCodecs.VAR_INT, PacketAspectPool::amount,
            ByteBufCodecs.VAR_INT, PacketAspectPool::total,
            PacketAspectPool::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAspectPool msg, IPayloadContext context) {
        ClientPacketHandlers.aspectPool(context.player(), msg.key, msg.amount, msg.total);
    }
}
