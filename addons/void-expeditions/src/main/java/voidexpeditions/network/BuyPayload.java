package voidexpeditions.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.block.ExpeditionTerminalBlockEntity;
import voidexpeditions.shop.Shop;

/** Client to server: a purchase at a terminal the player is near. {@code perk} is only read for a starting perk. */
public record BuyPayload(BlockPos pos, int offer, String perk) implements CustomPacketPayload {
    public static final Type<BuyPayload> TYPE = new Type<>(VoidExpeditions.id("buy"));
    public static final StreamCodec<ByteBuf, BuyPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BuyPayload::pos,
            ByteBufCodecs.VAR_INT, BuyPayload::offer,
            ByteBufCodecs.STRING_UTF8, BuyPayload::perk,
            BuyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BuyPayload msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        Shop.Offer[] offers = Shop.Offer.values();
        if (msg.offer < 0 || msg.offer >= offers.length || player.distanceToSqr(msg.pos.getCenter()) > 64.0) return;
        if (!(player.level().getBlockEntity(msg.pos) instanceof ExpeditionTerminalBlockEntity)) return;
        Shop.buy(player, offers[msg.offer], msg.perk);
    }
}
