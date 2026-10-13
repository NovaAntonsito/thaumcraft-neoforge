package thaumcraft.common.world.aura;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Port of thaumcraft.common.world.aura.AuraChunk.
 * <p>
 * In 1.12.2 the values were written by hand into the chunk NBT in ChunkDataEvent.Save/Load.
 * Here the object itself is a chunk data attachment ({@code TCAttachments.AURA}) saved with {@link #CODEC}.
 */
public class AuraChunk {
    public static final Codec<AuraChunk> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.SHORT.fieldOf("base").forGetter(AuraChunk::getBase),
            Codec.FLOAT.fieldOf("vis").forGetter(AuraChunk::getVis),
            Codec.FLOAT.fieldOf("flux").forGetter(AuraChunk::getFlux)
    ).apply(i, (base, vis, flux) -> new AuraChunk(null, base, vis, flux)));

    ChunkPos loc;
    short base;
    float vis;
    float flux;
    @Nullable
    WeakReference<LevelChunk> chunkRef;

    public AuraChunk(ChunkPos loc) {
        this.loc = loc;
    }

    public AuraChunk(@Nullable LevelChunk chunk, short base, float vis, float flux) {
        if (chunk != null) bind(chunk);
        this.base = base;
        this.vis = vis;
        this.flux = flux;
    }

    /** Links this data to its loaded chunk (position and save flag). */
    void bind(LevelChunk chunk) {
        loc = chunk.getPos();
        chunkRef = new WeakReference<>(chunk);
    }

    /** True if the chunk is already flagged to be saved. */
    public boolean isModified() {
        LevelChunk chunk = chunkRef != null ? chunkRef.get() : null;
        return chunk != null && chunk.isUnsaved();
    }

    /** Flags the chunk for saving. Replaces AuraHandler.dirtyChunks + world.markChunkDirty. */
    void markDirty() {
        LevelChunk chunk = chunkRef != null ? chunkRef.get() : null;
        if (chunk != null && !chunk.isUnsaved()) chunk.setUnsaved(true);
    }

    public short getBase() {
        return base;
    }

    public void setBase(short base) {
        this.base = base;
    }

    public float getVis() {
        return vis;
    }

    public void setVis(float vis) {
        this.vis = Math.min(32766.0f, Math.max(0.0f, vis));
    }

    public float getFlux() {
        return flux;
    }

    public void setFlux(float flux) {
        this.flux = Math.min(32766.0f, Math.max(0.0f, flux));
    }

    public ChunkPos getLoc() {
        return loc;
    }

    public void setLoc(ChunkPos loc) {
        this.loc = loc;
    }
}
