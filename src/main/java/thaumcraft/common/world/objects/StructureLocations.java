package thaumcraft.common.world.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.Thaumcraft;

/**
 * Not in Thaumcraft (dream branch): where the structures of a dimension were generated, by kind, for the
 * cartographic notes. Works as {@link MoundLocations}: world generation runs off the server thread, so new places
 * wait in a queue until the next server tick. Barrows stay in MoundLocations.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public class StructureLocations extends SavedData {
    public static final String ELDRITCH_RING = "eldritch_ring";
    private static final String NAME = "thaumcraft_structures";
    private static final Factory<StructureLocations> FACTORY = new Factory<>(StructureLocations::new, StructureLocations::load, null);
    private static final ConcurrentLinkedQueue<Pending> PENDING = new ConcurrentLinkedQueue<>();

    private record Pending(ResourceKey<Level> dimension, String kind, BlockPos pos) {
    }

    private final Map<String, List<BlockPos>> places = new HashMap<>();

    public static StructureLocations get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /** Called from world generation with a structure that was just placed. */
    public static void record(ResourceKey<Level> dimension, String kind, BlockPos pos) {
        PENDING.add(new Pending(dimension, kind, pos.immutable()));
    }

    @SubscribeEvent
    static void serverTick(ServerTickEvent.Post event) {
        Pending p;
        while ((p = PENDING.poll()) != null) {
            ServerLevel level = event.getServer().getLevel(p.dimension());
            if (level != null) {
                List<BlockPos> list = get(level).places.computeIfAbsent(p.kind(), k -> new ArrayList<>());
                if (!list.contains(p.pos())) {
                    list.add(p.pos());
                    get(level).setDirty();
                }
            }
        }
    }

    public List<BlockPos> all(String kind) {
        return places.getOrDefault(kind, List.of());
    }

    public static StructureLocations load(CompoundTag tag, HolderLookup.Provider provider) {
        StructureLocations data = new StructureLocations();
        for (String kind : tag.getAllKeys()) {
            List<BlockPos> list = new ArrayList<>();
            for (Tag t : tag.getList(kind, Tag.TAG_LONG)) list.add(BlockPos.of(((LongTag) t).getAsLong()));
            data.places.put(kind, list);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        places.forEach((kind, list) -> {
            ListTag l = new ListTag();
            for (BlockPos b : list) l.add(LongTag.valueOf(b.asLong()));
            tag.put(kind, l);
        });
        return tag;
    }
}
