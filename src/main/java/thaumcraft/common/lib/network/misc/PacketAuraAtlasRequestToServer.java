package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.items.tools.ItemAuraAtlas;

/** Not in Thaumcraft 4 (dream branch): the open atlas screen asks for fresh data, while the player still holds it. */
public record PacketAuraAtlasRequestToServer() implements CustomPacketPayload {
    public static final Type<PacketAuraAtlasRequestToServer> TYPE = new Type<>(Thaumcraft.id("aura_atlas_request"));
    public static final PacketAuraAtlasRequestToServer INSTANCE = new PacketAuraAtlasRequestToServer();
    public static final StreamCodec<ByteBuf, PacketAuraAtlasRequestToServer> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAuraAtlasRequestToServer msg, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer sp && ItemAuraAtlas.isHolding(sp)) {
            PacketDistributor.sendToPlayer(sp, PacketAuraAtlasToClient.around(sp, false));
        }
    }
}
