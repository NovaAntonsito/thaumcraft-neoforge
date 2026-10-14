package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.crafting.TileGolemBuilder;

/**
 * The golem press GUI asking the server either to check which components are at hand (check) or to start pressing
 * the golem. The original used PacketTileToServer with a "check" or "golem" tag.
 */
public record PacketGolemBuilderToServer(BlockPos pos, long golem, boolean check) implements CustomPacketPayload {
    public static final Type<PacketGolemBuilderToServer> TYPE = new Type<>(Thaumcraft.id("golem_builder"));
    public static final StreamCodec<ByteBuf, PacketGolemBuilderToServer> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketGolemBuilderToServer::pos,
            ByteBufCodecs.VAR_LONG, PacketGolemBuilderToServer::golem,
            ByteBufCodecs.BOOL, PacketGolemBuilderToServer::check,
            PacketGolemBuilderToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketGolemBuilderToServer msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.distanceToSqr(msg.pos.getCenter()) > 64.0) return;
        if (player.level().getBlockEntity(msg.pos) instanceof TileGolemBuilder builder) {
            if (msg.check) {
                builder.checkCraftFor(msg.golem, player);
            } else {
                builder.startCraft(msg.golem, player);
            }
        }
    }
}
