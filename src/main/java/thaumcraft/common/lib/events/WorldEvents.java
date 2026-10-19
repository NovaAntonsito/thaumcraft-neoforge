package thaumcraft.common.lib.events;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.tiles.devices.TileArcaneEar;
import thaumcraft.common.world.dim.MazeData;

/**
 * Partial port of WorldEvents (noteEvent, worldUnload), TC4's EventHandlerWorld maze load and of the ServerEvents world tick: note blocks played this tick are kept
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

    /**
     * The seals of an unloaded world are dropped, so they do not leak into the next save. The tasks are dropped too,
     * which the original did not do.
     */
    /** EventHandlerWorld.worldLoad: the overworld loads the Outer Lands mazes. */
    @SubscribeEvent
    public static void worldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            MazeData.loadMaze(level);
        }
    }

    @SubscribeEvent
    public static void worldUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }
        if (level.dimension() == Level.OVERWORLD) {
            MazeData.unloadMaze();
        }
        SealHandler.sealEntities.remove(level.dimension());
        TaskHandler.tasks.remove(level.dimension());
        thaumcraft.api.golems.GolemHelper.provisionRequests.remove(level.dimension());
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
