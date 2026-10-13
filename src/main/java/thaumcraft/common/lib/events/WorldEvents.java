package thaumcraft.common.lib.events;

import java.util.List;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.common.tiles.devices.TileArcaneEar;

/**
 * Partial port of WorldEvents (noteEvent) and of the ServerEvents world tick: note blocks played this tick are kept
 * for the arcane ears and forgotten at the end of the tick.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class WorldEvents {
    private WorldEvents() {
    }

    @SubscribeEvent
    public static void noteEvent(NoteBlockEvent.Play event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }
        TileArcaneEar.addNoteEvent(level.dimension(), event.getPos(), event.getInstrument().ordinal(), event.getVanillaNoteId());
    }

    @SubscribeEvent
    public static void worldTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        List<int[]> nbe = TileArcaneEar.noteBlockEvents.get(event.getLevel().dimension());
        if (nbe != null) {
            nbe.clear();
        }
    }
}
