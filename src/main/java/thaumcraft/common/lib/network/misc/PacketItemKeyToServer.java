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

/**
 * Port of PacketItemKeyToServer: key 1 (G) toggles the misc setting of the held caster; mod is 1 with ctrl and 2 with
 * shift. With the elemental shovel in the main hand it turns its 3x3.
 */
public record PacketItemKeyToServer(byte key, byte mod) implements CustomPacketPayload {
    public static final Type<PacketItemKeyToServer> TYPE = new Type<>(Thaumcraft.id("item_key"));
    public static final StreamCodec<ByteBuf, PacketItemKeyToServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, PacketItemKeyToServer::key, ByteBufCodecs.BYTE, PacketItemKeyToServer::mod, PacketItemKeyToServer::new);

    public PacketItemKeyToServer(int key, int mod) {
        this((byte) key, (byte) mod);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketItemKeyToServer message, IPayloadContext context) {
        Player player = context.player();
        boolean flag = false;
        if (message.key == 1 && player.getMainHandItem().getItem() instanceof ICaster) {
            CasterManager.toggleMisc(player.getMainHandItem(), player.level(), player, message.mod);
            flag = true;
        }
        if (!flag && message.key == 1 && player.getOffhandItem().getItem() instanceof ICaster) {
            CasterManager.toggleMisc(player.getOffhandItem(), player.level(), player, message.mod);
        }
        if (message.key == 1 && player.getMainHandItem().getItem() instanceof thaumcraft.common.items.tools.ItemElementalShovel) {
            byte b = thaumcraft.common.items.tools.ItemElementalShovel.getOrientation(player.getMainHandItem());
            thaumcraft.common.items.tools.ItemElementalShovel.setOrientation(player.getMainHandItem(), (byte) (b + 1));
        }
    }
}
