package thaumcraft.common.anomaly;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.common.config.ConfigItems;
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

    /** Salis mundus used in the air near a giant rift shrinks it (see {@link AnomalyGiantRift#tryShrink}). */
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ConfigItems.salisMundus.get())) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (AnomalyGiantRift.tryShrink(level, event.getEntity())) {
            if (!event.getEntity().getAbilities().instabuild) stack.shrink(1);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
