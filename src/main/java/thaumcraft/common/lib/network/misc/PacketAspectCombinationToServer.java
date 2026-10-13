package thaumcraft.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteManager;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/**
 * Reborn's PacketAspectCombinationToServer: combine two aspects at a research table. One point of each is spent
 * (from the table's bonus when the player has none), and the result gains a point, or three if it is new.
 */
public record PacketAspectCombinationToServer(BlockPos pos, String aspect1, String aspect2) implements CustomPacketPayload {
    public static final Type<PacketAspectCombinationToServer> TYPE = new Type<>(Thaumcraft.id("aspect_combination"));
    public static final StreamCodec<ByteBuf, PacketAspectCombinationToServer> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketAspectCombinationToServer::pos,
            ByteBufCodecs.STRING_UTF8, PacketAspectCombinationToServer::aspect1,
            ByteBufCodecs.STRING_UTF8, PacketAspectCombinationToServer::aspect2,
            PacketAspectCombinationToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAspectCombinationToServer msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        Aspect a1 = Aspect.getAspect(msg.aspect1);
        Aspect a2 = Aspect.getAspect(msg.aspect2);
        if (a1 == null || a2 == null || player.distanceToSqr(msg.pos.getCenter()) > 64.0) return;
        if (!(player.level().getBlockEntity(msg.pos) instanceof TileResearchTable table)) return;
        PlayerAspects aspects = ThaumcraftCapabilities.getAspects(player);
        boolean ab1 = table.bonusAspects.getAmount(a1) > 0;
        boolean ab2 = table.bonusAspects.getAmount(a2) > 0;
        if ((aspects.aspectCount(a1) > 0 || ab1) && (aspects.aspectCount(a2) > 0 || ab2)) {
            spend(player, aspects, table, a1);
            spend(player, aspects, table, a2);
            Aspect combo = ResearchManager.getCombinationResult(a1, a2);
            if (combo != null) ResearchNoteManager.checkAndSyncAspectKnowledge(player, combo, 1);
        }
    }

    private static void spend(ServerPlayer player, PlayerAspects aspects, TileResearchTable table, Aspect aspect) {
        if (aspects.aspectCount(aspect) <= 0) {
            table.bonusAspects.remove(aspect, 1);
            table.setChanged();
            table.syncTile(false);
        } else {
            ResearchNoteManager.spendAspect(player, aspect, 1);
        }
    }
}
