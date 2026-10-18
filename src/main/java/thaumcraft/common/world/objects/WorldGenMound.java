package thaumcraft.common.world.objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import thaumcraft.common.config.ConfigBlocks;

/**
 * Port of WorldGenMound: a barrow, a grassy mound with a cobblestone crypt inside. A ladder leads down from the
 * stairway on its west side to a lower vault with two loot crates or urns and a dungeon chest (one in three is a
 * trapped chest with TNT under it); the main hall has a skeleton spawner and a zombie spawner.
 * The original was 2400 setBlockState calls; the same blocks are kept here as layers (x rows, z columns), where
 * '.' leaves the world as it was. 1.21: stairs and iron bars store their shape, so it is worked out from the
 * neighbours once everything is placed (1.12 did it when drawing them).
 */
public class WorldGenMound {
    private static final String[][] LAYERS = {
            // y + 0
            {"...................", "...................", "...................", "...................", "...................", "...................", "......ccccccc......",
             "......cmccmmc......", "......ccccccc......", "......ccmcmmc......", "......cmmccmc......", "......ccccmcc......", "......ccccccc......",
             "...................", "...................", "...................", "...................", "...................", "..................."},
            // y + 1
            {"...................", "...................", "...................", "...................", "...................", "...................", "......mccmmcc......",
             "......m__l__c......", "......c_____m......", "......m.___.m......", "......c__.__c......", "......ccccccc......", "......mccmcmm......",
             "...................", "...................", "...................", "...................", "...................", "..................."},
            // y + 2
            {"...................", "...................", "...................", "...................", "...................", "...................", "......ccccccc......",
             "......c__l__c......", "......c_____c......", "......c_____c......", "......c_____c......", "......ccccmcc......", "......ccccccc......",
             "...................", "...................", "...................", "...................", "...................", "..................."},
            // y + 3
            {"...................", "...................", "...................", "...................", "...................", "...................", "......ccmcccc......",
             "......c__l__c......", "......c_____c......", "......c_____m......", "......c_____c......", "......cccmccc......", "......ccccccc......",
             "...................", "...................", "...................", "...................", "...................", "..................."},
            // y + 4
            {"...................", "...................", "...................", "...ccccccccccccc...", "...ccccccccccccc...", "...cccccccmccmcc...", "...ccccBBcBBcccc...",
             "...ccc0__l__Cccc...", "...ccm0_____Cccc...", "...ccc0_____Cccc...", "...cmc0_____Cccc...", "...ccccAAAAAcccc...", "...cccccmccccmcc...",
             "...cccccmccccccc...", "...cccccccccccmc...", "...ccccccccccccc...", "...................", "...................", "..................."},
            // y + 5
            {"...................", "...................", "...................", "...ccccmmccccccc...", "...cs_________sc...", "...c___________c...", "...c__m_____c__m...",
             "...c___________c...", "...c___________c...", "...c___________c...", "...c___________c...", "...c__c_____c__c...", "...c___________c...",
             "...c___________c...", "...ccccc0______c...", "...ccccccccccccc...", "...................", "...................", "..................."},
            // y + 6
            {"...................", "...................", "...................", "...ccccccccccmcc...", "...c___________c...", "...m___________c...", "...c__m_____c__c...",
             "...c___________m...", "...m___________c...", "...c___________c...", "...c___________m...", "...m__m_____c__c...", "...c___________c...",
             "...c___________c...", "...ccmc0_______m...", "...cccccccmccccc...", "...................", "...................", "..................."},
            // y + 7
            {"...................", "...................", "...................", "...ccccccccmcccc...", "...m___________c...", "...c___________c...", "...c__c_____c__c...",
             "...c___________c...", "...c___________c...", "...c___________c...", "...c___________c...", "...c__c_____m__c...", "...c___________c...",
             "...c___________c...", "...ccc0________c...", "...ccccccccmcccc...", "...................", "...................", "..................."},
            // y + 8
            {"....ddddcccdddd....", "...dddddcccddddd...", "..dddddccmccddddd..", ".ddccccmccccccccdd.", "dddcmmcccmmcccccddd", "dddcmccccccccmccddd", "dddccccFFFFFmcccddd",
             "dddcccH_____Gcmcddd", "dddccmH_____Gcccddd", "dddcccH_____Gmccddd", "dddcmcH_____Gcccddd", "dddccccEEEEEccccddd", "dddcmccmmcccccmcddd",
             "dddccccccccmccmcddd", "dddcc0____cmmcccddd", ".ddcccccccccccccdd.", "..ddddddddddddddd..", "...ddddddddddddd...", "....ddddddddddd...."},
            // y + 9
            {"....ggggA_Agggg....", "...gddddc_cddddg...", "..gdddddc_cdddddg..", ".gdcccccciccmcccdg.", "gddc___________cddg", "gddc___________cddg", "gddc__m_____c__cddg",
             "gddc___________cddg", "gddc___________cddg", "dddc___________mddg", "dddc___________cddg", "gddc__c_____m__cddg", "gddc___________cddg",
             "gddc___________cddg", "gddc___________cddg", ".gdccccmccccccccdd.", "..gdddddddddddddg..", "...gdddddddddddg...", "....gggddgggggg...."},
            // y + 10
            {"....tt..___........", "...tggddA_Aggggt...", "...gddddc_mddddg...", "..gcmmcmciccccccg..", ".gdc___________mdg.", ".gdc___________cdg.", ".gdc__c_____m__cdg.",
             ".ddc___________cdd.", ".gdm___________mdd.", "gddm___________mdgt", "gddc___________cdgt", ".ddc__c_____m__mdd.", ".gdc___________cdd.",
             "tgdm___________cdd.", ".gdc__________.cdg.", "..dccmccmcmmccmcdg.", "...ddddddddddddg...", "....gggggggdggg....", ".....t.gg...ttt...."},
            // y + 11
            {"...................", "....ttgg___.ttt....", "...tggddcbmdggg....", "...gdccccmccccdg...", "..gdc_________mdg..", "..dc__E_____E__cg..", "..gc_GcH___GcH_mgt.",
             ".ggm__F_____F__cgg.", "..gc___________cgg.", ".gdc___________cd..", ".ggc__E.____E__cdt.", ".ggm_GcH___GcH_cgg.", "..dc__F_____F__cgg.",
             ".tdc___________cgg.", ".tgdc_________cgg..", "..gddmccccmcccdgg..", "...gddgggggddgg....", ".......t...g.......", "..................."},
            // y + 12
            {"...................", "...................", "......gggggg..t....", "....ggggggggggg....", "...ggcccccccccdg...", "..ggccccccccmccd...", "...gccmccmcccccg...",
             "..tgcccccmcccccg...", "...gcccmcccccccg...", "..ggcccmcccccccgg..", "..tgcccccmccmccgg..", "...gcccccccccccg...", "..ggccmccccmcccg...",
             "..ggcccccccccccg.t.", "...ggcccccccccgtt..", "..tggggggddggggtt..", "...tggttt.tgg......", "...................", "..................."},
            // y + 13
            {"...................", "...................", "...................", ".......tt..........", "...ttgggggggggg....", "..ttgggggggggggg...", "....ggggggggggg....",
             "....gggdddggggg....", "....ggdddddgggg....", "..ttggddddddggg....", "...tgddgddddggg....", "...tggdddddgggg....", "....gggddgdgggg....",
             "....ggggggggggg....", ".....ggggggggg.....", "...tt..t.gg.ttt....", "...................", "...................", "..................."},
            // y + 14
            {"...................", "...................", "...................", "...................", "......tt.tt.ttt....", "...........ttttt...", "............ttt....",
             ".......ggg..t.t....", "......ggggg.ttt....", "....t.gggggg.t.....", ".....gg.ggggt......", "......ggggg.tt.....", ".......gg.gtttt....",
             "...........ttt.....", "...........ttt.....", "...................", "...................", "...................", "..................."},
            // y + 15
            {"...................", "...................", "...................", "...................", "...................", "...................", "...................",
             "...................", "........tt.........", "........ttt........", "......t.ttt........", "........ttt........", "..........t........",
             "...................", "...................", "...................", "...................", "...................", "..................."},
    };

