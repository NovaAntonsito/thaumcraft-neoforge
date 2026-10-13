package thaumcraft.common.blocks.essentia;

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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.blocks.ILabelable;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.consumables.ItemLabel;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.essentia.TileJarFillable;

/**
 * Port of BlockJar (warded jar and void jar). The jar keeps its essentia and label when broken:
 * the loot table copies the tile's components to the dropped item (BlockJar.spawnFilledJar).
 * The brain in a jar is BlockJarBrain.
 */
public class BlockJar extends BaseEntityBlock implements ILabelable {
    protected static final VoxelShape SHAPE = box(3, 0, 3, 13, 12, 13);

    private final BiFunction<BlockPos, BlockState, ? extends TileJarFillable> factory;
    private final MapCodec<BlockJar> codec;

    public BlockJar(BiFunction<BlockPos, BlockState, ? extends TileJarFillable> factory) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3f).sound(SoundsTC.SOUND_TYPE_JAR).noOcclusion());
        this.factory = factory;
        this.codec = simpleCodec(p -> new BlockJar(factory));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        BlockEntityTicker<TileJarFillable> ticker = TileJarFillable::tick;
        return type == TCBlockEntities.JAR.get() || type == TCBlockEntities.JAR_VOID.get() ? castTicker(ticker) : null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> BlockEntityTicker<T> castTicker(BlockEntityTicker<TileJarFillable> ticker) {
        return (BlockEntityTicker<T>) ticker;
    }

    /** The label faces the player who placed the jar. */
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity ent, ItemStack stack) {
        if (ent != null && world.getBlockEntity(pos) instanceof TileJarFillable jar) {
            jar.facing = facingFor(ent);
        }
    }

    private static int facingFor(LivingEntity ent) {
        int l = Mth.floor(ent.getYRot() * 4.0f / 360.0f + 0.5) & 0x3;
        return switch (l) {
            case 1 -> 5;
            case 2 -> 3;
            case 3 -> 4;
            default -> 2;
        };
    }

    /** A brass brace in hand locks the jar. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof TileJarFillable jar && !jar.blocked && stack.is(ConfigItems.jarBrace.get())) {
            jar.blocked = true;
            stack.consume(1, player);
            if (world.isClientSide) {
                world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.KEY.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
            } else {
                jar.setChanged();
                jar.syncTile(false);
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Sneak with an empty hand: on the label side it peels the label off, anywhere else it empties the jar. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TileJarFillable jar) || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Direction side = hit.getDirection();
        if (jar.aspectFilter != null && side.get3DDataValue() == jar.facing) {
            jar.aspectFilter = null;
            if (world.isClientSide) {
                world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.PAGE.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
            } else {
                world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5f + side.getStepX() / 3.0f, pos.getY() + 0.5f,
                        pos.getZ() + 0.5f + side.getStepZ() / 3.0f, new ItemStack(ConfigItems.label.get())));
                jar.setChanged();
                jar.syncTile(false);
            }
        } else {
            if (jar.aspectFilter == null) {
                jar.aspect = null;
            }
            if (world.isClientSide) {
                world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.JAR.get(), SoundSource.BLOCKS, 0.4f, 1.0f, false);
                world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5f,
                        1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.3f, false);
            } else {
                AuraHelper.polluteAura(world, pos, (float) jar.amount, true);
            }
            jar.amount = 0;
            jar.setChanged();
            jar.syncTile(false);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction side, ItemStack labelstack) {
        Level world = player.level();
        if (!(world.getBlockEntity(pos) instanceof TileJarFillable jar) || jar.aspectFilter != null) {
            return false;
        }
        AspectList labelAspects = ((ItemLabel) labelstack.getItem()).getAspects(labelstack);
        if (jar.amount == 0 && labelAspects == null) {
            return false;
        }
        if (jar.amount == 0) {
            jar.aspect = labelAspects.getAspects()[0];
        }
        jar.facing = facingFor(player);
        jar.aspectFilter = jar.aspect;
        jar.setChanged();
        jar.syncTile(true);
        world.playSound(null, pos, SoundsTC.JAR.get(), SoundSource.BLOCKS, 0.4f, 1.0f);
        return true;
    }

    /** The brace comes off as an item when the jar is broken. */
    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileJarFillable jar && jar.blocked) {
            Block.popResource(world, pos, new ItemStack(ConfigItems.jarBrace.get()));
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TileJarFillable jar) {
            float r = jar.amount / (float) TileJarFillable.CAPACITY;
            return Mth.floor(r * 14.0f) + (jar.amount > 0 ? 1 : 0);
        }
        return 0;
    }
}
