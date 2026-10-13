package thaumcraft.common.blocks.essentia;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.blocks.ILabelable;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.consumables.ItemLabel;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.essentia.TileAlembic;

/** Port of BlockAlembic. Stacked on an essentia smeltery; a label on a side locks it to one aspect. */
public class BlockAlembic extends BaseEntityBlock implements ILabelable {
    public static final MapCodec<BlockAlembic> CODEC = simpleCodec(p -> new BlockAlembic());
    protected static final VoxelShape SHAPE = box(2, 0, 2, 14, 16, 14);

    public BlockAlembic() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 20.0f).sound(SoundType.WOOD).noOcclusion());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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
        return new TileAlembic(pos, state);
    }

    /** Sneak with an empty hand: on the label side it removes the label, elsewhere it empties the alembic. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TileAlembic te) || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Direction side = hit.getDirection();
        if (te.aspectFilter != null && side.get3DDataValue() == te.facing) {
            te.aspectFilter = null;
            te.facing = Direction.DOWN.get3DDataValue();
            te.setChanged();
            te.syncTile(true);
            if (world.isClientSide) {
                world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.PAGE.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
            } else {
                world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5f + side.getStepX() / 3.0f, pos.getY() + 0.5f,
                        pos.getZ() + 0.5f + side.getStepZ() / 3.0f, new ItemStack(ConfigItems.label.get())));
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        if (player.getMainHandItem().isEmpty() && (te.aspectFilter == null || side.get3DDataValue() != te.facing)) {
            te.aspect = null;
            if (world.isClientSide) {
                world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.JAR.get(), SoundSource.BLOCKS, 0.4f, 1.0f, false);
                world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5f,
                        1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.3f, false);
            } else {
                AuraHelper.polluteAura(world, pos, (float) te.amount, true);
            }
            te.amount = 0;
            te.setChanged();
            te.syncTile(true);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction side, ItemStack labelstack) {
        if (!(player.level().getBlockEntity(pos) instanceof TileAlembic te) || side.getAxis() == Direction.Axis.Y || te.aspectFilter != null) {
            return false;
        }
        Aspect la = null;
        AspectList labelAspects = ((ItemLabel) labelstack.getItem()).getAspects(labelstack);
        if (labelAspects != null) {
            la = labelAspects.getAspects()[0];
        }
        if (te.amount == 0 && la == null) {
            return false;
        }
        Aspect aspect = null;
        if (te.amount == 0 && la != null) {
            aspect = la;
        }
        if (te.amount > 0) {
            aspect = te.aspect;
        }
        if (aspect == null) {
            return false;
        }
        te.aspectFilter = aspect;
        te.facing = side.get3DDataValue();
        te.setChanged();
        te.syncTile(true);
        player.level().playSound(null, pos, SoundsTC.PAGE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    /** Breaking it spills what it holds into the aura (BlockTCTile.breakBlock). */
    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !world.isClientSide && world.getBlockEntity(pos) instanceof TileAlembic te
                && te.getEssentiaAmount(Direction.UP) > 0) {
            AuraHelper.polluteAura(world, pos, (float) te.getEssentiaAmount(Direction.UP), true);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TileAlembic te) {
            float r = te.amount / (float) te.maxAmount;
            return Mth.floor(r * 14.0f) + (te.amount > 0 ? 1 : 0);
        }
        return 0;
    }
}
