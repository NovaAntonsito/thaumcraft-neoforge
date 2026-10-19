package thaumcraft.common.world.dim;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Port of TC4's MapBossData ("BossMapData"): how many boss rooms have been opened, which picks the next boss. 1.7 map
 * data was shared by every dimension; here it lives in the overworld's saved data.
 */
public class MapBossData extends SavedData {
    private static final String NAME = "BossMapData";
    private static final Factory<MapBossData> FACTORY = new Factory<>(MapBossData::new, MapBossData::load, null);

    public int bossCount;

    private static MapBossData load(CompoundTag tag, HolderLookup.Provider registries) {
        MapBossData data = new MapBossData();
        data.bossCount = tag.getInt("bossCount");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("bossCount", this.bossCount);
        return tag;
    }

    public static MapBossData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }
}
