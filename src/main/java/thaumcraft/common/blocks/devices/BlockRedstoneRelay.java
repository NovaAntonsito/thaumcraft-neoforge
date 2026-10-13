package thaumcraft.common.blocks.devices;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.devices.TileRedstoneRelay;

/**
 * Port of BlockRedstoneRelay: a repeater that turns on once its input reaches a set strength and then gives out a
 * set strength. Clicking the front torch raises the output, clicking the back one raises the input threshold
 * (both wrap from 15 back to 1).
 * <p>
 * 1.21: it builds on DiodeBlock, which carries the same logic as the 1.12 BlockRedstoneDiode it copied, so the
 * "enabled" property is the vanilla "powered". The two torches are part of the outline instead of a CodeChicken
 * ray trace; the one clicked is found from the hit position.
 */
public class BlockRedstoneRelay extends DiodeBlock implements EntityBlock {
    public static final MapCodec<BlockRedstoneRelay> CODEC = simpleCodec(p -> new BlockRedstoneRelay());

    private static final Map<Direction, AABB[]> CUBOIDS = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int quarter = switch (facing) {
                case WEST -> 1;
                case NORTH -> 2;
                case EAST -> 3;
                default -> 0;
            };
            AABB c0 = rotate(new AABB(-0.375, 0.0625, -0.375, -0.125, 0.25, -0.125), quarter);
            AABB c1 = rotate(new AABB(-0.125, 0.0625, 0.125, 0.125, 0.25, 0.375), quarter);
            CUBOIDS.put(facing, new AABB[] {c0, c1});
            SHAPES.put(facing, Shapes.or(SHAPE, Shapes.create(c0), Shapes.create(c1)));
        }
    }

    public BlockRedstoneRelay() {
        super(BlockBehaviour.Properties.of().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    /** Rotation.quarterRotations around the block center, then back to block coordinates. */
    private static AABB rotate(AABB box, int quarter) {
        double x0 = box.minX, z0 = box.minZ, x1 = box.maxX, z1 = box.maxZ;
        for (int i = 0; i < quarter; ++i) {
            double nx0 = -z1, nx1 = -z0, nz0 = x0, nz1 = x1;
            x0 = nx0;
            x1 = nx1;
            z0 = nz0;
            z1 = nz1;
        }
        return new AABB(x0 + 0.5, box.minY, z0 + 0.5, x1 + 0.5, box.maxY, z1 + 0.5);
    }

    /** Which torch the hit lands on: 0 for the output, 1 for the input, -1 for neither (RayTracer subHit). */
    public static int getSubHit(BlockState state, BlockPos pos, Vec3 hit) {
        Vec3 local = hit.subtract(pos.getX(), pos.getY(), pos.getZ());
        AABB[] cuboids = CUBOIDS.get(state.getValue(FACING));
        for (int i = 0; i < cuboids.length; ++i) {
            if (cuboids[i].inflate(0.001).contains(local)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected MapCodec<? extends DiodeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileRedstoneRelay(pos, state);
    }

    /** Faces away from the player, or toward them when sneaking. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir = context.getHorizontalDirection();
        Player player = context.getPlayer();
        return defaultBlockState().setValue(FACING, player != null && player.isShiftKeyDown() ? dir : dir.getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.mayBuild() || !(world.getBlockEntity(pos) instanceof TileRedstoneRelay tile)) {
            return InteractionResult.PASS;
        }
        int sub = getSubHit(state, pos, hit.getLocation());
        if (sub < 0) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide) {
            if (sub == 0) {
                tile.increaseOut();
            } else {
                tile.increaseIn();
            }
            world.playSound(null, pos, SoundsTC.KEY.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            checkTickOnNeighbor(world, pos, state);
            updateNeighborsInFront(world, pos, state);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected int getDelay(BlockState state) {
        return 2;
    }

    /** shouldBePowered: the input must reach the tile's threshold. */
    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        int pr = level.getBlockEntity(pos) instanceof TileRedstoneRelay tile ? tile.getIn() : 1;
        return getInputSignal(level, pos, state) >= pr;
    }

    /** getActiveSignal. */
    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(pos) instanceof TileRedstoneRelay tile ? tile.getOut() : 0;
    }
}
