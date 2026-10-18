package thaumcraft.common.blocks.basic;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.consumables.ItemPhial;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.misc.TileBanner;

/**
 * Port of BlockBannerTC: the 16 dyed banners and the crimson cult banner (no dye). Drawn by TileBannerRenderer, with
 * no collision. A phial puts its aspect on a dyed banner; using it sneaking takes it off.
 */
public class BlockBannerTC extends BaseEntityBlock {
    private static final VoxelShape POLE = Shapes.box(0.33000001311302185, 0.0, 0.33000001311302185, 0.6600000262260437, 2.0, 0.6600000262260437);
    private static final VoxelShape WALL_0 = Shapes.box(0.0, -1.0, 0.0, 1.0, 1.0, 0.25);
    private static final VoxelShape WALL_8 = Shapes.box(0.0, -1.0, 0.75, 1.0, 1.0, 1.0);
    private static final VoxelShape WALL_12 = Shapes.box(0.0, -1.0, 0.0, 0.25, 1.0, 1.0);
    private static final VoxelShape WALL_4 = Shapes.box(0.75, -1.0, 0.0, 1.0, 1.0, 1.0);

    @Nullable
    public final DyeColor dye;
    private final MapCodec<BlockBannerTC> codec;

    public BlockBannerTC(@Nullable DyeColor dye) {
        super(BlockBehaviour.Properties.of().mapColor(dye == null ? MapColor.COLOR_RED : dye.getMapColor()).strength(1.0f)
                .sound(SoundType.WOOD).noCollission().noOcclusion());
        this.dye = dye;
        this.codec = simpleCodec(p -> new BlockBannerTC(dye));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof TileBanner banner) {
            if (!banner.getWall()) {
                return POLE;
            }
            switch (banner.getBannerFacing()) {
                case 0:
                    return WALL_0;
                case 8:
                    return WALL_8;
                case 12:
                    return WALL_12;
                case 4:
                    return WALL_4;
                default:
                    break;
            }
        }
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level w, BlockPos pos, Player p, InteractionHand hand,
            BlockHitResult hit) {
        if (!(stack.getItem() instanceof ItemPhial phial)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!w.isClientSide && w.getBlockEntity(pos) instanceof TileBanner te && dye != null) {
            AspectList al = phial.getAspects(stack);
            if (al != null && al.size() > 0) {
                te.setAspect(al.getAspects()[0]);
                stack.shrink(1);
            }
            changed(w, pos, te);
        }
        return ItemInteractionResult.sidedSuccess(w.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level w, BlockPos pos, Player p, BlockHitResult hit) {
        if (!w.isClientSide && p.isShiftKeyDown() && w.getBlockEntity(pos) instanceof TileBanner te && dye != null) {
            te.setAspect(null);
            changed(w, pos, te);
        }
        return InteractionResult.sidedSuccess(w.isClientSide);
    }

    private static void changed(Level w, BlockPos pos, TileBanner te) {
        te.setChanged();
        te.syncTile(false);
        w.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    /** getPickBlock: the banner with its aspect. The drop does the same through its loot table. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack drop = new ItemStack(this);
        if (level.getBlockEntity(pos) instanceof TileBanner te && te.getAspect() != null) {
            drop.set(TCDataComponents.ASPECT_FILTER.get(), te.getAspect());
        }
        return drop;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileBanner(pos, state);
    }
}
