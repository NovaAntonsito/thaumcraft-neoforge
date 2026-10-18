package thaumcraft.common.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Calendar;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;

/**
 * The EntityRegistry.addSpawn calls of ConfigEntities. They depended on the allowSpawn* config options (and the
 * firebats in every biome on the date), so they are a biome modifier that checks them when the world loads:
 * data/thaumcraft/neoforge/biome_modifier/spawn_*.json.
 */
public final class TCBiomeModifiers {
    private TCBiomeModifiers() {
    }

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Thaumcraft.MODID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ConfigSpawns>> CONFIG_SPAWNS =
            SERIALIZERS.register("config_spawns", () -> ConfigSpawns.CODEC);

    /**
     * neoforge:add_spawns with a config option ("angry_zombie", "fire_bat", "wisp", "pech" or "halloween_fire_bat") and,
     * like the brainy zombies of the original, optionally only in biomes that already have monsters.
     */
    public record ConfigSpawns(String option, boolean onlyWithMonsters, HolderSet<Biome> biomes,
            List<MobSpawnSettings.SpawnerData> spawners) implements BiomeModifier {
        public static final MapCodec<ConfigSpawns> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.STRING.fieldOf("option").forGetter(ConfigSpawns::option),
                Codec.BOOL.optionalFieldOf("only_with_monsters", false).forGetter(ConfigSpawns::onlyWithMonsters),
                Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConfigSpawns::biomes),
                MobSpawnSettings.SpawnerData.CODEC.listOf().fieldOf("spawners").forGetter(ConfigSpawns::spawners))
                .apply(b, ConfigSpawns::new));

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.ADD || !biomes.contains(biome) || !allowed()) {
                return;
            }
            var spawns = builder.getMobSpawnSettings();
            for (MobSpawnSettings.SpawnerData data : spawners) {
                if (onlyWithMonsters && spawns.getSpawner(data.type.getCategory()).isEmpty()) {
                    continue;
                }
                spawns.addSpawn(data.type.getCategory(), data);
            }
        }

        private boolean allowed() {
            return switch (option) {
                case "angry_zombie" -> Config.ALLOW_SPAWN_ANGRY_ZOMBIE.get();
                case "fire_bat" -> Config.ALLOW_SPAWN_FIRE_BAT.get();
                case "wisp" -> Config.ALLOW_SPAWN_WISP.get();
                case "pech" -> Config.ALLOW_SPAWN_PECH.get();
                case "elder" -> Config.ALLOW_SPAWN_ELDER.get();
                case "halloween_fire_bat" -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    yield Config.ALLOW_SPAWN_FIRE_BAT.get() && calendar.get(Calendar.MONTH) + 1 == 10 && calendar.get(Calendar.DAY_OF_MONTH) == 31;
                }
                default -> true;
            };
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CONFIG_SPAWNS.get();
        }
    }
}
