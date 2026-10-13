package thaumcraft.common.world.aura;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import thaumcraft.Thaumcraft;
import thaumcraft.common.registry.TCAttachments;
import thaumcraft.common.world.biomes.BiomeHandler;

/** Port of thaumcraft.common.world.aura.AuraHandler. Dimension ids (int) become level keys. */
public class AuraHandler {
    public static int AURA_CEILING = 500;
    static final ConcurrentHashMap<ResourceKey<Level>, AuraWorld> auras = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<ResourceKey<Level>, BlockPos> riftTrigger = new ConcurrentHashMap<>();

    @Nullable
    public static AuraWorld getAuraWorld(ResourceKey<Level> dim) {
        return auras.get(dim);
    }

    @Nullable
    public static AuraChunk getAuraChunk(ResourceKey<Level> dim, int x, int y) {
        AuraWorld aw = auras.get(dim);
        return aw != null ? aw.getAuraChunkAt(x, y) : null;
    }

    public static void addAuraWorld(ResourceKey<Level> dim) {
        if (!auras.containsKey(dim)) {
            auras.put(dim, new AuraWorld(dim));
            Thaumcraft.LOGGER.info("Creating aura cache for world {}", dim.location());
        }
    }

    public static void removeAuraWorld(ResourceKey<Level> dim) {
        auras.remove(dim);
        Thaumcraft.LOGGER.info("Removing aura cache for world {}", dim.location());
    }

    /** Tracks the aura data of a chunk that just loaded. */
    public static void addAuraChunk(ResourceKey<Level> dim, LevelChunk chunk, AuraChunk ac) {
        ac.bind(chunk);
        auras.computeIfAbsent(dim, AuraWorld::new).getAuraChunks().put(chunk.getPos().toLong(), ac);
    }

    public static void removeAuraChunk(ResourceKey<Level> dim, int x, int y) {
        AuraWorld aw = auras.get(dim);
        if (aw != null) aw.getAuraChunks().remove(net.minecraft.world.level.ChunkPos.asLong(x, y));
    }

    @Nullable
    private static AuraChunk at(Level world, BlockPos pos) {
        return getAuraChunk(world.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static float getTotalAura(Level world, BlockPos pos) {
        AuraChunk ac = at(world, pos);
        return ac != null ? ac.getVis() + ac.getFlux() : 0.0f;
    }

    public static float getFluxSaturation(Level world, BlockPos pos) {
        AuraChunk ac = at(world, pos);
        return ac != null ? ac.getFlux() / ac.getBase() : 0.0f;
    }

    public static float getVis(Level world, BlockPos pos) {
        AuraChunk ac = at(world, pos);
        return ac != null ? ac.getVis() : 0.0f;
    }

    public static float getFlux(Level world, BlockPos pos) {
        AuraChunk ac = at(world, pos);
        return ac != null ? ac.getFlux() : 0.0f;
    }

    public static int getAuraBase(Level world, BlockPos pos) {
        AuraChunk ac = at(world, pos);
        return ac != null ? ac.getBase() : 0;
    }

    public static boolean shouldPreserveAura(Level world, @Nullable Player player, BlockPos pos) {
        return (player == null || player.getData(TCAttachments.KNOWLEDGE).isResearchComplete("AURAPRESERVE"))
                && getVis(world, pos) / getAuraBase(world, pos) < 0.1;
    }

    public static void addVis(Level world, BlockPos pos, float amount) {
        if (amount < 0.0f) return;
        modifyVisInChunk(at(world, pos), amount, true);
    }

    public static void addFlux(Level world, BlockPos pos, float amount) {
        if (amount < 0.0f) return;
        modifyFluxInChunk(at(world, pos), amount, true);
    }

    public static float drainVis(Level world, BlockPos pos, float amount, boolean simulate) {
        AuraChunk ac = at(world, pos);
        if (ac == null) return 0.0f;
        if (amount > ac.getVis()) amount = ac.getVis();
        return modifyVisInChunk(ac, -amount, !simulate) ? amount : 0.0f;
    }

    public static float drainFlux(Level world, BlockPos pos, float amount, boolean simulate) {
        AuraChunk ac = at(world, pos);
        if (ac == null) return 0.0f;
        if (amount > ac.getFlux()) amount = ac.getFlux();
        return modifyFluxInChunk(ac, -amount, !simulate) ? amount : 0.0f;
    }

    public static boolean modifyVisInChunk(@Nullable AuraChunk ac, float amount, boolean doit) {
        if (ac == null) return false;
        if (doit) {
            ac.setVis(Math.max(0.0f, ac.getVis() + amount));
            ac.markDirty();
        }
        return true;
    }

    private static boolean modifyFluxInChunk(@Nullable AuraChunk ac, float amount, boolean doit) {
        if (ac == null) return false;
        if (doit) {
            ac.setFlux(Math.max(0.0f, ac.getFlux() + amount));
            ac.markDirty();
        }
        return true;
    }

    /** Rolls the starting aura of a new chunk from its biome and its 4 neighbours' biomes. */
    public static AuraChunk generateAura(LevelChunk chunk, RandomSource rand) {
        Level world = chunk.getLevel();
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        Holder<Biome> bgb = world.getBiome(new BlockPos(cx * 16 + 8, 50, cz * 16 + 8));
        float life = BiomeHandler.getBiomeAuraModifier(bgb);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Holder<Biome> bgb2 = world.getBiome(new BlockPos((cx + dir.getStepX()) * 16 + 8, 50, (cz + dir.getStepZ()) * 16 + 8));
            life += BiomeHandler.getBiomeAuraModifier(bgb2);
        }
        life /= 5.0f;
        float noise = (float) (1.0 + rand.nextGaussian() * 0.10000000149011612);
        short base = (short) (life * 500.0f * noise);
        base = (short) Mth.clamp(base, 0, 500);
        return new AuraChunk(chunk, base, base, 0.0f);
    }
}
