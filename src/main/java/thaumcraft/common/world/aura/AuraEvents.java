package thaumcraft.common.world.aura;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.common.registry.TCAttachments;

/**
 * Aura parts of WorldEvents, ChunkEvents and ServerEvents.worldTick.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AuraEvents {
    private AuraEvents() {
    }

    private static final Map<ResourceKey<Level>, AuraThread> auraThreads = new HashMap<>();

    @SubscribeEvent
    static void worldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) AuraHandler.addAuraWorld(level.dimension());
    }

    @SubscribeEvent
    static void worldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraHandler.removeAuraWorld(level.dimension());
            auraThreads.remove(level.dimension());
        }
    }

    /** Saved chunks already carry their aura attachment; new ones get one generated. */
    @SubscribeEvent
    static void chunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        AuraChunk ac;
        if (chunk.hasData(TCAttachments.AURA)) {
            ac = chunk.getData(TCAttachments.AURA);
        } else {
            ac = AuraHandler.generateAura(chunk, level.random);
            chunk.setData(TCAttachments.AURA, ac);
            chunk.setUnsaved(true);
        }
        AuraHandler.addAuraChunk(level.dimension(), chunk, ac);
    }

    @SubscribeEvent
    static void chunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraHandler.removeAuraChunk(level.dimension(), event.getChunk().getPos().x, event.getChunk().getPos().z);
        }
    }

    @SubscribeEvent
    static void worldTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.getGameTime() % 20 != 0) return;
        auraThreads.computeIfAbsent(level.dimension(), k -> new AuraThread()).tick(level);
        if (AuraHandler.riftTrigger.containsKey(level.dimension())) {
            if (!thaumcraft.Config.WUSS_MODE.getAsBoolean()) {
                thaumcraft.common.entities.EntityFluxRift.createRift(level, AuraHandler.riftTrigger.get(level.dimension()));
            }
            AuraHandler.riftTrigger.remove(level.dimension());
        }
    }
}
