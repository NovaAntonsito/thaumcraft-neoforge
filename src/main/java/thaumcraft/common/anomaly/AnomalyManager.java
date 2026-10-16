package thaumcraft.common.anomaly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Config;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.common.world.aura.AuraWorld;

/**
 * The anomaly clock of one dimension, saved with the world. Every interval (config, with some jitter) a new
 * anomaly is drawn, weighted by the state of the aura. While as many anomalies as allowed are running, the clock
 * is paused. Every 5 anomalies the tier goes up; from tier 3 two can run at once.
 * <p>
 * The next anomalies are drawn when a cycle starts ({@link #upcoming}), so the thaumometer can foretell what kind
 * comes. When the clock runs out and a player in the dimension knows {@link #SEER_RESEARCH}, the first
 * {@link #CHOICES} are offered to them as cards ({@link PacketAnomalyChoice}); the clock waits until one is picked,
 * or for {@link #CHOICE_TICKS}, then one of them is drawn at random.
 */
public class AnomalyManager extends SavedData {
    private static final String NAME = "thaumcraft_anomalies";
    private static final Factory<AnomalyManager> FACTORY = new Factory<>(AnomalyManager::new, AnomalyManager::load, null);

    private long cycleStart = -1L;
    private long nextAt = -1L;
    private int cycle;
    private final List<ActiveAnomaly> active = new ArrayList<>();
    public static final String SEER_RESEARCH = "FORESIGHT";
    public static final int CHOICES = 3;
    public static final long CHOICE_TICKS = 1200L;
    /** The anomalies the next cycle will draw from, first one first. */
    private final List<String> upcoming = new ArrayList<>();
    /** The cards offered to the seers right now; empty when nobody is choosing. */
    private final List<String> choice = new ArrayList<>();
    private long choiceEnd = -1L;

    public static boolean isEnabledIn(ServerLevel level) {
        return Config.ANOMALIES_ENABLED.get()
                && Config.ANOMALY_DIMENSIONS.get().contains(level.dimension().location().toString());
    }

    /** The manager of that dimension. Only call it when {@link #isEnabledIn} is true, so other dimensions get no file. */
    public static AnomalyManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    public static AnomalyManager load(CompoundTag tag, HolderLookup.Provider provider) {
        AnomalyManager m = new AnomalyManager();
        m.cycleStart = tag.getLong("cycleStart");
        m.nextAt = tag.getLong("nextAt");
        m.cycle = tag.getInt("cycle");
        ListTag list = tag.getList("active", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ActiveAnomaly a = ActiveAnomaly.load(list.getCompound(i));
            if (a.type() != null) m.active.add(a);
        }
        readIds(tag.getList("upcoming", Tag.TAG_STRING), m.upcoming);
        readIds(tag.getList("choice", Tag.TAG_STRING), m.choice);
        m.choiceEnd = tag.getLong("choiceEnd");
        return m;
    }

    private static void readIds(ListTag list, List<String> into) {
        for (int i = 0; i < list.size(); i++) {
            String id = list.getString(i);
            if (Anomalies.get(id) != null) into.add(id);
        }
    }

    private static ListTag writeIds(List<String> ids) {
        ListTag list = new ListTag();
        for (String id : ids) list.add(StringTag.valueOf(id));
        return list;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong("cycleStart", cycleStart);
        tag.putLong("nextAt", nextAt);
        tag.putInt("cycle", cycle);
        ListTag list = new ListTag();
        for (ActiveAnomaly a : active) list.add(a.save());
        tag.put("active", list);
        tag.put("upcoming", writeIds(upcoming));
        tag.put("choice", writeIds(choice));
        tag.putLong("choiceEnd", choiceEnd);
        return tag;
    }

    public List<ActiveAnomaly> getActive() {
        return Collections.unmodifiableList(active);
    }

    public int getCycle() {
        return cycle;
    }

    public int getTier() {
        return 1 + cycle / 5;
    }

    public int getMaxActive() {
        return Math.min(Config.ANOMALY_MAX_ACTIVE.get(), getTier() >= 3 ? 2 : 1);
    }

    public boolean isPaused() {
        return active.size() >= getMaxActive();
    }

