package thaumcraft.common.world.biomes;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.world.objects.WorldGenBigMagicTree;
import thaumcraft.common.world.objects.WorldGenGreatwoodTrees;
import thaumcraft.common.world.objects.WorldGenSilverwoodTrees;

/**
 * Port of BiomeGenMagicalForest. The biome itself is data (data/thaumcraft/worldgen/biome/magical_forest.json:
 * colors, spawns, and the vanilla flowers, grass, lily pads and mushrooms of its decorator); this feature is its
 * decorate(): ambient grass, mossy boulders, huge mushrooms, the trees, and vishrooms next to the trunks.
 * <p>
 * 1.12 decorated with a +8 offset (the corner of four chunks); here the positions stay inside the chunk.
 */
public class BiomeGenMagicalForest extends Feature<NoneFeatureConfiguration> {
    /** decorator.treesPerChunk, plus the decorator's 1 in 10 extra tree. */
    private static final int TREES_PER_CHUNK = 2;

    public BiomeGenMagicalForest(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /** getRandomTreeFeature. */
    private static boolean generateTree(WorldGenLevel world, RandomSource random, BlockPos pos) {
        if (random.nextInt(18) == 0) {
            return new WorldGenSilverwoodTrees(false, 8, 5).generate(world, random, pos);
        }
        if (random.nextInt(12) == 0) {
            return new WorldGenGreatwoodTrees(false, random.nextInt(8) == 0).generate(world, random, pos);
        }
        return new WorldGenBigMagicTree(false).generate(world, random, pos);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        ChunkGenerator generator = context.chunkGenerator();
        BlockPos pos = context.origin();
        for (int a = 0; a < 3; ++a) {
            BlockPos pp;
            for (pp = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.offset(4 + random.nextInt(8), 0, 4 + random.nextInt(8)));
                 pp.getY() > 30 && !world.getBlockState(pp).is(Blocks.GRASS_BLOCK); pp = pp.below()) {
            }
            if (world.getBlockState(pp).is(Blocks.GRASS_BLOCK)) {
                world.setBlock(pp, ConfigBlocks.grassAmbient.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                break;
            }
        }
        for (int k = random.nextInt(3), i = 0; i < k; ++i) {
            BlockPos p2 = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.offset(random.nextInt(16), 0, random.nextInt(16)));
            placeConfigured(world, generator, random, p2, MiscOverworldFeatures.FOREST_ROCK);
        }
        for (int k = 0; k < 4; ++k) {
            for (int i = 0; i < 4; ++i) {
                if (random.nextInt(40) == 0) {
                    BlockPos p2 = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                            pos.offset(k * 4 + 1 + random.nextInt(3), 0, i * 4 + 1 + random.nextInt(3)));
                    // WorldGenBigMushroom without a type picks brown or red.
                    placeConfigured(world, generator, random, p2, random.nextBoolean() ? TreeFeatures.HUGE_BROWN_MUSHROOM : TreeFeatures.HUGE_RED_MUSHROOM);
                }
            }
        }
        int trees = TREES_PER_CHUNK + (random.nextFloat() < 0.1f ? 1 : 0);
        for (int i = 0; i < trees; ++i) {
            BlockPos tp = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.offset(random.nextInt(16), 0, random.nextInt(16)));
            generateTree(world, random, tp);
        }
        for (int a = 0; a < 8; ++a) {
            BlockPos p3;
            for (p3 = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.offset(random.nextInt(16), 0, random.nextInt(16)));
                 p3.getY() > 50 && !world.getBlockState(p3).is(Blocks.GRASS_BLOCK); p3 = p3.below()) {
            }
            if (world.getBlockState(p3).is(Blocks.GRASS_BLOCK) && world.getBlockState(p3.above()).canBeReplaced()
                    && isBlockAdjacentToWood(world, p3.above())) {
                world.setBlock(p3.above(), ConfigBlocks.vishroom.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }

    private static void placeConfigured(WorldGenLevel world, ChunkGenerator generator, RandomSource random, BlockPos pos,
                                        ResourceKey<ConfiguredFeature<?, ?>> key) {
        Registry<ConfiguredFeature<?, ?>> registry = world.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        ConfiguredFeature<?, ?> feature = registry.get(key);
        if (feature != null) {
            feature.place(world, generator, random, pos);
        }
    }

    private static boolean isBlockAdjacentToWood(WorldGenLevel world, BlockPos pos) {
        for (int xx = -1; xx <= 1; ++xx) {
            for (int yy = -1; yy <= 1; ++yy) {
                for (int zz = -1; zz <= 1; ++zz) {
                    if ((xx != 0 || yy != 0 || zz != 0) && Utils.isWoodLog(world, pos.offset(xx, yy, zz))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
