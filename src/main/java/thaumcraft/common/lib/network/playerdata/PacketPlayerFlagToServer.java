package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** Port of PacketPlayerFlagToServer: flag 1 resets the fall distance (the cloudstepper ring's air jump). */
public record PacketPlayerFlagToServer(byte flag) implements CustomPacketPayload {
    public static final Type<PacketPlayerFlagToServer> TYPE = new Type<>(Thaumcraft.id("player_flag"));
    public static final StreamCodec<ByteBuf, PacketPlayerFlagToServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, PacketPlayerFlagToServer::flag,
            PacketPlayerFlagToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketPlayerFlagToServer message, IPayloadContext context) {
        Player player = context.player();
        if (message.flag == 1) {
            player.fallDistance = 0.0f;
        }
    }
}
