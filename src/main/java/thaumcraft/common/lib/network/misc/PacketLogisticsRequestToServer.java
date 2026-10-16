package thaumcraft.common.lib.network.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.common.container.ContainerLogistics;

/**
 * The logistics screen asking for stacksize of an item, delivered to pos/side or (with no pos) to the player.
 * Split in stacks of at most the item's max size, as in 1.12.
 */
public record PacketLogisticsRequestToServer(BlockPos pos, Direction side, ItemStack stack, int stacksize) implements CustomPacketPayload {
    public static final Type<PacketLogisticsRequestToServer> TYPE = new Type<>(Thaumcraft.id("logistics_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketLogisticsRequestToServer> STREAM_CODEC = StreamCodec.of(
            (buffer, msg) -> {
                if (msg.pos == null || msg.side == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writeLong(msg.pos.asLong());
                    buffer.writeByte(msg.side.ordinal());
                }
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, msg.stack);
                buffer.writeInt(msg.stacksize);
            },
            buffer -> {
                BlockPos pos = null;
                Direction side = null;
                if (buffer.readBoolean()) {
                    pos = BlockPos.of(buffer.readLong());
                    side = Direction.values()[buffer.readByte()];
                }
                ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                return new PacketLogisticsRequestToServer(pos, side, stack, buffer.readInt());
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketLogisticsRequestToServer message, IPayloadContext context) {
        // 1.21: only from the open logistics screen, and no more requests than the request list can hold
        if (!(context.player() instanceof ServerPlayer player) || !(player.containerMenu instanceof ContainerLogistics)
                || message.stack.isEmpty()) {
            return;
        }
        Level world = player.level();
        int stacksize = message.stacksize;
        int ui = 0;
        while (stacksize > 0 && ui < 1000) {
            ItemStack s = message.stack.copy();
            s.setCount(Math.min(stacksize, s.getMaxStackSize()));
            stacksize -= s.getCount();
            if (message.pos != null) {
                GolemHelper.requestProvisioning(world, message.pos, message.side, s, ui);
            } else {
                GolemHelper.requestProvisioning(world, player, s, ui);
            }
            ++ui;
        }
    }
}
