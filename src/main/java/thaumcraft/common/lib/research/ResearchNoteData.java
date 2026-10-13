package thaumcraft.common.lib.research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.utils.HexUtils;

/**
 * The hex grid of a research note, from TC4 Research Port: Reborn. Stored on the note item as a data component
 * (the "hexgrid"/"aspects" NBT of the original).
 * <p>
 * Each cell has a type: 0 empty, 1 source aspect (on the outer ring), 2 aspect placed by the player.
 * Component values must not change in place: {@code ItemResearchNote.getData} hands out a copy, set it back after a change.
 */
public class ResearchNoteData {
    /** One cell of the grid. */
    public record HexEntry(@Nullable Aspect aspect, int type) {
    }

    /** A cell as saved: its coordinates, type and aspect. */
    private record Cell(int q, int r, int type, Optional<Aspect> aspect) {
        static final Codec<Cell> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("q").forGetter(Cell::q),
                Codec.INT.fieldOf("r").forGetter(Cell::r),
                Codec.INT.fieldOf("type").forGetter(Cell::type),
                Aspect.CODEC.optionalFieldOf("aspect").forGetter(Cell::aspect)
        ).apply(i, Cell::new));
    }

    public static final Codec<ResearchNoteData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("key").forGetter(d -> d.key),
            Codec.INT.optionalFieldOf("color", 0).forGetter(d -> d.color),
            Codec.BOOL.optionalFieldOf("complete", false).forGetter(d -> d.complete),
            Codec.BOOL.optionalFieldOf("generic", false).forGetter(d -> d.generic),
            Codec.INT.optionalFieldOf("copies", 0).forGetter(d -> d.copies),
            Codec.INT.optionalFieldOf("merged_theories", 0).forGetter(d -> d.mergedTeories),
            Cell.CODEC.listOf().optionalFieldOf("hexgrid", List.of()).forGetter(ResearchNoteData::cells),
            Aspect.CODEC.listOf().optionalFieldOf("aspects", List.of()).forGetter(d -> List.of(d.aspects.getAspects()))
    ).apply(i, ResearchNoteData::new));

    public static final StreamCodec<ByteBuf, ResearchNoteData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public HashMap<String, HexEntry> hexEntries = new HashMap<>();
    public HashMap<String, HexUtils.Hex> hexes = new HashMap<>();
    public AspectList aspects = new AspectList();
    public int mergedTeories;
    public boolean complete;
    public boolean generic;
    public String key = "";
    public int copies;
    public int color;

    public ResearchNoteData() {
    }

    private ResearchNoteData(String key, int color, boolean complete, boolean generic, int copies, int mergedTeories, List<Cell> cells, List<Aspect> aspects) {
        this.key = key;
        this.color = color;
        this.complete = complete;
        this.generic = generic;
        this.copies = copies;
        this.mergedTeories = mergedTeories;
        for (Cell c : cells) {
            HexUtils.Hex hex = new HexUtils.Hex(c.q(), c.r());
            hexEntries.put(hex.toString(), new HexEntry(c.aspect().orElse(null), c.type()));
            hexes.put(hex.toString(), hex);
        }
        for (Aspect a : aspects) this.aspects.add(a, 1);
    }

    private List<Cell> cells() {
        List<Cell> out = new ArrayList<>();
        for (HexUtils.Hex hex : hexes.values()) {
            HexEntry e = hexEntries.get(hex.toString());
            out.add(new Cell(hex.q, hex.r, e.type(), Optional.ofNullable(e.aspect())));
        }
        return out;
    }

    public ResearchNoteData copy() {
        ResearchNoteData d = new ResearchNoteData(key, color, complete, generic, copies, mergedTeories, cells(), List.of(aspects.getAspects()));
        d.aspects = aspects.copy();
        return d;
    }

    public boolean isComplete() {
        return complete;
    }

    public void generateHexesFromComp(Random rand, AspectList aspects, int complexity) {
        int radius = 2 + Math.min(2, complexity / 3);
        int blanks = complexity > 1 ? complexity : 0;
        generateHexes(rand, aspects, null, radius, blanks);
    }

    public void generateHexes(Random rand, AspectList aspects, @Nullable ResearchNotePattern meta, int radius, int blanks) {
        ArrayList<HexUtils.Hex> outerRing = meta != null && meta.hasMeta("SEPARATED")
                ? distributeRingRandomlySep(radius, aspects.size(), rand)
                : distributeRingRandomly(radius, aspects.size(), rand);
        HashMap<String, HexUtils.Hex> hexLocations = HexUtils.generateHexes(radius);
        // One point each, as the saved note reads them back (the copy cost is based on it).
        this.aspects = new AspectList();
        for (Aspect a : aspects.getAspects()) this.aspects.add(a, 1);
        for (HexUtils.Hex hex : hexLocations.values()) {
            hexEntries.put(hex.toString(), new HexEntry(null, 0));
            hexes.put(hex.toString(), hex);
        }
        Aspect[] list = aspects.getAspects();
        for (int i = 0; i != Math.min(outerRing.size(), list.length); i++) {
            HexUtils.Hex hex = outerRing.get(i);
            hexEntries.put(hex.toString(), new HexEntry(list[i], 1));
            hexes.put(hex.toString(), hex);
        }

        if (meta != null && (meta.hasMeta("ROUND") || meta.hasMeta("ROUNDEX"))) {
            // Only the outer ring (ROUNDEX), or the two outer rings (ROUND), are kept.
            HexUtils.Hex[] temp = hexes.values().toArray(new HexUtils.Hex[0]);
            Set<String> ring = HexUtils.getRing(radius).stream().map(HexUtils.Hex::toString).collect(Collectors.toSet());
            if (meta.hasMeta("ROUND")) {
                ring.addAll(HexUtils.getRing(radius - 1).stream().map(HexUtils.Hex::toString).collect(Collectors.toSet()));
            }
            for (HexUtils.Hex hex : temp) {
                if (!ring.contains(hex.toString())) {
                    hexes.remove(hex.toString());
                    hexEntries.remove(hex.toString());
                }
            }
        } else if (blanks > 0) {
            // Remove random empty cells, never one next to a source aspect.
            List<HexUtils.Hex> candidates = new LinkedList<>(hexes.values());
            candidates.removeIf(hex -> {
                HexEntry entry = hexEntries.get(hex.toString());
                if (entry != null && entry.type() == 0) {
                    for (int n = 0; n != 6; n++) {
                        HexEntry neighbour = hexEntries.get(hex.getNeighbour(n).toString());
                        if (neighbour != null && neighbour.type() == 1) return true;
                    }
                    return false;
                }
                return true;
            });
            for (int i = 0; i != blanks && !candidates.isEmpty(); i++) {
                HexUtils.Hex hex = candidates.get(rand.nextInt(candidates.size()));
                hexes.remove(hex.toString());
                hexEntries.remove(hex.toString());
                candidates.remove(hex);
            }
        }
    }

    private ArrayList<HexUtils.Hex> distributeRingRandomlySep(int radius, int entries, Random random) {
        ArrayList<HexUtils.Hex> ring = HexUtils.getRing(radius);
        ArrayList<HexUtils.Hex> results = new ArrayList<>();
        float spacing = (float) ring.size() / (float) (entries + 4);
        float pos = (float) random.nextInt(ring.size());
        for (int i = 0; i < entries; i++) {
            int index = Math.round(pos);
            results.add(ring.get(index >= ring.size() ? ring.size() - 1 : index));
            pos += spacing;
            if (i == 0 || i == entries / 2) pos += spacing * 2;
            pos %= ring.size();
        }
        return results;
    }

    private ArrayList<HexUtils.Hex> distributeRingRandomly(int radius, int entries, Random random) {
        ArrayList<HexUtils.Hex> ring = HexUtils.getRing(radius);
        ArrayList<HexUtils.Hex> results = new ArrayList<>();
        float spacing = (float) ring.size() / (float) entries;
        float pos = (float) random.nextInt(ring.size());
        for (int i = 0; i < entries; i++) {
            int index = Math.round(pos);
            results.add(ring.get(index >= ring.size() ? ring.size() - 1 : index));
            pos += spacing;
            if (pos >= (float) ring.size()) pos -= (float) ring.size();
        }
        return results;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ResearchNoteData d && key.equals(d.key) && color == d.color && complete == d.complete
                && generic == d.generic && copies == d.copies && mergedTeories == d.mergedTeories
                && hexEntries.equals(d.hexEntries) && List.of(aspects.getAspects()).equals(List.of(d.aspects.getAspects()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, color, complete, generic, copies, mergedTeories, hexEntries);
    }
}
