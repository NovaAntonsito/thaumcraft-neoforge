package thaumcraft.api.capabilities;

import java.util.regex.Pattern;

import net.minecraft.world.entity.player.Player;
import thaumcraft.common.registry.TCAttachments;

/** Port of ThaumcraftCapabilities: the player's knowledge and warp, now data attachments. */
public class ThaumcraftCapabilities {
    private static final Pattern OR = Pattern.compile(Pattern.quote("||"));

    // PLAYER RESEARCH /////////////////////////////////////////

    public static PlayerKnowledge getKnowledge(Player player) {
        return player.getData(TCAttachments.KNOWLEDGE);
    }

    /**
     * Shortcut method to check if player knows the passed research entries. All must be true.
     * Research does not need to be complete, just 'in progress'.
     * Individual entries can also contain && to do an 'and' check, e.g. "basicgolemancy&&infusion",
     * or || to do an 'or' check, e.g. "basicgolemancy||infusion". Queries should NOT contain both.
     */
    public static boolean knowsResearch(Player player, String... research) {
        for (String r : research) {
            if (r.contains("&&")) {
                if (!knowsResearch(player, r.split("&&"))) return false;
            } else if (r.contains("||")) {
                // The original split on the regex "||" (every position) and returned on the first match;
                // this is the literal 'or' it documents, still requiring the remaining entries.
                boolean any = false;
                for (String str : OR.split(r)) {
                    if (knowsResearch(player, str)) any = true;
                }
                if (!any) return false;
            } else if (!getKnowledge(player).isResearchKnown(r)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Shortcut method to check if player knows all the passed research entries.
     * Research needs to be complete; 'in progress' research only counts if a stage is passed in (e.g. "FOCUSFIRE@2").
     * Supports && and || like {@link #knowsResearch}.
     */
    public static boolean knowsResearchStrict(Player player, String... research) {
        for (String r : research) {
            if (r.contains("&&")) {
                if (!knowsResearchStrict(player, r.split("&&"))) return false;
            } else if (r.contains("||")) {
                boolean any = false;
                for (String str : OR.split(r)) {
                    if (knowsResearchStrict(player, str)) any = true;
                }
                if (!any) return false;
            } else if (r.contains("@")) {
                if (!getKnowledge(player).isResearchKnown(r)) return false;
            } else if (!getKnowledge(player).isResearchComplete(r)) {
                return false;
            }
        }
        return true;
    }

    // PLAYER ASPECTS (TC4 research points) /////////////////

    public static PlayerAspects getAspects(Player player) {
        return player.getData(TCAttachments.ASPECTS);
    }

    // PLAYER WARP /////////////////////////////////////////

    public static PlayerWarp getWarp(Player player) {
        return player.getData(TCAttachments.WARP);
    }
}
