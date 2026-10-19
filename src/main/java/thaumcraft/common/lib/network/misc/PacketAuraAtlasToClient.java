package thaumcraft.common.lib.network.misc;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.ClientPacketHandlers;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.common.world.aura.AuraNodeRegistry;

/**
 * Not in Thaumcraft 4 (dream branch): what the aura atlas shows. The aura of each chunk in a square around the player
 * (base -1 when the chunk is not loaded), and the known nodes in it. The first packet opens the atlas screen.
 */
public record PacketAuraAtlasToClient(boolean open, int cx, int cz, int radius, List<Cell> cells, List<Node> nodes)
        implements CustomPacketPayload {
    public static final Type<PacketAuraAtlasToClient> TYPE = new Type<>(Thaumcraft.id("aura_atlas_to_client"));
    public static final int RADIUS = 8;

    public record Cell(short base, float vis, float flux) {
        public static final StreamCodec<ByteBuf, Cell> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.SHORT, Cell::base,
                ByteBufCodecs.FLOAT, Cell::vis,
                ByteBufCodecs.FLOAT, Cell::flux,
                Cell::new);
    }

    /** type and modifier are the NodeType and NodeModifier ids (-1 for no modifier). */
    public record Node(BlockPos pos, byte type, byte modifier, AspectList aspects, boolean energized) {
        public static final StreamCodec<ByteBuf, Node> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Node::pos,
                ByteBufCodecs.BYTE, Node::type,
                ByteBufCodecs.BYTE, Node::modifier,
                AspectList.STREAM_CODEC, Node::aspects,
                ByteBufCodecs.BOOL, Node::energized,
                Node::new);
    }

    public static final StreamCodec<ByteBuf, PacketAuraAtlasToClient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketAuraAtlasToClient::open,
            ByteBufCodecs.VAR_INT, PacketAuraAtlasToClient::cx,
            ByteBufCodecs.VAR_INT, PacketAuraAtlasToClient::cz,
            ByteBufCodecs.VAR_INT, PacketAuraAtlasToClient::radius,
            Cell.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), PacketAuraAtlasToClient::cells,
            Node.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), PacketAuraAtlasToClient::nodes,
            PacketAuraAtlasToClient::new);

    /** The atlas around a player: cells row by row, north to south, west to east. */
    public static PacketAuraAtlasToClient around(ServerPlayer player, boolean open) {
        ServerLevel level = player.serverLevel();
        int cx = player.blockPosition().getX() >> 4;
        int cz = player.blockPosition().getZ() >> 4;
        List<Cell> cells = new ArrayList<>();
        for (int z = -RADIUS; z <= RADIUS; ++z) {
            for (int x = -RADIUS; x <= RADIUS; ++x) {
                AuraChunk ac = AuraHandler.getAuraChunk(level.dimension(), cx + x, cz + z);
                cells.add(ac == null ? new Cell((short) -1, 0, 0) : new Cell(ac.getBase(), ac.getVis(), ac.getFlux()));
            }
        }
        List<Node> nodes = new ArrayList<>();
        for (AuraNodeRegistry.Entry e : AuraNodeRegistry.get(level).around(cx, cz, RADIUS)) {
            nodes.add(new Node(e.pos(), (byte) e.type().ordinal(), (byte) (e.modifier() == null ? -1 : e.modifier().ordinal()),
                    e.aspects(), e.energized()));
        }
        return new PacketAuraAtlasToClient(open, cx, cz, RADIUS, cells, nodes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAuraAtlasToClient msg, IPayloadContext context) {
        ClientPacketHandlers.auraAtlas(msg);
    }
}
