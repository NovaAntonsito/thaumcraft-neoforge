package thaumcraft.common.lib.network.playerdata;

import java.util.HashMap;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.crafting.FocusElementNode;
import thaumcraft.common.tiles.crafting.TileFocalManipulator;

/** Port of PacketFocusNodesToServer: the node grid and focus name built in the focal manipulator GUI. */
public record PacketFocusNodesToServer(BlockPos pos, List<CompoundTag> nodes, String name) implements CustomPacketPayload {
    public static final Type<PacketFocusNodesToServer> TYPE = new Type<>(Thaumcraft.id("focus_nodes"));
    public static final StreamCodec<ByteBuf, PacketFocusNodesToServer> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketFocusNodesToServer::pos,
            ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list(256)), PacketFocusNodesToServer::nodes,
            ByteBufCodecs.stringUtf8(256), PacketFocusNodesToServer::name,
            PacketFocusNodesToServer::new);

    public PacketFocusNodesToServer(BlockPos pos, HashMap<Integer, FocusElementNode> data, String name) {
        this(pos, data.values().stream().map(FocusElementNode::serialize).toList(), name);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFocusNodesToServer message, IPayloadContext context) {
        Player player = context.player();
        if (player.distanceToSqr(message.pos.getCenter()) > 64.0 || !player.level().isLoaded(message.pos)) {
            return;
        }
        if (player.level().getBlockEntity(message.pos) instanceof TileFocalManipulator rt) {
            HashMap<Integer, FocusElementNode> data = new HashMap<>();
            for (CompoundTag t : message.nodes) {
                FocusElementNode node = new FocusElementNode();
                node.deserialize(t);
                data.put(node.id, node);
            }
            rt.data.clear();
            rt.data = data;
            rt.focusName = message.name;
            rt.setChanged();
        }
    }
}
