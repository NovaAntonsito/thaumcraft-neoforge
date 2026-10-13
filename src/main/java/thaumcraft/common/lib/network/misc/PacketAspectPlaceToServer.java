package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/** Reborn's PacketAspectPlaceToServer: write an aspect on a note cell ("" erases it). */
public record PacketAspectPlaceToServer(BlockPos pos, int q, int r, String aspect) implements CustomPacketPayload {
    public static final Type<PacketAspectPlaceToServer> TYPE = new Type<>(Thaumcraft.id("aspect_place"));
    public static final StreamCodec<ByteBuf, PacketAspectPlaceToServer> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketAspectPlaceToServer::pos,
            ByteBufCodecs.BYTE.map(Byte::intValue, Integer::byteValue), PacketAspectPlaceToServer::q,
            ByteBufCodecs.BYTE.map(Byte::intValue, Integer::byteValue), PacketAspectPlaceToServer::r,
            ByteBufCodecs.STRING_UTF8, PacketAspectPlaceToServer::aspect,
            PacketAspectPlaceToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAspectPlaceToServer msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (player.distanceToSqr(msg.pos.getCenter()) > 64.0) return;
        if (player.level().getBlockEntity(msg.pos) instanceof TileResearchTable table) {
            table.placeAspect(msg.q, msg.r, msg.aspect.isEmpty() ? null : Aspect.getAspect(msg.aspect), player);
        }
    }
}
