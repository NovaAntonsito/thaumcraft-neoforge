package thaumcraft.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import thaumcraft.common.aspects.ObjectAspects;
import thaumcraft.common.aspects.SyncObjectAspectsPayload;
import thaumcraft.common.lib.network.fx.PacketFXBlockArc;
import thaumcraft.common.lib.network.fx.PacketFXBlockMist;
import thaumcraft.common.lib.network.fx.PacketFXEssentiaSource;
import thaumcraft.common.lib.network.fx.PacketFXInfusionSource;
import thaumcraft.common.lib.network.fx.PacketFXScanSource;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.lib.network.playerdata.PacketAspectDiscovery;
import thaumcraft.common.lib.network.playerdata.PacketAspectDiscoveryError;
import thaumcraft.common.lib.network.misc.PacketAuraToClient;
import thaumcraft.common.lib.network.playerdata.PacketGivePlayerNoteToServer;
import thaumcraft.common.lib.network.misc.PacketAspectPlaceToServer;
import thaumcraft.common.lib.network.misc.PacketAspectCombinationToServer;
import thaumcraft.common.lib.network.misc.PacketCopyPlayerNoteToServer;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.common.lib.network.playerdata.PacketSyncProgressToServer;
import thaumcraft.common.lib.network.playerdata.PacketSyncResearchFlagsToServer;
import thaumcraft.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer;

