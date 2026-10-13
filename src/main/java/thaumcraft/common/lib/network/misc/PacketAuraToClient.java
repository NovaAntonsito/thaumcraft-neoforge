package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.client.ClientPacketHandlers;
import thaumcraft.common.world.aura.AuraChunk;

/** The aura of the chunk the player stands in, for the thaumometer's dial. */
public record PacketAuraToClient(short base, float vis, float flux) implements CustomPacketPayload {
    public static final Type<PacketAuraToClient> TYPE = new Type<>(Thaumcraft.id("aura_to_client"));
    public static final StreamCodec<ByteBuf, PacketAuraToClient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.SHORT, PacketAuraToClient::base,
            ByteBufCodecs.FLOAT, PacketAuraToClient::vis,
            ByteBufCodecs.FLOAT, PacketAuraToClient::flux,
            PacketAuraToClient::new);

    public PacketAuraToClient(AuraChunk ac) {
        this(ac.getBase(), ac.getVis(), ac.getFlux());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAuraToClient msg, IPayloadContext context) {
        ClientPacketHandlers.auraToClient(msg.base, msg.vis, msg.flux);
    }
}
