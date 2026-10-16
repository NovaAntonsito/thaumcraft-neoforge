package thaumcraft.common.world.biomes;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;

/**
 * The biomes of Registrar.registerBiomes. Magical Forest and Eerie are data (data/thaumcraft/worldgen/biome); the
 * Outer Lands come with the eldritch stage.
 * <p>
 * 1.12 put the magical forest in the WARM and COOL biome lists. Since 1.18 biomes are placed by climate, so a
 * TerraBlender region swaps the plain forest of the vanilla layout for it, in the share of the world that the
 * region's weight gives. The Eerie biome was registered but never generated, and it stays that way.
 */
public final class TCBiomes {
    public static final ResourceKey<Biome> MAGICAL_FOREST = key("magical_forest");
    public static final ResourceKey<Biome> EERIE = key("eerie");

    private TCBiomes() {
    }

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, name));
    }

    /** From common setup. */
    public static void registerRegions() {
        if (Config.GENERATE_MAGIC_FOREST.get() && Config.BIOME_MAGICAL_FOREST_WEIGHT.get() > 0) {
            Regions.register(new MagicalForestRegion(Config.BIOME_MAGICAL_FOREST_WEIGHT.get()));
        }
    }

    static class MagicalForestRegion extends Region {
        MagicalForestRegion(int weight) {
            super(ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "overworld"), RegionType.OVERWORLD, weight);
        }

        @Override
        public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            addModifiedVanillaOverworldBiomes(mapper, builder -> builder.replaceBiome(Biomes.FOREST, MAGICAL_FOREST));
        }
    }
}
