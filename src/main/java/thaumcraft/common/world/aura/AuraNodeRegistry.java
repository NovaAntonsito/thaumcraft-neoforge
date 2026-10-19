package thaumcraft.common.world.aura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.tiles.misc.TileAuraNode;

/**
 * Not in Thaumcraft 4 (dream branch): every aura node a dimension has loaded at least once, with its type and aspects,
 * so the aura atlas can show nodes in chunks that are no longer loaded. Nodes add themselves when they load and every
 * few seconds after; the node block takes itself out when it goes.
 */
public class AuraNodeRegistry extends SavedData {
    private static final String NAME = "thaumcraft_aura_nodes";

    public record Entry(BlockPos pos, NodeType type, @Nullable NodeModifier modifier, AspectList aspects, boolean energized) {
    }

    private final Map<Long, Entry> nodes = new HashMap<>();

    public static AuraNodeRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(AuraNodeRegistry::new, AuraNodeRegistry::load, null), NAME);
    }

    public static void update(ServerLevel level, TileAuraNode node) {
        Entry e = new Entry(node.getBlockPos().immutable(), node.getNodeType(), node.getNodeModifier(),
                (node.isEnergized() ? node.getAspectsBase() : node.getAspects()).copy(), node.isEnergized());
        AuraNodeRegistry reg = get(level);
        Entry old = reg.nodes.put(e.pos().asLong(), e);
        if (!e.equals(old)) reg.setDirty();
    }

    public static void remove(ServerLevel level, BlockPos pos) {
        AuraNodeRegistry reg = get(level);
        if (reg.nodes.remove(pos.asLong()) != null) reg.setDirty();
    }

    /** The nodes whose chunk lies within radius chunks of the given one. */
    public List<Entry> around(int cx, int cz, int radius) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : nodes.values()) {
            if (Math.abs((e.pos().getX() >> 4) - cx) <= radius && Math.abs((e.pos().getZ() >> 4) - cz) <= radius) out.add(e);
        }
        return out;
    }

    private static AuraNodeRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        AuraNodeRegistry reg = new AuraNodeRegistry();
        for (Tag t : tag.getList("Nodes", Tag.TAG_COMPOUND)) {
            CompoundTag n = (CompoundTag) t;
            BlockPos pos = BlockPos.of(n.getLong("Pos"));
            AspectList al = AspectList.CODEC.parse(NbtOps.INSTANCE, n.get("Aspects")).result().orElseGet(AspectList::new);
            reg.nodes.put(pos.asLong(), new Entry(pos, NodeType.byId(n.getByte("Type")), NodeModifier.byId(n.getByte("Mod")), al,
                    n.getBoolean("Energized")));
        }
        return reg;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Entry e : nodes.values()) {
            CompoundTag n = new CompoundTag();
            n.putLong("Pos", e.pos().asLong());
            n.putByte("Type", (byte) e.type().ordinal());
            n.putByte("Mod", (byte) (e.modifier() == null ? -1 : e.modifier().ordinal()));
            AspectList.CODEC.encodeStart(NbtOps.INSTANCE, e.aspects()).result().ifPresent(a -> n.put("Aspects", a));
            n.putBoolean("Energized", e.energized());
            list.add(n);
        }
        tag.put("Nodes", list);
        return tag;
    }
}
