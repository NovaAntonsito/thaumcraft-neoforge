package thaumcraft.common.blocks.world.taint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Port of BlockTaintFibre: the thin taint that coats every solid face around it, sometimes with a growth on top.
 * In 1.12 the connections and the growth were an actual state worked out on every render; here they are stored
 * and refreshed when a neighbour changes. The growth still comes from the position, so it is always the same for
 * the same block. Growth 3 (the glowing bulb) drops a flux crystal and releases flux when broken.
 * <p>
 * The highlight box was a ray trace over every face (CodeChickenLib); here it is all the faces together, or the
 * growth.
 */
public class BlockTaintFibre extends Block implements ITaintBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty GROWTH1 = BooleanProperty.create("growth1");
    public static final BooleanProperty GROWTH2 = BooleanProperty.create("growth2");
    public static final BooleanProperty GROWTH3 = BooleanProperty.create("growth3");
    public static final BooleanProperty GROWTH4 = BooleanProperty.create("growth4");

    protected static final VoxelShape AABB_UP = Block.box(0.0, 15.2, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape AABB_DOWN = Block.box(0.0, 0.0, 0.0, 16.0, 0.8, 16.0);
    protected static final VoxelShape AABB_EAST = Block.box(15.2, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape AABB_WEST = Block.box(0.0, 0.0, 0.0, 0.8, 16.0, 16.0);
    protected static final VoxelShape AABB_SOUTH = Block.box(0.0, 0.0, 15.2, 16.0, 16.0, 16.0);
    protected static final VoxelShape AABB_NORTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 0.8);
    private static final VoxelShape GROWTH1_BOX = Block.box(1.6, 0.0, 1.6, 14.4, 6.4, 14.4);
    private static final VoxelShape GROWTH2_BOX = Block.box(3.2, 0.0, 3.2, 12.8, 16.0, 12.8);
    private static final VoxelShape GROWTH3_BOX = Block.box(4.0, 0.0, 4.0, 12.0, 5.0, 12.0);
    private static final VoxelShape GROWTH4_BOX = Block.box(1.6, 4.8, 1.6, 14.4, 16.0, 14.4);

    public BlockTaintFibre() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.0f).sound(SoundsTC.SOUND_TYPE_GORE).randomTicks()
                .replaceable().noOcclusion().pushReaction(PushReaction.DESTROY).lightLevel(BlockTaintFibre::light));
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false).setValue(GROWTH1, false).setValue(GROWTH2, false).setValue(GROWTH3, false)
                .setValue(GROWTH4, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, GROWTH1, GROWTH2, GROWTH3, GROWTH4);
    }

    /** getLightValue: the bulb glows at 12, the tall growths at 6. */
    private static int light(BlockState state) {
        return state.getValue(GROWTH3) ? 12 : ((state.getValue(GROWTH2) || state.getValue(GROWTH4)) ? 6 : 0);
    }

    private static boolean hasGrowth(BlockState state) {
        return state.getValue(GROWTH1) || state.getValue(GROWTH2) || state.getValue(GROWTH3) || state.getValue(GROWTH4);
    }

    @Override
    public void die(Level world, BlockPos pos, BlockState state) {
        world.removeBlock(pos, false);
    }

    /** dropBlockAsItemWithChance: only the bulb drops something, and it releases flux. Never silk-harvested. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> ret = new ArrayList<>();
        if (state.getValue(GROWTH3)) {
            ServerLevel world = params.getLevel();
            if (world.random.nextInt(5) <= BlockUtils.getFortuneLevel(params)) {
                ret.add(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1));
            }
            Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
            if (origin != null) {
                AuraHelper.polluteAura(world, BlockPos.containing(origin), 1.0f, true);
            }
        }
        return ret;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!hasGrowth(state) && isOnlyAdjacentToTaint(world, pos)) {
            die(world, pos, state);
        } else if (!TaintHelper.isNearTaintSeed(world, pos)) {
            die(world, pos, state);
        } else {
            TaintHelper.spreadFibres(world, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!hasGrowth(state) && isOnlyAdjacentToTaint(world, pos)) {
            world.removeBlock(pos, false);
        }
    }

    /** The connections are not set by whoever places the fibre, so they are worked out once it is in the world. */
    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        BlockState actual = getActualState(state, world, pos);
        if (actual != state) {
            world.setBlock(pos, actual, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getActualState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos,
            BlockPos neighborPos) {
        return getActualState(state, world, pos);
    }

    public static int getAdjacentTaint(BlockGetter world, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (!TaintHelper.isTaint(world.getBlockState(pos.relative(dir)))) {
                ++count;
            }
        }
        return count;
    }

    /** True if every solid face around pos is taint. */
    public static boolean isOnlyAdjacentToTaint(BlockGetter world, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos np = pos.relative(dir);
            BlockState ns = world.getBlockState(np);
            if (!ns.isAir() && !TaintHelper.isTaint(ns) && ns.isFaceSturdy(world, np, dir.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    /** True if pos has more taint than open faces around it. */
    public static boolean isHemmedByTaint(BlockGetter world, BlockPos pos) {
        int c = 0;
        for (Direction dir : Direction.values()) {
            BlockPos np = pos.relative(dir);
            BlockState block = world.getBlockState(np);
            if (TaintHelper.isTaint(block)) {
                ++c;
            } else if (block.isAir()) {
                --c;
            } else if (block.getFluidState().isEmpty() && !block.isFaceSturdy(world, np, dir.getOpposite())) {
                --c;
            }
        }
        return c > 0;
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClientSide && entity instanceof LivingEntity living && !living.isInvertedHealAndHarm() && world.random.nextInt(750) == 0) {
            living.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, 200, 0, false, true));
        }
        super.stepOn(world, pos, state, entity);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
        if (id == 1) {
            if (world.isClientSide) {
                world.playLocalSound(pos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 0.1f, 0.9f + world.random.nextFloat() * 0.2f, false);
            }
            return true;
        }
        return super.triggerEvent(state, world, pos, id, param);
    }

    private static boolean drawAt(BlockGetter world, BlockPos pos, Direction side) {
        BlockState b = world.getBlockState(pos);
        return !b.is(ConfigBlocks.taintFibre.get()) && !b.is(ConfigBlocks.taintFeature.get()) && b.isFaceSturdy(world, pos, side.getOpposite());
    }

    private VoxelShape faces(BlockState state) {
        VoxelShape shape = Shapes.empty();
        if (state.getValue(UP)) shape = Shapes.or(shape, AABB_UP);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, AABB_DOWN);
        if (state.getValue(EAST)) shape = Shapes.or(shape, AABB_EAST);
        if (state.getValue(WEST)) shape = Shapes.or(shape, AABB_WEST);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, AABB_SOUTH);
        if (state.getValue(NORTH)) shape = Shapes.or(shape, AABB_NORTH);
        return shape;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(GROWTH1)) return GROWTH1_BOX;
        if (state.getValue(GROWTH2)) return GROWTH2_BOX;
        if (state.getValue(GROWTH3)) return GROWTH3_BOX;
        if (state.getValue(GROWTH4)) return GROWTH4_BOX;
        return faces(state);
    }

    /** addCollisionBoxToList: a thin slab against every face it covers. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return faces(state);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return true;
    }

    /** getActualState: a face against every solid neighbour, and a growth picked from the position. */
    public static BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        boolean d = drawAt(world, pos.below(), Direction.DOWN);
        boolean u = drawAt(world, pos.above(), Direction.UP);
        int growth = 0;
        Random rand = new Random(pos.asLong());
        int q = rand.nextInt(50);
        if (d) {
            if (q < 4) {
                growth = 1;
            } else if (q == 4 || q == 5) {
                growth = 2;
            } else if (q == 6) {
                growth = 3;
            }
        }
        if (u && q > 47) {
            growth = 4;
        }
        return state.setValue(DOWN, d).setValue(UP, u)
                .setValue(NORTH, drawAt(world, pos.north(), Direction.NORTH)).setValue(SOUTH, drawAt(world, pos.south(), Direction.SOUTH))
                .setValue(WEST, drawAt(world, pos.west(), Direction.WEST)).setValue(EAST, drawAt(world, pos.east(), Direction.EAST))
                .setValue(GROWTH1, growth == 1).setValue(GROWTH2, growth == 2).setValue(GROWTH3, growth == 3).setValue(GROWTH4, growth == 4);
    }
}
