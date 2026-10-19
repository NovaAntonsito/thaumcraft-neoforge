package thaumcraft.common.world.dim;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import thaumcraft.common.blocks.eldritch.BlockEldritchNothing;
import thaumcraft.common.blocks.world.taint.BlockTaintFibre;
import thaumcraft.common.config.ConfigBlocks;

/**
 * The 1.7 World calls the maze generators make, over the chunk being decorated. It also holds GenCommon's
 * decoration lists, which were static in the original: features now run on several threads at once.
 */
public final class GenWorld {
    final WorldGenLevel level;
    final RandomSource rand;
    final Difficulty difficultySetting;
    final ArrayList<BlockPos> decoCommon = new ArrayList<>();
    final ArrayList<BlockPos> crabSpawner = new ArrayList<>();
    final ArrayList<BlockPos> decoUrn = new ArrayList<>();
    private final BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
    /** Dream branch: set by {@link MazeExport}, which keeps the blocks and leaves the entities out. */
    boolean exporting;

    private static Map<Block, OldBlocks> byBlock;

    public GenWorld(WorldGenLevel level, RandomSource rand) {
        this.level = level;
        this.rand = rand;
        this.difficultySetting = level.getDifficulty();
    }

    private BlockPos at(int x, int y, int z) {
        return mpos.set(x, y, z);
    }

    /** Whether the chunk of a position is in the generation region: TC4 reached anywhere, 1.21 throws outside it. */
    private boolean inRegion(BlockPos p) {
        return level.hasChunk(p.getX() >> 4, p.getZ() >> 4);
    }

    public boolean isAirBlock(int x, int y, int z) {
        BlockPos p = at(x, y, z);
        return !inRegion(p) || level.isEmptyBlock(p);
    }

    boolean isOpaqueCube(int x, int y, int z) {
        BlockPos p = at(x, y, z);
        return inRegion(p) && level.getBlockState(p).isSolidRender(level, p);
    }

    public OldBlocks getBlock(int x, int y, int z) {
        BlockPos p = at(x, y, z);
        if (!inRegion(p)) return OldBlocks.air;
        BlockState s = level.getBlockState(p);
        if (s.isAir()) {
            return OldBlocks.air;
        }
        return blocks().getOrDefault(s.getBlock(), OldBlocks.other);
    }

    public BlockEntity getTileEntity(int x, int y, int z) {
        BlockPos p = at(x, y, z);
        return inRegion(p) ? level.getBlockEntity(p) : null;
    }

    public void setBlock(int x, int y, int z, OldBlocks block) {
        setBlock(x, y, z, block, 0, 3);
    }

