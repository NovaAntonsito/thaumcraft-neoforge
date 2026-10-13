package thaumcraft.common.lib.research;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/** Reads the note pattern files (assets/thaumcraft/research/patterns/*.json), from Reborn's OldResearchPattParser. */
public final class ResearchNotePatternParser {
    private static final Set<ResourceLocation> QUEUE = new LinkedHashSet<>();

    private ResearchNotePatternParser() {
    }

    public static void putIntoQueue(ResourceLocation location) {
        QUEUE.add(location);
    }

    /** Every queued pattern; the first one for a note wins. */
    public static List<ResearchNotePattern> parseQueue() {
        Map<String, ResearchNotePattern> map = new LinkedHashMap<>();
        for (ResourceLocation loc : QUEUE) {
            for (ResearchNotePattern pattern : parse(loc)) {
                if (map.containsKey(pattern.noteKey())) {
                    Thaumcraft.LOGGER.warn("Attempt to register some patterns to one target {}, skipping...", pattern.noteKey());
                } else {
                    map.put(pattern.noteKey(), pattern);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    public static List<ResearchNotePattern> parse(ResourceLocation location) {
        String s = "/assets/" + location.getNamespace() + "/" + location.getPath() + ".json";
        List<ResearchNotePattern> patterns = new ArrayList<>();
        try (InputStream stream = ResearchNotePatternParser.class.getResourceAsStream(s)) {
            if (stream == null) throw new RuntimeException("Input stream is null!");
            JsonObject obj = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement element : obj.get("entries").getAsJsonArray()) {
                JsonObject entry = element.getAsJsonObject();
                patterns.add(new ResearchNotePattern(
                        entry.get("target").getAsString(),
                        entry.get("stage").getAsInt(),
                        parseAspects(entry.getAsJsonArray("aspects")),
                        Mth.clamp(entry.get("radius").getAsInt(), 1, 4),
                        Math.max(0, entry.get("blanks").getAsInt()),
                        parseColor(entry.get("color")),
                        entry.has("hash-delta") ? entry.get("hash-delta").getAsInt() : 0,
                        parseMeta(entry)));
            }
        } catch (Exception e) {
            Thaumcraft.LOGGER.error("failed to parse file {}", location, e);
        }
        Thaumcraft.LOGGER.info("Loaded {} note patterns from {}", patterns.size(), location);
        return patterns;
    }

    private static String[] parseMeta(JsonObject entry) {
        if (!entry.has("meta")) return new String[0];
        JsonArray array = entry.getAsJsonArray("meta");
        String[] ret = new String[array.size()];
        for (int i = 0; i != ret.length; i++) ret[i] = array.get(i).getAsString();
        return ret;
    }

    private static int parseColor(JsonElement element) {
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) return primitive.getAsInt();
        String tag = primitive.getAsString();
        Aspect aspect = Aspect.getAspect(tag);
        if (aspect == null) throw new IllegalStateException("unknown aspect [" + tag + "]");
        return aspect.getColor();
    }

    private static AspectList parseAspects(JsonArray arr) {
        AspectList list = new AspectList();
        for (JsonElement element : arr) {
            String tag = element.getAsString();
            Aspect aspect = Aspect.getAspect(tag);
            if (aspect == null) throw new IllegalStateException("unknown aspect [" + tag + "]");
            list.add(aspect, 0);
        }
        return list;
    }
}
