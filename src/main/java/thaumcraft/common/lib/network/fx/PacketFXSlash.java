package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.FXDispatcher;

/** Port of PacketFXSlash: the swoosh of a sweeping blow, from one entity's middle to another's. */
public record PacketFXSlash(int source, int target) implements CustomPacketPayload {
    public static final Type<PacketFXSlash> TYPE = new Type<>(Thaumcraft.id("fx_slash"));

    public static final StreamCodec<ByteBuf, PacketFXSlash> STREAM_CODEC = StreamCodec.of((buf, m) -> {
        buf.writeInt(m.source);
        buf.writeInt(m.target);
    }, buf -> new PacketFXSlash(buf.readInt(), buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXSlash message, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity var2 = mc.level.getEntity(message.source);
        Entity var3 = mc.level.getEntity(message.target);
        if (var2 != null && var3 != null) {
            FXDispatcher.drawSlash(var2.getX(), var2.getBoundingBox().minY + var2.getBbHeight() / 2.0f, var2.getZ(), var3.getX(),
                    var3.getBoundingBox().minY + var3.getBbHeight() / 2.0f, var3.getZ(), 8);
        }
    }
}
