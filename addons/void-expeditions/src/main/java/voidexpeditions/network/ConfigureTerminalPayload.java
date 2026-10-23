package voidexpeditions.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.block.ExpeditionTerminalBlockEntity;
import voidexpeditions.run.Difficulty;

/** Client to server: new settings for a terminal the player is near. */
public record ConfigureTerminalPayload(BlockPos pos, int difficulty, int length) implements CustomPacketPayload {
    public static final Type<ConfigureTerminalPayload> TYPE = new Type<>(VoidExpeditions.id("configure_terminal"));
    public static final StreamCodec<ByteBuf, ConfigureTerminalPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConfigureTerminalPayload::pos,
            ByteBufCodecs.VAR_INT, ConfigureTerminalPayload::difficulty,
            ByteBufCodecs.VAR_INT, ConfigureTerminalPayload::length,
            ConfigureTerminalPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfigureTerminalPayload msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (player.distanceToSqr(msg.pos.getCenter()) > 64.0) return;
        if (player.level().getBlockEntity(msg.pos) instanceof ExpeditionTerminalBlockEntity terminal) {
            terminal.configure(Difficulty.byIndex(msg.difficulty), msg.length);
        }
    }
}
