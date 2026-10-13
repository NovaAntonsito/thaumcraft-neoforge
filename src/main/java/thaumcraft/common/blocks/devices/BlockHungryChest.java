package thaumcraft.common.blocks.devices;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileHungryChest;

/**
 * Port of BlockHungryChest: a single chest (it never pairs) that eats item entities touching it, putting them in
 * from the top.
 * <p>
 * 1.21: it builds on ChestBlock for the lid, the opening sounds and the menu. The type is always SINGLE.
 */
public class BlockHungryChest extends ChestBlock {
    public static final MapCodec<BlockHungryChest> CODEC = simpleCodec(p -> new BlockHungryChest());

    public BlockHungryChest() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD).ignitedByLava(),
                TCBlockEntities.HUNGRY_CHEST::get);
    }

    @Override
    public MapCodec<? extends ChestBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileHungryChest(pos, state);
    }

    /** onBlockPlacedBy: facing the player, never joined to another chest. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    /** Opens even with a block or a cat on top (displayGUIChest). */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null) {
                player.openMenu(provider);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** onEntityCollidedWithBlock: ThaumcraftInvHelper.insertStackAt from above. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ItemEntity item) || !item.isAlive()
                || !(level.getBlockEntity(pos) instanceof TileHungryChest chest)) {
            return;
        }
        ItemStack stack = item.getItem();
        int before = stack.getCount();
        ItemStack leftovers = HopperBlockEntity.addItem(null, chest, stack.copy(), Direction.UP);
        if (leftovers.isEmpty() || leftovers.getCount() != before) {
            item.playSound(SoundEvents.GENERIC_EAT, 0.25f, (level.random.nextFloat() - level.random.nextFloat()) * 0.2f + 1.0f);
        }
        if (!leftovers.isEmpty()) {
            item.setItem(leftovers);
        } else {
            item.discard();
        }
        chest.setChanged();
    }
}
