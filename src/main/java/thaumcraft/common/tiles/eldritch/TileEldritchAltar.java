package thaumcraft.common.tiles.eldritch;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.entities.monster.cult.EntityCultistCleric;
import thaumcraft.common.entities.monster.cult.EntityCultistKnight;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TC4's TileEldritchAltar, the keystone under the obelisk. It holds up to four Eldritch Eyes.
 * As a spawner it first raises four ritualist clerics around itself and then guards them with knights while any
 * cleric lives (type 0), or keeps calling eldritch guardians (type 1, also from the third eye on).
 */
public class TileEldritchAltar extends TileThaumcraft {
    private boolean spawner = false;
    private boolean open = false;
    private boolean spawnedClerics = false;
    private byte spawnType = 0;
    private byte eyes = 0;
    private int counter = 0;

    public TileEldritchAltar(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_ALTAR.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        eyes = tag.getByte("eyes");
        open = tag.getBoolean("open");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("eyes", eyes);
        tag.putBoolean("open", open);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        spawnedClerics = tag.getBoolean("spawnedClerics");
        spawner = tag.getBoolean("spawner");
        spawnType = tag.getByte("spawntype");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("spawnedClerics", spawnedClerics);
        tag.putBoolean("spawner", spawner);
        tag.putByte("spawntype", spawnType);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEldritchAltar tile) {
        if (tile.isSpawner() && tile.counter++ >= 80 && tile.counter % 40 == 0 && level instanceof ServerLevel server) {
            switch (tile.spawnType) {
                case 0:
                    if (!tile.spawnedClerics) {
                        tile.spawnClerics(server);
                    } else {
                        tile.spawnGuards(server);
                    }
                    break;
                case 1:
                    tile.spawnGuardian(server);
                    break;
                default:
                    break;
            }
        }
    }

    private AABB area() {
        return new AABB(worldPosition).inflate(24.0, 16.0, 24.0);
    }

    /** MathHelper.getRandomIntegerInRange(4, 10) times one of -1, 0, 1. */
    private BlockPos randomAround(ServerLevel world) {
        int i1 = worldPosition.getX() + Mth.nextInt(world.random, 4, 10) * Mth.nextInt(world.random, -1, 1);
        int j1 = worldPosition.getY() + Mth.nextInt(world.random, 0, 3) * Mth.nextInt(world.random, -1, 1);
        int k1 = worldPosition.getZ() + Mth.nextInt(world.random, 4, 10) * Mth.nextInt(world.random, -1, 1);
        return new BlockPos(i1, j1, k1);
    }

    private static boolean solidTop(ServerLevel world, BlockPos below) {
        return world.getBlockState(below).isFaceSturdy(world, below, Direction.UP);
    }

    /** checkNoEntityCollision, no colliding blocks and no liquid. */
    private static boolean fits(ServerLevel world, Mob mob) {
        return world.noCollision(mob) && world.isUnobstructed(mob) && !world.containsAnyLiquid(mob.getBoundingBox());
    }

    private void spawnGuards(ServerLevel world) {
        if (world.getEntitiesOfClass(EntityCultistCleric.class, area()).isEmpty()) {
            setSpawner(false);
            return;
        }
        List<EntityCultist> cultists = world.getEntitiesOfClass(EntityCultist.class, area());
        if (cultists.size() < 8) {
            EntityCultistKnight eg = TCEntities.CULTIST_KNIGHT.get().create(world);
            BlockPos p = randomAround(world);
            if (eg != null && solidTop(world, p.below())) {
                eg.setPos(p.getX(), p.getY(), p.getZ());
                if (fits(world, eg)) {
                    eg.finalizeSpawn(world, world.getCurrentDifficultyAt(p), MobSpawnType.SPAWNER, null);
                    eg.spawnAnim();
                    eg.restrictTo(worldPosition, 16);
                    world.addFreshEntity(eg);
                }
            }
        }
    }

    private void spawnGuardian(ServerLevel world) {
        EntityEldritchGuardian eg = TCEntities.ELDRITCH_GUARDIAN.get().create(world);
        BlockPos p = randomAround(world);
        if (eg != null && solidTop(world, p.below())) {
            eg.setPos(p.getX(), p.getY(), p.getZ());
            if (EntityEldritchGuardian.checkGuardianSpawnRules(TCEntities.ELDRITCH_GUARDIAN.get(), world, MobSpawnType.SPAWNER, p,
                    world.random) && fits(world, eg)) {
                eg.finalizeSpawn(world, world.getCurrentDifficultyAt(p), MobSpawnType.SPAWNER, null);
                eg.spawnAnim();
                eg.restrictTo(worldPosition, 16);
                world.addFreshEntity(eg);
            }
        }
    }

    private void spawnClerics(ServerLevel world) {
        int success = 0;
        int[][] corners = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] c : corners) {
            EntityCultistCleric cleric = TCEntities.CULTIST_CLERIC.get().create(world);
            BlockPos below = worldPosition.offset(c[0], -1, c[1]);
            if (cleric != null && solidTop(world, below)) {
                cleric.setPos(worldPosition.getX() + 0.5 + c[0], worldPosition.getY(), worldPosition.getZ() + 0.5 + c[1]);
                if (fits(world, cleric)) {
                    cleric.restrictTo(worldPosition, 8);
                    cleric.finalizeSpawn(world, world.getCurrentDifficultyAt(below), MobSpawnType.SPAWNER, null);
                    cleric.spawnAnim();
                    if (world.addFreshEntity(cleric)) {
                        ++success;
                        cleric.setIsRitualist(true);
                    }
                }
            }
        }
        if (success > 2) {
            spawnedClerics = true;
            setChanged();
        }
    }

    public boolean isSpawner() {
        return spawner;
    }

    public void setSpawner(boolean spawner) {
        this.spawner = spawner;
    }

    public byte getSpawnType() {
        return spawnType;
    }

    public void setSpawnType(byte spawnType) {
        this.spawnType = spawnType;
    }

    public byte getEyes() {
        return eyes;
    }

    public void setEyes(byte eyes) {
        this.eyes = eyes;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * True when a maze for this altar already exists in the Outer Lands; otherwise the original starts generating one
     * (a random 15-29 by 15-29 cell maze around these chunk coordinates) and returns false.
     * TODO: the maze comes with the Outer Lands dimension (stage 12c).
     */
    public boolean checkForMaze() {
        return false;
    }
}
