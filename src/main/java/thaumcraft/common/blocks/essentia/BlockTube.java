package thaumcraft.common.blocks.essentia;

import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.casters.ICaster;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.items.tools.ItemResonator;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.essentia.TileTube;
import thaumcraft.common.tiles.essentia.TileTubeBuffer;
import thaumcraft.common.tiles.essentia.TileTubeFilter;
import thaumcraft.common.tiles.essentia.TileTubeValve;

/**
 * Port of BlockTube, shared by the six kinds of essentia tube. The six side properties say which arms the model
 * draws; 1.12 worked them out on every redraw, here they are refreshed when neighbors or the tile change.
 * With a caster or resonator in hand the outline shows only the arm or core being looked at.
 */
public class BlockTube extends BaseEntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    /** Indexed by Direction.get3DDataValue(): down, up, north, south, west, east. */
    private static final BooleanProperty[] PROPS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};

    public static final MapCodec<BlockTube> CODEC = simpleCodec(p -> new BlockTube(TileTube::new));

    private final BiFunction<BlockPos, BlockState, ? extends TileTube> factory;

    public BlockTube(BiFunction<BlockPos, BlockState, ? extends TileTube> factory) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5f, 5.0f).sound(SoundType.METAL)
                .noOcclusion().dynamicShape());
        this.factory = factory;
        BlockState bs = stateDefinition.any();
        for (BooleanProperty p : PROPS) {
            bs = bs.setValue(p, false);
        }
        registerDefaultState(bs);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof TileTube tube) {
                TileTube.tick(lvl, pos, st, tube);
            }
        };
    }

    // ---- connections ----

    /** makeConnections: a side is drawn when this tile connects there and the tile there connects back. */
    public static BlockState withConnections(BlockState state, BlockGetter world, BlockPos pos) {
        if (!(state.getBlock() instanceof BlockTube)) {
            return state;
        }
        IEssentiaTransport tube = world.getBlockEntity(pos) instanceof IEssentiaTransport t ? t : null;
        for (Direction face : Direction.values()) {
            boolean con = tube != null && tube.isConnectable(face) && ThaumcraftApiHelper.getConnectableTile(world, pos, face) != null;
            state = state.setValue(PROPS[face.get3DDataValue()], con);
        }
        return state;
    }

    public static void refreshConnections(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState updated = withConnections(state, world, pos);
        if (updated != state) {
            world.setBlock(pos, updated, Block.UPDATE_CLIENTS);
        }
    }

    /** Refreshes this tube and the tubes around it. */
    public static void refreshAround(Level world, BlockPos pos) {
        refreshConnections(world, pos);
        for (Direction d : Direction.values()) {
            refreshConnections(world, pos.relative(d));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (world.getBlockEntity(pos) instanceof TileTube tile && placer != null) {
            // EnumFacing.getDirectionFromEntityLiving: the side facing the placer.
            tile.facing = Direction.orderedByNearest(placer)[0].getOpposite();
            tile.setChanged();
        }
        if (!world.isClientSide) {
            refreshAround(world, pos);
        }
        super.setPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return withConnections(state, world, pos);
    }

    // ---- shapes ----

    /** getBoundingBox: the core plus an arm to each side where something connects. */
    private static VoxelShape fullShape(BlockGetter source, BlockPos pos) {
        double minx = 0.3125, maxx = 0.6875, miny = 0.3125, maxy = 0.6875, minz = 0.3125, maxz = 0.6875;
        for (Direction fd : Direction.values()) {
            if (ThaumcraftApiHelper.getConnectableTile(source, pos, fd) != null) {
                switch (fd) {
                    case DOWN -> miny = 0.0;
                    case UP -> maxy = 1.0;
                    case NORTH -> minz = 0.0;
                    case SOUTH -> maxz = 1.0;
                    case WEST -> minx = 0.0;
                    case EAST -> maxx = 1.0;
                }
            }
        }
        return Shapes.box(minx, miny, minz, maxx, maxy, maxz);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // getSelectedBoundingBox: with a caster or resonator only the part being looked at is outlined.
        if (context instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof Player player && holdsTool(player)
                && world.getBlockEntity(pos) instanceof TileTube tile) {
            List<AABB> cuboids = tile.getTraceableCuboids(world);
            int hit = TileTube.pickPart(world, pos, cuboids, player);
            if (hit >= 0) {
                return Shapes.create(cuboids.get(hit));
            }
        }
        return fullShape(world, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return fullShape(world, pos);
    }

    private static boolean holdsTool(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack s = player.getItemInHand(hand);
            if (s.getItem() instanceof ICaster || s.getItem() instanceof ItemResonator) {
                return true;
            }
        }
        return false;
    }

    // ---- comparator, breaking ----

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TileTubeBuffer te) {
            float r = te.aspects.visSize() / (float) te.MAXAMOUNT;
            return Mth.floor(r * 14.0f) + (te.aspects.visSize() > 0 ? 1 : 0);
        }
        return 0;
    }

    /** Essentia left in the tube leaks into the aura. */
    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (world.isClientSide && world.getBlockEntity(pos) instanceof TileTube te && te.getEssentiaAmount(Direction.UP) > 0) {
            world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.1f,
                    1.0f + world.random.nextFloat() * 0.1f, false);
            for (int a = 0; a < 5; ++a) {
                thaumcraft.client.fx.FXDispatcher.drawVentParticles(pos.getX() + 0.33 + world.random.nextFloat() * 0.33, pos.getY() + 0.33 + world.random.nextFloat() * 0.33,
                        pos.getZ() + 0.33 + world.random.nextFloat() * 0.33, 0.0, 0.0, 0.0, Aspect.FLUX.getColor());
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !world.isClientSide && world.getBlockEntity(pos) instanceof TileTube te
                && te.getEssentiaAmount(Direction.UP) > 0) {
            AuraHelper.polluteAura(world, pos, (float) te.getEssentiaAmount(Direction.UP), true);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    // ---- valve and filter ----

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        // Casters and resonators work on the tile (onItemUseFirst); the block ignores them.
        if (stack.getItem() instanceof ICaster || stack.getItem() instanceof ItemResonator) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (state.is(thaumcraft.common.config.ConfigBlocks.tubeValve.get())) {
            if (stack.is(asItem())) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            return toggleValve(world, pos) ? ItemInteractionResult.sidedSuccess(world.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world.getBlockEntity(pos) instanceof TileTubeFilter te) {
            if (player.isShiftKeyDown() && te.aspectFilter != null) {
                return clearFilter(world, pos, te) ? ItemInteractionResult.sidedSuccess(world.isClientSide) : ItemInteractionResult.FAIL;
            }
            if (te.aspectFilter == null && stack.getItem() instanceof IEssentiaContainerItem container) {
                AspectList al = container.getAspects(stack);
                if (al != null && al.size() > 0) {
                    te.aspectFilter = al.getAspects()[0];
                    te.setChanged();
                    te.syncTile(true);
                    if (world.isClientSide) {
                        world.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundsTC.KEY.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
                    }
                }
                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.is(thaumcraft.common.config.ConfigBlocks.tubeValve.get())) {
            return toggleValve(world, pos) ? InteractionResult.sidedSuccess(world.isClientSide) : InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof TileTubeFilter te && te.aspectFilter != null) {
            return clearFilter(world, pos, te) ? InteractionResult.sidedSuccess(world.isClientSide) : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    private static boolean toggleValve(Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TileTubeValve te) {
            te.allowFlow = !te.allowFlow;
            te.setChanged();
            te.syncTile(true);
            if (!world.isClientSide) {
                world.playSound(null, pos, SoundsTC.SQUEEK.get(), SoundSource.BLOCKS, 0.7f, 0.9f + world.random.nextFloat() * 0.2f);
            }
            return true;
        }
        return false;
    }

    private static boolean clearFilter(Level world, BlockPos pos, TileTubeFilter te) {
        te.aspectFilter = null;
        te.setChanged();
        te.syncTile(true);
        if (world.isClientSide) {
            world.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundsTC.KEY.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
        return true;
    }

    /** The filtered tube's core is tinted with its filter (tintindex 1 in the model). */
    public static int filterColor(@Nullable BlockGetter world, @Nullable BlockPos pos) {
        if (world != null && pos != null && world.getBlockEntity(pos) instanceof TileTubeFilter te && te.aspectFilter != null) {
            return te.aspectFilter.getColor();
        }
        return 0xFFFFFF;
    }
}
