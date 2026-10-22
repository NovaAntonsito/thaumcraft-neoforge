package thaumcraft.common.world.dim;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import thaumcraft.Thaumcraft;

/**
 * Dream branch: builds each kind of maze cell with the TC4 generators and saves it as a structure template
 * (world/generated/thaumcraft/structures/maze/*.nbt), to be opened and reworked with structure blocks. The cells are
 * built far out in the level the command runs in, side by side, with all four exits open unless noted. The
 * entities the generators spawn (tablet, page, guardians) are left out.
 */
public final class MazeExport {
    /** Where the cells are built, in chunks. Far from any maze, so world generation never touches it. */
    private static final int ORIGIN = 50000;
    private static final int Y = 50;
    /** From the floor's underside to above the tallest room. */
    private static final int BOTTOM = Y - 2;
    private static final int HEIGHT = 24;

    private record Room(String name, int feature, boolean n, boolean s, boolean e, boolean w) {
    }

    private static final List<Room> ROOMS = List.of(
            new Room("passage_cross", 0, true, true, true, true),
            new Room("passage_straight", 0, true, true, false, false),
            new Room("passage_corner", 0, true, false, true, false),
            new Room("passage_tee", 0, true, true, true, false),
            new Room("passage_dead_end", 0, true, false, false, false),
            new Room("passage_runes", 11, true, true, true, true),
            new Room("passage_crust", 12, true, true, true, true),
            new Room("passage_taint", 13, true, true, true, true),
            new Room("passage_webs", 14, true, true, true, true),
            new Room("portal", 1, true, true, true, true),
            new Room("key_room", 6, true, false, false, false),
            new Room("nest", 7, true, false, false, false),
            new Room("library", 8, true, false, false, false));

    private MazeExport() {
    }

    /** Builds and saves every cell; returns the names saved. */
    public static List<String> exportAll(ServerLevel level) {
        List<String> saved = new ArrayList<>();
        int cx = ORIGIN;
        int cz = ORIGIN;
        for (Room r : ROOMS) {
            Cell cell = new Cell();
            cell.feature = (byte) r.feature;
            cell.north = r.n;
            cell.south = r.s;
            cell.east = r.e;
            cell.west = r.w;
            int x0 = cx;
            build(level, x0, cz, 1, () -> {
                GenWorld world = world(level);
                generate(world, x0, cz, cell);
                GenCommon.processDecorations(world);
            });
            if (save(level, r.name, x0, cz, 1)) saved.add(r.name);
            cx += 3;
        }
        // The boss room: four cells, 2x2, the way in on the north of the upper left one.
        int bx = cx;
        build(level, bx, cz, 2, () -> {
            GenWorld world = world(level);
            for (int f = 2; f <= 5; ++f) {
                Cell cell = new Cell();
                cell.feature = (byte) f;
                cell.north = f == 2;
                int x = bx + ((f == 3 || f == 5) ? 1 : 0);
                int z = cz + ((f == 4 || f == 5) ? 1 : 0);
                GenBossRoom.generateRoom(world, world.rand, x, z, Y, cell);
            }
            GenCommon.processDecorations(world);
        });
        if (save(level, "boss_room", bx, cz, 2)) saved.add("boss_room");
        return saved;
    }

    private static GenWorld world(ServerLevel level) {
        GenWorld world = new GenWorld(level, level.random);
        world.exporting = true;
        return world;
    }

    private static void generate(GenWorld world, int cx, int cz, Cell cell) {
        switch (cell.feature) {
            case 1 -> GenPortal.generatePortal(world, world.rand, cx, cz, Y, cell);
            case 6 -> GenKeyRoom.generateRoom(world, world.rand, cx, cz, Y, cell);
            case 7 -> GenNestRoom.generateRoom(world, world.rand, cx, cz, Y, cell);
            case 8 -> GenLibraryRoom.generateRoom(world, world.rand, cx, cz, Y, cell);
            default -> GenPassage.generateDefaultPassage(world, world.rand, cx, cz, Y, cell);
        }
    }

    /** Loads the chunks around the cells (the generators reach one block past them), clears them and builds. */
    private static void build(ServerLevel level, int cx, int cz, int cells, Runnable gen) {
        for (int x = cx - 1; x <= cx + cells; ++x) {
            for (int z = cz - 1; z <= cz + cells; ++z) {
                level.getChunk(x, z);
            }
        }
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = (cx - 1) * 16; x < (cx + cells + 1) * 16; ++x) {
            for (int z = (cz - 1) * 16; z < (cz + cells + 1) * 16; ++z) {
                for (int y = BOTTOM; y < BOTTOM + HEIGHT; ++y) {
                    level.setBlock(p.set(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        gen.run();
    }

    private static boolean save(ServerLevel level, String name, int cx, int cz, int cells) {
        StructureTemplateManager manager = level.getStructureManager();
        ResourceLocation id = Thaumcraft.id("maze/" + name);
        StructureTemplate template = manager.getOrCreate(id);
        // Air is kept, so a placed room carves its space; structure void is the block left out.
        template.fillFromWorld(level, new BlockPos(cx * 16, BOTTOM, cz * 16), new Vec3i(cells * 16, HEIGHT, cells * 16), false,
                Blocks.STRUCTURE_VOID);
        template.setAuthor("Thaumcraft");
        return manager.save(id);
    }
}
