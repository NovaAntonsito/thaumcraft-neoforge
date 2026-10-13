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

    static final ModConfigSpec SPEC = BUILDER.build();
}
