package thaumcraft.common.golems.seals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.api.golems.seals.ISeal;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.network.misc.PacketSealToClient;
import thaumcraft.common.registry.TCAttachments;

/**
 * Port of SealHandler: the registered seal types and the placed seals of each dimension.
 * In single player the client and the integrated server share these maps, as they did in 1.12.
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
            if (!quiet && seal != null && !world.isClientSide) {
                ItemStack stack = ItemSealPlacer.getSealStack(seal.getSeal().getKey());
                if (!stack.isEmpty()) {
                    world.addFreshEntity(new ItemEntity(world, pos.pos.getX() + 0.5 + pos.face.getStepX() / 1.7f,
                            pos.pos.getY() + 0.5 + pos.face.getStepY() / 1.7f, pos.pos.getZ() + 0.5 + pos.face.getStepZ() / 1.7f, stack));
                }
            }
        } catch (Exception e) {
            Thaumcraft.LOGGER.warn("Removing invalid seal at {}", pos.pos);
        }
        for (Task task : TaskHandler.getTasks(world.dimension()).values()) {
            if (task.getSealPos() != null && task.getSealPos().equals(pos)) {
                task.setSuspended(true);
            }
        }
        if (!world.isClientSide) {
            PacketDistributor.sendToPlayersInDimension((ServerLevel) world, new PacketSealToClient(new SealEntity(world, pos, null)));
        }
        if (!quiet) {
            markChunkAsDirty(world, pos.pos);
        }
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
            markChunkAsDirty(world, pos);
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
            markChunkAsDirty(world, seal.getSealPos().pos);
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

    /**
     * 1.12 queued the chunk in AuraHandler.dirtyChunks so it would be saved, and the seals were written in
     * ChunkDataEvent.Save. Here the chunk's SEALS attachment is rewritten now, which also flags the chunk for saving.
     */
    public static void markChunkAsDirty(Level world, BlockPos bp) {
        if (world.isClientSide || !world.isLoaded(bp)) {
            return;
        }
        LevelChunk chunk = world.getChunkAt(bp);
        List<CompoundTag> tagList = new ArrayList<>();
        for (ISealEntity seal : getSealsInChunk(world, chunk.getPos())) {
            tagList.add(seal.writeNBT());
        }
        chunk.setData(TCAttachments.SEALS, tagList);
        chunk.setUnsaved(true);
    }
}
