package thaumcraft.common.world.voidrealm;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;

/**
 * Dream branch: the void, a dimension only reached through the void rifts of the Whispers of the Void
 * (data/thaumcraft/dimension/void.json). Its labyrinths are TC4 Outer Lands mazes, kept far from the Outer Lands
 * ones in the shared maze map: each sits at chunk x {@link #OFFSET} + slot × {@link #SPACING}, and its z tells the
 * encounter (encounter × SPACING), so world generation knows which journal page goes in it.
 */
public final class VoidDim {
    public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION, Thaumcraft.id("void"));
    /** First chunk x of the void labyrinths. Outer Lands mazes stay below it, void ones above. */
    public static final int OFFSET = 100000;
    public static final int SPACING = 40;
    public static final int ENCOUNTERS = 5;

    private VoidDim() {
    }

    public static boolean is(Level level) {
        return level.dimension() == KEY;
    }

    /** True for the chunks of the void labyrinths. */
    public static boolean isVoidChunk(int cx) {
        return cx >= OFFSET - SPACING / 2;
    }

    /** The encounter (1 to 5) of the labyrinth a chunk belongs to. */
    public static int encounterAt(int cz) {
        return Math.max(1, Math.min(ENCOUNTERS, Math.round(cz / (float) SPACING)));
    }

    /** The size in cells of each encounter's labyrinth, growing toward the last one. */
    public static int mazeSize(int encounter) {
        return switch (encounter) {
            case 1 -> 9;
            case 2, 3 -> 11;
            case 4 -> 13;
            default -> 15;
        };
    }
}
