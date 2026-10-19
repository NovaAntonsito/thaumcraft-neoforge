package thaumcraft.common.world.objects;

import java.util.ArrayList;
import java.util.List;
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
 * Not in the original: where the barrows of a dimension were generated, for /thaumcraft locate barrow.
 * Barrows are placed by chunk generation, which runs off the server thread, so new ones wait in a queue and are
 * saved on the next server tick. Only barrows generated since this was added are known.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public class MoundLocations extends SavedData {
    private static final String NAME = "thaumcraft_barrows";
    private static final Factory<MoundLocations> FACTORY = new Factory<>(MoundLocations::new, MoundLocations::load, null);
    private static final ConcurrentLinkedQueue<Pending> PENDING = new ConcurrentLinkedQueue<>();

    private record Pending(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private final List<BlockPos> barrows = new ArrayList<>();

    public static MoundLocations get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /** Called from world generation with the entrance of a barrow that was just placed. */
    public static void record(ResourceKey<Level> dimension, BlockPos pos) {
        PENDING.add(new Pending(dimension, pos.immutable()));
    }

    @SubscribeEvent
    static void serverTick(ServerTickEvent.Post event) {
        Pending p;
        while ((p = PENDING.poll()) != null) {
            ServerLevel level = event.getServer().getLevel(p.dimension());
            if (level != null) {
                MoundLocations data = get(level);
                if (!data.barrows.contains(p.pos())) {
                    data.barrows.add(p.pos());
                    data.setDirty();
                }
            }
        }
    }

    /** The known barrow closest to pos, or null if none is known. */
    public BlockPos nearest(BlockPos pos) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos b : barrows) {
            double dx = b.getX() - pos.getX();
            double dz = b.getZ() - pos.getZ();
            double d = dx * dx + dz * dz;
            if (d < bestDist) {
                bestDist = d;
                best = b;
            }
        }
        return best;
    }

    /** Every known barrow (dream branch: the cartographic notes pick from them). */
    public List<BlockPos> all() {
        return barrows;
    }

    public int count() {
        return barrows.size();
    }

    public static MoundLocations load(CompoundTag tag, HolderLookup.Provider provider) {
        MoundLocations data = new MoundLocations();
        ListTag list = tag.getList("barrows", Tag.TAG_LONG);
        for (int i = 0; i < list.size(); ++i) {
            data.barrows.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (BlockPos b : barrows) {
            list.add(LongTag.valueOf(b.asLong()));
        }
        tag.put("barrows", list);
        return tag;
    }
}
