package thaumcraft.common.world.dim;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;

/**
 * The Outer Lands dimension (TC4's WorldProviderOuter, Config.dimensionOuterId). It is a data pack dimension:
 * data/thaumcraft/dimension/outer_lands.json and dimension_type/outer_lands.json.
 */
public final class OuterLands {
    public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION, Thaumcraft.id("outer_lands"));

    private OuterLands() {
    }

    public static boolean is(Level world) {
        return world.dimension() == KEY;
    }

    /** The Outer Lands, or the void of the dream branch, whose labyrinths are Outer Lands mazes and work the same. */
    public static boolean isMazeWorld(Level world) {
        return is(world) || thaumcraft.common.world.voidrealm.VoidDim.is(world);
    }
}
