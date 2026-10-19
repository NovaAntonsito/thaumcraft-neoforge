package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.FXDispatcher;

/**
 * Port of PacketFXShield: the rune flash of a shielded entity taking a hit. target is the attacker's id, or -1 for
 * no attacker, -2 for a fall and -3 for a falling block.
 */
public record PacketFXShield(int source, int target) implements CustomPacketPayload {
    public static final Type<PacketFXShield> TYPE = new Type<>(Thaumcraft.id("fx_shield"));

    public static final StreamCodec<ByteBuf, PacketFXShield> STREAM_CODEC = StreamCodec.of((buf, m) -> {
        buf.writeInt(m.source);
        buf.writeInt(m.target);
    }, buf -> new PacketFXShield(buf.readInt(), buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXShield message, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        FXDispatcher.shieldRunes(mc.level.getEntity(message.source), message.target);
    }
}
