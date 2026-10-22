package thaumcraft.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.Config;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.blocks.world.ore.BlockCrystal;
import thaumcraft.common.blocks.world.ore.ShardType;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.world.biomes.BiomeHandler;
import thaumcraft.common.world.objects.WorldGenCustomFlowers;
import thaumcraft.common.world.objects.WorldGenGreatwoodTrees;
import thaumcraft.common.world.objects.WorldGenSilverwoodTrees;

/**
 * Port of ThaumcraftWorldGenerator. The per-chunk code is the original's; it runs from two features (see
 * {@link thaumcraft.common.registry.TCFeatures}) instead of an IWorldGenerator.
 * <ul>
 *   <li>The dimension and biome blacklists are the biome modifiers' biome lists (data/thaumcraft/neoforge/biome_modifier).</li>
 *   <li>The regen options are not ported: chunks that already exist are not generated again.</li>
 *   <li>Heights use the original's absolute values (cinnabar below y 51, quartz below y 64), as 1.12 worlds started at 0.</li>
 *   <li>Aura generation stays in ChunkEvents (stage 2). Mounds come with the bosses (stage 9d-4).</li>
 * </ul>
 */
public final class ThaumcraftWorldGenerator {
    /** world.getHeight() of a 1.12 overworld. */
    private static final int WORLD_HEIGHT = 256;

    private ThaumcraftWorldGenerator() {
    }

