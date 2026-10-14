package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.crafting.TileGolemBuilder;

/** Which golem components the press found, one byte per component. The original used PacketTileToClient with "stuff". */
public record PacketGolemBuilderStuffToClient(BlockPos pos, byte[] stuff) implements CustomPacketPayload {
    public static final Type<PacketGolemBuilderStuffToClient> TYPE = new Type<>(Thaumcraft.id("golem_builder_stuff"));
    public static final StreamCodec<ByteBuf, PacketGolemBuilderStuffToClient> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketGolemBuilderStuffToClient::pos,
            ByteBufCodecs.BYTE_ARRAY, PacketGolemBuilderStuffToClient::stuff,
            PacketGolemBuilderStuffToClient::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketGolemBuilderStuffToClient msg, IPayloadContext context) {
        if (context.player().level().getBlockEntity(msg.pos) instanceof TileGolemBuilder builder) {
            builder.receiveStuff(msg.stuff);
        }
    }
}
