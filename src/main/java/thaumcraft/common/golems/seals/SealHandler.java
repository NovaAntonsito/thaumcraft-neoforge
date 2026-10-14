package thaumcraft.common.golems.seals;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.tasks.TaskHandler;

/**
 * Port of SealHandler: the registered seal types and the placed seals of each dimension.
 * TODO (stage 8b): saving the seals with their chunk, PacketSealToClient and the seal item dropped on removal.
 */
public class SealHandler {
    public static LinkedHashMap<String, ISeal> types = new LinkedHashMap<>();
    public static ConcurrentHashMap<ResourceKey<Level>, ConcurrentHashMap<SealPos, SealEntity>> sealEntities = new ConcurrentHashMap<>();
    static int count = 0;

    public static void registerSeal(ISeal seal) {
        if (types.containsKey(seal.getKey())) {
            Thaumcraft.LOGGER.error("Attempting to register Seal [{}] twice. Ignoring.", seal.getKey());
        } else {
            types.put(seal.getKey(), seal);
        }
    }

    public static String[] getRegisteredSeals() {
        return types.keySet().toArray(new String[0]);
    }

    public static ISeal getSeal(String key) {
        return types.get(key);
    }

    private static ConcurrentHashMap<SealPos, SealEntity> getSeals(ResourceKey<Level> dim) {
        return sealEntities.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
    }

    public static CopyOnWriteArrayList<SealEntity> getSealsInRange(Level world, BlockPos source, int range) {
        CopyOnWriteArrayList<SealEntity> out = new CopyOnWriteArrayList<>();
        for (SealEntity se : getSeals(world.dimension()).values()) {
            if (se.getSeal() != null && se.getSealPos() != null && se.sealPos.pos.distSqr(source) <= range * range) {
                out.add(se);
            }
        }
        return out;
    }

    public static CopyOnWriteArrayList<SealEntity> getSealsInChunk(Level world, ChunkPos chunk) {
        CopyOnWriteArrayList<SealEntity> out = new CopyOnWriteArrayList<>();
        for (SealEntity se : getSeals(world.dimension()).values()) {
            if (se.getSeal() != null && se.getSealPos() != null && new ChunkPos(se.sealPos.pos).equals(chunk)) {
                out.add(se);
            }
        }
        return out;
    }

    public static void removeSealEntity(Level world, SealPos pos, boolean quiet) {
        SealEntity seal = getSeals(world.dimension()).remove(pos);
        try {
            if (!world.isClientSide && seal != null && seal.seal != null) {
                seal.seal.onRemoval(world, pos.pos, pos.face);
            }
            // TODO (stage 8b): drop the seal's placer item unless quiet.
        } catch (Exception e) {
            Thaumcraft.LOGGER.warn("Removing invalid seal at {}", pos.pos);
        }
        for (Task task : TaskHandler.getTasks(world.dimension()).values()) {
            if (task.getSealPos() != null && task.getSealPos().equals(pos)) {
                task.setSuspended(true);
            }
        }
        // TODO (stage 8b): PacketSealToClient with an empty seal, and markChunkAsDirty unless quiet.
    }

    public static ISealEntity getSealEntity(ResourceKey<Level> dim, SealPos pos) {
        if (pos == null) {
            return null;
        }
        return getSeals(dim).get(pos);
    }

    public static boolean addSealEntity(Level world, BlockPos pos, Direction face, ISeal seal, Player player) {
        ConcurrentHashMap<SealPos, SealEntity> se = getSeals(world.dimension());
        SealPos sp = new SealPos(pos, face);
        if (se.containsKey(sp)) {
            return false;
        }
        SealEntity sealent = new SealEntity(world, sp, seal);
        sealent.setOwner(player.getUUID().toString());
        se.put(sp, sealent);
        if (!world.isClientSide) {
            sealent.syncToClient(world);
        }
        return true;
    }

    public static boolean addSealEntity(Level world, SealEntity seal) {
        if (world == null) {
            return false;
        }
        ConcurrentHashMap<SealPos, SealEntity> se = getSeals(world.dimension());
        if (se.containsKey(seal.getSealPos())) {
            return false;
        }
        se.put(seal.getSealPos(), seal);
        if (!world.isClientSide) {
            seal.syncToClient(world);
        }
        return true;
    }

    public static void tickSealEntities(Level world) {
        ConcurrentHashMap<SealPos, SealEntity> se = getSeals(world.dimension());
        ++count;
        for (SealEntity sealEntity : se.values()) {
            if (world.isLoaded(sealEntity.sealPos.pos)) {
                try {
                    if (count % 20 == 0 && !sealEntity.seal.canPlaceAt(world, sealEntity.sealPos.pos, sealEntity.sealPos.face)) {
                        removeSealEntity(world, sealEntity.sealPos, false);
                        continue;
                    }
                    sealEntity.tickSealEntity(world);
                } catch (Exception e) {
                    removeSealEntity(world, sealEntity.sealPos, false);
                }
            }
        }
    }
}
