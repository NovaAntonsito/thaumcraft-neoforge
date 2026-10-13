package thaumcraft.common.world.aura;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/** Port of AuraWorld: the loaded aura chunks of one dimension, keyed by {@link ChunkPos#toLong()}. */
public class AuraWorld {
    final ResourceKey<Level> dim;
    ConcurrentHashMap<Long, AuraChunk> auraChunks;

    public AuraWorld(ResourceKey<Level> dim) {
        this.auraChunks = new ConcurrentHashMap<>();
        this.dim = dim;
    }

    public ConcurrentHashMap<Long, AuraChunk> getAuraChunks() {
        return auraChunks;
    }

    public AuraChunk getAuraChunkAt(int x, int y) {
        return auraChunks.get(ChunkPos.asLong(x, y));
    }
}
