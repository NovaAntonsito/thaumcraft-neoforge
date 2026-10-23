package thaumcraft.api.maze;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

/**
 * A dimension built from Outer Lands labyrinths besides the Outer Lands themselves, such as the void of the Whispers
 * of the Void or an addon's. All of them share one maze map, so each keeps to its own band of chunk x: Thaumcraft's
 * mazes stay below {@link MazeWorlds#ADDON_START}, and addons pick theirs above it.
 * <p>
 * The dimension reuses the maze generation by placing the {@code thaumcraft:maze} feature in its biome (the
 * {@code thaumcraft:void} biome and dimension type already do), and it gets the Outer Lands rules: no flying, more
 * champions and no building near the boss.
 */
public interface IMazeWorld {

    /** Whether the maze cell at this chunk is one of this dimension's, so it builds it and no other dimension does. */
    boolean ownsChunk(int cx, int cz);

    /**
     * The key room of a labyrinth was built, its tablet and guardians aside: a place for chests, pages and the like.
     * {@code center} is the floor of the room's middle.
     */
    default void decorateKeyRoom(WorldGenLevel level, RandomSource random, BlockPos center, int cx, int cz) {
    }

    /** How many eldritch guardians watch a key room, from Thaumcraft's count by difficulty. */
    default int keyRoomGuardians(int base, int cx, int cz) {
        return base;
    }

    /** Whether the guardian number {@code index} of a key room is a champion, besides the first of four or more. */
    default boolean keyRoomChampion(int index, int cx, int cz) {
        return false;
    }

    /** A player steps into the portal at the middle of a labyrinth. */
    void onPortal(ServerPlayer player, ServerLevel level, BlockPos portal);
}