    /** The flags are ignored: world generation never notifies neighbours. */
    public void setBlock(int x, int y, int z, OldBlocks block, int meta, int flags) {
        BlockPos p = at(x, y, z).immutable();
        if (!inRegion(p)) return;
        BlockState s = state(block, meta);
        if (block == OldBlocks.blockTaintFibres) {
            s = BlockTaintFibre.getActualState(s, level, p);
        }
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /**
     * notifyBlocksOfNeighborChange: the only neighbour reaction here is the void's, which shows itself (gets its
     * block entity) when uncovered.
     */
    void notifyBlockChange(int x, int y, int z, OldBlocks block) {
        BlockPos c = new BlockPos(x, y, z);
        for (Direction d : Direction.values()) {
            BlockPos n = c.relative(d);
            if (!inRegion(n)) continue;
            BlockState s = level.getBlockState(n);
            if (s.getBlock() instanceof BlockEldritchNothing) {
                BlockState want = BlockEldritchNothing.stateFor(level, n);
                if (!want.is(s.getBlock())) {
                    level.setBlock(n, want, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /**
     * setFacing on the lock or the crab spawner just placed there. The 1.21 blocks keep it in their state, so their
     * models show the facing side.
     */
    public void setFacing(int x, int y, int z, ForgeDirection dir) {
        BlockPos p = at(x, y, z).immutable();
        if (!inRegion(p)) return;
        BlockState s = level.getBlockState(p);
        if (s.hasProperty(BlockStateProperties.FACING)) {
            level.setBlock(p, s.setValue(BlockStateProperties.FACING, Direction.from3DDataValue(dir.ordinal())), Block.UPDATE_CLIENTS);
        }
    }

    public void spawnEntityInWorld(Entity entity) {
        if (exporting) return;
        level.addFreshEntity(entity);
    }

    /** BlockUtils.countExposedSides: neighbours that are air. */
    int countExposedSides(int x, int y, int z) {
        int count = 0;
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (isAirBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)) {
                ++count;
            }
        }
        return count;
    }

    /** BlockUtils.isBlockAdjacentToAtleast with range 1, any meta (32767). */
    boolean isBlockAdjacentToAtleast(int x, int y, int z, OldBlocks id, int md, int amount) {
        int count = 0;
        for (int xx = -1; xx <= 1; ++xx) {
            for (int yy = -1; yy <= 1; ++yy) {
                for (int zz = -1; zz <= 1; ++zz) {
                    if (xx != 0 || yy != 0 || zz != 0) {
                        if (getBlock(x + xx, y + yy, z + zz) == id) {
                            ++count;
                        }
                        if (count >= amount) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** BlockUtils.isAdjacentToSolidBlock. */
    public boolean isAdjacentToSolidBlock(int x, int y, int z) {
        for (Direction d : Direction.values()) {
            BlockPos n = new BlockPos(x, y, z).relative(d);
            if (inRegion(n) && level.getBlockState(n).isFaceSturdy(level, n, d.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    /** The 1.7 block and meta as a 1.21 block state. */
    public BlockState state(OldBlocks block, int meta) {
        return switch (block) {
            case air -> Blocks.AIR.defaultBlockState();
            case bedrock -> Blocks.BEDROCK.defaultBlockState();
            case web -> Blocks.COBWEB.defaultBlockState();
            case mob_spawner -> Blocks.SPAWNER.defaultBlockState();
            case blockCosmeticSolid -> switch (meta) {
                case 12 -> ConfigBlocks.ancientRock.get().defaultBlockState();
                case 13 -> ConfigBlocks.ancientStoneNospawn.get().defaultBlockState();
                case 14 -> ConfigBlocks.crustedStone.get().defaultBlockState();
                case 15 -> ConfigBlocks.ancientPedestal.get().defaultBlockState();
                default -> ConfigBlocks.ancientStone.get().defaultBlockState();
            };
            case blockEldritch -> switch (meta) {
                case 0 -> ConfigBlocks.eldritchAltar.get().defaultBlockState();
                case 1 -> ConfigBlocks.eldritchObelisk.get().defaultBlockState();
                case 2 -> ConfigBlocks.eldritchObeliskBody.get().defaultBlockState();
                case 3 -> ConfigBlocks.eldritchCap.get().defaultBlockState();
                case 5 -> ConfigBlocks.eldritchStoneGlyphed.get().defaultBlockState();
                case 7 -> ConfigBlocks.ancientDoorway.get().defaultBlockState();
                case 8 -> ConfigBlocks.ancientLock.get().defaultBlockState();
                case 9 -> ConfigBlocks.crustedOpening.get().defaultBlockState();
                case 10 -> ConfigBlocks.runedStone.get().defaultBlockState();
                default -> ConfigBlocks.eldritchStoneGlowing.get().defaultBlockState();
            };
            case blockEldritchNothing -> ConfigBlocks.eldritchNothing.get().defaultBlockState();
            case blockStairsEldritch -> {
                // 1.7 stair metas: 0 east, 1 west, 2 south, 3 north; +4 upside down.
                Direction facing = switch (meta & 3) {
                    case 0 -> Direction.EAST;
                    case 1 -> Direction.WEST;
                    case 2 -> Direction.SOUTH;
                    default -> Direction.NORTH;
                };
                yield ConfigBlocks.ancientStairs.get().defaultBlockState().setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
            }
            case blockSlabStone -> ConfigBlocks.ancientSlab.get().defaultBlockState()
                    .setValue(SlabBlock.TYPE, (meta & 8) != 0 ? SlabType.TOP : SlabType.BOTTOM);
            case blockAiry -> ConfigBlocks.eldritchImpassable.get().defaultBlockState();
            case blockLootUrn -> (meta == 2 ? ConfigBlocks.lootUrnRare : meta == 1 ? ConfigBlocks.lootUrnUncommon
                    : ConfigBlocks.lootUrnCommon).get().defaultBlockState();
            case blockLootCrate -> (meta == 2 ? ConfigBlocks.lootCrateRare : meta == 1 ? ConfigBlocks.lootCrateUncommon
                    : ConfigBlocks.lootCrateCommon).get().defaultBlockState();
            // Meta 7 was the mixed crystal cluster; 1.12 has one cluster per aspect, so one of them at random.
            case blockCrystal -> switch (rand.nextInt(6)) {
                case 0 -> ConfigBlocks.crystalAir.get().defaultBlockState();
                case 1 -> ConfigBlocks.crystalFire.get().defaultBlockState();
                case 2 -> ConfigBlocks.crystalWater.get().defaultBlockState();
                case 3 -> ConfigBlocks.crystalEarth.get().defaultBlockState();
                case 4 -> ConfigBlocks.crystalOrder.get().defaultBlockState();
                default -> ConfigBlocks.crystalEntropy.get().defaultBlockState();
            };
            case blockTaintFibres -> ConfigBlocks.taintFibre.get().defaultBlockState();
            // 0 crusted taint, 1 tainted soil.
            case blockTaint -> (meta == 1 ? ConfigBlocks.taintSoil : ConfigBlocks.taintCrust).get().defaultBlockState();
            case blockEldritchPortal -> ConfigBlocks.eldritchPortal.get().defaultBlockState();
            case other -> Blocks.STONE.defaultBlockState();
        };
    }

    private static synchronized Map<Block, OldBlocks> blocks() {
        if (byBlock == null) {
            Map<Block, OldBlocks> m = new IdentityHashMap<>();
            m.put(Blocks.BEDROCK, OldBlocks.bedrock);
            m.put(Blocks.COBWEB, OldBlocks.web);
            m.put(Blocks.SPAWNER, OldBlocks.mob_spawner);
            for (var b : new Block[] {ConfigBlocks.ancientStone.get(), ConfigBlocks.ancientStoneNospawn.get(),
                    ConfigBlocks.ancientRock.get(), ConfigBlocks.crustedStone.get(), ConfigBlocks.ancientPedestal.get()}) {
                m.put(b, OldBlocks.blockCosmeticSolid);
            }
            for (var b : new Block[] {ConfigBlocks.eldritchAltar.get(), ConfigBlocks.eldritchObelisk.get(),
                    ConfigBlocks.eldritchObeliskBody.get(), ConfigBlocks.eldritchCap.get(), ConfigBlocks.eldritchStoneGlowing.get(),
                    ConfigBlocks.eldritchStoneGlyphed.get(), ConfigBlocks.ancientDoorway.get(), ConfigBlocks.ancientLock.get(),
                    ConfigBlocks.crustedOpening.get(), ConfigBlocks.runedStone.get()}) {
                m.put(b, OldBlocks.blockEldritch);
            }
            m.put(ConfigBlocks.eldritchNothing.get(), OldBlocks.blockEldritchNothing);
            m.put(ConfigBlocks.eldritchNothingExposed.get(), OldBlocks.blockEldritchNothing);
            m.put(ConfigBlocks.ancientStairs.get(), OldBlocks.blockStairsEldritch);
            m.put(ConfigBlocks.ancientSlab.get(), OldBlocks.blockSlabStone);
            m.put(ConfigBlocks.eldritchImpassable.get(), OldBlocks.blockAiry);
            m.put(ConfigBlocks.eldritchPortal.get(), OldBlocks.blockEldritchPortal);
            m.put(ConfigBlocks.taintFibre.get(), OldBlocks.blockTaintFibres);
            byBlock = m;
        }
        return byBlock;
    }
}
