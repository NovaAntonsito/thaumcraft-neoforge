package thaumcraft.common.world.biomes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.api.aspects.Aspect;

/**
 * Partial port of thaumcraft.common.world.biomes.BiomeHandler (biome aura info only).
 * <p>
 * The Forge BiomeDictionary no longer exists; each BiomeDictionary.Type is mapped to the
 * equivalent NeoForge common biome tag (c:is_*). Values are the original ones.
 */
public class BiomeHandler {
    public record BiomeInfo(float auraLevel, Aspect tag, boolean greatwood, float greatwoodChance) {
    }

    public static final Map<TagKey<Biome>, BiomeInfo> biomeInfo = new LinkedHashMap<>();

    public static void registerBiomeInfo(TagKey<Biome> type, float auraLevel, Aspect tag, boolean greatwood, float greatwoodchance) {
        biomeInfo.put(type, new BiomeInfo(auraLevel, tag, greatwood, greatwoodchance));
    }

    private static List<BiomeInfo> infoFor(Holder<Biome> biome) {
        List<BiomeInfo> out = new ArrayList<>();
        biomeInfo.forEach((tag, info) -> {
            if (biome.is(tag)) out.add(info);
        });
        return out;
    }

    /** Average aura level of the biome's types. 0.5 if it has none (the original's fallback). */
    public static float getBiomeAuraModifier(Holder<Biome> biome) {
        List<BiomeInfo> infos = infoFor(biome);
        if (infos.isEmpty()) return 0.5f;
        float average = 0.0f;
        for (BiomeInfo info : infos) average += info.auraLevel();
        return average / infos.size();
    }

    @Nullable
    public static Aspect getRandomBiomeTag(Holder<Biome> biome, RandomSource random) {
        List<BiomeInfo> infos = infoFor(biome);
        return infos.isEmpty() ? null : infos.get(random.nextInt(infos.size())).tag();
    }

    public static float getBiomeSupportsGreatwood(Holder<Biome> biome) {
        for (BiomeInfo info : infoFor(biome)) {
            if (info.greatwood()) return info.greatwoodChance();
        }
        return 0.0f;
    }

    static {
        registerBiomeInfo(Tags.Biomes.IS_AQUATIC, 0.33f, Aspect.WATER, false, 0.0f);      // WATER
        registerBiomeInfo(Tags.Biomes.IS_OCEAN, 0.33f, Aspect.WATER, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_RIVER, 0.4f, Aspect.WATER, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_WET, 0.4f, Aspect.WATER, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_LUSH, 0.5f, Aspect.WATER, true, 0.5f);
        registerBiomeInfo(Tags.Biomes.IS_HOT, 0.33f, Aspect.FIRE, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_DRY, 0.25f, Aspect.FIRE, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_NETHER, 0.125f, Aspect.FIRE, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_BADLANDS, 0.33f, Aspect.FIRE, false, 0.0f);       // MESA
        registerBiomeInfo(Tags.Biomes.IS_SPOOKY, 0.5f, Aspect.FIRE, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_DENSE_VEGETATION, 0.4f, Aspect.ORDER, false, 0.0f); // DENSE
        registerBiomeInfo(Tags.Biomes.IS_SNOWY, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_COLD, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_MUSHROOM, 0.75f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_MAGICAL, 0.75f, Aspect.ORDER, true, 1.0f);
        registerBiomeInfo(Tags.Biomes.IS_CONIFEROUS_TREE, 0.33f, Aspect.EARTH, true, 0.2f); // CONIFEROUS
        registerBiomeInfo(Tags.Biomes.IS_FOREST, 0.5f, Aspect.EARTH, true, 1.0f);
        registerBiomeInfo(Tags.Biomes.IS_SANDY, 0.25f, Aspect.EARTH, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_BEACH, 0.3f, Aspect.EARTH, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_JUNGLE, 0.6f, Aspect.EARTH, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_SAVANNA, 0.25f, Aspect.AIR, true, 0.2f);
        registerBiomeInfo(Tags.Biomes.IS_MOUNTAIN, 0.3f, Aspect.AIR, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_HILL, 0.33f, Aspect.AIR, false, 0.0f);             // HILLS
        registerBiomeInfo(Tags.Biomes.IS_PLAINS, 0.3f, Aspect.AIR, true, 0.2f);
        registerBiomeInfo(Tags.Biomes.IS_END, 0.125f, Aspect.AIR, false, 0.0f);
        // The original registers DRY twice; the second call overwrites the first, so DRY ends up 0.125 / perditio.
        registerBiomeInfo(Tags.Biomes.IS_DRY, 0.125f, Aspect.ENTROPY, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_SPARSE_VEGETATION, 0.2f, Aspect.ENTROPY, false, 0.0f); // SPARSE
        registerBiomeInfo(Tags.Biomes.IS_SWAMP, 0.5f, Aspect.ENTROPY, true, 0.2f);
        registerBiomeInfo(Tags.Biomes.IS_WASTELAND, 0.125f, Aspect.ENTROPY, false, 0.0f);
        registerBiomeInfo(Tags.Biomes.IS_DEAD, 0.1f, Aspect.ENTROPY, false, 0.0f);
    }
}
