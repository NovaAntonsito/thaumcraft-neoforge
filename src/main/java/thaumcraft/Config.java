package thaumcraft;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Mod config (config/thaumcraft-common.toml). Port of the ModConfig options used so far. */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("graphics");
    }

    public static final ModConfigSpec.BooleanValue SHOW_TAGS = BUILDER
            .comment("Item aspects are hidden by default and pressing shift reveals them.",
                    "Changing this setting to 'true' will reverse this behaviour and always",
                    "display aspects unless shift is pressed.")
            .define("showTags", false);

    public static final ModConfigSpec.BooleanValue SHOW_GOLEM_EMOTES = BUILDER
            .comment("Setting this to false will disable the emote particles golems show when working or getting stuck.")
            .define("showGolemEmotes", true);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.push("misc");
    }

    public static final ModConfigSpec.BooleanValue NO_SLEEP = BUILDER
            .comment("Setting this to true will make you get the recipe book for salis mundus without having to sleep first.")
            .define("noSleep", false);

    public static final ModConfigSpec.BooleanValue WUSS_MODE = BUILDER
            .comment("Setting this to true disables Warp, Taint spread and similar mechanics. You wuss.")
            .define("wussMode", false);

    public static final ModConfigSpec.BooleanValue ALLOW_CHEAT_SHEET = BUILDER
            .comment("Enables a version of the Thauminomicon in creative mode that grants you all the research when you first use it.")
            .define("allowCheatSheet", false);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.push("research");
    }

    public static final ModConfigSpec.DoubleValue RESEARCH_DIFFICULTY_MULTIPLIER = BUILDER
            .comment("Multiplier applied to the complexity of research notes. Higher values make notes bigger and harder.")
            .defineInRange("researchDifficultyMultiplier", 0.5, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue ASPECT_OBTAIN_MULTIPLIER = BUILDER
            .comment("Multiplier applied to the aspect points gained by scanning.")
            .defineInRange("aspectObtainMultiplier", 0.5, 0.0, 100.0);

    public static final ModConfigSpec.IntValue NOTIFICATION_DELAY = BUILDER
            .comment("How long (in ms) aspect notifications stay on screen.")
            .defineInRange("notificationDelay", 2000, 0, 60000);

    public static final ModConfigSpec.IntValue NOTIFICATION_MAX = BUILDER
            .comment("Maximum number of aspect notifications shown at once.")
            .defineInRange("notificationMax", 10, 1, 100);

    public static final ModConfigSpec.IntValue ASPECT_TOTAL_CAP = BUILDER
            .comment("Above this amount of an aspect in the pool, gains are reduced.")
            .defineInRange("aspectTotalCap", 10000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue PRIMAL_ASPECT_FLOOR = BUILDER
            .comment("Primal aspects in the pool never stay below this amount, so new players can start research notes. Not in Thaumcraft; 0 disables it.")
            .defineInRange("primalAspectFloor", 10, 0, 1000);

    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> ASPECT_BLACKLIST = BUILDER
            .comment("Aspects that never appear in randomly generated research notes.")
            .defineListAllowEmpty("aspectBlackList", java.util.List.of(), () -> "", o -> o instanceof String);

    public static final ModConfigSpec.BooleanValue INSTANT_SCANS = BUILDER
            .comment("Scan instantly with the thaumometer, as in Thaumcraft 6, instead of holding it for a second.")
            .define("instantScans", false);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.push("anomalies");
    }

    public static final ModConfigSpec.BooleanValue ANOMALIES_ENABLED = BUILDER
            .comment("World anomalies: rogue-lite modifiers that show up every so often and change the rules of the aura,",
                    "the casters and the arcane workbench. Not in Thaumcraft.")
            .define("enabled", true);

    public static final ModConfigSpec.IntValue ANOMALY_INTERVAL = BUILDER
            .comment("Minutes of game time between anomalies.")
            .defineInRange("intervalMinutes", 40, 1, 1440);

    public static final ModConfigSpec.DoubleValue ANOMALY_JITTER = BUILDER
            .comment("Random variation of the interval. 0.2 means up to 20% shorter or longer.")
            .defineInRange("intervalJitter", 0.2, 0.0, 0.9);

    public static final ModConfigSpec.IntValue ANOMALY_MAX_ACTIVE = BUILDER
            .comment("Most anomalies running at once. Two at once only happens from tier 3 (after 10 anomalies).")
            .defineInRange("maxActive", 2, 1, 5);

    public static final ModConfigSpec.DoubleValue ANOMALY_POSITIVE_WEIGHT = BUILDER
            .comment("Multiplier on the chance of positive anomalies.")
            .defineInRange("positiveWeight", 1.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue ANOMALY_NEGATIVE_WEIGHT = BUILDER
            .comment("Multiplier on the chance of negative anomalies. Wuss mode only allows positive ones.")
            .defineInRange("negativeWeight", 1.0, 0.0, 100.0);

    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> ANOMALY_BLACKLIST = BUILDER
            .comment("Ids of anomalies that never show up (see /thaumcraft anomaly list).")
            .defineListAllowEmpty("blacklist", java.util.List.of(), () -> "", o -> o instanceof String);

    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> ANOMALY_DIMENSIONS = BUILDER
            .comment("Dimensions where anomalies happen.")
            .defineListAllowEmpty("dimensions", java.util.List.of("minecraft:overworld"), () -> "minecraft:overworld", o -> o instanceof String);

    public static final ModConfigSpec.BooleanValue ANOMALY_SHOW_HUD = BUILDER
            .comment("Show the anomaly meter at the top of the screen.")
            .define("showHud", true);

    public static final ModConfigSpec.ConfigValue<String> ANOMALY_HUD_POSITION = BUILDER
            .comment("Where the anomaly meter goes: top_center or top_left.")
            .define("hudPosition", "top_center", o -> "top_center".equals(o) || "top_left".equals(o));

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.push("world");
    }

    public static final ModConfigSpec.IntValue ORE_DENSITY = BUILDER
            .comment("The % of normal ore amounts that will be spawned. For example 50 will spawn half",
                    "the ores while 200 will spawn double. Default 100")
            .worldRestart()
            .defineInRange("oreDensity", 100, 1, 500);

    public static final ModConfigSpec.BooleanValue GENERATE_CINNABAR = BUILDER.define("generateCinnabar", true);
    public static final ModConfigSpec.BooleanValue GENERATE_AMBER = BUILDER.define("generateAmber", true);
    public static final ModConfigSpec.BooleanValue GENERATE_QUARTZ = BUILDER.define("generateQuartz", true);
    public static final ModConfigSpec.BooleanValue GENERATE_CRYSTALS = BUILDER.define("generateCrystals", true);
    public static final ModConfigSpec.BooleanValue GENERATE_TREES = BUILDER.define("generateTrees", true);
    public static final ModConfigSpec.BooleanValue GENERATE_STRUCTURE = BUILDER.define("generateStructure", true);
    public static final ModConfigSpec.BooleanValue ALLOW_CHAMPION_MOBS = BUILDER
            .comment("Setting this to false will disable spawning champion mobs. Even when false they will still",
                    "have a greatly reduced chance of spawning in certain dangerous places.")
            .define("allowChampionMobs", true);
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_ANGRY_ZOMBIE = BUILDER.worldRestart().define("allowSpawnAngryZombie", true);
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_FIRE_BAT = BUILDER.worldRestart().define("allowSpawnFireBat", true);
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_WISP = BUILDER.worldRestart().define("allowSpawnWisp", true);
    public static final ModConfigSpec.BooleanValue ALLOW_SPAWN_PECH = BUILDER.worldRestart().define("allowSpawnPech", true);
    public static final ModConfigSpec.BooleanValue GENERATE_MAGIC_FOREST = BUILDER.worldRestart().define("generateMagicForest", true);
    public static final ModConfigSpec.IntValue BIOME_MAGICAL_FOREST_WEIGHT = BUILDER
            .comment("Higher values increases number of magical forest biomes. If you are using biome",
                    "addon mods you probably want to increase this weight quite a bit")
            .gameRestart()
            .defineInRange("biomeMagicalForestWeight", 5, 0, 100);

    public static final ModConfigSpec.IntValue TAINT_SPREAD_RATE = BUILDER
            .comment("The % chance of taint fibres spreading on a block tick.",
                    "Setting this to 0 will effectively stop taint fibre spread.")
            .defineInRange("taintSpreadRate", 100, 0, 500);

    public static final ModConfigSpec.IntValue TAINT_SPREAD_AREA = BUILDER
            .comment("The range at which taint can spread from a taint seed.",
                    "This value is only a base and will be modified by flux levels.")
            .defineInRange("taintSpreadArea", 32, 8, 256);

    static {
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