/** Packet registration. Replaces the SimpleNetworkWrapper in PacketHandler. */
public final class TCNetwork {
    private TCNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(SyncObjectAspectsPayload.TYPE, SyncObjectAspectsPayload.STREAM_CODEC, SyncObjectAspectsPayload::handle)
                .playToClient(PacketFXEssentiaSource.TYPE, PacketFXEssentiaSource.STREAM_CODEC, PacketFXEssentiaSource::handle)
                .playToClient(PacketFXInfusionSource.TYPE, PacketFXInfusionSource.STREAM_CODEC, PacketFXInfusionSource::handle)
                .playToClient(PacketFXBlockArc.TYPE, PacketFXBlockArc.STREAM_CODEC, PacketFXBlockArc::handle)
                .playToClient(PacketFXBlockMist.TYPE, PacketFXBlockMist.STREAM_CODEC, PacketFXBlockMist::handle)
                .playToClient(PacketFXScanSource.TYPE, PacketFXScanSource.STREAM_CODEC, PacketFXScanSource::handle)
                .playToClient(PacketSyncKnowledge.TYPE, PacketSyncKnowledge.STREAM_CODEC, PacketSyncKnowledge::handle)
                .playToServer(PacketSyncProgressToServer.TYPE, PacketSyncProgressToServer.STREAM_CODEC, PacketSyncProgressToServer::handle)
                .playToClient(PacketAspectPool.TYPE, PacketAspectPool.STREAM_CODEC, PacketAspectPool::handle)
                .playToClient(PacketAspectDiscovery.TYPE, PacketAspectDiscovery.STREAM_CODEC, PacketAspectDiscovery::handle)
                .playToClient(PacketAspectDiscoveryError.TYPE, PacketAspectDiscoveryError.STREAM_CODEC, PacketAspectDiscoveryError::handle)
                .playToClient(PacketAuraToClient.TYPE, PacketAuraToClient.STREAM_CODEC, PacketAuraToClient::handle)
                .playToClient(thaumcraft.common.anomaly.PacketAnomalySync.TYPE, thaumcraft.common.anomaly.PacketAnomalySync.STREAM_CODEC,
                        thaumcraft.common.anomaly.PacketAnomalySync::handle)
                .playToServer(PacketGivePlayerNoteToServer.TYPE, PacketGivePlayerNoteToServer.STREAM_CODEC, PacketGivePlayerNoteToServer::handle)
                .playToServer(PacketAspectPlaceToServer.TYPE, PacketAspectPlaceToServer.STREAM_CODEC, PacketAspectPlaceToServer::handle)
                .playToServer(PacketAspectCombinationToServer.TYPE, PacketAspectCombinationToServer.STREAM_CODEC, PacketAspectCombinationToServer::handle)
                .playToServer(PacketCopyPlayerNoteToServer.TYPE, PacketCopyPlayerNoteToServer.STREAM_CODEC, PacketCopyPlayerNoteToServer::handle)
                .playToServer(PacketSyncResearchFlagsToServer.TYPE, PacketSyncResearchFlagsToServer.STREAM_CODEC, PacketSyncResearchFlagsToServer::handle)
                .playToServer(thaumcraft.common.lib.network.misc.PacketGolemBuilderToServer.TYPE, thaumcraft.common.lib.network.misc.PacketGolemBuilderToServer.STREAM_CODEC,
                        thaumcraft.common.lib.network.misc.PacketGolemBuilderToServer::handle)
                .playToClient(thaumcraft.common.lib.network.misc.PacketGolemBuilderStuffToClient.TYPE, thaumcraft.common.lib.network.misc.PacketGolemBuilderStuffToClient.STREAM_CODEC,
                        thaumcraft.common.lib.network.misc.PacketGolemBuilderStuffToClient::handle)
                .playToClient(thaumcraft.common.lib.network.misc.PacketSealToClient.TYPE, thaumcraft.common.lib.network.misc.PacketSealToClient.STREAM_CODEC,
                        thaumcraft.common.lib.network.misc.PacketSealToClient::handle)
                .playToServer(PacketSelectThaumotoriumRecipeToServer.TYPE, PacketSelectThaumotoriumRecipeToServer.STREAM_CODEC,
                        PacketSelectThaumotoriumRecipeToServer::handle)
                .playToClient(thaumcraft.common.lib.network.fx.PacketFXFocusEffect.TYPE, thaumcraft.common.lib.network.fx.PacketFXFocusEffect.STREAM_CODEC,
                        thaumcraft.common.lib.network.fx.PacketFXFocusEffect::handle)
                .playToClient(thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact.TYPE, thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact.STREAM_CODEC,
                        thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact::handle)
                .playToClient(thaumcraft.common.lib.network.fx.PacketFXFocusPartImpactBurst.TYPE, thaumcraft.common.lib.network.fx.PacketFXFocusPartImpactBurst.STREAM_CODEC,
                        thaumcraft.common.lib.network.fx.PacketFXFocusPartImpactBurst::handle)
                .playToClient(thaumcraft.common.lib.network.fx.PacketFXZap.TYPE, thaumcraft.common.lib.network.fx.PacketFXZap.STREAM_CODEC,
                        thaumcraft.common.lib.network.fx.PacketFXZap::handle)
                .playToServer(thaumcraft.common.lib.network.misc.PacketItemKeyToServer.TYPE, thaumcraft.common.lib.network.misc.PacketItemKeyToServer.STREAM_CODEC,
                        thaumcraft.common.lib.network.misc.PacketItemKeyToServer::handle)
                .playToServer(thaumcraft.common.lib.network.misc.PacketFocusChangeToServer.TYPE, thaumcraft.common.lib.network.misc.PacketFocusChangeToServer.STREAM_CODEC,
                        thaumcraft.common.lib.network.misc.PacketFocusChangeToServer::handle)
                .playToServer(thaumcraft.common.lib.network.playerdata.PacketFocusNodesToServer.TYPE, thaumcraft.common.lib.network.playerdata.PacketFocusNodesToServer.STREAM_CODEC,
                        thaumcraft.common.lib.network.playerdata.PacketFocusNodesToServer::handle)
                .playToServer(thaumcraft.common.lib.network.playerdata.PacketFocusNameToServer.TYPE, thaumcraft.common.lib.network.playerdata.PacketFocusNameToServer.STREAM_CODEC,
                        thaumcraft.common.lib.network.playerdata.PacketFocusNameToServer::handle);
    }

    /** Sends a packet to every player within range of a point. Replaces sendToAllAround with a TargetPoint. */
    public static void sendToAllAround(net.minecraft.world.level.Level world, double x, double y, double z, double range,
                                       net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (world instanceof net.minecraft.server.level.ServerLevel sl) {
            PacketDistributor.sendToPlayersNear(sl, null, x, y, z, range, payload);
        }
    }

    public static void syncObjectAspects(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncObjectAspectsPayload(ObjectAspects.getEntries()));
    }
}
