package thaumcraft.common.lib.research;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchAddendum;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.consumables.ItemPhial;
import thaumcraft.common.lib.InternalMethodHandler;

/**
 * Port of ResearchManager: research progression and the research JSON parser.
 * <p>
 * Item strings in the JSONs were converted for 1.21 (tools gen_research.py): "namespace:id[;count][;aspect=tag]",
 * "tag:&lt;item tag&gt;" (was "oredict:") and "ench:&lt;enchantment&gt;[;level]" (was the enchanted_placeholder item).
 */
public class ResearchManager {
    /** Players whose knowledge must be sent at the end of the tick (by UUID, was by name). */
    public static ConcurrentHashMap<UUID, Boolean> syncList = new ConcurrentHashMap<>();
    public static boolean noFlags = false;
    public static LinkedHashSet<Integer> craftingReferences = new LinkedHashSet<>();
    /** Research files, as registered by ThaumcraftApi.registerResearchLocation. */
    public static final List<ResourceLocation> jsonLocs = new ArrayList<>();

    public static boolean addKnowledge(Player player, PlayerKnowledge.EnumKnowledgeType type, @Nullable ResearchCategory category, int amount) {
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        if (!type.hasFields()) category = null;
        // TODO: ResearchEvent.Knowledge, and PacketKnowledgeGain for each full point gained (the HUD effect).
        knowledge.addKnowledge(type, category, amount);
        syncList.put(player.getUUID(), true);
        return true;
    }

    public static boolean completeResearch(Player player, String researchkey, boolean sync) {
        boolean b = false;
        while (progressResearch(player, researchkey, sync)) b = true;
        return b;
    }

    public static boolean completeResearch(Player player, String researchkey) {
        return completeResearch(player, researchkey, true);
    }

    public static boolean startResearchWithPopup(Player player, String researchkey) {
        boolean b = progressResearch(player, researchkey, true);
        if (b) {
            PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
            knowledge.setResearchFlag(researchkey, PlayerKnowledge.EnumResearchFlag.POPUP);
            knowledge.setResearchFlag(researchkey, PlayerKnowledge.EnumResearchFlag.RESEARCH);
        }
        return b;
    }

    public static boolean progressResearch(Player player, String researchkey) {
        return progressResearch(player, researchkey, true);
    }

