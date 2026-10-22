package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.research.PointExchange;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/** Not in Thaumcraft (dream branch): buy an offer of the point exchange at a research table. */
public record PacketPointExchangeToServer(BlockPos pos, int offer) implements CustomPacketPayload {
    public static final Type<PacketPointExchangeToServer> TYPE = new Type<>(Thaumcraft.id("point_exchange"));
    public static final StreamCodec<ByteBuf, PacketPointExchangeToServer> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketPointExchangeToServer::pos,
            ByteBufCodecs.VAR_INT, PacketPointExchangeToServer::offer,
            PacketPointExchangeToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketPointExchangeToServer msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.distanceToSqr(msg.pos.getCenter()) > 64.0) return;
        if (msg.offer < 0 || msg.offer >= PointExchange.Offer.values().length) return;
        if (player.level().getBlockEntity(msg.pos) instanceof TileResearchTable table) {
            PointExchange.exchange(player, table, PointExchange.Offer.values()[msg.offer]);
        }
    }
}
