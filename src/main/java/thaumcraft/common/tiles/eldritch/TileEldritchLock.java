package thaumcraft.common.tiles.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.monster.boss.EntityCultistPortalGreater;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;
import thaumcraft.common.entities.monster.tainted.EntityTaintacle;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.world.dim.Cell;
import thaumcraft.common.world.dim.CellLoc;
import thaumcraft.common.world.dim.GenCommon;
import thaumcraft.common.world.dim.GenWorld;
import thaumcraft.common.world.dim.MapBossData;
import thaumcraft.common.world.dim.MazeHandler;
import thaumcraft.common.world.dim.OldBlocks;

/**
 * Port of TC4's TileEldritchLock, the Ancient Locking Mechanism in the doorway of a boss room. A Runed Tablet starts
 * the count; it pumps every 5 ticks and after 100 ticks the doorway opens and the next boss of the four (golem,
 * warden, crimson portal, taintacles) appears in the room. The facing is the block's FACING state.
 */
public class TileEldritchLock extends TileThaumcraft {
    public int count = -1;
    private static final int[][] PED = new int[][]{{2, 2, 2}, {0, -1, 1}, {3, 3, 3}};

    public TileEldritchLock(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_LOCK.get(), pos, state);
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        count = tag.getShort("count");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putShort("count", (short) count);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileEldritchLock tile) {
        if (tile.count != -1) {
            ++tile.count;
            if (!level.isClientSide && tile.count % 5 == 0) {
                level.playSound(null, pos, SoundsTC.PUMP.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            if (tile.count > 100) {
                tile.doBossSpawn();
            }
        }
    }

    private void doBossSpawn() {
        if (!(level instanceof ServerLevel world)) {
            return;
        }
        BlockPos pos = getBlockPos();
        world.playSound(null, pos, SoundsTC.ICE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        int centerx = pos.getX() >> 4;
        int centerz = pos.getZ() >> 4;
        int exit = 0;

        for (int a = -2; a <= 2; ++a) {
            for (int b = -2; b <= 2; ++b) {
                Cell c = MazeHandler.getFromHashMap(new CellLoc(cx + a, cz + b));
                if (c != null && c.feature == 2) {
                    centerx = cx + a;
                    centerz = cz + b;
                }

                if (c != null && c.feature >= 2 && c.feature <= 5 && (c.north || c.south || c.east || c.west)) {
                    exit = c.feature;
                }
            }
        }

        MapBossData mbd = MapBossData.get(world);
        ++mbd.bossCount;
        if (world.random.nextFloat() < 0.25F) {
            ++mbd.bossCount;
        }

        mbd.setDirty();
        GenWorld gen = new GenWorld(world, world.random);
        switch (mbd.bossCount % 4) {
            case 0 -> spawnGolemBossRoom(world, gen, centerx, centerz, exit);
            case 1 -> spawnWardenBossRoom(world, gen, centerx, centerz, exit);
            case 2 -> spawnCultistBossRoom(world, gen, centerx, centerz, exit);
            case 3 -> spawnTaintBossRoom(world, gen, centerx, centerz, exit);
        }

        for (int a = -2; a <= 2; ++a) {
            for (int b = -2; b <= 2; ++b) {
                for (int c = -2; c <= 2; ++c) {
                    BlockPos p = pos.offset(a, b, c);
                    if (world.getBlockState(p).is(ConfigBlocks.eldritchImpassable.get())) {
                        TCNetwork.sendToAllAround(world, p.getX(), p.getY(), p.getZ(), 32.0, new PacketFXBlockSparkle(p, 4194368));
                        world.removeBlock(p, false);
                    }
                }
            }
        }

        world.removeBlock(pos, false);
    }

    private void tellNearby(ServerLevel world, String key) {
        BlockPos pos = getBlockPos();
        for (ServerPlayer ep : world.players()) {
            if (ep.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 300.0) {
                ep.sendSystemMessage(Component.translatable(key));
            }
        }
    }

    /** The yaw and pitch from a spawn point toward the lock, as the original worked them out. */
    private float[] lookAtLock(double x, double eyeY, double z) {
        BlockPos pos = getBlockPos();
        double d0 = pos.getX() - x;
        double d1 = pos.getY() - eyeY;
        double d2 = pos.getZ() - z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = (float) (Mth.atan2(d2, d0) * 180.0 / Math.PI) - 90.0F;
        float f1 = (float) (-(Mth.atan2(d1, d3) * 180.0 / Math.PI));
        return new float[]{f, f1};
    }

    private void spawnWardenBossRoom(ServerLevel world, GenWorld gen, int cx, int cz, int exit) {
        tellNearby(world, "tc.boss.warden");
        RandomSource rand = world.random;
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        int x2 = x;
        int z2 = z;
        switch (exit) {
            case 2 -> {
                x2 = x + 8;
                z2 = z + 8;
            }
            case 3 -> {
                x2 = x - 8;
                z2 = z + 8;
            }
            case 4 -> {
                x2 = x + 8;
                z2 = z - 8;
            }
            case 5 -> {
                x2 = x - 8;
                z2 = z - 8;
            }
        }

        GenCommon.genObelisk(gen, x2, y + 4, z);
        GenCommon.genObelisk(gen, x, y + 4, z2);
        gen.setBlock(x2, y + 2, z, OldBlocks.blockEldritch, 3, 3);
        gen.setBlock(x, y + 2, z2, OldBlocks.blockEldritch, 3, 3);

        for (int a = -1; a <= 1; ++a) {
            for (int b = -1; b <= 1; ++b) {
                if (a != 0 && b != 0 && rand.nextFloat() < 0.9F) {
                    float rr = rand.nextFloat();
                    int md = rr < 0.1F ? 2 : (rr < 0.3F ? 1 : 0);
                    gen.setBlock(x2 + a, y + 2, z + b, OldBlocks.blockLootUrn, md, 3);
                }

                if (a != 0 && b != 0 && rand.nextFloat() < 0.9F) {
                    float rr = rand.nextFloat();
                    int md = rr < 0.1F ? 2 : (rr < 0.3F ? 1 : 0);
                    gen.setBlock(x + a, y + 2, z2 + b, OldBlocks.blockLootUrn, md, 3);
                }
            }
        }

        gen.setBlock(x - 2, y + 3, z - 2, OldBlocks.blockEldritch, 10, 3);
        gen.setBlock(x - 2, y + 3, z + 2, OldBlocks.blockEldritch, 10, 3);
        gen.setBlock(x + 2, y + 3, z + 2, OldBlocks.blockEldritch, 10, 3);
        gen.setBlock(x + 2, y + 3, z - 2, OldBlocks.blockEldritch, 10, 3);
        gen.setBlock(x - 2, y + 2, z - 2, OldBlocks.blockCosmeticSolid, 15, 3);
        gen.setBlock(x - 2, y + 2, z + 2, OldBlocks.blockCosmeticSolid, 15, 3);
        gen.setBlock(x + 2, y + 2, z + 2, OldBlocks.blockCosmeticSolid, 15, 3);
        gen.setBlock(x + 2, y + 2, z - 2, OldBlocks.blockCosmeticSolid, 15, 3);

        for (int a = 0; a < 3; ++a) {
            for (int b = 0; b < 3; ++b) {
                if (PED[a][b] < 0) {
                    gen.setBlock(x2 - 1 + b, y + 2, z2 - 1 + a, OldBlocks.blockEldritch, 4, 3);
                } else {
                    gen.setBlock(x2 - 1 + b, y + 2, z2 - 1 + a, OldBlocks.blockStairsEldritch, PED[a][b], 3);
                }
            }
        }

        EntityEldritchWarden boss = TCEntities.ELDRITCH_WARDEN.get().create(world);
        if (boss == null) {
            return;
        }
        float[] rot = lookAtLock(x2 + 0.5, y + 3 + boss.getEyeHeight(), z2 + 0.5);
        boss.moveTo(x2 + 0.5, y + 3, z2 + 0.5, rot[0], rot[1]);
        boss.finalizeSpawn(world, world.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.EVENT, null);
        boss.restrictTo(new BlockPos(x, y + 2, z), 32);
        world.addFreshEntity(boss);
    }

    private void spawnGolemBossRoom(ServerLevel world, GenWorld gen, int cx, int cz, int exit) {
        tellNearby(world, "tc.boss.golem");
        RandomSource rand = world.random;
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;
        int x2 = 0;
        int z2 = 0;
        switch (exit) {
            case 2 -> {
                x2 = 8;
                z2 = 8;
            }
            case 3 -> {
                x2 = -8;
                z2 = 8;
            }
            case 4 -> {
                x2 = 8;
                z2 = -8;
            }
            case 5 -> {
                x2 = -8;
                z2 = -8;
            }
        }

        GenCommon.genObelisk(gen, x + x2, y + 4, z + z2);
        GenCommon.genObelisk(gen, x - x2, y + 4, z + z2);
        GenCommon.genObelisk(gen, x + x2, y + 4, z - z2);
        gen.setBlock(x + x2, y + 2, z + z2, OldBlocks.blockEldritch, 3, 3);
        gen.setBlock(x - x2, y + 2, z + z2, OldBlocks.blockEldritch, 3, 3);
        gen.setBlock(x + x2, y + 2, z - z2, OldBlocks.blockEldritch, 3, 3);

        for (int a = 0; a < 3; ++a) {
            for (int b = 0; b < 3; ++b) {
                if (PED[a][b] < 0) {
                    gen.setBlock(x - 1 + b, y + 2, z - 1 + a, OldBlocks.blockEldritch, 4, 3);
                } else {
                    gen.setBlock(x - 1 + b, y + 2, z - 1 + a, OldBlocks.blockStairsEldritch, PED[a][b], 3);
                }
            }
        }

        for (int a = -10; a <= 10; ++a) {
            for (int b = -10; b <= 10; ++b) {
                if ((a < -2 && b < -2 || a > 2 && b > 2 || a < -2 && b > 2 || a > 2 && b < -2) && rand.nextFloat() < 0.15F && gen.isAirBlock(x + a, y + 2, z + b)) {
                    float rr = rand.nextFloat();
                    int md = rr < 0.05F ? 2 : (rr < 0.2F ? 1 : 0);
                    gen.setBlock(x + a, y + 2, z + b, rand.nextFloat() < 0.3F ? OldBlocks.blockLootCrate : OldBlocks.blockLootUrn, md, 3);
                }
            }
        }

        EntityEldritchGolem boss = TCEntities.ELDRITCH_GOLEM.get().create(world);
        if (boss == null) {
            return;
        }
        float[] rot = lookAtLock(x + 0.5, y + 3 + boss.getEyeHeight(), z + 0.5);
        boss.moveTo(x + 0.5, y + 3, z + 0.5, rot[0], rot[1]);
        boss.finalizeSpawn(world, world.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.EVENT, null);
        world.addFreshEntity(boss);
    }

    private void spawnCultistBossRoom(ServerLevel world, GenWorld gen, int cx, int cz, int exit) {
        tellNearby(world, "tc.boss.crimson");
        RandomSource rand = world.random;
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;

        for (int a = -4; a <= 4; ++a) {
            for (int b = -4; b <= 4; ++b) {
                if ((Math.abs(a) != 2 && Math.abs(b) != 2 || !rand.nextBoolean()) && (Math.abs(a) != 3 && Math.abs(b) != 3 || !(rand.nextFloat() > 0.33F)) && (Math.abs(a) != 4 && Math.abs(b) != 4 || !(rand.nextFloat() > 0.25F))) {
                    gen.setBlock(x + b, y + 1, z + a, OldBlocks.blockEldritch, 7, 3);
                }
            }
        }

        for (int a = 0; a < 5; ++a) {
            for (int b = 0; b < 5; ++b) {
                if (a == 0 || a == 4 || b == 0 || b == 4) {
                    gen.setBlock(x - 8 + b * 4, y + 2, z - 8 + a * 4, OldBlocks.blockCosmeticSolid, 11, 3);
                    gen.setBlock(x - 8 + b * 4, y + 3, z - 8 + a * 4, OldBlocks.blockEldritch, 5, 3);
                    gen.setBlock(x - 8 + b * 4, y + 4, z - 8 + a * 4, OldBlocks.blockSlabStone, 1, 3);
                    gen.setBlock(x - 8 + b * 4, y + 10, z - 8 + a * 4, OldBlocks.blockCosmeticSolid, 11, 3);
                    gen.setBlock(x - 8 + b * 4, y + 9, z - 8 + a * 4, OldBlocks.blockEldritch, 5, 3);
                    gen.setBlock(x - 8 + b * 4, y + 8, z - 8 + a * 4, OldBlocks.blockSlabStone, 9, 3);
                }
            }
        }

        EntityCultistPortalGreater boss = TCEntities.CULTIST_PORTAL_GREATER.get().create(world);
        if (boss == null) {
            return;
        }
        boss.moveTo(x + 0.5, y + 2, z + 0.5, 0.0F, 0.0F);
        world.addFreshEntity(boss);
    }

    private void spawnTaintBossRoom(ServerLevel world, GenWorld gen, int cx, int cz, int exit) {
        tellNearby(world, "tc.boss.taint");
        RandomSource rand = world.random;
        int x = cx * 16 + 16;
        int y = 50;
        int z = cz * 16 + 16;

        for (int a = -12; a <= 12; ++a) {
            for (int b = -12; b <= 12; ++b) {
                // Utils.setBiomeAt(..., biomeTaint): there is no taint biome.
                for (int c = 0; c < 9; ++c) {
                    if (gen.isAirBlock(x + b, y + 2 + c, z + a) && gen.isAdjacentToSolidBlock(x + b, y + 2 + c, z + a) && rand.nextInt(3) != 0) {
                        gen.setBlock(x + b, y + 2 + c, z + a, OldBlocks.blockTaintFibres, rand.nextInt(4) == 0 ? 1 : 0, 3);
                    }
                }

                if ((double) rand.nextFloat() < 0.15) {
                    gen.setBlock(x + b, y + 2, z + a, OldBlocks.blockTaint, 0, 3);
                    if ((double) rand.nextFloat() < 0.2) {
                        gen.setBlock(x + b, y + 3, z + a, OldBlocks.blockTaint, 0, 3);
                    }
                }

                if ((Math.abs(a) != 4 && Math.abs(b) != 4 || !rand.nextBoolean()) && (Math.abs(a) < 5 && Math.abs(b) < 5 || !(rand.nextFloat() > 0.33F)) && (Math.abs(a) < 7 && Math.abs(b) < 7 || !(rand.nextFloat() > 0.25F))) {
                    gen.setBlock(x + b, y + 1, z + a, OldBlocks.blockTaint, 1, 3);
                }
            }
        }

        EntityTaintacle boss1 = taintacle(world, world.getDifficulty() == Difficulty.HARD);
        spawnTaintacle(world, boss1, x + 0.5, y + 3, z + 0.5);
        EntityTaintacle boss2 = taintacle(world, rand.nextBoolean());
        spawnTaintacle(world, boss2, x + 3.5, y + 3, z + 3.5);
        EntityTaintacle boss3 = taintacle(world, !isGiant(boss2));
        spawnTaintacle(world, boss3, x - 2.5, y + 3, z + 3.5);
        EntityTaintacle boss4 = taintacle(world, rand.nextBoolean());
        spawnTaintacle(world, boss4, x + 3.5, y + 3, z - 2.5);
        EntityTaintacle boss5 = taintacle(world, !isGiant(boss4));
        spawnTaintacle(world, boss5, x - 2.5, y + 3, z - 2.5);
    }

    private static EntityTaintacle taintacle(ServerLevel world, boolean giant) {
        return giant ? TCEntities.TAINTACLE_GIANT.get().create(world) : TCEntities.TAINTACLE.get().create(world);
    }

    private static boolean isGiant(EntityTaintacle e) {
        return e instanceof thaumcraft.common.entities.monster.boss.EntityTaintacleGiant;
    }

    private static void spawnTaintacle(ServerLevel world, EntityTaintacle boss, double x, double y, double z) {
        if (boss == null) {
            return;
        }
        boss.moveTo(x, y, z, 0.0F, 0.0F);
        EntityUtils.makeChampion(boss, true);
        world.addFreshEntity(boss);
    }
}
