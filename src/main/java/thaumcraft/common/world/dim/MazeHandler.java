package thaumcraft.common.world.dim;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

/**
 * Port of TC4's MazeHandler: the cells of every Outer Lands maze, one per chunk, by chunk coordinates. The original
 * kept them in labyrinth.dat in the world folder; here they are the overworld's saved data "thaumcraft_labyrinth"
 * ({@link MazeData}), loaded with the server.
 */
public class MazeHandler {
    public static ConcurrentHashMap<CellLoc, Short> labyrinth = new ConcurrentHashMap<>();
    /** The saved data that writes the map, once the overworld has loaded it. */
    static MazeData data;

    private static void changed() {
        MazeData d = data;
        if (d != null) {
            d.setDirty();
        }
    }

    public static synchronized void putToHashMap(CellLoc key, Cell cell) {
        labyrinth.put(key, cell.pack());
        changed();
    }

    public static synchronized void putToHashMapRaw(CellLoc key, short cell) {
        labyrinth.put(key, cell);
        changed();
    }

    public static synchronized Cell getFromHashMap(CellLoc key) {
        return labyrinth.containsKey(key) ? new Cell(labyrinth.get(key)) : null;
    }

    public static synchronized void removeFromHashMap(CellLoc key) {
        labyrinth.remove(key);
        changed();
    }

    public static synchronized short getFromHashMapRaw(CellLoc key) {
        return labyrinth.containsKey(key) ? labyrinth.get(key) : 0;
    }

    public static synchronized void clearHashMap() {
        labyrinth.clear();
    }

    static void readNBT(CompoundTag nbt) {
        ListTag tagList = nbt.getList("cells", Tag.TAG_COMPOUND);
        for (int a = 0; a < tagList.size(); ++a) {
            CompoundTag cell = tagList.getCompound(a);
            int x = cell.getInt("x");
            int z = cell.getInt("z");
            short v = cell.getShort("cell");
            labyrinth.put(new CellLoc(x, z), v);
        }
    }

    static CompoundTag writeNBT(CompoundTag nbt) {
        ListTag tagList = new ListTag();
        for (Map.Entry<CellLoc, Short> entry : labyrinth.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            short v = entry.getValue();
            if (v <= 0) {
                continue;
            }
            CellLoc loc = entry.getKey();
            CompoundTag cell = new CompoundTag();
            cell.putInt("x", loc.x);
            cell.putInt("z", loc.z);
            cell.putShort("cell", v);
            tagList.add(cell);
        }
        nbt.put("cells", tagList);
        return nbt;
    }

    public static boolean mazesInRange(int chunkX, int chunkZ, int w, int h) {
        for (int x = -w; x <= w; ++x) {
            for (int z = -h; z <= h; ++z) {
                if (getFromHashMap(new CellLoc(chunkX + x, chunkZ + z)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Starts generating a w by h maze around these chunk coordinates on its own thread, as the original did. */
    public static void startMaze(int chunkX, int chunkZ, int w, int h, long seed) {
        Thread t = new Thread(new MazeThread(chunkX, chunkZ, w, h, seed), "Thaumcraft maze");
        t.setDaemon(true);
        t.start();
    }

    public static void generateEldritch(WorldGenLevel level, RandomSource random, int cx, int cz) {
        CellLoc loc = new CellLoc(cx, cz);
        Cell cell = getFromHashMap(loc);
        if (cell != null) {
            GenWorld world = new GenWorld(level, random);
            switch (cell.feature) {
                case 1:
                    GenPortal.generatePortal(world, random, cx, cz, 50, cell);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    GenBossRoom.generateRoom(world, random, cx, cz, 50, cell);
                    break;
                case 6:
                    GenKeyRoom.generateRoom(world, random, cx, cz, 50, cell);
                    break;
                case 7:
                    GenNestRoom.generateRoom(world, random, cx, cz, 50, cell);
                    break;
                case 8:
                    GenLibraryRoom.generateRoom(world, random, cx, cz, 50, cell);
                    break;
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                default:
                    generatePassage(world, random, cx, cz, 50, cell);
            }

            GenCommon.processDecorations(world);
        }
    }

    private static void generatePassage(GenWorld world, RandomSource random, int cx, int cz, int y, Cell cell) {
        random.nextInt(1);
        GenPassage.generateDefaultPassage(world, random, cx, cz, y, cell);
    }
}
