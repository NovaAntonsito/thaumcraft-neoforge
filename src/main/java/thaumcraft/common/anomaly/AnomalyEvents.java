package thaumcraft.common.anomaly;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Runs the anomaly clock and keeps the HUD of each player in sync with their dimension. */
public final class AnomalyEvents {
    private AnomalyEvents() {
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && AnomalyManager.isEnabledIn(level)) {
            AnomalyManager.get(level).tick(level);
        }
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) AnomalyManager.syncTo(sp);
    }

    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) AnomalyManager.syncTo(sp);
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) AnomalyManager.syncTo(sp);
    }
}