    private static BlockState stairs(Direction facing, Half half) {
        return Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    /** The 1.12 block and metadata of each letter. Stairs: meta & 3 is east, west, south, north; meta & 4 is upside down. */
    private static BlockState block(char c) {
        return switch (c) {
            case '_' -> Blocks.AIR.defaultBlockState();
            case 'c' -> Blocks.COBBLESTONE.defaultBlockState();
            case 'm' -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case 'd' -> Blocks.DIRT.defaultBlockState();
            case 'g' -> Blocks.GRASS_BLOCK.defaultBlockState();
            case 't' -> Blocks.SHORT_GRASS.defaultBlockState();
            case 'i' -> Blocks.IRON_BARS.defaultBlockState();
            case 'l' -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST);
            case 's' -> Blocks.SPAWNER.defaultBlockState();
            case 'b' -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            case '0' -> stairs(Direction.NORTH, Half.BOTTOM);
            case 'A' -> stairs(Direction.EAST, Half.BOTTOM);
            case 'B' -> stairs(Direction.WEST, Half.BOTTOM);
            case 'C' -> stairs(Direction.SOUTH, Half.BOTTOM);
            case 'E' -> stairs(Direction.EAST, Half.TOP);
            case 'F' -> stairs(Direction.WEST, Half.TOP);
            case 'G' -> stairs(Direction.SOUTH, Half.TOP);
            case 'H' -> stairs(Direction.NORTH, Half.TOP);
            default -> null;
        };
    }

