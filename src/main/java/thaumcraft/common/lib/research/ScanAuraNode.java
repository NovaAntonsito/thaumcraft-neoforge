package thaumcraft.common.lib.research;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.IScanThing;
import thaumcraft.common.tiles.misc.TileAuraNode;

/**
 * Scanning an aura node (dream branch). Each kind of node is learned once ("!node_&lt;type&gt;"), giving a fifth of its
 * aspects as knowledge, as scanning a node in Thaumcraft 4 taught its aspects. The first one also completes NODES.
 */
public class ScanAuraNode implements IScanThing {
    public static final String PREFIX = "!node_";
    public static final String BASE_RESEARCH = "NODES";

    private static TileAuraNode node(Player player, Object obj) {
        return obj instanceof BlockPos pos && player.level().getBlockEntity(pos) instanceof TileAuraNode n ? n : null;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        return node(player, obj) != null;
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        TileAuraNode n = node(player, obj);
        return n == null ? "" : PREFIX + n.getNodeType().name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public void onSuccess(Player player, Object obj) {
        TileAuraNode n = node(player, obj);
        if (n == null || !(player instanceof ServerPlayer sp)) return;
        AspectList al = n.getAspects();
        for (Aspect aspect : al.getAspects()) {
            ResearchNoteManager.checkAndSyncAspectKnowledge(sp, aspect, Math.max(1, al.getAmount(aspect) / 5));
        }
        if (!ThaumcraftCapabilities.knowsResearch(sp, BASE_RESEARCH)) ResearchManager.completeResearch(sp, BASE_RESEARCH);
    }
}
