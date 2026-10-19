package thaumcraft.common.world.dim;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** labyrinth.dat as the overworld's saved data: it writes {@link MazeHandler#labyrinth} whenever a cell changes. */
public class MazeData extends SavedData {
    private static final String NAME = "thaumcraft_labyrinth";

    private static MazeData load(CompoundTag tag, HolderLookup.Provider registries) {
        MazeHandler.readNBT(tag);
        return new MazeData();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return MazeHandler.writeNBT(tag);
    }

    /** EventHandlerWorld.worldLoad for dimension 0: MazeHandler.loadMaze. */
    public static void loadMaze(ServerLevel overworld) {
        MazeHandler.clearHashMap();
        MazeHandler.data = overworld.getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(MazeData::new, MazeData::load, null), NAME);
    }

    public static void unloadMaze() {
        MazeHandler.data = null;
        MazeHandler.clearHashMap();
    }
}