    /** GetValidSpawnBlocks: stone, grass and dirt. */
    private static boolean isValidBlock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT);
    }

    /** LocationIsValidSpawn: ground of stone, grass or dirt (or snow or tall grass on it) within 2 blocks, air above. */
    public static boolean locationIsValidSpawn(WorldGenLevel world, BlockPos pos) {
        int distanceToAir;
        for (distanceToAir = 0; !world.isEmptyBlock(pos.above(distanceToAir)) && distanceToAir < 3; ++distanceToAir) {
        }
        if (distanceToAir > 2) {
            return false;
        }
        BlockPos pos2 = pos.above(distanceToAir - 1);
        BlockState blockID = world.getBlockState(pos2);
        BlockState blockIDBelow = world.getBlockState(pos2.below());
        if (!world.isEmptyBlock(pos2.above())) {
            return false;
        }
        if (isValidBlock(blockID)) {
            return true;
        }
        return (blockID.is(Blocks.SNOW) || blockID.is(Blocks.SHORT_GRASS) || blockID.is(Blocks.FERN) || blockID.is(Blocks.DEAD_BUSH))
                && isValidBlock(blockIDBelow);
    }

    /** generate: the four corners and the middle of the top must all be on valid ground. */
    public boolean generate(WorldGenLevel world, RandomSource rand, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (!locationIsValidSpawn(world, new BlockPos(i + 9, j + 9, k + 9)) || !locationIsValidSpawn(world, new BlockPos(i, j + 9, k))
                || !locationIsValidSpawn(world, new BlockPos(i + 18, j + 9, k)) || !locationIsValidSpawn(world, new BlockPos(i + 18, j + 9, k + 18))
                || !locationIsValidSpawn(world, new BlockPos(i, j + 9, k + 18))) {
            return false;
        }
        for (int y = 0; y < LAYERS.length; ++y) {
            for (int x = 0; x < 19; ++x) {
                String row = LAYERS[y][x];
                for (int z = 0; z < 19; ++z) {
                    BlockState state = block(row.charAt(z));
                    if (state != null) {
                        world.setBlock(new BlockPos(i + x, j + y, k + z), state, 2);
                    }
                }
            }
        }
        for (int y = 0; y < LAYERS.length; ++y) {
            for (int x = 0; x < 19; ++x) {
                String row = LAYERS[y][x];
                for (int z = 0; z < 19; ++z) {
                    char c = row.charAt(z);
                    if (c == 'i' || (c >= 'A' && c <= 'H') || c == '0') {
                        BlockPos bp = new BlockPos(i + x, j + y, k + z);
                        BlockState state = world.getBlockState(bp);
                        BlockState shaped = Block.updateFromNeighbourShapes(state, world, bp);
                        if (shaped != state) {
                            world.setBlock(bp, shaped, 2);
                        }
                    }
                }
            }
        }
        generate2(world, rand, i, j, k);
        return true;
    }

    /** generate2: the two loot blocks of the vault, its chest, and what the spawners spawn. */
    public boolean generate2(WorldGenLevel world, RandomSource rand, int i, int j, int k) {
        world.setBlock(new BlockPos(i + 9, j + 1, k + 7), lootBlock(rand).defaultBlockState(), 2);
        world.setBlock(new BlockPos(i + 9, j + 1, k + 11), lootBlock(rand).defaultBlockState(), 2);
        BlockPos chestPos = new BlockPos(i + 10, j + 1, k + 9);
        if (rand.nextInt(3) == 0) {
            world.setBlock(chestPos, Blocks.TRAPPED_CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), 2);
            world.setBlock(new BlockPos(i + 10, j - 1, k + 9), Blocks.TNT.defaultBlockState(), 2);
        } else {
            world.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), 2);
        }
        RandomizableContainer.setBlockEntityLootTable(world, rand, chestPos, BuiltInLootTables.SIMPLE_DUNGEON);
        if (world.getBlockEntity(new BlockPos(i + 4, j + 5, k + 4)) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(EntityType.SKELETON, rand);
        }
        if (world.getBlockEntity(new BlockPos(i + 4, j + 5, k + 14)) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(EntityType.ZOMBIE, rand);
        }
        return true;
    }

    /** 10% rare, 23% uncommon, otherwise common; 30% a crate, otherwise an urn. */
    private static Block lootBlock(RandomSource rand) {
        float rr = rand.nextFloat();
        int md = (rr < 0.1f) ? 2 : ((rr < 0.33f) ? 1 : 0);
        boolean crate = rand.nextFloat() < 0.3f;
        return switch (md) {
            case 1 -> crate ? ConfigBlocks.lootCrateUncommon.get() : ConfigBlocks.lootUrnUncommon.get();
            case 2 -> crate ? ConfigBlocks.lootCrateRare.get() : ConfigBlocks.lootUrnRare.get();
            default -> crate ? ConfigBlocks.lootCrateCommon.get() : ConfigBlocks.lootUrnCommon.get();
        };
    }
}
