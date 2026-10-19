package thaumcraft.common.lib.events;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.tiles.devices.TileArcaneEar;
import thaumcraft.common.entities.monster.boss.EntityThaumcraftBoss;
import thaumcraft.common.world.dim.Cell;
import thaumcraft.common.world.dim.CellLoc;
import thaumcraft.common.world.dim.MazeData;
import thaumcraft.common.world.dim.MazeHandler;
import thaumcraft.common.world.dim.OuterLands;

/**
 * Partial port of WorldEvents (noteEvent, worldUnload), TC4's EventHandlerWorld maze load and of the ServerEvents world tick: note blocks played this tick are kept
 * for the arcane ears and forgotten at the end of the tick.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class WorldEvents {
    private WorldEvents() {
    }

    /**
     * EventHandlerWorld.placeBlockEvent: in the Outer Lands, outside creative, nothing can be placed in a boss room
     * while a boss is within 32 blocks. EntityMultiPlaceEvent is an EntityPlaceEvent, so this covers both.
     */
    @SubscribeEvent
    public static void placeBlockEvent(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && isNearActiveBoss(level, event.getEntity() instanceof Player p ? p : null, event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static boolean isNearActiveBoss(Level world, Player player, BlockPos pos) {
        if (OuterLands.is(world) && player != null && !player.getAbilities().instabuild) {
            Cell c = MazeHandler.getFromHashMap(new CellLoc(pos.getX() >> 4, pos.getZ() >> 4));
            if (c != null && c.feature >= 2 && c.feature <= 5) {
                List<EntityThaumcraftBoss> list = thaumcraft.common.lib.utils.EntityUtils.getEntitiesInRange(world, pos.getX(), pos.getY(), pos.getZ(),
                        null, EntityThaumcraftBoss.class, 32.0);
                return !list.isEmpty();
            }
        }
        return false;
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
