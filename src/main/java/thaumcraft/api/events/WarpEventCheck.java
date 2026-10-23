package thaumcraft.api.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Fired on the NeoForge bus each time a player's warp may set off an event, before the roll. Cancelling it skips
 * this check entirely: no event, and the counter and temporary warp stay as they are.
 */
public class WarpEventCheck extends PlayerEvent implements ICancellableEvent {
    public WarpEventCheck(Player player) {
        super(player);
    }
}
