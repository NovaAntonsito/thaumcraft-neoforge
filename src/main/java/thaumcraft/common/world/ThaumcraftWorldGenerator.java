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
     * generateSurface: 1 chunk in 100 tries a barrow (WorldGenMound), and 1 in 500 of the rest gets a lesser crimson
     * portal on the surface.
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
}
