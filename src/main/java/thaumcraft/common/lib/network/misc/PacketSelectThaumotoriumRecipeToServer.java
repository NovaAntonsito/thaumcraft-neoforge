package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.crafting.TileThaumatorium;

/** Port of PacketSelectThaumotoriumRecipeToServer: a click on a recipe in the thaumatorium. The recipe id replaces its hash. */
public record PacketSelectThaumotoriumRecipeToServer(BlockPos pos, ResourceLocation recipe) implements CustomPacketPayload {
    public static final Type<PacketSelectThaumotoriumRecipeToServer> TYPE = new Type<>(Thaumcraft.id("select_thaumatorium_recipe"));
    public static final StreamCodec<ByteBuf, PacketSelectThaumotoriumRecipeToServer> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketSelectThaumotoriumRecipeToServer::pos,
            ResourceLocation.STREAM_CODEC, PacketSelectThaumotoriumRecipeToServer::recipe,
            PacketSelectThaumotoriumRecipeToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSelectThaumotoriumRecipeToServer msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.distanceToSqr(msg.pos.getCenter()) > 64.0) return;
        if (player.level().getBlockEntity(msg.pos) instanceof TileThaumatorium thaumatorium) {
            thaumatorium.selectRecipe(player, msg.recipe);
        }
    }
}
