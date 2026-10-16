package thaumcraft.common.blocks.world.ore;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * Port of BlockCrystal: a vis crystal cluster that grows on rock while the aura holds more vis than its base, and
 * spreads to nearby rock. The flux cluster feeds on flux instead.
 * <p>
 * The original's unlisted UP/DOWN/... properties (which faces touch solid rock) are computed by the client model
 * ({@code CrystalModel}) and by {@link #getShape}, from the same {@link #drawAt} test.
 */
public class BlockCrystal extends Block {
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 3);
    public static final IntegerProperty GENERATION = IntegerProperty.create("gen", 1, 4);

    private static final VoxelShape UP_SHAPE = Block.box(0, 8, 0, 16, 16, 16);
    private static final VoxelShape DOWN_SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(8, 0, 0, 16, 16, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 0, 8, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 8, 16, 16, 16);
    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 0, 16, 16, 8);

    public final Aspect aspect;

    public BlockCrystal(Aspect aspect) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.25f).sound(SoundsTC.SOUND_TYPE_CRYSTAL)
                .randomTicks().noCollission().noOcclusion().lightLevel(s -> 1).pushReaction(PushReaction.DESTROY));
        this.aspect = aspect;
        registerDefaultState(stateDefinition.any().setValue(SIZE, 0).setValue(GENERATION, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, GENERATION);
    }

    /** getDrops: one crystal per growth stage. Never silk-harvested. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> ret = new ArrayList<>();
        for (int count = getGrowth(state) + 1, i = 0; i < count; ++i) {
            ret.add(ConfigItems.crystalEssence.get().makeCrystal(aspect, 1));
        }
        return ret;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(3 + getGeneration(state)) != 0) {
            return;
        }
        int threshold = 10;
        int growth = getGrowth(state);
        int generation = getGeneration(state);
        if (aspect != Aspect.FLUX) {
            if (AuraHelper.getVis(worldIn, pos) <= threshold) {
                if (growth > 0) {
                    worldIn.setBlockAndUpdate(pos, state.setValue(SIZE, growth - 1));
                    AuraHelper.addVis(worldIn, pos, threshold);
                } else if (BlockUtils.isBlockTouching(worldIn, pos, this)) {
                    worldIn.removeBlock(pos, false);
                    AuraHandler.addVis(worldIn, pos, threshold);
                }
            } else if (AuraHelper.getVis(worldIn, pos) > AuraHandler.getAuraBase(worldIn, pos) + threshold) {
                if (growth < 3 && growth < 5 - generation + pos.asLong() % 3L) {
                    if (AuraHelper.drainVis(worldIn, pos, threshold, false) > 0.0f) {
                        worldIn.setBlockAndUpdate(pos, state.setValue(SIZE, growth + 1));
                    }
                } else if (generation < 4) {
                    BlockPos p2 = spreadCrystal(worldIn, pos);
                    if (p2 != null && AuraHelper.drainVis(worldIn, pos, threshold, false) > 0.0f) {
                        if (rand.nextInt(6) == 0) {
                            --generation;
                        }
                        worldIn.setBlockAndUpdate(p2, defaultBlockState().setValue(GENERATION, generation + 1));
                    }
                }
            }
        } else if (AuraHelper.getFlux(worldIn, pos) <= threshold) {
            if (growth > 0) {
                worldIn.setBlockAndUpdate(pos, state.setValue(SIZE, growth - 1));
                AuraHelper.polluteAura(worldIn, pos, threshold, false);
            } else if (BlockUtils.isBlockTouching(worldIn, pos, this)) {
                worldIn.removeBlock(pos, false);
                AuraHelper.polluteAura(worldIn, pos, threshold, false);
            }
        } else if (AuraHelper.getFlux(worldIn, pos) > AuraHandler.getAuraBase(worldIn, pos) + threshold) {
            if (growth < 3 && growth < 5 - generation + pos.asLong() % 3L) {
                if (AuraHelper.drainFlux(worldIn, pos, threshold, false) > 0.0f) {
                    worldIn.setBlockAndUpdate(pos, state.setValue(SIZE, growth + 1));
                }
            } else if (generation < 4) {
                BlockPos p2 = spreadCrystal(worldIn, pos);
                if (p2 != null && AuraHelper.drainFlux(worldIn, pos, threshold, false) > 0.0f) {
                    if (rand.nextInt(6) == 0) {
                        --generation;
                    }
                    worldIn.setBlockAndUpdate(p2, defaultBlockState().setValue(GENERATION, generation + 1));
                }
            }
        }
    }

    public static BlockPos spreadCrystal(Level world, BlockPos pos) {
        int xx = pos.getX() + world.random.nextInt(3) - 1;
        int yy = pos.getY() + world.random.nextInt(3) - 1;
        int zz = pos.getZ() + world.random.nextInt(3) - 1;
        BlockPos t = new BlockPos(xx, yy, zz);
        if (t.equals(pos)) {
            return null;
        }
        BlockState bs = world.getBlockState(t);
        if (bs.getFluidState().isEmpty() && (bs.isAir() || bs.canBeReplaced()) && world.random.nextInt(16) == 0
                && BlockUtils.isBlockTouchingRock(world, t, true)) {
            return t;
        }
        return null;
    }

    /** neighborChanged: without rock to hold on to, the cluster drops its crystals. */
    @Override
    protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide && !BlockUtils.isBlockTouchingRock(worldIn, pos, true)) {
            Block.dropResources(state, worldIn, pos);
            worldIn.removeBlock(pos, false);
        }
    }

    /** canPlaceBlockAt: only next to solid rock. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return BlockUtils.isBlockTouchingRock(worldIn, pos, true);
    }

    /** True if the neighbour on that side is rock with a solid face towards the crystal. */
    public static boolean drawAt(BlockGetter worldIn, BlockPos pos, Direction side) {
        BlockPos np = pos.relative(side);
        BlockState fbs = worldIn.getBlockState(np);
        return BlockUtils.isRock(fbs) && fbs.isFaceSturdy(worldIn, np, side.getOpposite());
    }

    /** getBoundingBox: the half block against the only rock face, or the full block if there are several. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        boolean up = drawAt(world, pos, Direction.UP);
        boolean down = drawAt(world, pos, Direction.DOWN);
        boolean east = drawAt(world, pos, Direction.EAST);
        boolean west = drawAt(world, pos, Direction.WEST);
        boolean south = drawAt(world, pos, Direction.SOUTH);
        boolean north = drawAt(world, pos, Direction.NORTH);
        int c = (up ? 1 : 0) + (down ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0) + (south ? 1 : 0) + (north ? 1 : 0);
        if (c > 1) {
            return Shapes.block();
        }
        if (up) return UP_SHAPE;
        if (down) return DOWN_SHAPE;
        if (east) return EAST_SHAPE;
        if (west) return WEST_SHAPE;
        if (south) return SOUTH_SHAPE;
        if (north) return NORTH_SHAPE;
        return Shapes.block();
    }

    public int getGrowth(BlockState state) {
        return state.getValue(SIZE);
    }

    public int getGeneration(BlockState state) {
        return state.getValue(GENERATION);
    }
}
