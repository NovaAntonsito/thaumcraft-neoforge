package thaumcraft.api.maze;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** The dimensions made of Outer Lands labyrinths, see {@link IMazeWorld}. Register them during mod construction. */
public final class MazeWorlds {
    /** The first chunk x free for addons in the shared maze map. */
    public static final int ADDON_START = 200000;

    private static final Map<ResourceKey<Level>, IMazeWorld> WORLDS = new ConcurrentHashMap<>();

    private MazeWorlds() {
    }

    public static void register(ResourceKey<Level> dimension, IMazeWorld world) {
        WORLDS.put(dimension, world);
    }

    @Nullable
    public static IMazeWorld get(Level level) {
        return WORLDS.get(level.dimension());
    }

    /** Whether some registered dimension builds the maze cell at this chunk. */
    public static boolean isClaimed(int cx, int cz) {
        for (IMazeWorld w : WORLDS.values()) {
            if (w.ownsChunk(cx, cz)) return true;
        }
        return false;
    }
}
