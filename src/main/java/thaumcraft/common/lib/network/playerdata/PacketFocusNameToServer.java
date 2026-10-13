package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;

/** Port of PacketFocusNameToServer: the name typed for the focus in the focal manipulator GUI. */
public record PacketFocusNameToServer(BlockPos pos, String name) implements CustomPacketPayload {
    public static final Type<PacketFocusNameToServer> TYPE = new Type<>(Thaumcraft.id("focus_name"));
    public static final StreamCodec<ByteBuf, PacketFocusNameToServer> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketFocusNameToServer::pos,
            ByteBufCodecs.stringUtf8(256), PacketFocusNameToServer::name,
            PacketFocusNameToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFocusNameToServer message, IPayloadContext context) {
        Player player = context.player();
        if (player.distanceToSqr(message.pos.getCenter()) > 64.0 || !player.level().isLoaded(message.pos)) {
            return;
        }
        if (player.level().getBlockEntity(message.pos) instanceof TileFocalManipulator rt) {
            rt.focusName = message.name;
            rt.setChanged();
        }
    }
}
