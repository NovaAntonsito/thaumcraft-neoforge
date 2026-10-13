package thaumcraft.common.lib.research;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import thaumcraft.api.aspects.AspectList;

/**
 * A hand-made layout for one research note, from TC4 Research Port: Reborn: the aspects on the outer ring,
 * the grid radius, how many empty cells to remove, and layout flags (SEPARATED, ROUND, ROUNDEX).
 */
public class ResearchNotePattern {
    private final String targetResearch;
    private final int targetStage;
    private final AspectList aspects;
    private final Set<String> meta;
    private final int radius;
    private final int blanks;
    private final int hashDelta;
    private final int color;

    public ResearchNotePattern(String targetResearch, int targetStage, AspectList aspects, int radius, int blanks, int color, int hashDelta, String[] meta) {
        this.targetResearch = targetResearch;
        this.targetStage = targetStage;
        this.aspects = aspects;
        this.hashDelta = hashDelta;
        this.radius = radius;
        this.blanks = blanks;
        this.color = color;
        this.meta = new HashSet<>(Arrays.asList(meta));
    }

    public boolean hasMeta(String meta) {
        return this.meta.contains(meta);
    }

    public int color() {
        return color;
    }

    /** The note key this pattern is for: "rn_KEY_stage". */
    public String noteKey() {
        return "rn_" + targetResearch + "_" + targetStage;
    }

    public AspectList aspects() {
        return aspects.copy();
    }

    public int radius() {
        return radius;
    }

    public int blanks() {
        return blanks;
    }

    public long seed() {
        return 31 * (31 * (31 * 2880321 + radius) + blanks) + hashDelta;
    }
}
