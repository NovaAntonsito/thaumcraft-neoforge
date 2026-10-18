package thaumcraft.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.SoundsTC;

/**
 * Port of PacketWarpMessage: tells the player they gained or lost warp (type 0 permanent, 1 normal, 2 temporary).
 * As in the original, gaining permanent warp only whispers (its text is never shown), and losing permanent or
 * temporary warp says nothing.
 */
public record PacketWarpMessage(int data, byte warpType) implements CustomPacketPayload {
    public static final Type<PacketWarpMessage> TYPE = new Type<>(Thaumcraft.id("warp_message"));
    public static final StreamCodec<ByteBuf, PacketWarpMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketWarpMessage::data,
            ByteBufCodecs.BYTE, PacketWarpMessage::warpType,
            PacketWarpMessage::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketWarpMessage message, IPayloadContext context) {
        if (message.data != 0) {
            processMessage(message);
        }
    }

    static void processMessage(PacketWarpMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (message.warpType == 0 && message.data > 0) {
            player.playSound(SoundsTC.WHISPERS.get(), 0.5f, 1.0f);
        } else if (message.warpType == 1) {
            String text = "tc.addwarpsticky";
            if (message.data < 0) {
                text = "tc.removewarpsticky";
            } else {
                player.playSound(SoundsTC.WHISPERS.get(), 0.5f, 1.0f);
            }
            player.displayClientMessage(Component.translatable(text), true);
        } else if (message.data > 0) {
            player.displayClientMessage(Component.translatable("tc.addwarptemp"), true);
        }
    }
}
