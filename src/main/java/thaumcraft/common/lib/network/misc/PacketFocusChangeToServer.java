package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.casters.ICaster;
import thaumcraft.common.items.casters.CasterManager;

/** Port of PacketFocusChangeToServer: mount the focus with this sorting key ("REMOVE" takes it off). */
public record PacketFocusChangeToServer(String focus) implements CustomPacketPayload {
    public static final Type<PacketFocusChangeToServer> TYPE = new Type<>(Thaumcraft.id("focus_change"));
    public static final StreamCodec<ByteBuf, PacketFocusChangeToServer> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(PacketFocusChangeToServer::new, PacketFocusChangeToServer::focus);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFocusChangeToServer message, IPayloadContext context) {
        Player player = context.player();
        if (player.getMainHandItem().getItem() instanceof ICaster) {
            CasterManager.changeFocus(player.getMainHandItem(), player.level(), player, message.focus);
        } else if (player.getOffhandItem().getItem() instanceof ICaster) {
            CasterManager.changeFocus(player.getOffhandItem(), player.level(), player, message.focus);
        }
    }
}