    public long getRemaining(long now) {
        return Math.max(0L, nextAt - now);
    }

    public void tick(ServerLevel level) {
        long now = level.getGameTime();
        boolean changed = false;
        if (cycleStart < 0L || nextAt < 0L) {
            schedule(level, now);
            changed = true;
        }
        for (Iterator<ActiveAnomaly> it = active.iterator(); it.hasNext(); ) {
            ActiveAnomaly a = it.next();
            if (now >= a.end() || (now % 20L == 0L && a.type().isFinished(level, a))) {
                it.remove();
                end(level, a);
                changed = true;
            }
        }
        if (upcoming.isEmpty() && now % 200L == 0L) {
            // Worlds saved before the foresight existed.
            refillUpcoming(level);
            changed = true;
        }
        if (!choice.isEmpty()) {
            // The clock waits while a seer chooses.
            cycleStart++;
            nextAt++;
            setDirty();
            if (now >= choiceEnd) resolveChoice(level, null);
        } else if (isPaused()) {
            // The clock waits while the slots are full.
            cycleStart++;
            nextAt++;
            setDirty();
        } else if (now >= nextAt) {
            refillUpcoming(level);
            List<ServerPlayer> seers = seers(level);
            if (!seers.isEmpty() && upcoming.size() >= 2) {
                choice.addAll(upcoming.subList(0, Math.min(CHOICES, upcoming.size())));
                choiceEnd = now + CHOICE_TICKS;
                PacketAnomalyChoice offer = new PacketAnomalyChoice(List.copyOf(choice), (int) CHOICE_TICKS);
                for (ServerPlayer p : level.players()) {
                    if (seers.contains(p)) {
                        PacketDistributor.sendToPlayer(p, offer);
                    } else {
                        p.sendSystemMessage(Component.translatable("tc.anomaly.choosing").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                    }
                }
            } else {
                AnomalyType type = upcoming.isEmpty() ? null : Anomalies.get(upcoming.get(0));
                if (type != null) start(level, type, now, -1);
                cycle++;
                schedule(level, now);
            }
            changed = true;
        }
        if (now % 20L == 0L) {
            for (ActiveAnomaly a : List.copyOf(active)) a.type().onTick(level, a);
        }
        if (changed) {
            setDirty();
            sync(level);
        } else if (now % 200L == 0L) {
            sync(level);
        }
    }

    private void schedule(ServerLevel level, long now) {
        double jitter = Config.ANOMALY_JITTER.get();
        double factor = 1.0 + jitter * (level.random.nextDouble() * 2.0 - 1.0);
        long interval = Math.max(200L, Math.round(Config.ANOMALY_INTERVAL.get() * 1200L * factor));
        cycleStart = now;
        nextAt = now + interval;
        refillUpcoming(level);
    }

    /** Drops the upcoming anomalies that can no longer run, and draws new ones up to {@link #CHOICES}. */
    private void refillUpcoming(ServerLevel level) {
        upcoming.removeIf(id -> !canRun(Anomalies.get(id)));
        while (upcoming.size() < CHOICES) {
            AnomalyType t = draw(level, upcoming);
            if (t == null) break;
            upcoming.add(t.id);
        }
        setDirty();
    }

    private boolean canRun(@Nullable AnomalyType t) {
        if (t == null || Config.ANOMALY_BLACKLIST.get().contains(t.id) || isActive(t.id) || t.getMinTier() > getTier()) return false;
        return !Config.WUSS_MODE.get() || t.polarity == AnomalyType.Polarity.POSITIVE;
    }

    /** The players in the dimension who know Foresight and get to choose. */
    private static List<ServerPlayer> seers(ServerLevel level) {
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : level.players()) {
            if (ThaumcraftCapabilities.knowsResearchStrict(p, SEER_RESEARCH)) out.add(p);
        }
        return out;
    }

