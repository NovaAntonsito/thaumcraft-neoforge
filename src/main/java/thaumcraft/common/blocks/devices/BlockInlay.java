package thaumcraft.common.blocks.devices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.crafting.IInfusionStabiliserExt;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.devices.TileStabilizer;

/**
 * Port of BlockInlay (vis inlay). A gold and redstone line laid on the floor, wired like redstone dust: it carries
 * the charge of an infusion stabilizer (its source) to the arcane pedestals it touches, losing 1 per block. During
 * infusion a pedestal with charge lets the stabilizer absorb an instability event. The four sides were computed in
 * getActualState in 1.12; they are stored in the state now.
 */
public class BlockInlay extends Block implements IInfusionStabiliserExt {
    public static final EnumProperty<EnumAttachPosition> NORTH = EnumProperty.create("north", EnumAttachPosition.class);
    public static final EnumProperty<EnumAttachPosition> EAST = EnumProperty.create("east", EnumAttachPosition.class);
    public static final EnumProperty<EnumAttachPosition> SOUTH = EnumProperty.create("south", EnumAttachPosition.class);
    public static final EnumProperty<EnumAttachPosition> WEST = EnumProperty.create("west", EnumAttachPosition.class);
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 15);
    private static final VoxelShape[] REDSTONE_WIRE_AABB = new VoxelShape[16];
    static {
        // Index bits follow EnumFacing.getHorizontalIndex: south 1, west 2, north 4, east 8.
        for (int i = 0; i < 16; ++i) {
            double minx = (i & 2) != 0 ? 0 : 3, maxx = (i & 8) != 0 ? 16 : 13;
            double minz = (i & 4) != 0 ? 0 : 3, maxz = (i & 1) != 0 ? 16 : 13;
            REDSTONE_WIRE_AABB[i] = Block.box(minx, 0, minz, maxx, 1, maxz);
        }
    }

    public BlockInlay() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5f).sound(SoundType.METAL).noCollission()
                .noOcclusion().lightLevel(s -> 1).pushReaction(PushReaction.DESTROY));
        registerDefaultState(stateDefinition.any().setValue(NORTH, EnumAttachPosition.NONE).setValue(EAST, EnumAttachPosition.NONE)
                .setValue(SOUTH, EnumAttachPosition.NONE).setValue(WEST, EnumAttachPosition.NONE).setValue(CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, CHARGE);
    }

    private static EnumProperty<EnumAttachPosition> prop(Direction face) {
        return switch (face) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> EAST;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return REDSTONE_WIRE_AABB[getAABBIndex(state)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    private static int getAABBIndex(BlockState state) {
        int i = 0;
        boolean flag = state.getValue(NORTH) != EnumAttachPosition.NONE;
        boolean flag2 = state.getValue(EAST) != EnumAttachPosition.NONE;
        boolean flag3 = state.getValue(SOUTH) != EnumAttachPosition.NONE;
        boolean flag4 = state.getValue(WEST) != EnumAttachPosition.NONE;
        if (flag || (flag3 && !flag && !flag2 && !flag4)) {
            i |= 1 << Direction.NORTH.get2DDataValue();
        }
        if (flag2 || (flag4 && !flag && !flag2 && !flag3)) {
            i |= 1 << Direction.EAST.get2DDataValue();
        }
        if (flag3 || (flag && !flag2 && !flag3 && !flag4)) {
            i |= 1 << Direction.SOUTH.get2DDataValue();
        }
        if (flag4 || (flag2 && !flag && !flag3 && !flag4)) {
            i |= 1 << Direction.WEST.get2DDataValue();
        }
        return i;
    }

    private static BlockState withConnections(BlockState state, BlockGetter world, BlockPos pos) {
        for (Direction face : Direction.Plane.HORIZONTAL) {
            state = state.setValue(prop(face), getAttachPosition(world, pos, face));
        }
        return state;
    }

    private static EnumAttachPosition getAttachPosition(BlockGetter world, BlockPos pos, Direction direction) {
        BlockPos blockpos = pos.relative(direction);
        if (canConnectTo(world.getBlockState(blockpos))) {
            return EnumAttachPosition.SIDE;
        }
        if (isSourceBlock(world, blockpos)) {
            return EnumAttachPosition.EXT;
        }
        return EnumAttachPosition.NONE;
    }

    protected static boolean canConnectTo(BlockState blockState) {
        Block block = blockState.getBlock();
        return block == ConfigBlocks.inlay.get() || block instanceof BlockPedestal;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction face, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (face.getAxis().isHorizontal()) {
            return state.setValue(prop(face), getAttachPosition(level, pos, face));
        }
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos below = pos.below();
        return world.getBlockState(below).isFaceSturdy(world, below, Direction.UP);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return switch (rot) {
            case CLOCKWISE_180 -> state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST))
                    .setValue(SOUTH, state.getValue(NORTH)).setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(WEST)).setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 -> state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(EAST)).setValue(WEST, state.getValue(SOUTH));
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirrorIn) {
        return switch (mirrorIn) {
            case LEFT_RIGHT -> state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
            default -> super.mirror(state, mirrorIn);
        };
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        int charge = stateIn.getValue(CHARGE);
        if (charge > 0 && rand.nextInt(20 - charge) == 0) {
            Direction face = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
            if (getAttachPosition(worldIn, pos, face) != EnumAttachPosition.NONE) {
                double d0 = pos.getX() + 0.5 + rand.nextGaussian() * 0.08;
                double d2 = pos.getY() + 0.025f;
                double d3 = pos.getZ() + 0.5 + rand.nextGaussian() * 0.08;
                double f0 = face.getStepX() / 70.0 * (1.0 - rand.nextFloat() * 0.1);
                double f2 = face.getStepZ() / 70.0 * (1.0 - rand.nextFloat() * 0.1);
                float r = Mth.nextInt(rand, 150, 200) / 255.0f;
                float g = Mth.nextInt(rand, 0, 200) / 255.0f;
                thaumcraft.client.fx.FXDispatcher.drawLineSparkle(rand, d0, d2, d3, f0, 0.0, f2, 0.33f, r, g, g / 2.0f, 0, 1.0f, 0.0f, 16);
            }
        }
    }

    /** Grey when empty, brighter with charge. */
    public static int colorMultiplier(int meta) {
        float f = meta / 15.0f;
        float f2 = f * 0.5f + 0.5f;
        if (meta == 0) {
            f2 = 0.3f;
        }
        int i = Mth.clamp((int) (f2 * 255.0f), 0, 255);
        return 0xFF000000 | i << 16 | i << 8 | i;
    }

    @Override
    protected void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!worldIn.isClientSide && !oldState.is(state.getBlock())) {
            updateSurroundingInlay(worldIn, pos, state);
            for (Direction face : Direction.Plane.HORIZONTAL) {
                notifyInlayNeighborsOfStateChange(worldIn, pos.relative(face));
            }
        }
    }

    public static void notifyInlayNeighborsOfStateChange(Level worldIn, BlockPos pos) {
        BlockState bs = worldIn.getBlockState(pos);
        if (bs.is(ConfigBlocks.inlay.get()) || bs.getBlock() instanceof BlockPedestal) {
            worldIn.updateNeighborsAt(pos, bs.getBlock());
            for (Direction face : Direction.Plane.HORIZONTAL) {
                worldIn.updateNeighborsAt(pos.relative(face), bs.getBlock());
            }
        }
    }

    public static BlockState updateSurroundingInlay(Level worldIn, BlockPos pos, BlockState state) {
        Set<BlockPos> blocksNeedingUpdate = new HashSet<>();
        state = calculateChanges(worldIn, pos, pos, state, blocksNeedingUpdate);
        List<BlockPos> list = new ArrayList<>(blocksNeedingUpdate);
        for (BlockPos blockpos : list) {
            worldIn.updateNeighborsAt(blockpos, worldIn.getBlockState(pos).getBlock());
        }
        return state;
    }

    public static int getMaxStrength(Level worldIn, BlockPos pos, int strength) {
        BlockState bs = worldIn.getBlockState(pos);
        if (!bs.hasProperty(CHARGE)) {
            return strength;
        }
        int i = bs.getValue(CHARGE);
        return Math.max(i, strength);
    }

    public static int getSourceStrength(BlockGetter world, BlockPos pos) {
        for (Direction face : Direction.Plane.HORIZONTAL) {
            int e = getSourceStrengthAt(world, pos.relative(face));
            if (e > 0) {
                return e;
            }
        }
        return 0;
    }

    public static int getSourceStrengthAt(BlockGetter world, BlockPos pos) {
        if (isSourceBlock(world, pos) && world.getBlockEntity(pos) instanceof TileStabilizer te) {
            return te.getEnergy();
        }
        return 0;
    }

    public static boolean isSourceBlock(BlockGetter world, BlockPos pos) {
        return world.getBlockState(pos).is(ConfigBlocks.stabilizer.get());
    }

    public static BlockState calculateChanges(Level worldIn, BlockPos pos1, BlockPos pos2, BlockState state, Set<BlockPos> blocksNeedingUpdate) {
        BlockState iblockstate = state;
        int current = state.getValue(CHARGE);
        int max = 0;
        max = getMaxStrength(worldIn, pos2, max);
        int source = getSourceStrength(worldIn, pos1);
        if (source > 0 && source > max - 1) {
            max = source;
        }
        int neighbour = 0;
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos1.relative(face);
            boolean flag = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();
            if (flag) {
                neighbour = getMaxStrength(worldIn, blockpos, neighbour);
            }
        }
        if (neighbour > max) {
            max = neighbour - 1;
        } else if (max > 0) {
            --max;
        } else {
            max = 0;
        }
        if (source > max - 1) {
            max = source;
        }
        if (current != max) {
            state = state.setValue(CHARGE, max);
            if (worldIn.getBlockState(pos1) == iblockstate) {
                worldIn.setBlock(pos1, state, 2);
            }
            blocksNeedingUpdate.add(pos1);
            for (Direction face : Direction.values()) {
                blocksNeedingUpdate.add(pos1.relative(face));
            }
        }
        return state;
    }

    @Override
    protected void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, worldIn, pos, newState, movedByPiston);
        if (!worldIn.isClientSide && !state.is(newState.getBlock())) {
            for (Direction face : Direction.Plane.HORIZONTAL) {
                worldIn.updateNeighborsAt(pos.relative(face), this);
            }
            for (Direction face : Direction.Plane.HORIZONTAL) {
                notifyInlayNeighborsOfStateChange(worldIn, pos.relative(face));
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide) {
            if (canSurvive(state, worldIn, pos)) {
                updateSurroundingInlay(worldIn, pos, state);
            } else {
                dropResources(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean canStabaliseInfusion(Level world, BlockPos pos) {
        return true;
    }

    @Override
    public float getStabilizationAmount(Level world, BlockPos pos) {
        return 0.025f;
    }

    public enum EnumAttachPosition implements StringRepresentable {
        SIDE("side"),
        NONE("none"),
        EXT("ext");

        private final String name;

        EnumAttachPosition(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
