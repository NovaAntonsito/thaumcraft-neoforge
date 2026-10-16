package thaumcraft.common.lib.research;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.consumables.ItemResearchNote;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.playerdata.PacketAspectDiscovery;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.lib.utils.InventoryUtils;

/**
 * The TC4 research notes on top of the TC6 Thaumonomicon, from TC4 Research Port: Reborn (OldResearchManager).
 * <p>
 * {@link #patchResearch} turns the THEORY knowledge a research stage asks for into one note ("rn_KEY_stage"),
 * solved with the hex minigame at a research table, and drops the OBSERVATION knowledge (TC6 theorycraft is not ported).
 */
public final class ResearchNoteManager {
    public static final List<Aspect> PRIMAL_ASPECTS = PlayerAspects.PRIMAL_ASPECTS;
    private static final Map<String, ResearchNoteData> NOTES = new LinkedHashMap<>();
    private static final Map<String, List<String>> IMPLICIT_PARENTS = new HashMap<>();
    private static final Map<String, ResearchNotePattern> NOTE_PATTERNS = new HashMap<>();
    public static final Map<Aspect, Integer> ASPECT_COMPLEXITY = new LinkedHashMap<>();
    private static final Random RANDOM = new Random();
    private static final String[] PATTERN_FILES = {"alchemy", "auromancy", "basics", "eldritch", "golemancy", "infusion", "artifice"};

    private ResearchNoteManager() {
    }

    /** ManualRegister midInit + postInit + loadComplete: patterns, research patch, aspect complexity, implicit links. */
    public static void init() {
        for (String file : PATTERN_FILES) ResearchNotePatternParser.putIntoQueue(Thaumcraft.id("research/patterns/" + file));
        ResearchNotePatternParser.parseQueue().forEach(p -> NOTE_PATTERNS.put(p.noteKey(), p));
        patchResearch();
        computeAspectComplexity();
        registerImplicitParents("UNLOCKELDRITCH", "RIFTCLOSER");
    }

    public static void registerImplicitParents(String research, String... parents) {
        IMPLICIT_PARENTS.put(research, Arrays.asList(parents));
    }

    public static void computeAspectComplexity() {
        Set<String> blackList = new HashSet<>(Config.ASPECT_BLACKLIST.get());
        for (Aspect aspect : Aspect.aspects.values()) {
            int comp = blackList.contains(aspect.getTag()) ? -1 : computeAspectComplexity(aspect, 0);
            ASPECT_COMPLEXITY.put(aspect, comp);
        }
    }

    /** Runs after the research files are parsed (and again after /thaumcraft research reload). */
    public static void patchResearch() {
        NOTES.clear();
        ResearchCategory basics = ResearchCategories.getResearchCategory("BASICS");
        if (basics != null) {
            basics.research.remove("KNOWLEDGETYPES");
            basics.research.remove("THEORYRESEARCH");
            ResearchEntry scan = basics.research.get("CELESTIALSCANNING");
            if (scan != null) {
                scan.setDisplayColumn(2);
                scan.setDisplayRow(-3);
                scan.setParents(new String[]{"RESEARCH"});
                scan.getStages()[0].setText("research.CELESTIALSCANNINGOLD.stage.1");
                ResearchEntry.EnumResearchMeta[] meta = scan.getMeta() == null ? new ResearchEntry.EnumResearchMeta[0] : scan.getMeta();
                if (!scan.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN)) {
                    meta = Arrays.copyOf(meta, meta.length + 1);
                    meta[meta.length - 1] = ResearchEntry.EnumResearchMeta.HIDDEN;
                }
                scan.setMeta(meta);
            }
        }

        loadJsonResearchDirect(Thaumcraft.id("research/oldresearch"));