    public static boolean progressResearch(Player player, String researchkey, boolean sync) {
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        if (knowledge.isResearchComplete(researchkey) || !doesPlayerHaveRequisites(player, researchkey)) return false;
        // TODO: ResearchEvent.Research, once the API events are ported.
        if (!knowledge.isResearchKnown(researchkey)) knowledge.addResearch(researchkey);
        ResearchEntry re = ResearchCategories.getResearch(researchkey);
        if (re != null) {
            boolean popups = true;
            if (re.getStages() != null) {
                ResearchStage[] stages = re.getStages();
                int cs = knowledge.getResearchStage(researchkey);
                ResearchStage currentStage = null;
                if (cs > 0) {
                    cs = Math.min(cs, stages.length);
                    currentStage = stages[cs - 1];
                }
                if (stages.length == 1 && cs == 0 && hasNoRequirements(stages[0])) {
                    ++cs;
                } else if (stages.length > 1 && stages.length <= cs + 1 && cs < stages.length && hasNoRequirements(stages[cs])) {
                    ++cs;
                }
                knowledge.setResearchStage(researchkey, Math.min(stages.length + 1, cs + 1));
                popups = cs >= stages.length;
                int warp = 0;
                if (currentStage != null) warp = currentStage.getWarp();
                if (popups) {
                    cs = Math.min(cs, stages.length);
                    currentStage = stages[cs - 1];
                }
                if (currentStage != null) {
                    warp += currentStage.getWarp();
                    if (warp > 0 && !Config.WUSS_MODE.getAsBoolean() && !player.level().isClientSide) {
                        if (warp > 1) {
                            int w2 = warp / 2;
                            if (warp - w2 > 0) InternalMethodHandler.addWarpToPlayer(player, warp - w2, PlayerWarp.EnumWarpType.PERMANENT);
                            if (w2 > 0) InternalMethodHandler.addWarpToPlayer(player, w2, PlayerWarp.EnumWarpType.NORMAL);
                        } else {
                            InternalMethodHandler.addWarpToPlayer(player, warp, PlayerWarp.EnumWarpType.PERMANENT);
                        }
                    }
                }
            }
            if (popups) {
                if (sync) {
                    knowledge.setResearchFlag(researchkey, PlayerKnowledge.EnumResearchFlag.POPUP);
                    if (!noFlags) {
                        knowledge.setResearchFlag(researchkey, PlayerKnowledge.EnumResearchFlag.RESEARCH);
                    } else {
                        noFlags = false;
                    }
                    if (re.getRewardItem() != null) {
                        for (ItemStack rs : re.getRewardItem()) {
                            if (!player.getInventory().add(rs.copy())) player.spawnAtLocation(rs.copy(), 1.0f);
                        }
                    }
                    if (re.getRewardKnow() != null) {
                        for (ResearchStage.Knowledge rk : re.getRewardKnow()) {
                            addKnowledge(player, rk.type, rk.category, rk.type.getProgression() * rk.amount);
                        }
                    }
                }
                for (ResearchCategory rc : ResearchCategories.researchCategories.values()) {
                    for (ResearchEntry ri : rc.research.values()) {
                        if (ri.getAddenda() == null || !knowledge.isResearchComplete(ri.getKey())) continue;
                        for (ResearchAddendum addendum : ri.getAddenda()) {
                            if (addendum.getResearch() != null && Arrays.asList(addendum.getResearch()).contains(researchkey)) {
                                player.sendSystemMessage(Component.translatable("tc.addaddendum", ri.getLocalizedName()));
                                knowledge.setResearchFlag(ri.getKey(), PlayerKnowledge.EnumResearchFlag.PAGE);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (re != null && re.getSiblings() != null) {
            for (String sibling : re.getSiblings()) {
                if (!knowledge.isResearchComplete(sibling) && doesPlayerHaveRequisites(player, sibling)) {
                    completeResearch(player, sibling, sync);
                }
            }
        }
        if (sync) {
            syncList.put(player.getUUID(), true);
            if (re != null) player.giveExperiencePoints(5);
        }
        return true;
    }

    private static boolean hasNoRequirements(ResearchStage stage) {
        return stage.getCraft() == null && stage.getObtain() == null && stage.getKnow() == null && stage.getResearch() == null;
    }

    public static boolean doesPlayerHaveRequisites(Player player, String key) {
        ResearchEntry ri = ResearchCategories.getResearch(key);
        if (ri == null) return true;
        String[] parents = ri.getParentsStripped();
        return parents == null || ThaumcraftCapabilities.knowsResearchStrict(player, parents);
    }

    public static Aspect getCombinationResult(Aspect aspect1, Aspect aspect2) {
        return Aspect.getCombinationResult(aspect1, aspect2);
    }

    // --- Parsing ---

    public static void parseAllResearch() {
        for (ResourceLocation loc : jsonLocs) {
            String s = "/assets/" + loc.getNamespace() + "/" + loc.getPath();
            if (!s.endsWith(".json")) s += ".json";
            InputStream stream = ResearchManager.class.getResourceAsStream(s);
            // Files of addons live in their own module, which only the class loader sees.
            if (stream == null) stream = ResearchManager.class.getClassLoader().getResourceAsStream(s.substring(1));
            if (stream == null) {
                Thaumcraft.LOGGER.warn("Research file not found: {}", loc);
                continue;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray entries = obj.get("entries").getAsJsonArray();
                int a = 0;
                for (JsonElement element : entries) {
                    ++a;
                    try {
                        addResearchToCategory(parseResearchJson(element.getAsJsonObject()));
                    } catch (Exception e) {
                        Thaumcraft.LOGGER.warn("Invalid research entry [{}] found in {}", a, loc, e);
                        --a;
                    }
                }
                Thaumcraft.LOGGER.info("Loaded {} research entries from {}", a, loc);
            } catch (Exception e) {
                Thaumcraft.LOGGER.warn("Invalid research file: {}", loc, e);
            }
        }
    }

    /** Parses one research entry and adds it to its category (used for the research note entries). */
    public static void addResearchJson(JsonObject obj) throws Exception {
        addResearchToCategory(parseResearchJson(obj));
    }

    private static ResearchEntry parseResearchJson(JsonObject obj) throws Exception {
        ResearchEntry entry = new ResearchEntry();
        entry.setKey(obj.getAsJsonPrimitive("key").getAsString());
        entry.setName(obj.getAsJsonPrimitive("name").getAsString());
        entry.setCategory(obj.getAsJsonPrimitive("category").getAsString());
        if (obj.has("icons")) {
            String[] icons = arrayJsonToString(obj.get("icons").getAsJsonArray());
            if (icons != null) {
                Object[] ir = new Object[icons.length];
                for (int a = 0; a < icons.length; ++a) {
                    ItemStack stack = parseJSONtoItemStack(icons[a]);
                    if (!stack.isEmpty()) {
                        ir[a] = stack;
                    } else if (icons[a].startsWith("focus")) {
                        ir[a] = icons[a];
                    } else {
                        // A texture, or an item not ported yet (the GUI draws those as unknown).
                        ir[a] = ResourceLocation.parse(icons[a].split(";")[0].toLowerCase(Locale.ROOT));
                    }
                }
                entry.setIcons(ir);
            }
        }
        if (obj.has("parents")) entry.setParents(arrayJsonToString(obj.get("parents").getAsJsonArray()));
        if (obj.has("siblings")) entry.setSiblings(arrayJsonToString(obj.get("siblings").getAsJsonArray()));
        if (obj.has("meta")) {
            String[] meta = arrayJsonToString(obj.get("meta").getAsJsonArray());
            if (meta != null) {
                ResearchEntry.EnumResearchMeta[] metas = new ResearchEntry.EnumResearchMeta[meta.length];
                for (int i = 0; i < meta.length; i++) metas[i] = ResearchEntry.EnumResearchMeta.valueOf(meta[i].toUpperCase());
                entry.setMeta(metas);
            }
        }
        if (obj.has("location")) {
            JsonArray location = obj.get("location").getAsJsonArray();
            if (location.size() == 2) {
                entry.setDisplayColumn(location.get(0).getAsInt());
                entry.setDisplayRow(location.get(1).getAsInt());
            }
        }
        if (obj.has("reward_item")) entry.setRewardItem(parseJsonItemList(arrayJsonToString(obj.get("reward_item").getAsJsonArray())));
        if (obj.has("reward_knowledge")) entry.setRewardKnow(parseKnowledge(arrayJsonToString(obj.get("reward_knowledge").getAsJsonArray())));
        List<ResearchStage> stages = new ArrayList<>();
        for (JsonElement element : obj.get("stages").getAsJsonArray()) {
            JsonObject stageObj = element.getAsJsonObject();
            ResearchStage stage = new ResearchStage();
            stage.setText(stageObj.getAsJsonPrimitive("text").getAsString());
            if (stageObj.has("recipes")) stage.setRecipes(arrayJsonToResourceLocations(stageObj.get("recipes").getAsJsonArray()));
            if (stageObj.has("required_item")) stage.setObtain(parseJsonOreList(arrayJsonToString(stageObj.get("required_item").getAsJsonArray())));
            if (stageObj.has("required_craft")) {
                stage.setCraft(parseJsonOreList(arrayJsonToString(stageObj.get("required_craft").getAsJsonArray())));
                if (stage.getCraft() != null) {
                    int[] refs = new int[stage.getCraft().length];
                    int q = 0;
                    for (Object o : stage.getCraft()) {
                        int code = o instanceof ItemStack stack ? createItemStackHash(stack) : ("tag:" + o).hashCode();
                        craftingReferences.add(code);
                        refs[q++] = code;
                    }
                    stage.setCraftReference(refs);
                }
            }
            if (stageObj.has("required_knowledge")) stage.setKnow(parseKnowledge(arrayJsonToString(stageObj.get("required_knowledge").getAsJsonArray())));
            if (stageObj.has("required_research")) {
                String[] research = arrayJsonToString(stageObj.get("required_research").getAsJsonArray());
                if (research != null) {
                    String[] rKey = new String[research.length];
                    String[] rIcn = new String[research.length];
                    for (int a = 0; a < research.length; ++a) {
                        String[] ss = research[a].split(";");
                        rKey[a] = ss[0];
                        rIcn[a] = ss.length > 1 ? ss[1] : null;
                    }
                    stage.setResearch(rKey);
                    stage.setResearchIcon(rIcn);
                }
            }
            if (stageObj.has("warp")) stage.setWarp(stageObj.getAsJsonPrimitive("warp").getAsInt());
            stages.add(stage);
        }
        if (!stages.isEmpty()) entry.setStages(stages.toArray(new ResearchStage[0]));
        if (obj.has("addenda")) {
            List<ResearchAddendum> addenda = new ArrayList<>();
            for (JsonElement element : obj.get("addenda").getAsJsonArray()) {
                JsonObject addendumObj = element.getAsJsonObject();
                ResearchAddendum addendum = new ResearchAddendum();
                addendum.setText(addendumObj.getAsJsonPrimitive("text").getAsString());
                if (addendumObj.has("recipes")) addendum.setRecipes(arrayJsonToResourceLocations(addendumObj.get("recipes").getAsJsonArray()));
                if (addendumObj.has("required_research")) addendum.setResearch(arrayJsonToString(addendumObj.get("required_research").getAsJsonArray()));
                addenda.add(addendum);
            }
            if (!addenda.isEmpty()) entry.setAddenda(addenda.toArray(new ResearchAddendum[0]));
        }
        return entry;
    }

    @Nullable
    private static ResearchStage.Knowledge[] parseKnowledge(@Nullable String[] sl) {
        if (sl == null) return null;
        List<ResearchStage.Knowledge> kl = new ArrayList<>();
        for (String s : sl) {
            ResearchStage.Knowledge k = ResearchStage.Knowledge.parse(s);
            if (k != null) kl.add(k);
        }
        return kl.isEmpty() ? null : kl.toArray(new ResearchStage.Knowledge[0]);
    }

    /** The original hashed stack.toString() ("1xitem.name@meta"); variants are separate items now, so the id is enough. */
    public static int createItemStackHash(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().hashCode();
    }

    @Nullable
    private static String[] arrayJsonToString(JsonArray jsonArray) {
        List<String> out = new ArrayList<>();
        for (JsonElement element : jsonArray) out.add(element.getAsString());
        return out.isEmpty() ? null : out.toArray(new String[0]);
    }

    @Nullable
    private static ResourceLocation[] arrayJsonToResourceLocations(JsonArray jsonArray) {
        List<ResourceLocation> out = new ArrayList<>();
        // 1.12 resource locations were lowercased by their constructor.
        for (JsonElement element : jsonArray) out.add(ResourceLocation.parse(element.getAsString().toLowerCase(Locale.ROOT)));
        return out.isEmpty() ? null : out.toArray(new ResourceLocation[0]);
    }

    @Nullable
    private static ItemStack[] parseJsonItemList(@Nullable String[] stacks) {
        if (stacks == null) return null;
        List<ItemStack> out = new ArrayList<>();
        for (String s : stacks) {
            ItemStack stack = parseJSONtoItemStack(s);
            if (!stack.isEmpty()) out.add(stack);
        }
        return out.isEmpty() ? null : out.toArray(new ItemStack[0]);
    }

    /** ItemStacks, item tag names (Strings) and enchantment requirements. Invalid entries are skipped. */
    @Nullable
    private static Object[] parseJsonOreList(@Nullable String[] stacks) {
        if (stacks == null) return null;
        List<Object> out = new ArrayList<>();
        for (String s : stacks) {
            if (s.startsWith("tag:")) {
                out.add(s.substring(4));
            } else if (s.startsWith("ench:")) {
                String[] ss = s.substring(5).split(";");
                ResourceLocation id = ResourceLocation.tryParse(ss[0]);
                if (id != null) out.add(new ResearchStage.EnchantmentRequirement(id, ss.length > 1 ? Integer.parseInt(ss[1]) : 1));
            } else {
                ItemStack stack = parseJSONtoItemStack(s);
                if (!stack.isEmpty()) out.add(stack);
            }
        }
        return out.isEmpty() ? null : out.toArray();
    }

    /** "namespace:id[;count][;aspect=tag]". Returns EMPTY for unknown items. */
    public static ItemStack parseJSONtoItemStack(@Nullable String entry) {
        if (entry == null) return ItemStack.EMPTY;
        String[] split = entry.split(";");
        ResourceLocation id = ResourceLocation.tryParse(split[0]);
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        if (item == Items.AIR) return ItemStack.EMPTY;
        int num = 1;
        Aspect aspect = null;
        for (int a = 1; a < split.length; ++a) {
            if (split[a].startsWith("aspect=")) {
                aspect = Aspect.getAspect(split[a].substring(7));
            } else {
                try {
                    num = Integer.parseInt(split[a]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        ItemStack stack;
        if (aspect != null && item == ConfigItems.crystalEssence.get()) {
            stack = ConfigItems.crystalEssence.get().makeCrystal(aspect, num);
        } else if (aspect != null && item == ConfigItems.phial.get()) {
            stack = ItemPhial.makeFilledPhial(aspect);
            stack.setCount(num);
        } else {
            stack = new ItemStack(item, num);
        }
        return stack;
    }

    private static void addResearchToCategory(ResearchEntry ri) {
        ResearchCategory rl = ResearchCategories.getResearchCategory(ri.getCategory());
        if (rl != null && !rl.research.containsKey(ri.getKey())) {
            for (ResearchEntry rr : rl.research.values()) {
                if (rr.getDisplayColumn() == ri.getDisplayColumn() && rr.getDisplayRow() == ri.getDisplayRow()) {
                    Thaumcraft.LOGGER.warn("Research [{}] not added as it overlaps with existing research [{}] at {},{}",
                            ri.getKey(), rr.getKey(), ri.getDisplayColumn(), rr.getDisplayRow());
                    return;
                }
            }
            rl.research.put(ri.getKey(), ri);
            if (ri.getDisplayColumn() < rl.minDisplayColumn) rl.minDisplayColumn = ri.getDisplayColumn();
            if (ri.getDisplayRow() < rl.minDisplayRow) rl.minDisplayRow = ri.getDisplayRow();
            if (ri.getDisplayColumn() > rl.maxDisplayColumn) rl.maxDisplayColumn = ri.getDisplayColumn();
            if (ri.getDisplayRow() > rl.maxDisplayRow) rl.maxDisplayRow = ri.getDisplayRow();
        } else {
            Thaumcraft.LOGGER.warn("Could not add invalid research entry {}", ri.getKey());
        }
    }
}
