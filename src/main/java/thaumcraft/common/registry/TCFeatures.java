package thaumcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.common.world.ThaumcraftWorldGenerator;

/**
 * The world generation of ThaumcraftWorldGenerator (an IWorldGenerator in 1.12) as features that run once per
 * chunk. Biome modifiers in data/thaumcraft/neoforge/biome_modifier add them to the overworld biomes.
 */
public final class TCFeatures {
    private TCFeatures() {
    }

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Thaumcraft.MODID);

    /** generateOres: cinnabar, quartz, amber and vis crystals. */
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> ORES = FEATURES.register("ores",
            () -> new ThaumcraftWorldGenerator.OresFeature(NoneFeatureConfiguration.CODEC));
    /** generateVegetation: greatwood and silverwood trees, and cinderpearls in hot deserts. */
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> VEGETATION = FEATURES.register("vegetation",
            () -> new ThaumcraftWorldGenerator.VegetationFeature(NoneFeatureConfiguration.CODEC));
    /** generateSurface: the barrows and the lesser crimson portals. */
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> STRUCTURES = FEATURES.register("structures",
            () -> new ThaumcraftWorldGenerator.StructuresFeature(NoneFeatureConfiguration.CODEC));
    /** BiomeGenMagicalForest.decorate, placed by the magical forest biome. */
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> MAGICAL_FOREST = FEATURES.register("magical_forest",
            () -> new thaumcraft.common.world.biomes.BiomeGenMagicalForest(NoneFeatureConfiguration.CODEC));
}