        int patchedStages = 0;
        int patchedResearches = 0;
        for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry entry : category.research.values()) {
                ResearchStage[] stages = entry.getStages();
                if (stages == null) continue;
                patchedResearches++;
                for (int i = 0; i != stages.length; i++) {
                    ResearchStage stage = stages[i];
                    patchedStages++;
                    if (stage == null || stage.getKnow() == null) continue;
                    int theoryCount = 0;
                    for (ResearchStage.Knowledge knowledge : stage.getKnow()) {
                        if (knowledge.type == PlayerKnowledge.EnumKnowledgeType.THEORY) theoryCount++;
                    }
                    stage.setKnow(null);
                    if (theoryCount == 0) continue;

                    String key = "rn_" + entry.getKey() + "_" + i;
                    stage.setResearch(append(stage.getResearch(), key));
                    stage.setResearchIcon(append(stage.getResearchIcon(), null));
                    NOTES.put(key, createNote(key, theoryCount));
                }
            }
        }
        Thaumcraft.LOGGER.info("Research notes: patched {} stages in {} researches and created {} research notes",
                patchedStages, patchedResearches, NOTES.size());
    }

    private static String[] append(@Nullable String[] array, @Nullable String value) {
        if (array == null) return new String[]{value};
        String[] out = Arrays.copyOf(array, array.length + 1);
        out[array.length] = value;
        return out;
    }

    private static int computeAspectComplexity(Aspect aspect, int depth) {
        if (aspect.isPrimal()) return PRIMAL_ASPECTS.contains(aspect) ? depth : -1;
        List<Integer> childDepths = new ArrayList<>();
        for (Aspect asp : aspect.getComponents()) childDepths.add(computeAspectComplexity(asp, depth + 1));
        return Collections.max(childDepths);
    }

    public static int getAspectComplexity(Aspect a) {
        return ASPECT_COMPLEXITY.getOrDefault(a, -1);
    }

    public static List<String> parentsOfResearch(String research) {
        ResearchEntry res = ResearchCategories.getResearch(research);
        if (res != null) {
            String[] parents = res.getParentsClean();
            if (parents != null) {
                List<String> ret = new ArrayList<>(Arrays.asList(parents));
                List<String> implicit = IMPLICIT_PARENTS.get(research);
                if (implicit != null) ret.addAll(implicit);
                return ret;
            }
        }
        return Collections.emptyList();
    }

    public static AspectList getRandomAspects(Random rand, int maxComplexity, int quantity) {
        List<Aspect> possible = ASPECT_COMPLEXITY.keySet().stream().filter(aspect -> {
            int comp = ASPECT_COMPLEXITY.get(aspect);
            return comp != -1 && comp <= maxComplexity && comp >= Mth.clamp((maxComplexity / 4) - 2, 0, 2);
        }).collect(Collectors.toList());
        AspectList selected = new AspectList();
        int upto = Math.min(quantity, possible.size());
        for (int i = 0; i < upto; i++) {
            int toadd = rand.nextInt(possible.size());
            selected.add(possible.get(toadd), 1);
            possible.remove(toadd);
        }
        return selected;
    }

    private static ResearchNoteData createNote(String key, int theoriesCount) {
        ResearchNoteData data = new ResearchNoteData();
        data.key = key;
        data.mergedTeories = theoriesCount;
        ResearchNotePattern pattern = NOTE_PATTERNS.get(key);
        if (pattern != null) {
            data.color = pattern.color();
        } else {
            Aspect[] asps = Aspect.aspects.values().toArray(new Aspect[0]);
            RANDOM.setSeed(key.hashCode());
            data.color = asps[RANDOM.nextInt(asps.length)].getColor();
        }
        return data;
    }

    public static boolean isNote(String key) {
        return NOTES.containsKey(key);
    }

    /** The blank note for a key, before its grid is drawn. Null if no stage asks for it. */
    @Nullable
    public static ItemStack noteStack(String key) {
        ResearchNoteData data = NOTES.get(key);
        if (data == null) return null;
        ItemStack stack = new ItemStack(ConfigItems.researchNote.get());
        ItemResearchNote.setData(stack, data.copy());
        return stack;
    }

    /** Breadth-first over the research's parents: the theories every earlier note asked for, plus one, times the config multiplier. */
    public static int getResearchComplexity(String key) {
        int sum = 0;
        Set<String> seen = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        String start = getStrippedKey(key);
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty()) {
            String res = queue.poll();
            ResearchEntry research = ResearchCategories.getResearch(res);
            if (research != null && research.getStages() != null) {
                for (ResearchStage stage : research.getStages()) {
                    if (stage == null || stage.getResearch() == null) continue;
                    for (String s : stage.getResearch()) {
                        ResearchNoteData note = NOTES.get(s);
                        if (s.startsWith("rn_") && note != null) sum += note.mergedTeories;
                    }
                }
            }
            for (String parent : parentsOfResearch(res)) {
                if (seen.add(parent)) queue.add(parent);
            }
        }
        return (int) ((sum + 1) * Config.RESEARCH_DIFFICULTY_MULTIPLIER.get());
    }

    /** The thaumonomicon's "get note": costs one ink and one paper (free in creative). */
    public static void givePlayerResearchNote(Level world, Player player, String key) {
        if (hasResearchNote(player, key)) return;
        if (!player.isCreative() && (!consumeInkFromPlayer(player, false)
                || !InventoryUtils.isPlayerCarryingAmount(player, new ItemStack(Items.PAPER)))) {
            return;
        }
        ItemStack note = noteStack(key);
        if (note == null) return;
        if (!player.isCreative()) {
            InventoryUtils.consumePlayerItem(player, new ItemStack(Items.PAPER), true);
            consumeInkFromPlayer(player, true);
        }
        ResearchNoteData data = computeNoteData(world, key);
        if (data != null) ItemResearchNote.setData(note, data);
        if (!player.getInventory().add(note)) player.drop(note, false);
        player.inventoryMenu.broadcastChanges();
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.WRITE.get(), SoundSource.PLAYERS, 0.75f, 1.0f);
    }

    /** Client check before asking for a note: tools, paper and ink, with a chat message for what is missing. */
    public static boolean playerHasInk(Player player, boolean sendMessage) {
        if (player.isCreative()) return true;
        boolean tools = player.getInventory().items.stream().anyMatch(s -> s.getItem() instanceof IScribeTools);
        if (!tools) {
            if (sendMessage) player.sendSystemMessage(Component.translatable("tile.researchtable.need.st").withStyle(ChatFormatting.RED));
            return false;
        }
        if (!player.getInventory().contains(new ItemStack(Items.PAPER))) {
            if (sendMessage) player.sendSystemMessage(Component.translatable("tile.researchtable.need.paper").withStyle(ChatFormatting.RED));
            return false;
        }
        if (!consumeInkFromPlayer(player, false)) {
            if (sendMessage) {
                player.sendSystemMessage(Component.translatable("tile.researchtable.noink.0").withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.translatable("tile.researchtable.noink.1").withStyle(ChatFormatting.RED));
            }
            return false;
        }
        return true;
    }

    @Nullable
    public static ResearchNoteData computeNoteData(Level world, String key) {
        ResearchNotePattern pattern = NOTE_PATTERNS.get(key);
        return pattern != null ? computeNoteDataFromPattern(pattern, key) : computeNoteDataFromRandom(world, key);
    }

    @Nullable
    private static ResearchNoteData computeNoteDataFromPattern(ResearchNotePattern pattern, String key) {
        ResearchNoteData template = NOTES.get(key);
        if (template == null) return null;
        ResearchNoteData data = template.copy();
        Random rand = new Random((31 * pattern.seed()) + key.hashCode());
        data.generateHexes(rand, pattern.aspects(), pattern, pattern.radius(), pattern.blanks());
        return data;
    }

    @Nullable
    private static ResearchNoteData computeNoteDataFromRandom(Level world, String key) {
        ResearchNoteData template = NOTES.get(key);
        if (template == null) return null;
        ResearchNoteData data = template.copy();
        long worldSeed = world instanceof ServerLevel sl ? sl.getSeed() : 0L;
        Random rand = new Random(31 * ((31 * worldSeed) + key.hashCode()) + data.color);
        int complexity = getResearchComplexity(key) + data.mergedTeories;
        int complexityClamped = Mth.clamp(complexity, 0, 12);
        AspectList aspects = getRandomAspects(rand, complexity, Math.min(11, complexityClamped + 2));
        data.generateHexesFromComp(rand, aspects, complexityClamped);
        return data;
    }

    /** Scribing tools in the inventory that still have ink. doIt uses one charge; an empty inkwell is kept, not broken. */
    public static boolean consumeInkFromPlayer(Player player, boolean doIt) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof IScribeTools && stack.getDamageValue() < stack.getMaxDamage()) {
                if (doIt && !thaumcraft.common.anomaly.AnomalyModifiers.freeInk(player.level())) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean hasResearchNote(Player player, String key) {
        for (ItemStack stack : player.getInventory().items) {
            ResearchNoteData data = ItemResearchNote.getData(stack);
            if (data != null && data.key.equals(key)) return true;
        }
        return false;
    }

    /** "rn_KEY_2" to "KEY". */
    public static String getStrippedKey(String key) {
        int a = key.indexOf('_');
        int b = key.lastIndexOf('_');
        return a >= 0 && b > a ? key.substring(a + 1, b) : key;
    }

    /** Whether two hexes link: both aspects known, and one is a direct component of the other. */
    public static boolean canConnect(PlayerAspects known, @Nullable Aspect aspect1, @Nullable Aspect aspect2) {
        if (!known.isKnowAspect(aspect1) || !known.isKnowAspect(aspect2)) return false;
        return !aspect1.isPrimal() && (aspect1.getComponents()[0] == aspect2 || aspect1.getComponents()[1] == aspect2)
                || !aspect2.isPrimal() && (aspect2.getComponents()[0] == aspect1 || aspect2.getComponents()[1] == aspect1);
    }

    /**
     * The note is solved when every source aspect links to the first one. Then the cells that are not part of
     * the chain are dropped and the note is marked complete (it becomes a discovery).
     */
    public static boolean checkResearchCompletion(ItemStack contents, ResearchNoteData note, Player player) {
        PlayerAspects known = ThaumcraftCapabilities.getAspects(player);
        List<String> checked = new ArrayList<>();
        List<String> main = new ArrayList<>();
        List<String> remains = new ArrayList<>();
        for (HexUtils.Hex hex : note.hexes.values()) {
            if (note.hexEntries.get(hex.toString()).type() == 1) main.add(hex.toString());
        }
        for (HexUtils.Hex hex : note.hexes.values()) {
            if (note.hexEntries.get(hex.toString()).type() == 1) {
                main.remove(hex.toString());
                checkConnections(note, hex, checked, main, remains, known);
                break;
            }
        }
        if (!main.isEmpty()) return false;
        List<String> remove = new ArrayList<>();
        for (HexUtils.Hex hex : note.hexes.values()) {
            if (note.hexEntries.get(hex.toString()).type() != 1 && !remains.contains(hex.toString())) remove.add(hex.toString());
        }
        for (String s : remove) {
            note.hexEntries.remove(s);
            note.hexes.remove(s);
        }
        note.complete = true;
        ItemResearchNote.setData(contents, note);
        return true;
    }

    private static void checkConnections(ResearchNoteData note, HexUtils.Hex hex, List<String> checked, List<String> main, List<String> remains, PlayerAspects known) {
        checked.add(hex.toString());
        for (int a = 0; a < 6; ++a) {
            HexUtils.Hex target = hex.getNeighbour(a);
            ResearchNoteData.HexEntry te = note.hexEntries.get(target.toString());
            if (!checked.contains(target.toString()) && te != null && te.type() >= 1
                    && canConnect(known, note.hexEntries.get(hex.toString()).aspect(), te.aspect())) {
                remains.add(target.toString());
                if (te.type() == 1) main.remove(target.toString());
                checkConnections(note, target, checked, main, remains, known);
            }
        }
    }

    /**
     * ScanManager.checkAndSyncAspectKnowledge: adds points to the player's pool. A new aspect is discovered
     * with two bonus points; past aspectTotalCap the gain shrinks.
     */
    public static void checkAndSyncAspectKnowledge(ServerPlayer player, Aspect aspect, int amount) {
        PlayerAspects aspects = ThaumcraftCapabilities.getAspects(player);
        if (!aspects.isKnowAspect(aspect)) {
            PacketDistributor.sendToPlayer(player, new PacketAspectDiscovery(aspect.getTag()));
            amount += 2;
        }
        int cap = Config.ASPECT_TOTAL_CAP.get();
        if (aspects.aspectCount(aspect) >= cap) amount = (int) Math.sqrt(amount);
        if (amount > 1 && (float) aspects.aspectCount(aspect) >= (float) cap * 1.25f) amount = 1;
        if (aspects.addToAspectPool(aspect, amount)) {
            PacketDistributor.sendToPlayer(player, new PacketAspectPool(aspect.getTag(), amount, aspects.aspectCount(aspect)));
        }
    }

    /** Not in Thaumcraft: raises each primal to primalAspectFloor. Returns true if anything changed (the caller syncs). */
    public static boolean topUpPrimals(PlayerAspects aspects) {
        int floor = Config.PRIMAL_ASPECT_FLOOR.get();
        boolean changed = false;
        for (Aspect primal : PRIMAL_ASPECTS) {
            if (aspects.aspectCount(primal) < floor) {
                aspects.setAspectCount(primal, floor);
                changed = true;
            }
        }
        return changed;
    }

    /** Takes points out of the pool and tells the client the new total (no notification). */
    public static boolean spendAspect(ServerPlayer player, Aspect aspect, int amount) {
        PlayerAspects aspects = ThaumcraftCapabilities.getAspects(player);
        if (!aspects.addToAspectPool(aspect, -amount)) return false;
        topUpPrimals(aspects);
        PacketDistributor.sendToPlayer(player, new PacketAspectPool(aspect.getTag(), 0, aspects.aspectCount(aspect)));
        return true;
    }

    /** Reads the extra research entries (RESEARCH, RESEARCHEXPERTISE, ...) into their categories. */
    private static void loadJsonResearchDirect(ResourceLocation loc) {
        String s = "/assets/" + loc.getNamespace() + "/" + loc.getPath() + ".json";
        try (InputStream stream = ResearchNoteManager.class.getResourceAsStream(s)) {
            if (stream == null) {
                Thaumcraft.LOGGER.warn("Research file not found: {}", loc);
                return;
            }
            JsonObject obj = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            int a = 0;
            for (JsonElement element : obj.get("entries").getAsJsonArray()) {
                try {
                    ResearchManager.addResearchJson(element.getAsJsonObject());
                    a++;
                } catch (Exception e) {
                    Thaumcraft.LOGGER.warn("Invalid research entry [{}] found in {}", a, loc, e);
                }
            }
            Thaumcraft.LOGGER.info("Loaded {} research entries from {}", a, loc);
        } catch (Exception e) {
            Thaumcraft.LOGGER.warn("Invalid research file: {}", loc, e);
        }
    }
}
