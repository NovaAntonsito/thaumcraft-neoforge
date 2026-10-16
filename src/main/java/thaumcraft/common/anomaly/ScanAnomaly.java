package thaumcraft.common.anomaly;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.IScanThing;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteManager;

/**
 * Scanning the sky (the thaumometer pointed at nothing) while an anomaly runs learns it: research
 * "!anomaly_&lt;id&gt;" and, the first time, ANOMALIES. A local anomaly has to be scanned within
 * {@link #LOCAL_RANGE} blocks of it. Each one gives aspect points by its kind ({@link #aspectsFor}).
 */
public class ScanAnomaly implements IScanThing {
    public static final String PREFIX = "!anomaly_";
    public static final String BASE_RESEARCH = "ANOMALIES";
    public static final double LOCAL_RANGE = 32.0;
    /** The anomaly each player's scan just named, between getResearchKey and onSuccess (server side). */
    private final Map<UUID, String> pending = new HashMap<>();

    /** The first running anomaly this player can still learn by looking at the sky from here, or null. */
    @Nullable
    private static String target(Player player) {
        if (player.level().isClientSide) {
            for (PacketAnomalySync.Entry e : AnomalyClientState.active) {
                if (learnable(player, e.id(), e.local() ? new BlockPos(e.x(), (int) player.getY(), e.z()) : null)) return e.id();
            }
        } else if (player.level() instanceof ServerLevel sl && AnomalyManager.isEnabledIn(sl)) {
            for (ActiveAnomaly a : AnomalyManager.get(sl).getActive()) {
                BlockPos loc = a.getLocation();
                if (learnable(player, a.id(), loc != null ? new BlockPos(loc.getX(), (int) player.getY(), loc.getZ()) : null)) return a.id();
            }
        }
        return null;
    }

    private static boolean learnable(Player player, String id, @Nullable BlockPos loc) {
        if (ThaumcraftCapabilities.knowsResearch(player, PREFIX + id)) return false;
        return loc == null || player.blockPosition().distSqr(loc) <= LOCAL_RANGE * LOCAL_RANGE;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        return obj == null && target(player) != null;
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        String id = target(player);
        if (id == null) return "";
        if (!player.level().isClientSide) pending.put(player.getUUID(), id);
        return PREFIX + id;
    }

    @Override
    public void onSuccess(Player player, Object obj) {
        if (!(player instanceof ServerPlayer sp)) return;
        String id = pending.remove(sp.getUUID());
        AnomalyType type = id != null ? Anomalies.get(id) : null;
        if (type == null) return;
        AspectList al = aspectsFor(type.polarity);
        for (Aspect aspect : al.getAspects()) {
            ResearchNoteManager.checkAndSyncAspectKnowledge(sp, aspect, al.getAmount(aspect));
        }
        if (!ThaumcraftCapabilities.knowsResearch(sp, BASE_RESEARCH)) ResearchManager.completeResearch(sp, BASE_RESEARCH);
    }

    /** Positive anomalies teach auram, negative ones vitium and alienis, mixed ones a bit of each. */
    public static AspectList aspectsFor(AnomalyType.Polarity polarity) {
        return switch (polarity) {
            case POSITIVE -> new AspectList().add(Aspect.AURA, 6).add(Aspect.MAGIC, 4);
            case NEGATIVE -> new AspectList().add(Aspect.FLUX, 6).add(Aspect.ELDRITCH, 4);
            case MIXED -> new AspectList().add(Aspect.AURA, 4).add(Aspect.FLUX, 4).add(Aspect.MAGIC, 2);
        };
    }
}