    /** BlockMatcher.forBlock(Blocks.STONE): in 1.12 that block was also granite, diorite and andesite. */
    private static boolean isReplaceableOreGen(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES);
    }

    private static boolean isFlat(FeaturePlaceContext<?> context) {
        return context.chunkGenerator() instanceof FlatLevelSource;
    }

    public static void generateVegetation(WorldGenLevel world, RandomSource random, int chunkX, int chunkZ) {
        if (random.nextInt(80) == 3) {
            generateSilverwood(world, random, chunkX, chunkZ);
        }
        if (random.nextInt(25) == 7) {
            generateGreatwood(world, random, chunkX, chunkZ);
        }
        int randPosX = chunkX * 16 + 8;
        int randPosZ = chunkZ * 16 + 8;
        BlockPos bp = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(randPosX, 0, randPosZ));
        // topBlock == SAND && temperature > 1: the deserts and the badlands.
        Holder<Biome> biome = world.getBiome(bp);
        if ((biome.is(Tags.Biomes.IS_DESERT) || biome.is(BiomeTags.IS_BADLANDS)) && biome.value().getBaseTemperature() > 1.0f
                && random.nextInt(30) == 0) {
            generateFlowers(world, random, bp, ConfigBlocks.cinderpearl.get());
        }
    }

    public static void generateOres(WorldGenLevel world, RandomSource random, int chunkX, int chunkZ) {
        float density = Config.ORE_DENSITY.get() / 100.0f;
        if (Config.GENERATE_CINNABAR.get()) {
            for (int i = 0; i < Math.round(18.0f * density); ++i) {
                int randPosX = chunkX * 16 + 8 + Mth.nextInt(random, -6, 6);
                int randPosY = random.nextInt(WORLD_HEIGHT / 5);
                int randPosZ = chunkZ * 16 + 8 + Mth.nextInt(random, -6, 6);
                BlockPos pos = new BlockPos(randPosX, randPosY, randPosZ);
                if (isReplaceableOreGen(world.getBlockState(pos))) {
                    world.setBlock(pos, ConfigBlocks.oreCinnabar.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        if (Config.GENERATE_QUARTZ.get()) {
            for (int i = 0; i < Math.round(18.0f * density); ++i) {
                int randPosX = chunkX * 16 + 8 + Mth.nextInt(random, -6, 6);
                int randPosY = random.nextInt(WORLD_HEIGHT / 4);
                int randPosZ = chunkZ * 16 + 8 + Mth.nextInt(random, -6, 6);
                BlockPos pos = new BlockPos(randPosX, randPosY, randPosZ);
                if (isReplaceableOreGen(world.getBlockState(pos))) {
                    world.setBlock(pos, ConfigBlocks.oreQuartz.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        if (Config.GENERATE_AMBER.get()) {
            for (int i = 0; i < Math.round(20.0f * density); ++i) {
                int randPosX = chunkX * 16 + 8 + Mth.nextInt(random, -6, 6);
                int randPosZ2 = chunkZ * 16 + 8 + Mth.nextInt(random, -6, 6);
                int randPosY2 = world.getHeight(Heightmap.Types.MOTION_BLOCKING, randPosX, randPosZ2) - random.nextInt(25);
                BlockPos pos = new BlockPos(randPosX, randPosY2, randPosZ2);
                if (isReplaceableOreGen(world.getBlockState(pos))) {
                    world.setBlock(pos, ConfigBlocks.oreAmber.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        if (Config.GENERATE_CRYSTALS.get()) {
            int t = 8;
            int maxCrystals = Math.round(64.0f * density);
            int cc = 0;
            for (int j = 0; j < Math.round(t * density); ++j) {
                int randPosX2 = chunkX * 16 + 8 + Mth.nextInt(random, -6, 6);
                int randPosZ3 = chunkZ * 16 + 8 + Mth.nextInt(random, -6, 6);
                int randPosY3 = random.nextInt(Math.max(5, world.getHeight(Heightmap.Types.MOTION_BLOCKING, randPosX2, randPosZ3) - 5));
                BlockPos bp = new BlockPos(randPosX2, randPosY3, randPosZ3);
                int md = random.nextInt(6);
                if (random.nextInt(3) == 0) {
                    Aspect tag = BiomeHandler.getRandomBiomeTag(world.getBiome(bp), random);
                    if (tag == null) {
                        md = random.nextInt(6);
                    } else {
                        md = ShardType.getMetaByAspect(tag);
                    }
                }
                Block oreBlock = ShardType.byMetadata(md).getOre();
                for (int xx = -1; xx <= 1; ++xx) {
                    for (int yy = -1; yy <= 1; ++yy) {
                        for (int zz = -1; zz <= 1; ++zz) {
                            if (random.nextInt(3) != 0) {
                                BlockPos p = bp.offset(xx, yy, zz);
                                BlockState bs = world.getBlockState(p);
                                if (bs.getFluidState().isEmpty() && (bs.isAir() || bs.canBeReplaced()) && BlockUtils.isBlockTouchingRock(world, p, true)) {
                                    int amt = 1 + random.nextInt(3);
                                    world.setBlock(p, oreBlock.defaultBlockState().setValue(BlockCrystal.SIZE, amt), Block.UPDATE_CLIENTS);
                                    cc += amt;
                                }
                            }
                        }
                    }
                }
                if (cc > maxCrystals) {
                    break;
                }
            }
        }
    }

    public static boolean generateFlowers(WorldGenLevel world, RandomSource random, BlockPos pos, Block block) {
        return new WorldGenCustomFlowers(block).generate(world, random, pos);
    }

    public static boolean generateGreatwood(WorldGenLevel world, RandomSource random, int chunkX, int chunkZ) {
        int x = chunkX * 16 + 8 + Mth.nextInt(random, -4, 4);
        int z = chunkZ * 16 + 8 + Mth.nextInt(random, -4, 4);
        BlockPos bp = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        if (BiomeHandler.getBiomeSupportsGreatwood(world.getBiome(bp)) > random.nextFloat()) {
            return new WorldGenGreatwoodTrees(false, random.nextInt(8) == 0).generate(world, random, bp);
        }
        return false;
    }

    /**
     * generateSilverwood. Biomes 18 and 28 of 1.12 (forest hills and birch forest hills) are gone; windswept forest
     * and old growth birch forest stand in for them.
     */
    public static boolean generateSilverwood(WorldGenLevel world, RandomSource random, int chunkX, int chunkZ) {
        int x = chunkX * 16 + 8 + Mth.nextInt(random, -4, 4);
        int z = chunkZ * 16 + 8 + Mth.nextInt(random, -4, 4);
        BlockPos bp = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        Holder<Biome> bio = world.getBiome(bp);
        if (BiomeHandler.getBiomeSupportsGreatwood(bio) / 2.0f > random.nextFloat() || (!bio.is(thaumcraft.common.world.biomes.TCBiomes.MAGICAL_FOREST) && bio.is(Tags.Biomes.IS_MAGICAL))
                || bio.is(Biomes.WINDSWEPT_FOREST) || bio.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) {
            return new WorldGenSilverwoodTrees(false, 7, 4).generate(world, random, bp);
        }
        return false;
    }

    /** generateOres, once per chunk. */
    public static class OresFeature extends Feature<NoneFeatureConfiguration> {
        public OresFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            BlockPos origin = context.origin();
            generateOres(context.level(), context.random(), origin.getX() >> 4, origin.getZ() >> 4);
            return true;
        }
    }

    /** generateVegetation, once per chunk; not in flat worlds. */
    public static class VegetationFeature extends Feature<NoneFeatureConfiguration> {
        public VegetationFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            if (!Config.GENERATE_TREES.get() || isFlat(context)) {
                return false;
            }
            BlockPos origin = context.origin();
            generateVegetation(context.level(), context.random(), origin.getX() >> 4, origin.getZ() >> 4);
            return true;
        }
    }

    /**
     * generateSurface: 1 chunk in 100 tries a barrow (WorldGenMound), 1 in 66 of the rest an eldritch altar ring (from
     * TC4, which TC6 left out), and 1 in 500 of the rest gets a lesser crimson portal on the surface.
     * The portal's top block must be ground, rock, sand or snow; in 1.12 grass had a material of its own, so not on grass.
     */
    public static void generateSurface(WorldGenLevel world, RandomSource random, int chunkX, int chunkZ) {
        int randPosX = chunkX * 16 + 8 + Mth.nextInt(random, -4, 4);
        int randPosZ = chunkZ * 16 + 8 + Mth.nextInt(random, -4, 4);
        BlockPos p = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(randPosX, 0, randPosZ)).below(9);
        if (p.getY() < world.getMaxBuildHeight()) {
            if (random.nextInt(100) == 0) {
                if (new thaumcraft.common.world.objects.WorldGenMound().generate(world, random, p)) {
                    // Not in the original: remembered for /thaumcraft locate barrow (the entrance, on the west side).
                    thaumcraft.common.world.objects.MoundLocations.record(world.getLevel().dimension(), p.offset(0, 9, 9));
                    // TC4 (dream branch): a dark node floats over the barrow.
                    if (Config.GENERATE_NODES.get()) {
                        createRandomNodeAt(world, p.offset(9, 8, 9), random, false, true, false);
                    }
                }
            } else if (random.nextInt(66) == 0) {
                // TC4: an eldritch altar ring, with a dark node above the obelisk on the dream branch.
                thaumcraft.common.world.objects.WorldGenEldritchRing ring = new thaumcraft.common.world.objects.WorldGenEldritchRing();
                ring.chunkX = chunkX;
                ring.chunkZ = chunkZ;
                ring.width = 11 + random.nextInt(6) * 2;
                ring.height = 11 + random.nextInt(6) * 2;
                if (ring.generate(world, random, p.above(8))) {
                    // Dream branch: remembered for the cartographic notes.
                    thaumcraft.common.world.objects.StructureLocations.record(world.getLevel().dimension(),
                            thaumcraft.common.world.objects.StructureLocations.ELDRITCH_RING, p.above(8));
                    thaumcraft.common.world.dim.MazeHandler.startMaze(chunkX, chunkZ, ring.width, ring.height, random.nextLong());
                }
            } else if (random.nextInt(500) == 0) {
                BlockPos p2 = p.above(8);
                BlockState bs = world.getBlockState(p2);
                if (isPortalGround(bs)) {
                    thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser eg =
                            thaumcraft.common.registry.TCEntities.CULTIST_PORTAL_LESSER.get().create(world.getLevel());
                    if (eg != null) {
                        eg.setPos(p2.getX() + 0.5, p2.getY() + 1, p2.getZ() + 0.5);
                        eg.finalizeSpawn(world, world.getCurrentDifficultyAt(p2), net.minecraft.world.entity.MobSpawnType.STRUCTURE, null);
                        world.addFreshEntity(eg);
                    }
                }
            }
        }
    }

    /** Material.GROUND, ROCK, SAND or SNOW. */
    private static boolean isPortalGround(BlockState bs) {
        return bs.is(net.minecraft.world.level.block.Blocks.DIRT) || bs.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT)
                || bs.is(net.minecraft.world.level.block.Blocks.PODZOL) || bs.is(net.minecraft.world.level.block.Blocks.FARMLAND)
                || bs.is(net.minecraft.world.level.block.Blocks.CLAY) || bs.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                || bs.is(BlockTags.SAND) || bs.is(BlockTags.BASE_STONE_OVERWORLD) || bs.is(BlockTags.TERRACOTTA)
                || bs.is(Tags.Blocks.ORES) || bs.is(net.minecraft.world.level.block.Blocks.SNOW);
    }

    /** generateSurface, once per chunk; not in flat worlds. */
    public static class StructuresFeature extends Feature<NoneFeatureConfiguration> {
        public StructuresFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            if (!Config.GENERATE_STRUCTURE.get() || isFlat(context)) {
                return false;
            }
            BlockPos origin = context.origin();
            generateSurface(context.level(), context.random(), origin.getX() >> 4, origin.getZ() >> 4);
            return true;
        }
    }

    // ---- Aura nodes, from Thaumcraft 4 (dream branch) ----

    /** generateWildNodes: one chunk in nodeRarity gets a node on the first air above the bottom of the world. */
    public static class NodesFeature extends Feature<NoneFeatureConfiguration> {
        public NodesFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            if (!Config.GENERATE_NODES.get()) {
                return false;
            }
            WorldGenLevel world = context.level();
            RandomSource random = context.random();
            if (random.nextInt(Config.NODE_RARITY.get()) != 0) {
                return false;
            }
            BlockPos origin = context.origin();
            int x = (origin.getX() >> 4) * 16 + random.nextInt(16);
            int z = (origin.getZ() >> 4) * 16 + random.nextInt(16);
            int bottom = world.getMinBuildHeight();
            int selectedY = getFirstUncoveredY(world, x, z);
            if (selectedY < bottom + 2) {
                selectedY = bottom + 32 + random.nextInt(64);
            }
            if (world.isEmptyBlock(new BlockPos(x, selectedY + 1, z))) {
                ++selectedY;
            }
            int p = random.nextInt(4);
            BlockState b = world.getBlockState(new BlockPos(x, selectedY + p, z));
            if (b.isAir() || b.canBeReplaced()) {
                selectedY += p;
            }
            if (selectedY >= world.getMaxBuildHeight()) {
                return false;
            }
            createRandomNodeAt(world, new BlockPos(x, selectedY, z), random, false, false, false);
            return true;
        }
    }

    /** Utils.getFirstUncoveredY: the first air going up from five blocks above the bottom of the world (often a cave). */
    private static int getFirstUncoveredY(WorldGenLevel world, int x, int z) {
        int y = world.getMinBuildHeight() + 5;
        while (y < world.getMaxBuildHeight() - 1 && !world.isEmptyBlock(new BlockPos(x, y + 1, z))) {
            ++y;
        }
        return y;
    }

    /**
     * createRandomNodeAt: a node with aspects from the biome and its surroundings. Silverwood nodes are pure, eerie
     * ones dark, and the rest one in specialNodeRarity of a special type. Small and silverwood nodes are weaker.
     */
    public static void createRandomNodeAt(net.minecraft.world.level.LevelAccessor world, BlockPos pos, RandomSource random,
                                          boolean silverwood, boolean eerie, boolean small) {
        java.util.List<Aspect> basicAspects = Aspect.getPrimalAspects();
        java.util.List<Aspect> complexAspects = Aspect.getCompoundAspects();
        int special = Config.SPECIAL_NODE_RARITY.get();
        thaumcraft.api.nodes.NodeType type = thaumcraft.api.nodes.NodeType.NORMAL;
        if (silverwood) {
            type = thaumcraft.api.nodes.NodeType.PURE;
        } else if (eerie) {
            type = thaumcraft.api.nodes.NodeType.DARK;
        } else if (random.nextInt(special) == 0) {
            switch (random.nextInt(10)) {
                case 0, 1, 2 -> type = thaumcraft.api.nodes.NodeType.DARK;
                case 3, 4, 5 -> type = thaumcraft.api.nodes.NodeType.UNSTABLE;
                case 6, 7, 8 -> type = thaumcraft.api.nodes.NodeType.PURE;
                default -> type = thaumcraft.api.nodes.NodeType.HUNGRY;
            }
        }
        thaumcraft.api.nodes.NodeModifier modifier = null;
        if (random.nextInt(special / 2) == 0) {
            switch (random.nextInt(3)) {
                case 0 -> modifier = thaumcraft.api.nodes.NodeModifier.BRIGHT;
                case 1 -> modifier = thaumcraft.api.nodes.NodeModifier.PALE;
                default -> modifier = thaumcraft.api.nodes.NodeModifier.FADING;
            }
        }
        Holder<Biome> bg = world.getBiome(pos);
        // BiomeHandler.getBiomeAura: Thaumcraft 6 keeps a 0-1 level per biome; 400 brings it to Thaumcraft 4's range.
        int baura = Math.max(2, (int) (BiomeHandler.getBiomeAuraModifier(bg) * 400.0f));
        // The taint biome of the original does not exist; its 1.5x boost and tainted nodes are left out.
        if (silverwood || small) {
            baura /= 4;
        }
        int value = random.nextInt(Math.max(1, baura / 2)) + baura / 2;
        Aspect ra = BiomeHandler.getRandomBiomeTag(bg, random);
        AspectList al = new AspectList();
        if (ra != null) {
            al.add(ra, 2);
        } else {
            al.add(complexAspects.get(random.nextInt(complexAspects.size())), 1);
            al.add(basicAspects.get(random.nextInt(basicAspects.size())), 1);
        }
        for (int a = 0; a < 3; ++a) {
            if (random.nextBoolean()) {
                if (random.nextInt(special) == 0) {
                    al.merge(complexAspects.get(random.nextInt(complexAspects.size())), 1);
                } else {
                    al.merge(basicAspects.get(random.nextInt(basicAspects.size())), 1);
                }
            }
        }
        switch (type) {
            case HUNGRY -> {
                // Fames and lucrum are gone from Thaumcraft 6; desiderium and vacuos take their place.
                al.merge(Aspect.DESIRE, 2);
                if (random.nextBoolean()) al.merge(Aspect.VOID, 1);
            }
            case PURE -> {
                if (random.nextBoolean()) al.merge(Aspect.LIFE, 2);
                else al.merge(Aspect.ORDER, 2);
            }
            case DARK -> {
                if (random.nextBoolean()) al.merge(Aspect.DEATH, 1);
                if (random.nextBoolean()) al.merge(Aspect.UNDEAD, 1);
                if (random.nextBoolean()) al.merge(Aspect.ENTROPY, 1);
                if (random.nextBoolean()) al.merge(Aspect.DARKNESS, 1);
            }
            default -> {
            }
        }
        int water = 0;
        int lava = 0;
        int stone = 0;
        int foliage = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-5, -5, -5), pos.offset(5, 5, 5))) {
            BlockState bi = world.getBlockState(p);
            if (bi.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
                ++water;
            } else if (bi.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) {
                ++lava;
            } else if (bi.is(net.minecraft.world.level.block.Blocks.STONE)) {
                ++stone;
            }
            if (bi.is(BlockTags.LEAVES) || bi.is(BlockTags.REPLACEABLE_BY_TREES)) {
                ++foliage;
            }
        }
        if (water > 100) al.merge(Aspect.WATER, 1);
        if (lava > 100) {
            al.merge(Aspect.FIRE, 1);
            al.merge(Aspect.EARTH, 1);
        }
        if (stone > 500) al.merge(Aspect.EARTH, 1);
        if (foliage > 100) al.merge(Aspect.PLANT, 1);
        Aspect[] sorted = al.getAspectsSortedByAmount();
        int[] spread = new int[sorted.length];
        float total = 0.0f;
        for (int a = 0; a < spread.length; ++a) {
            spread[a] = al.getAmount(sorted[a]) == 2 ? 50 + random.nextInt(25) : 25 + random.nextInt(50);
            total += spread[a];
        }
        for (int a = 0; a < spread.length; ++a) {
            al.merge(sorted[a], (int) (spread[a] / total * value));
        }
        createNodeAt(world, pos, type, modifier, al);
    }

    /** createNodeAt: puts a node block in the air (or replaces nothing) and gives it its kind and aspects. */
    public static void createNodeAt(net.minecraft.world.level.LevelAccessor world, BlockPos pos, thaumcraft.api.nodes.NodeType nt,
                                    @javax.annotation.Nullable thaumcraft.api.nodes.NodeModifier nm, AspectList al) {
        if (world.isEmptyBlock(pos)) {
            world.setBlock(pos, ConfigBlocks.auraNode.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        if (world.getBlockEntity(pos) instanceof thaumcraft.common.tiles.misc.TileAuraNode node) {
            node.setNodeType(nt);
            node.setNodeModifier(nm);
            node.setAspects(al);
            node.setChanged();
        }
    }
}
