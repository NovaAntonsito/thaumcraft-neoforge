package voidexpeditions.run;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A player's expeditions, synced to them. The run: whether one is going, its difficulty and length, the encounter
 * they are in (1-based), the perks picked (ids; repeats stack), the three offered at the portal if any, whether the
 * last encounter's boss fell and whether the extra life is still there. The loadout bought at the terminal for the
 * next run: an extra life and a starting perk; and whether a focus was already bought for it. And how many runs
 * were started on which day, for the daily limit.
 */
public final class RunState {
    public static final Codec<RunState> CODEC = CompoundTag.CODEC.xmap(RunState::fromTag, RunState::toTag);
    public static final StreamCodec<ByteBuf, RunState> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(RunState::fromTag, RunState::toTag);

    public boolean active;
    public Difficulty difficulty = Difficulty.RESTLESS;
    public int length = RunLength.OPTIONS[0];
    public int stage;
    public final List<String> perks = new ArrayList<>();
    public final List<String> offered = new ArrayList<>();
    public boolean bossSlain;
    public boolean extraLife;

    public boolean nextExtraLife;
    public String nextStartPerk = "";
    public boolean focusBought;

    public long day = -1;
    public int runsToday;

    /** How many times a perk was picked this run. */
    public int count(String perk) {
        int n = 0;
        for (String p : perks) {
            if (p.equals(perk)) n++;
        }
        return n;
    }

    /** Runs left today, {@code today} being the overworld's day. */
    public int runsLeft(long today) {
        return Expeditions.RUNS_PER_DAY - (day == today ? runsToday : 0);
    }

    /** Starts a run, spending the loadout. */
    public void start(Difficulty difficulty, int length, long today) {
        if (day != today) {
            day = today;
            runsToday = 0;
        }
        runsToday++;
        this.active = true;
        this.difficulty = difficulty;
        this.length = length;
        this.stage = 1;
        this.perks.clear();
        this.offered.clear();
        this.bossSlain = false;
        this.extraLife = nextExtraLife;
        if (!nextStartPerk.isEmpty()) perks.add(nextStartPerk);
        nextExtraLife = false;
        nextStartPerk = "";
        focusBought = false;
    }

    public void clear() {
        active = false;
        stage = 0;
        perks.clear();
        offered.clear();
        bossSlain = false;
        extraLife = false;
    }

    private static ListTag strings(List<String> list) {
        ListTag tag = new ListTag();
        for (String s : list) tag.add(StringTag.valueOf(s));
        return tag;
    }

    private static void readStrings(CompoundTag tag, String key, List<String> into) {
        for (Tag t : tag.getList(key, Tag.TAG_STRING)) into.add(t.getAsString());
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Active", active);
        tag.putInt("Difficulty", difficulty.ordinal());
        tag.putInt("Length", length);
        tag.putInt("Stage", stage);
        tag.put("Perks", strings(perks));
        tag.put("Offered", strings(offered));
        tag.putBoolean("BossSlain", bossSlain);
        tag.putBoolean("ExtraLife", extraLife);
        tag.putBoolean("NextExtraLife", nextExtraLife);
        tag.putString("NextStartPerk", nextStartPerk);
        tag.putBoolean("FocusBought", focusBought);
        tag.putLong("Day", day);
        tag.putInt("RunsToday", runsToday);
        return tag;
    }

    public static RunState fromTag(CompoundTag tag) {
        RunState s = new RunState();
        s.active = tag.getBoolean("Active");
        s.difficulty = Difficulty.byIndex(tag.getInt("Difficulty"));
        s.length = tag.contains("Length") ? tag.getInt("Length") : RunLength.OPTIONS[0];
        s.stage = tag.getInt("Stage");
        readStrings(tag, "Perks", s.perks);
        readStrings(tag, "Offered", s.offered);
        s.bossSlain = tag.getBoolean("BossSlain");
        s.extraLife = tag.getBoolean("ExtraLife");
        s.nextExtraLife = tag.getBoolean("NextExtraLife");
        s.nextStartPerk = tag.getString("NextStartPerk");
        s.focusBought = tag.getBoolean("FocusBought");
        s.day = tag.contains("Day") ? tag.getLong("Day") : -1;
        s.runsToday = tag.getInt("RunsToday");
        return s;
    }
}
