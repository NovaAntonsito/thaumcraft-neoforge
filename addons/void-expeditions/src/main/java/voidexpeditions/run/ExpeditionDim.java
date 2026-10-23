package voidexpeditions.run;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import thaumcraft.api.maze.MazeWorlds;
import voidexpeditions.VoidExpeditions;

/**
 * The dimension of the expeditions (data/voidexpeditions/dimension/expedition.json), the void's type and biome under
 * another name so its labyrinths are its own. Each labyrinth sits at chunk x {@link #START} + slot × {@link #SPACING},
 * and its z tells the run's difficulty and the encounter: (difficulty × 8 + encounter) × SPACING.
 */
public final class ExpeditionDim {
    public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION, VoidExpeditions.id("expedition"));
    public static final int START = MazeWorlds.ADDON_START;
    public static final int SPACING = 40;
    /** The band of chunk x of the labyrinths, so other addons can take the next one. */
    public static final int BAND = 1000000;

    private ExpeditionDim() {
    }

    public static boolean is(Level level) {
        return level.dimension() == KEY;
    }

    public static boolean ownsChunk(int cx) {
        return cx >= START - SPACING / 2 && cx < START + BAND;
    }

    public static int mazeZ(Difficulty difficulty, int encounter) {
        return (difficulty.ordinal() * 8 + encounter) * SPACING;
    }

    public static Difficulty difficultyAt(int cz) {
        return Difficulty.byIndex(Math.round(cz / (float) SPACING) / 8);
    }

    public static int encounterAt(int cz) {
        return Math.max(1, Math.round(cz / (float) SPACING) % 8);
    }

    /** The size in cells of an encounter's labyrinth: 9 for the first, growing to 15. */
    public static int mazeSize(int encounter) {
        return Math.min(15, 9 + 2 * (encounter / 2));
    }
}
