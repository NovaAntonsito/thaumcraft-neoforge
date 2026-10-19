package thaumcraft.common.anomaly;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import thaumcraft.common.entities.EntityVoidRift;
import thaumcraft.common.world.voidrealm.VoidEncounters;

/**
 * Dream branch: the call of the void (mixed, local). The next encounter of the Whispers of the Void: a void rift opens
 * a few hundred blocks from a player who has the event and has not finished it, and the anomaly's marker leads there.
 * It can only be drawn in the overworld while such a player is in it. If nobody has gone in when it runs out, the
 * rift fades; once someone has, it stays until the encounter's page is read.
 */
public class AnomalyVoidCalling extends AnomalyType {
    /** The player whose first encounter starts it on waking; otherwise one is drawn. */
    @Nullable
    static ServerPlayer forced;

    public AnomalyVoidCalling() {
        super("void_calling", Polarity.MIXED);
        weight(6).durationMinutes(20, 30);
    }

    /** Starts the first encounter for a player who just woke up. False if the anomaly could not start. */
    public static boolean startFor(ServerLevel level, ServerPlayer player) {
        if (!AnomalyManager.isEnabledIn(level)) return false;
        AnomalyManager m = AnomalyManager.get(level);
        if (m.isActive(Anomalies.VOID_CALLING.id)) return false;
        forced = player;
        try {
            m.start(level, Anomalies.VOID_CALLING, level.getGameTime(), -1);
        } finally {
            forced = null;
        }
        m.sync(level);
        return m.isActive(Anomalies.VOID_CALLING.id);
    }

    private static List<ServerPlayer> eligible(ServerLevel level) {
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : level.players()) if (VoidEncounters.eligible(p)) out.add(p);
        return out;
    }

    @Override
    public boolean canStartIn(ServerLevel level) {
        return level.dimension() == Level.OVERWORLD && !eligible(level).isEmpty();
    }

    @Override
    public void onStart(ServerLevel level, ActiveAnomaly anomaly) {
        ServerPlayer target = forced;
        if (target == null) {
            List<ServerPlayer> players = eligible(level);
            if (players.isEmpty()) return;
            target = players.get(level.random.nextInt(players.size()));
        }
        EntityVoidRift rift = VoidEncounters.openRift(level, target);
        if (rift == null) return;
        anomaly.data().putUUID("rift", rift.getUUID());
        anomaly.data().putUUID("owner", target.getUUID());
        anomaly.setLocation(rift.blockPosition());
        target.sendSystemMessage(Component.translatable("tc.void.call").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    @Nullable
    private static EntityVoidRift getRift(ServerLevel level, ActiveAnomaly anomaly) {
        if (!anomaly.data().hasUUID("rift")) return null;
        Entity e = level.getEntity(anomaly.data().getUUID("rift"));
        return e instanceof EntityVoidRift r && r.isAlive() ? r : null;
    }

    @Override
    public boolean isFinished(ServerLevel level, ActiveAnomaly anomaly) {
        if (anomaly.getLocation() == null) return true;
        if (!level.isPositionEntityTicking(anomaly.getLocation())) return false;
        return getRift(level, anomaly) == null;
    }

    @Override
    public void onEnd(ServerLevel level, ActiveAnomaly anomaly) {
        EntityVoidRift rift = getRift(level, anomaly);
        if (rift != null && !rift.wasEntered()) {
            rift.discard();
            if (anomaly.data().hasUUID("owner")) {
                ServerPlayer owner = level.getServer().getPlayerList().getPlayer(anomaly.data().getUUID("owner"));
                if (owner != null) {
                    VoidEncounters.forgetRift(owner);
                    owner.sendSystemMessage(Component.translatable("tc.void.fade").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
            }
        }
    }
}
