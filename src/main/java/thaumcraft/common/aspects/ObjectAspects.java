package thaumcraft.common.aspects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigAspects;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

/**
 * Item aspects. Replaces ThaumcraftApi.registerObjectTag + ConfigAspects.
 * <p>
 * TC6 registered these in code using the OreDictionary. Here they are loaded from datapacks,
 * {@code data/<namespace>/thaumcraft/aspects/*.json}:
 * <pre>
 * { "entries": [
 *     { "items": ["minecraft:apple"], "aspects": { "herba": 5, "victus": 5 } },
 *     { "items": ["#minecraft:logs"], "aspects": { "herba": 20 } },
 *     { "items": ["minecraft:torch"], "complex": true, "aspects": { "lux": 5 } },
 *     { "items": ["minecraft:brick"], "base": "minecraft:clay_ball", "aspects": { "ignis": 1 } }
 * ] }
 * </pre>
 * A leading "#" means an item tag (the modern replacement for OreDictionary names).
 * An entry for the exact item wins over a tag entry; among several, the last one loaded wins.
 * <ul>
 * <li>{@code complex}: registerComplexObjectTag. The aspects are added to the ones generated from recipes.</li>
 * <li>{@code base}: {@code new AspectList(new ItemStack(base))}. Starts from the aspects of another item.</li>
 * <li>{@code merge}: applied last with {@link AspectList#merge} (keeps the highest amount).</li>
 * </ul>
 * Items with no entry get aspects generated from their recipes, see
 * {@link ThaumcraftCraftingManager#getObjectTags}.
 * <p>
 * The server loads the data and sends it to clients with {@link SyncObjectAspectsPayload}.
 */
public final class ObjectAspects {
    private ObjectAspects() {
    }

    /** A raw entry: "minecraft:apple" or "#minecraft:logs" plus aspects. Tags are resolved on lookup. */
    public record Entry(List<String> items, AspectList aspects, boolean complex, Optional<String> base,
                        Optional<AspectList> merge) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.listOf().fieldOf("items").forGetter(Entry::items),
                AspectList.CODEC.optionalFieldOf("aspects", new AspectList()).forGetter(Entry::aspects),
                Codec.BOOL.optionalFieldOf("complex", false).forGetter(Entry::complex),
                Codec.STRING.optionalFieldOf("base").forGetter(Entry::base),
                AspectList.CODEC.optionalFieldOf("merge").forGetter(Entry::merge)
        ).apply(i, Entry::new));

        public static final StreamCodec<io.netty.buffer.ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), Entry::items,
                AspectList.STREAM_CODEC, Entry::aspects,
                ByteBufCodecs.BOOL, Entry::complex,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), Entry::base,
                ByteBufCodecs.optional(AspectList.STREAM_CODEC), Entry::merge,
                Entry::new);
    }

    private static final Codec<List<Entry>> FILE_CODEC = Entry.CODEC.listOf().fieldOf("entries").codec();

    private static List<Entry> entries = List.of();
    private static Map<Item, Entry> byItem = Map.of();
    private static Map<TagKey<Item>, Entry> byTag = Map.of();

    /** Final (pre-bonus) aspects per item, explicit or generated. TC6 kept these in CommonInternals.objectTags. */
    private static final Map<Item, AspectList> CACHE = new ConcurrentHashMap<>();

    // What generation reads. Set from the server and, on clients, from the synced recipes.
    @Nullable private static volatile RecipeManager recipes;
    @Nullable private static volatile HolderLookup.Provider registries;
    @Nullable private static volatile PotionBrewing brewing;

    /** The aspects of an item stack (a new list each call, safe to modify). */
    public static AspectList get(ItemStack stack) {
        return ThaumcraftCraftingManager.getObjectTags(stack);
    }

    public static List<Entry> getEntries() {
        return entries;
    }

    /** The data entry for an item: exact item first, then the last matching tag. */
    @Nullable
    public static Entry findEntry(ItemStack stack) {
        Entry exact = byItem.get(stack.getItem());
        if (exact != null) return exact;
        Entry found = null;
        for (Map.Entry<TagKey<Item>, Entry> e : byTag.entrySet()) {
            if (stack.is(e.getKey())) found = e.getValue();
        }
        return found;
    }

    @Nullable
    public static AspectList getCached(Item item) {
        return CACHE.get(item);
    }

    /** Remembers an item's aspects. Skipped until recipes are known, so early lookups are not frozen empty. */
    public static void cache(Item item, AspectList aspects) {
        if (recipes != null) CACHE.put(item, aspects);
    }

    /** Forgets generated aspects. Called when data, recipes or tags change. */
    public static void invalidate() {
        CACHE.clear();
        ConfigAspects.clearPotionCache();
    }

    public static void setContext(RecipeManager recipeManager, HolderLookup.Provider lookup, PotionBrewing potionBrewing) {
        recipes = recipeManager;
        registries = lookup;
        brewing = potionBrewing;
        invalidate();
    }

    @Nullable
    public static RecipeManager recipes() {
        return recipes;
    }

    public static HolderLookup.Provider registries() {
        HolderLookup.Provider lookup = registries;
        return lookup != null ? lookup : RegistryAccess.EMPTY;
    }

    @Nullable
    public static PotionBrewing brewing() {
        return brewing;
    }

    /** Replaces all data. Used by the server reload and by the client sync packet. */
    public static void setEntries(List<Entry> newEntries) {
        Map<Item, Entry> items = new HashMap<>();
        Map<TagKey<Item>, Entry> tags = new LinkedHashMap<>();
        for (Entry entry : newEntries) {
            for (String id : entry.items()) {
                if (id.startsWith("#")) {
                    ResourceLocation tag = ResourceLocation.tryParse(id.substring(1));
                    if (tag == null) {
                        Thaumcraft.LOGGER.warn("Invalid tag in aspect data: {}", id);
                        continue;
                    }
                    TagKey<Item> key = TagKey.create(Registries.ITEM, tag);
                    tags.remove(key); // re-insert so the latest definition is last and wins
                    tags.put(key, entry);
                } else {
                    ResourceLocation itemId = ResourceLocation.tryParse(id);
                    if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                        Thaumcraft.LOGGER.debug("Unknown item in aspect data, skipping: {}", id);
                        continue;
                    }
                    items.put(BuiltInRegistries.ITEM.get(itemId), entry);
                }
            }
        }
        entries = List.copyOf(newEntries);
        byItem = items;
        byTag = tags;
        invalidate();
    }

    /** Loads data/&lt;ns&gt;/thaumcraft/aspects/*.json. Registered in AddReloadListenerEvent. */
    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().create();

        public ReloadListener() {
            super(GSON, "thaumcraft/aspects");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            List<Entry> loaded = new ArrayList<>();
            files.forEach((file, json) -> FILE_CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> Thaumcraft.LOGGER.error("Error reading aspects from {}: {}", file, err))
                    .ifPresent(loaded::addAll));
            setEntries(loaded);
            Thaumcraft.LOGGER.info("Loaded {} aspect entries from {} files", loaded.size(), files.size());
        }
    }
}
