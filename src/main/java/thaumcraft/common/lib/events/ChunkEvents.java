package thaumcraft.common.lib.events;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.common.golems.seals.SealEntity;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.lib.network.misc.PacketSealToClient;
import thaumcraft.common.registry.TCAttachments;

/**
 * Partial port of ChunkEvents: the seal part. The aura part became the AURA chunk attachment.
 * <p>
 * 1.12 wrote the seals of a chunk into the chunk NBT in ChunkDataEvent.Save and read them back in ChunkDataEvent.Load.
 * Here they live in the SEALS chunk attachment, which SealHandler.markChunkAsDirty keeps up to date, so loading
 * reads it on ChunkEvent.Load and unloading drops the seals from the handler (the "!isLoaded" branch of chunkSave).
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ChunkEvents {
    private ChunkEvents() {
    }

    @SubscribeEvent
    public static void chunkLoad(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk) || !(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }
        List<CompoundTag> tagList = chunk.getExistingDataOrNull(TCAttachments.SEALS);
        if (tagList == null) {
            return;
        }
        for (CompoundTag tasknbt : tagList) {
            SealEntity seal = new SealEntity();
            seal.readNBT(tasknbt);
            if (seal.getSeal() != null) {
                SealHandler.addSealEntity(level, seal);
            }
        }
    }

    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }
        for (ISealEntity seal : SealHandler.getSealsInChunk(level, event.getChunk().getPos())) {
            SealHandler.removeSealEntity(level, seal.getSealPos(), true);
        }
    }

    @SubscribeEvent
    public static void chunkWatch(ChunkWatchEvent.Watch event) {
        for (ISealEntity seal : SealHandler.getSealsInChunk(event.getLevel(), event.getPos())) {
            PacketDistributor.sendToPlayer(event.getPlayer(), new PacketSealToClient(seal));
        }
    }
}