    /** Ends the choice: starts {@code id} if it was offered, otherwise one of the cards at random. */
    private void resolveChoice(ServerLevel level, @Nullable String id) {
        if (choice.isEmpty()) return;
        String pick = id != null && choice.contains(id) ? id : choice.get(level.random.nextInt(choice.size()));
        choice.clear();
        choiceEnd = -1L;
        long now = level.getGameTime();
        AnomalyType type = Anomalies.get(pick);
        if (type != null && !isActive(type.id)) start(level, type, now, -1);
        cycle++;
        schedule(level, now);
        PacketDistributor.sendToPlayersInDimension(level, PacketAnomalyChoice.CLOSE);
        setDirty();
        sync(level);
    }

    /** A seer picked a card. False if nothing is being chosen, the card was not offered, or the player is no seer. */
    public boolean choose(ServerPlayer player, String id) {
        ServerLevel level = player.serverLevel();
        if (choice.isEmpty() || !choice.contains(id) || !ThaumcraftCapabilities.knowsResearchStrict(player, SEER_RESEARCH)) return false;
        AnomalyType type = Anomalies.get(id);
        if (type != null) {
            Component name = Component.translatable(type.getTranslationKey()).withColor(type.polarity.color);
            for (ServerPlayer p : level.players()) {
                p.sendSystemMessage(Component.translatable("tc.anomaly.chosen", player.getDisplayName(), name).withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
        resolveChoice(level, id);
        return true;
    }

    /** Weighted draw. More flux in the loaded chunks favours negative anomalies, more vis favours positive ones. */
    @Nullable
    public AnomalyType draw(ServerLevel level) {
        return draw(level, List.of());
    }

    /** As {@link #draw(ServerLevel)}, leaving out the ids in {@code exclude}. */
    @Nullable
    public AnomalyType draw(ServerLevel level, Collection<String> exclude) {
        RandomSource rand = level.random;
        float sumVis = 0.0f;
        float sumFlux = 0.0f;
        float sumBase = 0.0f;
        AuraWorld aw = AuraHandler.getAuraWorld(level.dimension());
        if (aw != null) {
            for (AuraChunk ac : aw.getAuraChunks().values()) {
                sumVis += ac.getVis();
                sumFlux += ac.getFlux();
                sumBase += ac.getBase();
            }
        }
        float visRatio = Math.min(2.0f, sumVis / Math.max(1.0f, sumBase));
        float fluxRatio = Math.min(2.0f, sumFlux / Math.max(1.0f, sumBase));
        int moon = level.getMoonPhase();
        double positive = Config.ANOMALY_POSITIVE_WEIGHT.get() * (0.5 + visRatio) * (moon == 0 ? 1.25 : 1.0);
        double negative = Config.ANOMALY_NEGATIVE_WEIGHT.get() * (1.0 + 3.0 * fluxRatio) * (moon == 4 ? 1.25 : 1.0);
        boolean wuss = Config.WUSS_MODE.get();
        List<? extends String> blacklist = Config.ANOMALY_BLACKLIST.get();
        List<AnomalyType> candidates = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        double total = 0.0;
        for (AnomalyType t : Anomalies.all()) {
            if (blacklist.contains(t.id) || isActive(t.id) || t.getMinTier() > getTier() || exclude.contains(t.id)) continue;
            if (wuss && t.polarity != AnomalyType.Polarity.POSITIVE) continue;
            double w = t.getWeight() * switch (t.polarity) {
                case POSITIVE -> positive;
                case NEGATIVE -> negative;
                case MIXED -> (positive + negative) / 2.0;
            };
            if (w <= 0.0) continue;
            candidates.add(t);
            weights.add(w);
            total += w;
        }
        if (candidates.isEmpty()) return null;
        double r = rand.nextDouble() * total;
        for (int i = 0; i < candidates.size(); i++) {
            r -= weights.get(i);
            if (r <= 0.0) return candidates.get(i);
        }
        return candidates.get(candidates.size() - 1);
    }

    public boolean isActive(String id) {
        for (ActiveAnomaly a : active) if (a.id().equals(id)) return true;
        return false;
    }

    /** Starts an anomaly now. {@code duration} below 0 picks one at random within the anomaly's range. */
    public void start(ServerLevel level, AnomalyType type, long now, int duration) {
        if (isActive(type.id)) return;
        if (duration < 0) {
            int span = Math.max(0, type.getMaxDuration() - type.getMinDuration());
            duration = type.getMinDuration() + (span > 0 ? level.random.nextInt(span + 1) : 0);
        }
        ActiveAnomaly a = new ActiveAnomaly(type.id, now, now + duration);
        active.add(a);
        type.onStart(level, a);
        Component name = Component.translatable(type.getTranslationKey()).withColor(type.polarity.color);
        for (ServerPlayer p : level.players()) {
            p.sendSystemMessage(Component.translatable("tc.anomaly.start", name).withStyle(ChatFormatting.DARK_PURPLE));
            p.sendSystemMessage(Component.translatable(type.getTranslationKey() + ".desc").withStyle(ChatFormatting.GRAY));
            p.playNotifySound(type.polarity == AnomalyType.Polarity.NEGATIVE ? SoundEvents.BEACON_DEACTIVATE : SoundEvents.BEACON_ACTIVATE,
                    SoundSource.AMBIENT, 1.0f, 1.0f);
        }
        setDirty();
        sync(level);
    }

    /** Stops one anomaly, or all of them when {@code id} is null. Returns how many stopped. */
    public int stop(ServerLevel level, @Nullable String id) {
        int n = 0;
        for (Iterator<ActiveAnomaly> it = active.iterator(); it.hasNext(); ) {
            ActiveAnomaly a = it.next();
            if (id == null || a.id().equals(id)) {
                it.remove();
                end(level, a);
                n++;
            }
        }
        if (n > 0) {
            setDirty();
            sync(level);
        }
        return n;
    }

    private void end(ServerLevel level, ActiveAnomaly a) {
        AnomalyType type = a.type();
        if (type == null) return;
        type.onEnd(level, a);
        Component name = Component.translatable(type.getTranslationKey()).withColor(type.polarity.color);
        for (ServerPlayer p : level.players()) {
            p.sendSystemMessage(Component.translatable("tc.anomaly.end", name).withStyle(ChatFormatting.GRAY));
        }
    }

    /** Sets the time left until the next anomaly. */
    public void setTimer(ServerLevel level, long ticks) {
        long now = level.getGameTime();
        cycleStart = now;
        nextAt = now + Math.max(1L, ticks);
        setDirty();
        sync(level);
    }

    public PacketAnomalySync makePacket(long now) {
        List<PacketAnomalySync.Entry> entries = new ArrayList<>();
        for (ActiveAnomaly a : active) {
            net.minecraft.core.BlockPos loc = a.getLocation();
            entries.add(new PacketAnomalySync.Entry(a.id(), (int) Math.max(0L, a.end() - now), (int) (a.end() - a.start()),
                    loc != null, loc != null ? loc.getX() : 0, loc != null ? loc.getZ() : 0));
        }
        AnomalyType next = upcoming.isEmpty() ? null : Anomalies.get(upcoming.get(0));
        int choosing = choice.isEmpty() ? 0 : (int) Math.max(1L, choiceEnd - now);
        return new PacketAnomalySync(true, (int) getRemaining(now), (int) Math.max(1L, nextAt - cycleStart),
                isPaused() || choosing > 0, cycle, entries, next != null ? next.polarity.ordinal() : -1, choosing);
    }

    public void sync(ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, makePacket(level.getGameTime()));
    }

    /** Sends the state of the player's dimension, or "disabled" when anomalies don't run there. */
    public static void syncTo(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (isEnabledIn(level)) {
            AnomalyManager m = get(level);
            PacketDistributor.sendToPlayer(player, m.makePacket(level.getGameTime()));
            // A seer who logs in or arrives while the others choose gets the cards too.
            if (!m.choice.isEmpty() && ThaumcraftCapabilities.knowsResearchStrict(player, SEER_RESEARCH)) {
                PacketDistributor.sendToPlayer(player, new PacketAnomalyChoice(List.copyOf(m.choice),
                        (int) Math.max(1L, m.choiceEnd - level.getGameTime())));
            } else {
                PacketDistributor.sendToPlayer(player, PacketAnomalyChoice.CLOSE);
            }
        } else {
            PacketDistributor.sendToPlayer(player, PacketAnomalySync.DISABLED);
        }
    }
}
