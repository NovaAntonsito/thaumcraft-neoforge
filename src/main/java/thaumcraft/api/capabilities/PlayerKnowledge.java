package thaumcraft.api.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;

/**
 * Port of IPlayerKnowledge + PlayerKnowledge.DefaultImpl, now a data attachment.
 * <p>
 * Unless there are specific tasks you need to perform, you are better off using ResearchManager.completeResearch and
 * addKnowledge. These methods are purely for manipulation of the player's knowledge list without any checks.
 */
public class PlayerKnowledge {
    public enum EnumResearchStatus {
        UNKNOWN, COMPLETE, IN_PROGRESS
    }

    public enum EnumResearchFlag implements StringRepresentable {
        PAGE, RESEARCH, POPUP;

        public static final Codec<EnumResearchFlag> CODEC = StringRepresentable.fromEnum(EnumResearchFlag::values);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    public enum EnumKnowledgeType {
        THEORY(32, true, "T"),
        OBSERVATION(16, true, "O");

        private final int progression;
        private final boolean hasFields;
        private final String abbr;

        EnumKnowledgeType(int progression, boolean hasFields, String abbr) {
            this.progression = progression;
            this.hasFields = hasFields;
            this.abbr = abbr;
        }

        /** Raw points needed for one full knowledge point. */
        public int getProgression() {
            return progression;
        }

        public boolean hasFields() {
            return hasFields;
        }

        public String getAbbreviation() {
            return abbr;
        }
    }

    /** One research the player knows, as written by serializeNBT: its key, stage (if any) and flags. */
    private record Entry(String key, int stage, List<EnumResearchFlag> flags) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(Entry::key),
                Codec.INT.optionalFieldOf("stage", 0).forGetter(Entry::stage),
                EnumResearchFlag.CODEC.listOf().optionalFieldOf("flags", List.of()).forGetter(Entry::flags)
        ).apply(i, Entry::new));
    }

    public static final Codec<PlayerKnowledge> CODEC = RecordCodecBuilder.create(i -> i.group(
            Entry.CODEC.listOf().optionalFieldOf("research", List.of()).forGetter(PlayerKnowledge::entries),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("knowledge", Map.of()).forGetter(k -> k.knowledge)
    ).apply(i, PlayerKnowledge::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnowledge> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private final Set<String> research = new LinkedHashSet<>();
    private final Map<String, Integer> stages = new HashMap<>();
    private final Map<String, EnumSet<EnumResearchFlag>> flags = new HashMap<>();
    private final Map<String, Integer> knowledge = new HashMap<>();

    public PlayerKnowledge() {
        addAutoUnlockResearch();
    }

    /** deserializeNBT */
    private PlayerKnowledge(List<Entry> entries, Map<String, Integer> knowledge) {
        for (Entry e : entries) {
            if (isResearchKnown(e.key())) continue;
            research.add(e.key());
            if (e.stage() > 0) stages.put(e.key(), e.stage());
            for (EnumResearchFlag flag : e.flags()) setResearchFlag(e.key(), flag);
        }
        this.knowledge.putAll(knowledge);
        addAutoUnlockResearch();
    }

    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        for (String key : research) {
            EnumSet<EnumResearchFlag> f = flags.get(key);
            out.add(new Entry(key, stages.getOrDefault(key, 0), f == null ? List.of() : List.copyOf(f)));
        }
        return out;
    }

    /** A snapshot of this knowledge (what serializeNBT + deserializeNBT gave the original sync packet). */
    public PlayerKnowledge copy() {
        return new PlayerKnowledge(entries(), knowledge);
    }

    private void addAutoUnlockResearch() {
        for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : cat.research.values()) {
                if (ri.hasMeta(ResearchEntry.EnumResearchMeta.AUTOUNLOCK)) addResearch(ri.getKey());
            }
        }
    }

    /** Clears all research and knowledge. */
    public void clear() {
        research.clear();
        flags.clear();
        stages.clear();
        knowledge.clear();
    }

    // --- Research ---

    /** @return if the research is known and if it is completed or still in progress */
    public EnumResearchStatus getResearchStatus(String res) {
        if (!isResearchKnown(res)) return EnumResearchStatus.UNKNOWN;
        ResearchEntry entry = ResearchCategories.getResearch(res);
        if (entry == null || entry.getStages() == null || getResearchStage(res) > entry.getStages().length) {
            return EnumResearchStatus.COMPLETE;
        }
        return EnumResearchStatus.IN_PROGRESS;
    }

    /**
     * You may also pass in research as 'example@2': true if 'example' is known and at least at stage 2.
     * An empty string is always known (used by crafting recipes).
     */
    public boolean isResearchKnown(@Nullable String res) {
        if (res == null) return false;
        if (res.isEmpty()) return true;
        String[] ss = res.split("@");
        return (ss.length <= 1 || getResearchStage(ss[0]) >= Mth.getInt(ss[1], 0)) && research.contains(ss[0]);
    }

    public boolean isResearchComplete(String res) {
        return getResearchStatus(res) == EnumResearchStatus.COMPLETE;
    }

    /**
     * Stages start at 1 and research is 'complete' if the stage exceeds the number of stages in the entry.
     * Returns 0 if the research is known but has no stage set, -1 if it is not known at all.
     */
    public int getResearchStage(@Nullable String res) {
        if (res == null || !research.contains(res)) return -1;
        return stages.getOrDefault(res, 0);
    }

    public boolean setResearchStage(@Nullable String res, int stage) {
        if (res == null || !research.contains(res) || stage <= 0) return false;
        stages.put(res, stage);
        return true;
    }

    public boolean addResearch(String res) {
        if (!isResearchKnown(res)) {
            research.add(res);
            return true;
        }
        return false;
    }

    public boolean removeResearch(String res) {
        if (isResearchKnown(res)) {
            research.remove(res);
            return true;
        }
        return false;
    }

    public Set<String> getResearchList() {
        return Collections.unmodifiableSet(research);
    }

    public boolean setResearchFlag(String res, EnumResearchFlag flag) {
        return flags.computeIfAbsent(res, k -> EnumSet.noneOf(EnumResearchFlag.class)).add(flag);
    }

    public boolean clearResearchFlag(String res, EnumResearchFlag flag) {
        EnumSet<EnumResearchFlag> list = flags.get(res);
        if (list != null) {
            boolean b = list.remove(flag);
            if (list.isEmpty()) flags.remove(res);
            return b;
        }
        return false;
    }

    public boolean hasResearchFlag(String res, EnumResearchFlag flag) {
        EnumSet<EnumResearchFlag> list = flags.get(res);
        return list != null && list.contains(flag);
    }

    // --- Knowledge (theory and observation points) ---

    private static String getKey(EnumKnowledgeType type, @Nullable ResearchCategory category) {
        return type.getAbbreviation() + "_" + (category == null ? "" : category.key);
    }

    public boolean addKnowledge(EnumKnowledgeType type, @Nullable ResearchCategory category, int amount) {
        String key = getKey(type, category);
        int c = getKnowledgeRaw(type, category);
        if (c + amount < 0) return false;
        knowledge.put(key, c + amount);
        return true;
    }

    /** Full points (raw points divided by the type's progression). */
    public int getKnowledge(EnumKnowledgeType type, @Nullable ResearchCategory category) {
        return (int) Math.floor(getKnowledgeRaw(type, category) / (double) type.getProgression());
    }

    public int getKnowledgeRaw(EnumKnowledgeType type, @Nullable ResearchCategory category) {
        return knowledge.getOrDefault(getKey(type, category), 0);
    }
}
